package com.example.smartair.parent;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import utils.ChildAccountManager;
import utils.InhalerLogManager;
import utils.TriageHistoryManager;
import utils.TriggersManager;
import utils.ZoneHistoryManager;

public class ParentExportHistoryActivity extends AppCompatActivity {

    private static final String TAG = "ParentExportHistory";

    // UI Components
    private Spinner spinnerChildSelector;
    private Button buttonOpenDownloads;
    private TextView textSelectedDateRange;
    private TextView textSelectedSymptoms;
    private TextView textSelectedTriggers;

    // Data Management
    private List<Map<String, Object>> childrenList;
    private final List<String> childrenNames = new ArrayList<>();
    private final Map<String, String> childNameToUidMap = new HashMap<>();

    // Concurrent map to handle async data aggregation safely
    private final Map<String, ChildDataAggregator> childDataMap = new ConcurrentHashMap<>();

    // Export State
    private boolean isExportingPdf;
    private int totalChildrenToExport = 0;
    private final AtomicInteger completedExports = new AtomicInteger(0);

    // Filter State
    @Nullable private Date filterStartDate = null;
    @Nullable private Date filterEndDate = null;
    private final Set<String> selectedSymptoms = new HashSet<>();
    private final Set<String> selectedTriggers = new HashSet<>();
    private boolean filtersApplied = false;

    // --- Inner Class for Data Aggregation ---
    private static class ChildDataAggregator {
        final String childName;
        List<Map<String, Object>> zoneHistory = new ArrayList<>();
        List<Map<String, Object>> symptoms = new ArrayList<>();
        List<Map<String, Object>> triggers = new ArrayList<>();
        List<Map<String, Object>> rescueUsage = new ArrayList<>();
        List<Map<String, Object>> controlUsage = new ArrayList<>();

        // We expect 5 asynchronous callbacks per child
        final AtomicInteger pendingCallbacks = new AtomicInteger(5);
        boolean isReady = false;

        ChildDataAggregator(String name) {
            this.childName = name;
        }

        void markCallbackComplete() {
            if (pendingCallbacks.decrementAndGet() == 0) {
                isReady = true;
            }
        }
    }

    // --- Lifecycle & Setup ---

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_export_history);

        setupToolbar();
        setupViews();
        loadChildren();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Export History");
        }
    }

    private void setupViews() {
        spinnerChildSelector = findViewById(R.id.spinnerChildSelector);
        RadioButton radioCsv = findViewById(R.id.radioCsv);
        Button buttonExport = findViewById(R.id.buttonExport);
        buttonOpenDownloads = findViewById(R.id.buttonOpenDownloads);
        Button buttonReturn = findViewById(R.id.buttonReturn);

        // Filter Buttons
        Button buttonStartDate = findViewById(R.id.buttonStartDate);
        Button buttonEndDate = findViewById(R.id.buttonEndDate);
        Button buttonSelectSymptoms = findViewById(R.id.buttonSelectSymptoms);
        Button buttonSelectTriggers = findViewById(R.id.buttonSelectTriggers);
        Button buttonClearFilters = findViewById(R.id.buttonClearFilters);
        Button buttonApplyFilters = findViewById(R.id.buttonApplyFilters);

        textSelectedDateRange = findViewById(R.id.textSelectedDateRange);
        textSelectedSymptoms = findViewById(R.id.textSelectedSymptoms);
        textSelectedTriggers = findViewById(R.id.textSelectedTriggers);

        buttonReturn.setOnClickListener(v -> finish());
        buttonOpenDownloads.setOnClickListener(v -> openDownloadsFolder());

        // Filter Listeners
        buttonStartDate.setOnClickListener(v -> showDatePicker(true));
        buttonEndDate.setOnClickListener(v -> showDatePicker(false));
        buttonSelectSymptoms.setOnClickListener(v -> showMultiSelectDialog(true));
        buttonSelectTriggers.setOnClickListener(v -> showMultiSelectDialog(false));
        buttonClearFilters.setOnClickListener(v -> clearFilters());
        buttonApplyFilters.setOnClickListener(v -> {
            filtersApplied = true;
            Toast.makeText(this, "Filters applied. Proceed to Export.", Toast.LENGTH_SHORT).show();
        });

        buttonExport.setOnClickListener(v -> handleExport(radioCsv.isChecked()));
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
                runOnUiThread(() -> {
                    childrenList = children;
                    childrenNames.clear();
                    childNameToUidMap.clear();
                    if (children != null) {
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
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(ParentExportHistoryActivity.this,
                        "Failed to load children: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    // --- Export Logic ---

    private void handleExport(boolean isCsv) {
        String selectedChild = (String) spinnerChildSelector.getSelectedItem();
        if (selectedChild == null) {
            Toast.makeText(this, "Please select a child", Toast.LENGTH_SHORT).show();
            return;
        }

        isExportingPdf = !isCsv;
        childDataMap.clear();
        totalChildrenToExport = 0;
        completedExports.set(0);
        buttonOpenDownloads.setVisibility(View.GONE);

        Toast.makeText(this, "Retrieving data for " + selectedChild + "...", Toast.LENGTH_SHORT).show();

        totalChildrenToExport = 1;
        String childUid = childNameToUidMap.get(selectedChild);
        if (childUid != null) {
            retrieveSingleChildData(childUid, selectedChild);
        } else {
            Toast.makeText(this, "Invalid child selection", Toast.LENGTH_SHORT).show();
        }
    }

    private void retrieveSingleChildData(String childUid, String childName) {
        ChildDataAggregator aggregator = new ChildDataAggregator(childName);
        childDataMap.put(childName, aggregator);

        // Fetch data in parallel
        ZoneHistoryManager.getAllZoneHistoryForChild(childUid, new ZoneHistoryManager.ZoneHistoryDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) { processData(childName, data, "zone"); }
            @Override
            public void onFailure(Exception e) { processData(childName, new ArrayList<>(), "zone"); }
        });

        TriageHistoryManager.getAllTriageDataForChild(childUid, new TriageHistoryManager.TriageDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) { processData(childName, data, "symptoms"); }
            @Override
            public void onFailure(Exception e) { processData(childName, new ArrayList<>(), "symptoms"); }
        });

        TriggersManager.getAllTriggersForChild(childUid, new TriggersManager.TriggersDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) { processData(childName, data, "triggers"); }
            @Override
            public void onFailure(Exception e) { processData(childName, new ArrayList<>(), "triggers"); }
        });

        InhalerLogManager.getRescueInhalerLogsForChild(childUid, new InhalerLogManager.InhalerLogDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) { processData(childName, data, "rescue"); }
            @Override
            public void onFailure(Exception e) { processData(childName, new ArrayList<>(), "rescue"); }
        });

        InhalerLogManager.getControlInhalerLogsForChild(childUid, new InhalerLogManager.InhalerLogDataCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) { processData(childName, data, "control"); }
            @Override
            public void onFailure(Exception e) { processData(childName, new ArrayList<>(), "control"); }
        });
    }

    // Consolidated processing method
    private void processData(String childName, List<Map<String, Object>> data, String type) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator == null) return;

        List<Map<String, Object>> safeData = data != null ? data : new ArrayList<>();

        synchronized (aggregator) {
            switch (type) {
                case "zone": aggregator.zoneHistory = safeData; break;
                case "symptoms": aggregator.symptoms = safeData; break;
                case "triggers": aggregator.triggers = safeData; break;
                case "rescue": aggregator.rescueUsage = safeData; break;
                case "control": aggregator.controlUsage = safeData; break;
            }
            aggregator.markCallbackComplete();
        }

        checkAndGenerateExport(childName);
    }

    private void checkAndGenerateExport(String childName) {
        ChildDataAggregator aggregator = childDataMap.get(childName);
        if (aggregator != null && aggregator.isReady) {
            ChildDataAggregator finalAggregator = filtersApplied ? makeFilteredAggregator(aggregator) : aggregator;

            if (isExportingPdf) {
                generatePdfForChild(finalAggregator);
            } else {
                generateCsvForChild(finalAggregator);
            }

            int completed = completedExports.incrementAndGet();
            // For single-child export, completion handling is in handleExportSuccess
        }
    }

    // --- Filter Logic ---

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

        DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            Calendar c = Calendar.getInstance();

            Calendar endThreshold = Calendar.getInstance();
            Calendar startThreshold = Calendar.getInstance();
            endThreshold.add(Calendar.MONTH, -3);
            startThreshold.add(Calendar.MONTH, -6);
            long endThresholdInMillis = endThreshold.getTimeInMillis();
            long startThresholdInMillis = startThreshold.getTimeInMillis();

            c.set(y, m, d);
            if (isStart) {
                if (c.getTimeInMillis() >= startThresholdInMillis) {
                    c.set(Calendar.HOUR_OF_DAY, 0);
                    c.set(Calendar.MINUTE, 0);
                    filterStartDate = c.getTime();
                } else {
                    Toast.makeText(this, "Start date must be at least 6 months ago.",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                if (c.getTimeInMillis() <= endThresholdInMillis) {
                    c.set(Calendar.HOUR_OF_DAY, 23);
                    c.set(Calendar.MINUTE, 59);

                    filterEndDate = c.getTime();
                } else {
                    Toast.makeText(this, "End date must be at least 3 months ago.",
                            Toast.LENGTH_SHORT).show();
                }



            }
            updateDateRangeLabel();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showMultiSelectDialog(boolean forSymptoms) {
        String title = forSymptoms ? "Select symptoms" : "Select triggers";
        String[] options = forSymptoms ? new String[]{
                "Breathing difficulty", "Talking difficulty", "Walking difficulty",
                "Consciousness issues", "Medication used", "Other symptoms"
        } : new String[]{
                "Dust", "Pollen", "Exercise", "Smoke", "Cold Air", "Pets", "Mold", "Other"
        };

        Set<String> current = forSymptoms ? selectedSymptoms : selectedTriggers;
        boolean[] checked = new boolean[options.length];
        for (int i = 0; i < options.length; i++) checked[i] = current.contains(options[i]);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> {
                    if (isChecked) current.add(options[which]); else current.remove(options[which]);
                })
                .setPositiveButton("OK", (d, w) -> {
                    TextView target = forSymptoms ? textSelectedSymptoms : textSelectedTriggers;
                    target.setText(current.isEmpty() ? (forSymptoms ? "All symptoms" : "All triggers") : String.join(", ", current));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean inDateRange(@Nullable Date d) {
        if (d == null) return filterStartDate == null && filterEndDate == null;
        if (filterStartDate != null && d.before(filterStartDate)) return false;
        if (filterEndDate != null && d.after(filterEndDate)) return false;
        return true;
    }

    private ChildDataAggregator makeFilteredAggregator(ChildDataAggregator original) {
        ChildDataAggregator filtered = new ChildDataAggregator(original.childName);

        // Helper to filter lists
        filtered.zoneHistory = filterList(original.zoneHistory, "date", false, false);
        filtered.symptoms = filterList(original.symptoms, "timestamp", true, false);
        filtered.triggers = filterList(original.triggers, "timestamp", false, true);
        filtered.rescueUsage = filterList(original.rescueUsage, "timestamp", false, false);
        filtered.controlUsage = filterList(original.controlUsage, "timestamp", false, false);

        filtered.isReady = true;
        return filtered;
    }

    private List<Map<String, Object>> filterList(List<Map<String, Object>> source, String dateKey, boolean checkSymptoms, boolean checkTriggers) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : source) {
            Date d = extractDateFromEntry(item, dateKey);
            if (!inDateRange(d)) continue;
            if (checkSymptoms && !triageMatchesSelectedSymptoms(item)) continue;
            if (checkTriggers && !triggerMatchesSelected(item)) continue;
            result.add(item);
        }
        return result;
    }

    private boolean triageMatchesSelectedSymptoms(Map<String, Object> triage) {
        if (selectedSymptoms.isEmpty()) return true;
        Map<String, String> map = new HashMap<>();
        map.put("breathing", "Breathing difficulty");
        map.put("talking", "Talking difficulty");
        map.put("walking", "Walking difficulty");
        map.put("consciousness", "Consciousness issues");
        map.put("medication", "Medication used");
        map.put("otherSymptoms", "Other symptoms");

        for (Map.Entry<String, String> e : map.entrySet()) {
            if (isTrueish(triage.get(e.getKey())) && selectedSymptoms.contains(e.getValue())) return true;
        }
        return false;
    }

    private boolean triggerMatchesSelected(Map<String, Object> trigger) {
        if (selectedTriggers.isEmpty()) return true;
        Object type = trigger.getOrDefault("triggerType", trigger.get("type"));
        return type != null && selectedTriggers.contains(String.valueOf(type));
    }

    // --- File Generation ---

    private void generatePdfForChild(ChildDataAggregator aggregator) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "SmartAir_" + aggregator.childName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".pdf";
        File pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            document.add(new Paragraph("SmartAir Health Report").setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Child: " + aggregator.childName + "\nGenerated: " +
                    new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            String filterSummary = buildFilterSummary();
            if (!filterSummary.equals("No filters applied")) {
                document.add(new Paragraph(filterSummary).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("\n"));
            }

            // Tables
            addPdfTable(document, "Zone History", aggregator.zoneHistory, new String[]{"Date", "Zone", "PEF"},
                    (entry) -> new String[]{formatDate(getDateObject(entry)), String.valueOf(entry.getOrDefault("zone", "N/A")), String.valueOf(entry.getOrDefault("pefValue", ""))});

            addPdfTable(document, "Symptoms", aggregator.symptoms, new String[]{"Date", "Symptoms", "Severity"},
                    (entry) -> new String[]{formatDate(entry.get("timestamp")), buildSymptomsString(entry), String.valueOf(entry.getOrDefault("severity", "N/A"))});

            addPdfTable(document, "Triggers", aggregator.triggers, new String[]{"Date", "Type", "Description"},
                    (entry) -> new String[]{formatDate(entry.get("timestamp")), String.valueOf(entry.getOrDefault("triggerType", "N/A")), String.valueOf(entry.getOrDefault("description", ""))});

            addPdfTable(document, "Rescue Inhaler Usage", aggregator.rescueUsage, new String[]{"Date", "Medication", "Dosage"},
                    (entry) -> new String[]{formatDate(entry.get("timestamp")), String.valueOf(entry.getOrDefault("medicineName", "N/A")), String.valueOf(entry.getOrDefault("dosage", "N/A"))});

            addPdfTable(document, "Control Inhaler Usage", aggregator.controlUsage, new String[]{"Date", "Medication", "Dosage"},
                    (entry) -> new String[]{formatDate(entry.get("timestamp")), String.valueOf(entry.getOrDefault("medicineName", "N/A")), String.valueOf(entry.getOrDefault("dosage", "N/A"))});

            scanFile(pdfFile);
            handleExportSuccess(pdfFile, aggregator.childName, "PDF");

        } catch (IOException e) {
            Log.e(TAG, "Failed to generate PDF", e);
            runOnUiThread(() -> Toast.makeText(this, "Error generating PDF", Toast.LENGTH_SHORT).show());
        }
    }

    private void generateCsvForChild(ChildDataAggregator aggregator) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "SmartAir_" + aggregator.childName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".csv";
        File csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        try (FileOutputStream fos = new FileOutputStream(csvFile)) {
            StringBuilder sb = new StringBuilder();
            sb.append("SmartAir Health Report\nChild: ").append(aggregator.childName).append("\n");
            sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())).append("\n\n");

            appendCsvSection(sb, "Zone History", "Date,Zone,PEF", aggregator.zoneHistory,
                    (entry) -> String.format("%s,%s,%s", escapeCsv(formatDate(getDateObject(entry))), escapeCsv(String.valueOf(entry.getOrDefault("zone", ""))), escapeCsv(String.valueOf(entry.getOrDefault("pefValue", "")))));

            appendCsvSection(sb, "Symptoms", "Date,Symptoms,Severity", aggregator.symptoms,
                    (entry) -> String.format("%s,%s,%s", escapeCsv(formatDate(entry.get("timestamp"))), escapeCsv(buildSymptomsString(entry)), escapeCsv(String.valueOf(entry.getOrDefault("severity", "")))));

            appendCsvSection(sb, "Triggers", "Date,Type,Description", aggregator.triggers,
                    (entry) -> String.format("%s,%s,%s", escapeCsv(formatDate(entry.get("timestamp"))), escapeCsv(String.valueOf(entry.getOrDefault("triggerType", ""))), escapeCsv(String.valueOf(entry.getOrDefault("description", "")))));

            appendCsvSection(sb, "Inhaler Usage", "Date,Medication,Dosage,Type", aggregator.rescueUsage,
                    (entry) -> String.format("%s,%s,%s,Rescue", escapeCsv(formatDate(entry.get("timestamp"))), escapeCsv(String.valueOf(entry.getOrDefault("medicineName", ""))), escapeCsv(String.valueOf(entry.getOrDefault("dosage", "")))));

            // Include control inhaler usage as a separate section to match PDF content
            appendCsvSection(sb, "Inhaler Usage (Control)", "Date,Medication,Dosage,Type", aggregator.controlUsage,
                    (entry) -> String.format("%s,%s,%s,Control", escapeCsv(formatDate(entry.get("timestamp"))), escapeCsv(String.valueOf(entry.getOrDefault("medicineName", ""))), escapeCsv(String.valueOf(entry.getOrDefault("dosage", "")))));

            fos.write(sb.toString().getBytes());
            scanFile(csvFile);
            handleExportSuccess(csvFile, aggregator.childName, "CSV");

        } catch (IOException e) {
            Log.e(TAG, "Failed to generate CSV", e);
            runOnUiThread(() -> Toast.makeText(this, "Error generating CSV", Toast.LENGTH_SHORT).show());
        }
    }

    private interface RowDataMapper { String[] map(Map<String, Object> entry); }
    private interface CsvRowMapper { String map(Map<String, Object> entry); }

    private void addPdfTable(Document doc, String title, List<Map<String, Object>> data, String[] headers, RowDataMapper mapper) {
        doc.add(new Paragraph(title).setFontSize(14).setBold());
        if (data.isEmpty()) {
            doc.add(new Paragraph("No data available.").setItalic());
        } else {
            Table table = new Table(UnitValue.createPercentArray(headers.length)).useAllAvailableWidth();
            for (String h : headers) table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()).setTextAlignment(TextAlignment.CENTER));
            for (Map<String, Object> entry : data) {
                for (String val : mapper.map(entry)) table.addCell(new Paragraph(val != null ? val : ""));
            }
            doc.add(table);
        }
        doc.add(new Paragraph("\n"));
    }

    private void appendCsvSection(StringBuilder sb, String title, String header, List<Map<String, Object>> data, CsvRowMapper mapper) {
        sb.append(title).append("\n").append(header).append("\n");
        if (data.isEmpty()) sb.append("No data available\n");
        else for (Map<String, Object> entry : data) sb.append(mapper.map(entry)).append("\n");
        sb.append("\n");
    }

    private void handleExportSuccess(File file, String childName, String type) {
        runOnUiThread(() -> {
            Toast.makeText(this, type + " saved to Downloads folder.", Toast.LENGTH_LONG).show();
            buttonOpenDownloads.setVisibility(View.VISIBLE);
            if (type.equals("PDF")) openPdfFile(file); else openCsvFile(file);
        });
    }

    // --- Utilities ---

    @Nullable
    private Date extractDateFromEntry(Map<String, Object> entry, String key) {
        Object o = entry.getOrDefault(key, entry.getOrDefault("timestamp", entry.get("date")));
        if (o instanceof Date) return (Date) o;
        if (o instanceof com.google.firebase.Timestamp) return ((com.google.firebase.Timestamp) o).toDate();
        if (o instanceof Long) return new Date((Long) o);
        if (o instanceof String) {
            try { return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse((String) o); }
            catch (ParseException ignored) {} // Try common formats if needed
        }
        return null;
    }

    private Object getDateObject(Map<String, Object> entry) {
        return entry.getOrDefault("date", entry.get("timestamp"));
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "N/A";
        try {
            if (dateObj instanceof Long) return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(new Date((Long) dateObj));
            if (dateObj instanceof com.google.firebase.Timestamp) return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(((com.google.firebase.Timestamp) dateObj).toDate());
            return dateObj.toString();
        } catch (Exception e) { return dateObj.toString(); }
    }

    private String buildFilterSummary() {
        if (!filtersApplied) return "No filters applied";
        List<String> parts = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (filterStartDate != null || filterEndDate != null)
            parts.add("Date: " + (filterStartDate != null ? df.format(filterStartDate) : "...") + " to " + (filterEndDate != null ? df.format(filterEndDate) : "..."));
        if (!selectedSymptoms.isEmpty()) parts.add("Symptoms: " + String.join(", ", selectedSymptoms));
        if (!selectedTriggers.isEmpty()) parts.add("Triggers: " + String.join(", ", selectedTriggers));
        return parts.isEmpty() ? "No filters applied" : String.join(" | ", parts);
    }

    private String buildSymptomsString(Map<String, Object> entry) {
        List<String> items = new ArrayList<>();
        if (isTrueish(entry.get("breathing"))) items.add("Breathing");
        if (isTrueish(entry.get("talking"))) items.add("Talking");
        if (isTrueish(entry.get("walking"))) items.add("Walking");
        if (isTrueish(entry.get("consciousness"))) items.add("Consciousness");
        if (isTrueish(entry.get("medication"))) items.add("Medication");
        Object other = entry.get("otherSymptoms");
        if (other != null && !other.toString().trim().isEmpty()) items.add(other.toString().trim());
        return items.isEmpty() ? "None" : String.join(", ", items);
    }

    private boolean isTrueish(Object v) {
        if (v instanceof Boolean) return (Boolean) v;
        String s = String.valueOf(v).trim();
        return s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true") || s.equals("1");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void scanFile(File file) {
        MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, null);
    }

    private void openPdfFile(File file) { openFile(file, "application/pdf"); }
    private void openCsvFile(File file) { openFile(file, "text/csv"); }

    private void openFile(File file, String mimeType) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "File saved to Downloads.", Toast.LENGTH_LONG).show();
        }
    }

    private void openDownloadsFolder() {
        try {
            startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (Exception e) {
            Toast.makeText(this, "Files are in your Downloads folder.", Toast.LENGTH_LONG).show();
        }
    }
}