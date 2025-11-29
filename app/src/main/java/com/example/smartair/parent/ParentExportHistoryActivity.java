package com.example.smartair.parent;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.InhalerLogManager;
import utils.TriageHistoryManager;
import utils.TriggersManager;
import utils.ZoneHistoryManager;

public class ParentExportHistoryActivity extends AppCompatActivity {

    private Spinner spinnerChildSelector;
    private RadioGroup radioGroupFormat;
    private RadioButton radioCsv;
    private RadioButton radioPdf;
    private Button buttonExport;
    private Button buttonReturn;

    private List<Map<String, Object>> childrenList;
    private List<String> childrenNames;
    private Map<String, String> childNameToUidMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_export_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Export History");
        }

        spinnerChildSelector = findViewById(R.id.spinnerChildSelector);
        radioGroupFormat = findViewById(R.id.radioGroupFormat);
        radioCsv = findViewById(R.id.radioCsv);
        radioPdf = findViewById(R.id.radioPdf);
        buttonExport = findViewById(R.id.buttonExport);
        buttonReturn = findViewById(R.id.buttonReturn);
        buttonReturn.setOnClickListener(v -> finish());

        childrenList = new ArrayList<>();
        childrenNames = new ArrayList<>();
        childNameToUidMap = new HashMap<>();

        loadChildren();

        buttonExport.setOnClickListener(v -> handleExport());
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

    private void handleExport() {
        String selectedChild = (String) spinnerChildSelector.getSelectedItem();
        if (selectedChild == null) {
            Toast.makeText(this, "Please select a child", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCsv = radioCsv.isChecked();
        Toast.makeText(this, "Retrieving data for " + selectedChild + "...", Toast.LENGTH_SHORT).show();

        if (selectedChild.equals("All Children")) {
            retrieveAllChildrenData(isCsv);
        } else {
            String childUid = childNameToUidMap.get(selectedChild);
            retrieveSingleChildData(childUid, selectedChild, isCsv);
        }
    }

    private void retrieveAllChildrenData(boolean isCsv) {
        if (childrenList == null || childrenList.isEmpty()) {
            Toast.makeText(this, "No children found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Map<String, Object> child : childrenList) {
            String uid = (String) child.get("uid");
            String name = (String) child.get("name");
            if (uid != null && name != null) {
                retrieveSingleChildData(uid, name, isCsv);
            }
        }
    }

    private void retrieveSingleChildData(String childUid, String childName, boolean isCsv) {
        ChildIdManager manager = new ChildIdManager(this);
        manager.SaveChildId(childUid);

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
        // TODO: Implement CSV/PDF generation with this data
    }

    private void processSymptomsData(String childName, List<Map<String, Object>> data) {
        // TODO: Implement CSV/PDF generation with this data
    }

    private void processTriggersData(String childName, List<Map<String, Object>> data) {
        // TODO: Implement CSV/PDF generation with this data
    }

    private void processRescueUsageData(String childName, List<Map<String, Object>> data) {
        // TODO: Implement CSV/PDF generation with this data
    }

    private void processControlUsageData(String childName, List<Map<String, Object>> data) {
        // TODO: Implement CSV/PDF generation with this data
    }
}
