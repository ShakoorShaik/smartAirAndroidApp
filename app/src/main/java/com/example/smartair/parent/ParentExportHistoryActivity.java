package com.example.smartair.parent;

import android.app.DownloadManager;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.smartair.R;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.InhalerLogManager;
import utils.TriageHistoryManager;
import utils.TriggersManager;
import utils.ZoneHistoryManager;

public class ParentExportHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ParentExportHistory";

    private Spinner spinnerChildSelector;
    private Button buttonOpenDownloads;

    private List<Map<String, Object>> childrenList;
    private final List<String> childrenNames = new ArrayList<>();
    private final Map<String, String> childNameToUidMap = new HashMap<>();
    private final Map<String, ChildDataAggregator> childDataMap = new HashMap<>();
    private boolean isExportingPdf;
    private int totalChildrenToExport = 0;
    private final AtomicInteger completedExports = new AtomicInteger(0);
    private boolean isExportingAllChildren = false;
    // Track generated files for potential cleanup or future features (e.g., batch operations, sharing)
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<File> generatedFiles = new ArrayList<>();

    // Data aggregator class to collect all async data
    private static class ChildDataAggregator {
        String childName;
        List<Map<String, Object>> zoneHistory = new ArrayList<>();
        List<Map<String, Object>> symptoms = new ArrayList<>();
        List<Map<String, Object>> triggers = new ArrayList<>();
        List<Map<String, Object>> rescueUsage = new ArrayList<>();
        List<Map<String, Object>> controlUsage = new ArrayList<>();
        AtomicInteger pendingCallbacks = new AtomicInteger(5); // 5 data types to retrieve
        boolean isReady = false;

        ChildDataAggregator(String name) {
            this.childName = name;
        }

        synchronized void markCallbackComplete() {
            if (pendingCallbacks.decrementAndGet() == 0) {
                isReady = true;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_export_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Export History");
        }

        spinnerChildSelector = findViewById(R.id.spinnerChildSelector);
        RadioButton radioCsv = findViewById(R.id.radioCsv);
        Button buttonExport = findViewById(R.id.buttonExport);
        buttonOpenDownloads = findViewById(R.id.buttonOpenDownloads);
        Button buttonReturn = findViewById(R.id.buttonReturn);
        buttonReturn.setOnClickListener(v -> finish());
        buttonOpenDownloads.setOnClickListener(v -> openDownloadsFolder());

        childrenList = new ArrayList<>();

        loadChildren();

        buttonExport.setOnClickListener(v -> {
            boolean isCsv = radioCsv.isChecked();
            handleExport(isCsv);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChildren() {
        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                childrenList = children;
                childrenNames.clear();
                childNameToUidMap.clear();

                childrenNames.add("All Children");

                if (children != null && !children.isEmpty()) {
                    for (Map<String, Object> child : children) {
                        String name = (String) child.get("name");
                        String uid = (String) child.get("uid");
                        if (name != null && uid != null) {
                            childrenNames.add(name);
                            childNameToUidMap.put(name, uid);
                        }
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ParentExportHistoryActivity.this,
                        android.R.layout.simple_spinner_item,
                        childrenNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerChildSelector.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentExportHistoryActivity.this,
                        "Failed to load children: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleExport(boolean isCsv) {
        String selectedChild = (String) spinnerChildSelector.getSelectedItem();
        if (selectedChild == null) {
            Toast.makeText(this, "Please select a child", Toast.LENGTH_SHORT).show();
            return;
        }

        isExportingPdf = !isCsv;
        childDataMap.clear();
        isExportingAllChildren = false;
        totalChildrenToExport = 0;
        completedExports.set(0);
        generatedFiles.clear();
        buttonOpenDownloads.setVisibility(View.GONE);

        Toast.makeText(this, "Retrieving data for " + selectedChild + "...\nFiles will be saved to Downloads folder", Toast.LENGTH_SHORT).show();

        if (selectedChild.equals("All Children")) {
            retrieveAllChildrenData();
        } else {
            totalChildrenToExport = 1;
            String childUid = childNameToUidMap.get(selectedChild);
            retrieveSingleChildData(childUid, selectedChild);
        }
    }

    private void retrieveAllChildrenData() {
        if (childrenList == null || childrenList.isEmpty()) {
            Toast.makeText(this, "No children found", Toast.LENGTH_SHORT).show();
            return;
        }

        isExportingAllChildren = true;
        totalChildrenToExport = 0;
        completedExports.set(0);

        // Count valid children
        for (Map<String, Object> child : childrenList) {
            String uid = (String) child.get("uid");
            String name = (String) child.get("name");
            if (uid != null && name != null) {
                totalChildrenToExport++;
            }
        }

        if (totalChildrenToExport == 0) {
            Toast.makeText(this, "No valid children found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start data retrieval for all children
        for (Map<String, Object> child : childrenList) {
            String uid = (String) child.get("uid");
            String name = (String) child.get("name");
            if (uid != null && name != null) {
                retrieveSingleChildData(uid, name);
            }
        }
    }

    private void retrieveSingleChildData(String childUid, String childName) {
        ChildIdManager manager = new ChildIdManager(this);
        manager.SaveChildId(childUid);

        // Initialize data aggregator for this child
        ChildDataAggregator aggregator = new ChildDataAggregator(childName);
        childDataMap.put(childName, aggregator);

        retrieveZoneHistory(childUid, childName);
        retrieveSymptoms(childUid, childName);
        retrieveTriggers(childUid, childName);
        retrieveRescueUsage(childUid, childName);
        retrieveControlUsage(childUid, childName);

        manager.clearChildId();
    }

    private void retrieveZoneHistory(String childUid, String childName) {
        ZoneHistoryManager.getAllZoneHistoryForChild(childUid, new ZoneHistoryManager.ZoneHistoryDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> zoneHistoryData) {
                processZoneHistoryData(childName, zoneHistoryData);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentExportHistoryActivity.this,
                        "Failed to retrieve zone history for " + childName,
                        Toast.LENGTH_SHORT).show();
                processZoneHistoryData(childName, new ArrayList<>());
            }
        });
    }

    private void retrieveSymptoms(String childUid, String childName) {
        TriageHistoryManager.getAllTriageDataForChild(childUid, new TriageHistoryManager.TriageDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> triageData) {
                processSymptomsData(childName, triageData);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentExportHistoryActivity.this,
                        "Failed to retrieve symptoms for " + childName,
                        Toast.LENGTH_SHORT).show();
                processSymptomsData(childName, new ArrayList<>());
            }
        });
    }

    private void retrieveTriggers(String childUid, String childName) {
        TriggersManager.getAllTriggersForChild(childUid, new TriggersManager.TriggersDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> triggersData) {
                processTriggersData(childName, triggersData);
            }

            @Override
            public void onFailure(Exception e) {
                // Triggers collection might not exist, that's okay
                processTriggersData(childName, new ArrayList<>());
            }
        });
    }

    private void retrieveRescueUsage(String childUid, String childName) {
        InhalerLogManager.getRescueInhalerLogsForChild(childUid, new InhalerLogManager.InhalerLogDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> rescueData) {
                processRescueUsageData(childName, rescueData);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentExportHistoryActivity.this,
                        "Failed to retrieve rescue usage for " + childName,
                        Toast.LENGTH_SHORT).show();
                processRescueUsageData(childName, new ArrayList<>());
            }
        });
    }

    private void retrieveControlUsage(String childUid, String childName) {
        InhalerLogManager.getControlInhalerLogsForChild(childUid, new InhalerLogManager.InhalerLogDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> controlData) {
                processControlUsageData(childName, controlData);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentExportHistoryActivity.this,
                        "Failed to retrieve control usage for " + childName,
                        Toast.LENGTH_SHORT).show();
                processControlUsageData(childName, new ArrayList<>());
            }
        });
    }

    // Helper methods to process retrieved data (to be used for CSV/PDF generation)
    private void processZoneHistoryData(String childName, List<Map<String, Object>> data) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null) {
            aggregator.zoneHistory = data != null ? data : new ArrayList<>();
            aggregator.markCallbackComplete();
            checkAndGenerateExport(childName);
        }
    }

    private void processSymptomsData(String childName, List<Map<String, Object>> data) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null) {
            aggregator.symptoms = data != null ? data : new ArrayList<>();
            aggregator.markCallbackComplete();
            checkAndGenerateExport(childName);
        }
    }

    private void processTriggersData(String childName, List<Map<String, Object>> data) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null) {
            aggregator.triggers = data != null ? data : new ArrayList<>();
            aggregator.markCallbackComplete();
            checkAndGenerateExport(childName);
        }
    }

    private void processRescueUsageData(String childName, List<Map<String, Object>> data) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null) {
            aggregator.rescueUsage = data != null ? data : new ArrayList<>();
            aggregator.markCallbackComplete();
            checkAndGenerateExport(childName);
        }
    }

    private void processControlUsageData(String childName, List<Map<String, Object>> data) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null) {
            aggregator.controlUsage = data != null ? data : new ArrayList<>();
            aggregator.markCallbackComplete();
            checkAndGenerateExport(childName);
        }
    }

    private void checkAndGenerateExport(String childName) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null && aggregator.isReady) {
            if (isExportingPdf) {
                generatePdfForChild(aggregator);
            } else {
                generateCsvForChild(aggregator);
            }

            // Track completion for "All Children" export
            int completed = completedExports.incrementAndGet();
            if (isExportingAllChildren && completed == totalChildrenToExport) {
                runOnUiThread(() -> {
                    String format = isExportingPdf ? "PDF" : "CSV";
                    Toast.makeText(ParentExportHistoryActivity.this,
                            "Successfully exported " + completed + " " + format + " file(s) to Downloads folder",
                            Toast.LENGTH_LONG).show();

                    // Show the "Open Downloads Folder" button
                    buttonOpenDownloads.setVisibility(View.VISIBLE);
                });
            }
        }
    }

    private void generatePdfForChild(ChildDataAggregator aggregator) {
        try {
            // Create filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "SmartAir_" + aggregator.childName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";

            // Create file in Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File pdfFile = new File(downloadsDir, fileName);

            // Initialize PDF writer
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add title
            document.add(new Paragraph("SmartAir Health Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            // Add child name and date
            document.add(new Paragraph("Child: " + aggregator.childName + "\nGenerated: " + 
                    new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // Add Zone History section
            addSectionTitle(document, "Zone History");
            if (aggregator.zoneHistory.isEmpty()) {
                document.add(new Paragraph("No zone history data available.").setItalic());
            } else {
                Table zoneTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3})).useAllAvailableWidth();
                zoneTable.addHeaderCell(createHeaderCell("Date"));
                zoneTable.addHeaderCell(createHeaderCell("Zone"));
                zoneTable.addHeaderCell(createHeaderCell("Notes"));

                for (Map<String, Object> entry : aggregator.zoneHistory) {
                    zoneTable.addCell(formatDate(entry.get("date")));
                    zoneTable.addCell(String.valueOf(entry.getOrDefault("zone", "N/A")));
                    zoneTable.addCell(String.valueOf(entry.getOrDefault("notes", "")));
                }
                document.add(zoneTable);
            }
            document.add(new Paragraph("\n"));

            // Add Symptoms section
            addSectionTitle(document, "Symptoms");
            if (aggregator.symptoms.isEmpty()) {
                document.add(new Paragraph("No symptoms data available.").setItalic());
            } else {
                Table symptomsTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2})).useAllAvailableWidth();
                symptomsTable.addHeaderCell(createHeaderCell("Date"));
                symptomsTable.addHeaderCell(createHeaderCell("Symptoms"));
                symptomsTable.addHeaderCell(createHeaderCell("Severity"));

                for (Map<String, Object> entry : aggregator.symptoms) {
                    symptomsTable.addCell(formatDate(entry.get("date")));

                    // Extract symptoms list
                    Object symptomsObj = entry.get("symptoms");
                    String symptomsStr = "";
                    if (symptomsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> symptomsList = (List<String>) symptomsObj;
                        symptomsStr = String.join(", ", symptomsList);
                    } else if (symptomsObj != null) {
                        symptomsStr = symptomsObj.toString();
                    }
                    symptomsTable.addCell(symptomsStr.isEmpty() ? "N/A" : symptomsStr);

                    symptomsTable.addCell(String.valueOf(entry.getOrDefault("severity", "N/A")));
                }
                document.add(symptomsTable);
            }
            document.add(new Paragraph("\n"));

            // Add Triggers section
            addSectionTitle(document, "Triggers");
            if (aggregator.triggers.isEmpty()) {
                document.add(new Paragraph("No triggers data available.").setItalic());
            } else {
                Table triggersTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2})).useAllAvailableWidth();
                triggersTable.addHeaderCell(createHeaderCell("Date"));
                triggersTable.addHeaderCell(createHeaderCell("Trigger"));
                triggersTable.addHeaderCell(createHeaderCell("Type"));

                for (Map<String, Object> entry : aggregator.triggers) {
                    triggersTable.addCell(formatDate(entry.get("date")));
                    triggersTable.addCell(String.valueOf(entry.getOrDefault("trigger", "N/A")));
                    triggersTable.addCell(String.valueOf(entry.getOrDefault("type", "N/A")));
                }
                document.add(triggersTable);
            }
            document.add(new Paragraph("\n"));

            // Add Rescue Inhaler Usage section
            addSectionTitle(document, "Rescue Inhaler Usage");
            if (aggregator.rescueUsage.isEmpty()) {
                document.add(new Paragraph("No rescue inhaler usage data available.").setItalic());
            } else {
                Table rescueTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3})).useAllAvailableWidth();
                rescueTable.addHeaderCell(createHeaderCell("Date"));
                rescueTable.addHeaderCell(createHeaderCell("Puffs"));
                rescueTable.addHeaderCell(createHeaderCell("Notes"));

                for (Map<String, Object> entry : aggregator.rescueUsage) {
                    rescueTable.addCell(formatDate(entry.get("timestamp")));
                    rescueTable.addCell(String.valueOf(entry.getOrDefault("puffs", "N/A")));
                    rescueTable.addCell(String.valueOf(entry.getOrDefault("notes", "")));
                }
                document.add(rescueTable);
            }
            document.add(new Paragraph("\n"));

            // Add Control Inhaler Usage section
            addSectionTitle(document, "Control Inhaler Usage");
            if (aggregator.controlUsage.isEmpty()) {
                document.add(new Paragraph("No control inhaler usage data available.").setItalic());
            } else {
                Table controlTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 3})).useAllAvailableWidth();
                controlTable.addHeaderCell(createHeaderCell("Date"));
                controlTable.addHeaderCell(createHeaderCell("Puffs"));
                controlTable.addHeaderCell(createHeaderCell("Notes"));

                for (Map<String, Object> entry : aggregator.controlUsage) {
                    controlTable.addCell(formatDate(entry.get("timestamp")));
                    controlTable.addCell(String.valueOf(entry.getOrDefault("puffs", "N/A")));
                    controlTable.addCell(String.valueOf(entry.getOrDefault("notes", "")));
                }
                document.add(controlTable);
            }

            // Close document
            document.close();

            // Scan file to make it immediately visible in Downloads app
            scanFile(pdfFile);

            // Track generated file
            generatedFiles.add(pdfFile);

            // Show success message and open file
            runOnUiThread(() -> {
                if (!isExportingAllChildren) {
                    Toast.makeText(this, "PDF exported successfully to Downloads folder", Toast.LENGTH_LONG).show();
                    buttonOpenDownloads.setVisibility(View.VISIBLE);
                    openPdfFile(pdfFile);
                } else {
                    // For "All Children", show individual progress
                    int completed = completedExports.get();
                    Toast.makeText(this, "Exported PDF for " + aggregator.childName + " to Downloads (" + completed + "/" + totalChildrenToExport + ")", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate PDF for " + aggregator.childName, e);
            runOnUiThread(() -> 
                Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private void generateCsvForChild(ChildDataAggregator aggregator) {
        try {
            // Create filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "SmartAir_" + aggregator.childName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".csv";

            // Create file in Downloads directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File csvFile = new File(downloadsDir, fileName);

            StringBuilder csvContent = new StringBuilder();

            // Add header
            csvContent.append("SmartAir Health Report\n");
            csvContent.append("Child: ").append(aggregator.childName).append("\n");
            csvContent.append("Generated: ").append(new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");

            // Zone History section
            csvContent.append("Zone History\n");
            csvContent.append("Date,Zone,Notes\n");
            if (aggregator.zoneHistory.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.zoneHistory) {
                    csvContent.append(escapeCsv(formatDate(entry.get("date")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("zone", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("notes", "")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Symptoms section
            csvContent.append("Symptoms\n");
            csvContent.append("Date,Symptoms,Severity\n");
            if (aggregator.symptoms.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.symptoms) {
                    csvContent.append(escapeCsv(formatDate(entry.get("date")))).append(",");

                    Object symptomsObj = entry.get("symptoms");
                    String symptomsStr = "";
                    if (symptomsObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> symptomsList = (List<String>) symptomsObj;
                        symptomsStr = String.join("; ", symptomsList);
                    } else if (symptomsObj != null) {
                        symptomsStr = symptomsObj.toString();
                    }
                    csvContent.append(escapeCsv(symptomsStr.isEmpty() ? "N/A" : symptomsStr)).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("severity", "N/A")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Triggers section
            csvContent.append("Triggers\n");
            csvContent.append("Date,Trigger,Type\n");
            if (aggregator.triggers.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.triggers) {
                    csvContent.append(escapeCsv(formatDate(entry.get("date")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("trigger", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("type", "N/A")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Rescue Inhaler Usage section
            csvContent.append("Rescue Inhaler Usage\n");
            csvContent.append("Date,Puffs,Notes\n");
            if (aggregator.rescueUsage.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.rescueUsage) {
                    csvContent.append(escapeCsv(formatDate(entry.get("timestamp")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("puffs", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("notes", "")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Control Inhaler Usage section
            csvContent.append("Control Inhaler Usage\n");
            csvContent.append("Date,Puffs,Notes\n");
            if (aggregator.controlUsage.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.controlUsage) {
                    csvContent.append(escapeCsv(formatDate(entry.get("timestamp")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("puffs", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("notes", "")))).append("\n");
                }
            }

            // Write to file
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(csvContent.toString().getBytes());
            fos.close();

            // Scan file to make it immediately visible in Downloads app
            scanFile(csvFile);

            // Track generated file
            generatedFiles.add(csvFile);

            // Show success message and open file
            runOnUiThread(() -> {
                if (!isExportingAllChildren) {
                    Toast.makeText(this, "CSV exported successfully to Downloads folder", Toast.LENGTH_LONG).show();
                    buttonOpenDownloads.setVisibility(View.VISIBLE);
                    openCsvFile(csvFile);
                } else {
                    // For "All Children", show individual progress
                    int completed = completedExports.get();
                    Toast.makeText(this, "Exported CSV for " + aggregator.childName + " to Downloads (" + completed + "/" + totalChildrenToExport + ")", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Failed to generate CSV for " + aggregator.childName, e);
            runOnUiThread(() -> 
                Toast.makeText(this, "Failed to generate CSV: " + e.getMessage(), Toast.LENGTH_LONG).show()
            );
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void addSectionTitle(Document document, String title) {
        document.add(new Paragraph(title)
                .setFontSize(14)
                .setBold());
    }

    private Cell createHeaderCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setTextAlignment(TextAlignment.CENTER);
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";

        try {
            if (dateObj instanceof Long) {
                return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(new Date((Long) dateObj));
            } else if (dateObj instanceof com.google.firebase.Timestamp) {
                return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(((com.google.firebase.Timestamp) dateObj).toDate());
            } else {
                return dateObj.toString();
            }
        } catch (Exception e) {
            return dateObj.toString();
        }
    }

    private void openPdfFile(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, 
                    getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer found. File saved in Downloads folder.", Toast.LENGTH_LONG).show();
        }
    }

    private void openCsvFile(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, 
                    getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "text/csv");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No CSV viewer found. File saved in Downloads folder.", Toast.LENGTH_LONG).show();
        }
    }

    private void openDownloadsFolder() {
        try {
            // Use ACTION_VIEW_DOWNLOADS to open the system Downloads app
            Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            // Fallback: If the device doesn't have a standard "Downloads" app
            Toast.makeText(this, "Could not open Downloads app directly.", Toast.LENGTH_SHORT).show();

            // Optional: Try opening a generic file manager as a backup
            try {
                Intent genericIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                genericIntent.setDataAndType(uri, "*/*");
                startActivity(genericIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "No file manager found. Files are saved in Downloads folder.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to open Downloads folder", e);
            Toast.makeText(this, 
                    "Could not open Downloads folder. Your files are saved in Downloads.", 
                    Toast.LENGTH_LONG).show();
        }
    }

    private void scanFile(File file) {
        MediaScannerConnection.scanFile(
                this,
                new String[]{file.getAbsolutePath()},
                null,
                (path, uri) -> {
                    // File scan complete - file is now visible in Downloads app
                }
        );
    }
}
