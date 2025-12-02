package com.example.smartair.parent;

import android.app.DownloadManager;
import android.content.Intent;
import android.app.DatePickerDialog;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
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
    // Filter UI
    private Button buttonStartDate;
    private Button buttonEndDate;
    private Button buttonSelectSymptoms;
    private Button buttonSelectTriggers;
    private Button buttonClearFilters;
    private Button buttonApplyFilters;
    private TextView textSelectedDateRange;
    private TextView textSelectedSymptoms;
    private TextView textSelectedTriggers;

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

    // Filter state
    @Nullable private Date filterStartDate = null;
    @Nullable private Date filterEndDate = null;
    private final Set<String> selectedSymptoms = new HashSet<>();
    private final Set<String> selectedTriggers = new HashSet<>();
    private boolean filtersApplied = false;

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
        // Filter
        buttonStartDate = findViewById(R.id.buttonStartDate);
        buttonEndDate = findViewById(R.id.buttonEndDate);
        buttonSelectSymptoms = findViewById(R.id.buttonSelectSymptoms);
        buttonSelectTriggers = findViewById(R.id.buttonSelectTriggers);
        buttonClearFilters = findViewById(R.id.buttonClearFilters);
        buttonApplyFilters = findViewById(R.id.buttonApplyFilters);
        textSelectedDateRange = findViewById(R.id.textSelectedDateRange);
        textSelectedSymptoms = findViewById(R.id.textSelectedSymptoms);
        textSelectedTriggers = findViewById(R.id.textSelectedTriggers);
        buttonReturn.setOnClickListener(v -> finish());
        buttonOpenDownloads.setOnClickListener(v -> openDownloadsFolder());

        childrenList = new ArrayList<>();

        loadChildren();

        // Wire filters
        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));
        buttonSelectSymptoms.setOnClickListener(v -> showMultiSelectDialog(true));
        buttonSelectTriggers.setOnClickListener(v -> showMultiSelectDialog(false));
        buttonClearFilters.setOnClickListener(v -> clearFilters());
        buttonApplyFilters.setOnClickListener(v -> {
            filtersApplied = true;
            Toast.makeText(this, "Filters applied. Proceed to Export.", Toast.LENGTH_SHORT).show();
        });

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

    private void updateDateRangeLabel() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (filterStartDate == null && filterEndDate == null) {
            textSelectedDateRange.setText("No date range selected");
            return;
        }
        String start = filterStartDate != null ? df.format(filterStartDate) : "...";
        String end = filterEndDate != null ? df.format(filterEndDate) : "...";
        textSelectedDateRange.setText("Date range: " + start + " to " + end);
    }

    private void clearFilters() {
        filterStartDate = null;
        filterEndDate = null;
        selectedSymptoms.clear();
        selectedTriggers.clear();
        filtersApplied = false;
        updateDateRangeLabel();
        textSelectedSymptoms.setText("All symptoms");
        textSelectedTriggers.setText("All triggers");
    }

    private void showDatePicker(boolean isStart) {
        final Calendar cal = Calendar.getInstance();
        Date pre = isStart ? filterStartDate : filterEndDate;
        if (pre != null) cal.setTime(pre);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, y);
            c.set(Calendar.MONTH, m);
            c.set(Calendar.DAY_OF_MONTH, d);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date chosen = c.getTime();
            if (isStart) {
                filterStartDate = chosen;
            } else {
                // end date: set to end of day
                c.set(Calendar.HOUR_OF_DAY, 23);
                c.set(Calendar.MINUTE, 59);
                c.set(Calendar.SECOND, 59);
                filterEndDate = c.getTime();
            }
            updateDateRangeLabel();
        }, year, month, day);
        dialog.show();
    }

    private void showMultiSelectDialog(boolean forSymptoms) {
        // Static options; could be dynamic in future
        String title = forSymptoms ? "Select symptoms" : "Select triggers";
        String[] options = forSymptoms ? new String[]{
                "Breathing difficulty","Talking difficulty","Walking difficulty","Consciousness issues","Medication used","Other symptoms"
        } : new String[]{
                "Dust","Pollen","Exercise","Smoke","Cold Air","Pets","Mold","Other"
        };
        Set<String> current = forSymptoms ? selectedSymptoms : selectedTriggers;
        boolean[] checked = new boolean[options.length];
        for (int i = 0; i < options.length; i++) {
            checked[i] = current.contains(options[i]);
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> {
                    if (isChecked) current.add(options[which]); else current.remove(options[which]);
                })
                .setPositiveButton("OK", (d, w) -> {
                    if (forSymptoms) {
                        textSelectedSymptoms.setText(current.isEmpty() ? "All symptoms" : String.join(", ", current));
                    } else {
                        textSelectedTriggers.setText(current.isEmpty() ? "All triggers" : String.join(", ", current));
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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
            // Apply filters if requested
            ChildDataAggregator toExport = filtersApplied ? makeFilteredAggregator(aggregator) : aggregator;
            if (isExportingPdf) {
                generatePdfForChild(toExport);
            } else {
                generateCsvForChild(toExport);
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

    private boolean inDateRange(@Nullable Date d) {
        if (d == null) return filterStartDate == null && filterEndDate == null; // treat as included when no range
        if (filterStartDate != null && d.before(filterStartDate)) return false;
        if (filterEndDate != null && d.after(filterEndDate)) return false;
        return true;
    }

    @Nullable
    private Date extractDateFromEntry(Map<String, Object> entry, String preferredKey) {
        try {
            Object o = entry.get(preferredKey);
            if (o == null) {
                // Try common alternatives
                o = entry.get("timestamp");
                if (o == null) o = entry.get("date");
            }
            if (o == null) return null;
            if (o instanceof Date) return (Date) o;
            if (o instanceof com.google.firebase.Timestamp) return ((com.google.firebase.Timestamp) o).toDate();
            if (o instanceof Number) return new Date(((Number) o).longValue());
            if (o instanceof String) {
                // Expecting ISO yyyy-MM-dd for zoneHistory
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    return sdf.parse((String) o);
                } catch (ParseException ignored) {
                    // try full datetime
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    return sdf2.parse((String) o);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private boolean triageMatchesSelectedSymptoms(Map<String, Object> triage) {
        if (selectedSymptoms.isEmpty()) return true;
        // Map triage fields to human labels used in UI
        Map<String, String> fieldToLabel = new HashMap<>();
        fieldToLabel.put("breathing", "Breathing difficulty");
        fieldToLabel.put("talking", "Talking difficulty");
        fieldToLabel.put("walking", "Walking difficulty");
        fieldToLabel.put("consciousness", "Consciousness issues");
        fieldToLabel.put("medication", "Medication used");
        fieldToLabel.put("otherSymptoms", "Other symptoms");

        for (Map.Entry<String, String> e : fieldToLabel.entrySet()) {
            Object v = triage.get(e.getKey());
            boolean present = false;
            if (v instanceof Boolean) present = (Boolean) v;
            else if (v instanceof String) {
                String s = ((String) v).trim();
                present = s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || !s.isEmpty();
            } else if (v != null) present = true;
            if (present && selectedSymptoms.contains(e.getValue())) return true;
        }
        return false;
    }

    private boolean triggerMatchesSelected(Map<String, Object> trigger) {
        if (selectedTriggers.isEmpty()) return true;
        Object type = trigger.get("triggerType");
        if (type == null) type = trigger.get("type");
        String s = type != null ? String.valueOf(type) : "";
        return selectedTriggers.contains(s);
    }

    private ChildDataAggregator makeFilteredAggregator(ChildDataAggregator original) {
        ChildDataAggregator filtered = new ChildDataAggregator(original.childName);
        // Filter zone history by date range
        List<Map<String, Object>> zone = new ArrayList<>();
        for (Map<String, Object> e : original.zoneHistory) {
            Date d = extractDateFromEntry(e, "date");
            if (inDateRange(d)) zone.add(e);
        }
        filtered.zoneHistory = zone;

        // Filter symptoms by date and selection
        List<Map<String, Object>> sym = new ArrayList<>();
        for (Map<String, Object> e : original.symptoms) {
            Date d = extractDateFromEntry(e, "timestamp");
            if (!inDateRange(d)) continue;
            if (!triageMatchesSelectedSymptoms(e)) continue;
            sym.add(e);
        }
        filtered.symptoms = sym;

        // Filter triggers by date and type selection
        List<Map<String, Object>> trig = new ArrayList<>();
        for (Map<String, Object> e : original.triggers) {
            Date d = extractDateFromEntry(e, "timestamp");
            if (!inDateRange(d)) continue;
            if (!triggerMatchesSelected(e)) continue;
            trig.add(e);
        }
        filtered.triggers = trig;

        // Filter inhaler logs
        List<Map<String, Object>> rescue = new ArrayList<>();
        for (Map<String, Object> e : original.rescueUsage) {
            Date d = extractDateFromEntry(e, "timestamp");
            if (inDateRange(d)) rescue.add(e);
        }
        filtered.rescueUsage = rescue;

        List<Map<String, Object>> control = new ArrayList<>();
        for (Map<String, Object> e : original.controlUsage) {
            Date d = extractDateFromEntry(e, "timestamp");
            if (inDateRange(d)) control.add(e);
        }
        filtered.controlUsage = control;

        // Mark as ready to export
        filtered.isReady = true;
        return filtered;
    }

    private String buildFilterSummary() {
        if (!filtersApplied || (filterStartDate == null && filterEndDate == null && selectedSymptoms.isEmpty() && selectedTriggers.isEmpty())) {
            return "No filters applied";
        }
        List<String> parts = new ArrayList<>();
        if (filterStartDate != null || filterEndDate != null) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String start = filterStartDate != null ? df.format(filterStartDate) : "...";
            String end = filterEndDate != null ? df.format(filterEndDate) : "...";
            parts.add("Date: " + start + " to " + end);
        }
        if (!selectedSymptoms.isEmpty()) parts.add("Symptoms: " + String.join(", ", selectedSymptoms));
        if (!selectedTriggers.isEmpty()) parts.add("Triggers: " + String.join(", ", selectedTriggers));
        return String.join(" | ", parts);
    }

    private String buildSymptomsString(Map<String, Object> triageEntry) {
        List<String> items = new ArrayList<>();
        // Severity is a separate column
        // Add flags if present
        if (isTrueish(triageEntry.get("breathing"))) items.add("Breathing difficulty");
        if (isTrueish(triageEntry.get("talking"))) items.add("Talking difficulty");
        if (isTrueish(triageEntry.get("walking"))) items.add("Walking difficulty");
        if (isTrueish(triageEntry.get("consciousness"))) items.add("Consciousness issues");
        if (isTrueish(triageEntry.get("medication"))) items.add("Medication used");
        Object other = triageEntry.get("otherSymptoms");
        if (other != null) {
            String s = String.valueOf(other).trim();
            if (!s.isEmpty()) items.add(s);
        }
        return items.isEmpty() ? "N/A" : String.join(", ", items);
    }

    private boolean isTrueish(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean) return (Boolean) v;
        String s = String.valueOf(v).trim();
        return s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equals("1");
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

            // Add filters summary
            String filterSummary = buildFilterSummary();
            if (filterSummary != null && !filterSummary.isEmpty()) {
                document.add(new Paragraph(filterSummary).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("\n"));
            }

            // Add Zone History section
            addSectionTitle(document, "Zone History");
            if (aggregator.zoneHistory.isEmpty()) {
                document.add(new Paragraph("No zone history data available.").setItalic());
            } else {
                Table zoneTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2})).useAllAvailableWidth();
                zoneTable.addHeaderCell(createHeaderCell("Date"));
                zoneTable.addHeaderCell(createHeaderCell("Zone"));
                zoneTable.addHeaderCell(createHeaderCell("PEF"));

                for (Map<String, Object> entry : aggregator.zoneHistory) {
                    zoneTable.addCell(formatDate(entry.get("date")));
                    zoneTable.addCell(String.valueOf(entry.getOrDefault("zone", "N/A")));
                    zoneTable.addCell(String.valueOf(entry.getOrDefault("pefValue", "")));
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
                    symptomsTable.addCell(formatDate(entry.get("timestamp")));
                    symptomsTable.addCell(buildSymptomsString(entry));
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
                triggersTable.addHeaderCell(createHeaderCell("Type"));
                triggersTable.addHeaderCell(createHeaderCell("Description"));

                for (Map<String, Object> entry : aggregator.triggers) {
                    triggersTable.addCell(formatDate(entry.get("timestamp")));
                    triggersTable.addCell(String.valueOf(entry.getOrDefault("triggerType", "N/A")));
                    triggersTable.addCell(String.valueOf(entry.getOrDefault("description", "")));
                }
                document.add(triggersTable);
            }
            document.add(new Paragraph("\n"));

            // Add Rescue Inhaler Usage section
            addSectionTitle(document, "Rescue Inhaler Usage");
            if (aggregator.rescueUsage.isEmpty()) {
                document.add(new Paragraph("No rescue inhaler usage data available.").setItalic());
            } else {
                Table rescueTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2})).useAllAvailableWidth();
                rescueTable.addHeaderCell(createHeaderCell("Date"));
                rescueTable.addHeaderCell(createHeaderCell("Medication"));
                rescueTable.addHeaderCell(createHeaderCell("Dosage"));
                rescueTable.addHeaderCell(createHeaderCell("Type"));

                for (Map<String, Object> entry : aggregator.rescueUsage) {
                    rescueTable.addCell(formatDate(entry.get("timestamp")));
                    rescueTable.addCell(String.valueOf(entry.getOrDefault("medicineName", "N/A")));
                    rescueTable.addCell(String.valueOf(entry.getOrDefault("dosage", "N/A")));
                    rescueTable.addCell(String.valueOf(entry.getOrDefault("medicationType", "N/A")));
                }
                document.add(rescueTable);
            }
            document.add(new Paragraph("\n"));

            // Add Control Inhaler Usage section
            addSectionTitle(document, "Control Inhaler Usage");
            if (aggregator.controlUsage.isEmpty()) {
                document.add(new Paragraph("No control inhaler usage data available.").setItalic());
            } else {
                Table controlTable = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2})).useAllAvailableWidth();
                controlTable.addHeaderCell(createHeaderCell("Date"));
                controlTable.addHeaderCell(createHeaderCell("Medication"));
                controlTable.addHeaderCell(createHeaderCell("Dosage"));
                controlTable.addHeaderCell(createHeaderCell("Type"));

                for (Map<String, Object> entry : aggregator.controlUsage) {
                    controlTable.addCell(formatDate(entry.get("timestamp")));
                    controlTable.addCell(String.valueOf(entry.getOrDefault("medicineName", "N/A")));
                    controlTable.addCell(String.valueOf(entry.getOrDefault("dosage", "N/A")));
                    controlTable.addCell(String.valueOf(entry.getOrDefault("medicationType", "N/A")));
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
            csvContent.append("Generated: ").append(new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date())).append("\n");
            String filterSummary = buildFilterSummary();
            if (filterSummary != null && !filterSummary.isEmpty()) {
                csvContent.append(filterSummary).append("\n");
            }
            csvContent.append("\n");

            // Zone History section
            csvContent.append("Zone History\n");
            csvContent.append("Date,Zone,PEF\n");
            if (aggregator.zoneHistory.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.zoneHistory) {
                    csvContent.append(escapeCsv(formatDate(entry.get("date")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("zone", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("pefValue", "")))).append("\n");
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
                    csvContent.append(escapeCsv(formatDate(entry.get("timestamp")))).append(",");
                    String symptomsStr = buildSymptomsString(entry);
                    csvContent.append(escapeCsv(symptomsStr)).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("severity", "N/A")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Triggers section
            csvContent.append("Triggers\n");
            csvContent.append("Date,Type,Description\n");
            if (aggregator.triggers.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.triggers) {
                    csvContent.append(escapeCsv(formatDate(entry.get("timestamp")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("triggerType", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("description", "")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Rescue Inhaler Usage section
            csvContent.append("Rescue Inhaler Usage\n");
            csvContent.append("Date,Medication,Dosage,Type\n");
            if (aggregator.rescueUsage.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.rescueUsage) {
                    csvContent.append(escapeCsv(formatDate(entry.get("timestamp")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("medicineName", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("dosage", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("medicationType", "N/A")))).append("\n");
                }
            }
            csvContent.append("\n");

            // Control Inhaler Usage section
            csvContent.append("Control Inhaler Usage\n");
            csvContent.append("Date,Medication,Dosage,Type\n");
            if (aggregator.controlUsage.isEmpty()) {
                csvContent.append("No data available\n");
            } else {
                for (Map<String, Object> entry : aggregator.controlUsage) {
                    csvContent.append(escapeCsv(formatDate(entry.get("timestamp")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("medicineName", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("dosage", "N/A")))).append(",");
                    csvContent.append(escapeCsv(String.valueOf(entry.getOrDefault("medicationType", "N/A")))).append("\n");
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
