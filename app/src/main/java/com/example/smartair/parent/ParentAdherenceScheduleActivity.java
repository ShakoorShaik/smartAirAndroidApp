package com.example.smartair.parent;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.smartair.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import utils.AdherenceScheduleManager;
import utils.ChildAccountManager;

public class ParentAdherenceScheduleActivity extends AppCompatActivity {

    private Spinner spinnerChild, spinnerFrequency;
    private CardView cardDose1, cardDose2;
    private Button btnDose1Time, btnDose2Time, btnSaveSchedule, btnBack;

    private String dose1Time = "";
    private String dose2Time = "";

    private List<Map<String, Object>> childrenList;
    private final List<String> childrenNames = new ArrayList<>();
    private final Map<String, String> childNameToUidMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_adherence_schedule);

        // Initialize UI components
        spinnerChild = findViewById(R.id.spinnerChild);
        spinnerFrequency = findViewById(R.id.spinnerFrequency);
        cardDose1 = findViewById(R.id.cardDose1);
        cardDose2 = findViewById(R.id.cardDose2);
        btnDose1Time = findViewById(R.id.btnDose1Time);
        btnDose2Time = findViewById(R.id.btnDose2Time);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        btnBack = findViewById(R.id.btnBack);

        // Load children for spinner
        loadChildren();

        // Set up spinner listener to show/hide Dose 2 based on frequency
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFrequency = parent.getItemAtPosition(position).toString();

                // Show Dose 2 card only for "Twice Daily (2x)"
                if (selectedFrequency.equals("Twice Daily (2x)")) {
                    cardDose2.setVisibility(View.VISIBLE);
                } else {
                    cardDose2.setVisibility(View.GONE);
                    dose2Time = ""; // Clear dose 2 time when hidden
                    btnDose2Time.setText("Set Time");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set up Dose 1 time picker
        btnDose1Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(1);
            }
        });

        // Set up Dose 2 time picker
        btnDose2Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(2);
            }
        });

        // Set up Save button
        btnSaveSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSchedule();
            }
        });

        // Set up Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the activity and return to previous screen
            }
        });
    }

    /**
     * Shows a time picker dialog for the specified dose
     * @param doseNumber 1 for Dose 1, 2 for Dose 2
     */
    private void showTimePicker(int doseNumber) {
        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create and show time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                        // Format time as HH:MM AM/PM
                        String amPm = selectedHour >= 12 ? "PM" : "AM";
                        int displayHour = selectedHour % 12;
                        if (displayHour == 0) displayHour = 12;

                        String timeString = String.format(Locale.getDefault(),
                                "%02d:%02d %s", displayHour, selectedMinute, amPm);

                        // Update the appropriate button and store the time
                        if (doseNumber == 1) {
                            dose1Time = timeString;
                            btnDose1Time.setText(timeString);
                        } else {
                            dose2Time = timeString;
                            btnDose2Time.setText(timeString);
                        }
                    }
                },
                hour,
                minute,
                false // Use 12-hour format
        );

        timePickerDialog.setTitle("Select Time for Dose " + doseNumber);
        timePickerDialog.show();
    }

    /**
     * Load linked children from Firebase and populate the spinner
     */
    private void loadChildren() {
        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                childrenList = children;
                childrenNames.clear();
                childNameToUidMap.clear();

                if (children.isEmpty()) {
                    childrenNames.add("No children linked");
                    Toast.makeText(ParentAdherenceScheduleActivity.this,
                            "Please link a child first", Toast.LENGTH_LONG).show();
                } else {
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
                        ParentAdherenceScheduleActivity.this,
                        android.R.layout.simple_spinner_item,
                        childrenNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerChild.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ParentAdherenceScheduleActivity.this,
                        "Error loading children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                childrenNames.add("Error loading children");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ParentAdherenceScheduleActivity.this,
                        android.R.layout.simple_spinner_item,
                        childrenNames
                );
                spinnerChild.setAdapter(adapter);
            }
        });
    }

    /**
     * Validates and saves the adherence schedule
     */
    private void saveSchedule() {
        // Validate child selection
        if (spinnerChild.getSelectedItem() == null || childrenList == null || childrenList.isEmpty()) {
            Toast.makeText(this, "Please select a child", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedChildName = spinnerChild.getSelectedItem().toString();
        String childUid = childNameToUidMap.get(selectedChildName);

        if (childUid == null) {
            Toast.makeText(this, "Invalid child selection", Toast.LENGTH_SHORT).show();
            return;
        }

        String frequency = spinnerFrequency.getSelectedItem().toString();

        // Validate that at least Dose 1 time is set
        if (dose1Time.isEmpty()) {
            Toast.makeText(this, "Please set a time for Dose 1", Toast.LENGTH_SHORT).show();
            return;
        }

        // If Twice Daily is selected, validate Dose 2 time
        if (frequency.equals("Twice Daily (2x)") && dose2Time.isEmpty()) {
            Toast.makeText(this, "Please set a time for Dose 2", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save schedule to Firebase
        List<String> doseTimes = new ArrayList<>();
        doseTimes.add(dose1Time);
        if (frequency.equals("Twice Daily (2x)")) {
            doseTimes.add(dose2Time);
        }

        AdherenceScheduleManager.saveSchedule(childUid, frequency, doseTimes,
                new AdherenceScheduleManager.ScheduleCallback() {
                    @Override
                    public void onSuccess() {
                        StringBuilder message = new StringBuilder("Schedule Saved for " + selectedChildName + "!\n");
                        message.append("Frequency: ").append(frequency).append("\n");
                        message.append("Dose 1: ").append(dose1Time);

                        if (frequency.equals("Twice Daily (2x)")) {
                            message.append("\nDose 2: ").append(dose2Time);
                        }

                        Toast.makeText(ParentAdherenceScheduleActivity.this,
                                message.toString(), Toast.LENGTH_LONG).show();

                        // Optionally close the activity after saving
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ParentAdherenceScheduleActivity.this,
                                "Error saving schedule: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
