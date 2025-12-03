package com.example.smartair.parent.sharewithprovider;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParentConfigureProviderVisibility extends AppCompatActivity {

    private Spinner spinnerChildSelector;
    private Switch switchRescueLogs, switchAdherence, switchSymptoms, switchTriggers,
            switchPEF, switchTriage, switchCharts;
    private Button btnSaveSettings;
    private Button returnPage;

    private List<Map<String, Object>> childrenList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private String selectedChildUid = "";
    private String selectedChildName = "";

    private FirebaseAuth mAuth;
    private String currentUserId;

    private ParentViewableDataWriting dataWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.child_share_with_provider_settings);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = mAuth.getCurrentUser().getUid();

        dataWriter = new ParentViewableDataWriting();

        initViews();

        returnPage.setOnClickListener(v -> finish());

        loadChildrenFromDatabase();
        setupSpinner();

        btnSaveSettings.setOnClickListener(v -> saveSettingsToFirebase());
    }

    private void initViews() {
        spinnerChildSelector = findViewById(R.id.spinnerChildSelector);
        switchRescueLogs = findViewById(R.id.switchRescueLogs);
        switchAdherence = findViewById(R.id.switchAdherence);
        switchSymptoms = findViewById(R.id.switchSymptoms);
        switchTriggers = findViewById(R.id.switchTriggers);
        switchPEF = findViewById(R.id.switchPEF);
        switchTriage = findViewById(R.id.switchTriage);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        returnPage = findViewById(R.id.Return);
    }

    private void loadChildrenFromDatabase() {
        Toast.makeText(this, "Loading children...", Toast.LENGTH_SHORT).show();

        dataWriter.getLinkedChildren(currentUserId, new ParentViewableDataWriting.ChildrenLoadCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                runOnUiThread(() -> {
                    childrenList.clear();
                    childrenList.addAll(children);

                    Toast.makeText(ParentConfigureProviderVisibility.this,
                            "Found " + children.size() + " children",
                            Toast.LENGTH_SHORT).show();

                    updateSpinnerData();

                    if (!childrenList.isEmpty()) {
                        Map<String, Object> firstChild = childrenList.get(0);
                        selectedChildName = (String) firstChild.get("name");
                        selectedChildUid = (String) firstChild.get("uid");
                        loadChildSettings(selectedChildUid);
                    } else {
                        Toast.makeText(ParentConfigureProviderVisibility.this,
                                "No children linked to your account",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ParentConfigureProviderVisibility.this,
                            "Failed to load children: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateSpinnerData();
                });
            }
        });
    }

    private void updateSpinnerData() {
        List<String> childNames = new ArrayList<>();

        if (childrenList.isEmpty()) {
            childNames.add("No children found");
        } else {
            for (Map<String, Object> child : childrenList) {
                String name = (String) child.get("name");
                if (name != null && !name.isEmpty()) {
                    childNames.add(name);
                }
            }
        }

        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                childNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChildSelector.setAdapter(spinnerAdapter);

        if (!childrenList.isEmpty()) {
            spinnerChildSelector.setSelection(0);
        }
    }

    private void setupSpinner() {
        spinnerChildSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (childrenList.size() > position && position >= 0) {
                    Map<String, Object> selectedChild = childrenList.get(position);
                    selectedChildName = (String) selectedChild.get("name");
                    selectedChildUid = (String) selectedChild.get("uid");
                    loadChildSettings(selectedChildUid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadChildSettings(String childUid) {
        resetSwitches();

        if (childUid == null || childUid.isEmpty()) {
            return;
        }

        dataWriter.loadChildSettings(childUid, new ParentViewableDataWriting.SettingsLoadCallback() {
            @Override
            public void onSuccess(ParentProviderViewables settings) {
                runOnUiThread(() -> updateSwitchesFromSettings(settings));
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ParentConfigureProviderVisibility.this,
                            "Failed to load settings: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateSwitchesFromSettings(ParentProviderViewables settings) {
        switchRescueLogs.setChecked(settings.isRescueLogs());
        switchAdherence.setChecked(settings.isControllerSummary());
        switchSymptoms.setChecked(settings.isSymptomLog());
        switchTriggers.setChecked(settings.isTriggerLog());
        switchPEF.setChecked(settings.isPeakFlow());
        switchTriage.setChecked(settings.isTriageIncident());
    }

    private void resetSwitches() {
        switchRescueLogs.setChecked(false);
        switchAdherence.setChecked(false);
        switchSymptoms.setChecked(false);
        switchTriggers.setChecked(false);
        switchPEF.setChecked(false);
        switchTriage.setChecked(false);
    }

    private void saveSettingsToFirebase() {
        if (selectedChildUid == null || selectedChildUid.isEmpty() ||
                selectedChildName.equals("No children found")) {
            Toast.makeText(this, "Please select a child first", Toast.LENGTH_SHORT).show();
            return;
        }

        ParentProviderViewables settings = new ParentProviderViewables();
        settings.SetRescues(switchRescueLogs.isChecked());
        settings.SetController(switchAdherence.isChecked());
        settings.SetSymptom(switchSymptoms.isChecked());
        settings.SetTrigger(switchTriggers.isChecked());
        settings.SetPeakFlow(switchPEF.isChecked());
        settings.SetTriage(switchTriage.isChecked());

        dataWriter.saveChildSettings(selectedChildUid, settings,
                new ParentViewableDataWriting.SettingsSaveCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(ParentConfigureProviderVisibility.this,
                                    "Settings saved for " + selectedChildName,
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            Toast.makeText(ParentConfigureProviderVisibility.this,
                                    "Failed to save settings: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}