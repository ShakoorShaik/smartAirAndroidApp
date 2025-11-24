package com.example.smartair.child;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

import utils.DatabaseManager;

public class LogUsageActivity extends AppCompatActivity {

    private RadioGroup medicationTypeRadioGroup;
    private RadioButton rescueRadioButton;
    private RadioButton controllerRadioButton;
    private EditText doseCountEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_usage);

        medicationTypeRadioGroup = findViewById(R.id.medicationTypeRadioGroup);
        rescueRadioButton = findViewById(R.id.rescueRadioButton);
        controllerRadioButton = findViewById(R.id.controllerRadioButton);
        doseCountEditText = findViewById(R.id.doseCountEditText);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInhalerLog();
            }
        });
    }

    private void saveInhalerLog() {
        String medicationType;
        if (rescueRadioButton.isChecked()) {
            medicationType = "Rescue";
        } else if (controllerRadioButton.isChecked()) {
            medicationType = "Controller";
        } else {
            Toast.makeText(this, "Please select a medication type", Toast.LENGTH_SHORT).show();
            return;
        }

        String doseCountStr = doseCountEditText.getText().toString().trim();
        if (TextUtils.isEmpty(doseCountStr) || Integer.parseInt(doseCountStr) == 0) {
            doseCountEditText.setError("Dose count cannot be empty or zero");
            return;
        }
        int doseCount = Integer.parseInt(doseCountStr);

        Map<String, Object> inhalerLog = new HashMap<>();
        inhalerLog.put("medicationType", medicationType);
        inhalerLog.put("doseCount", doseCount);
        inhalerLog.put("timestamp", Timestamp.now());
        inhalerLog.put("enteredBy", "Child");
        inhalerLog.put("techniqueCompleted", false);
        inhalerLog.put("techniqueSteps", null);
        inhalerLog.put("preDoseStatus", null);
        inhalerLog.put("postDoseStatus", null);
        inhalerLog.put("breathRating", null);

        DatabaseManager.FirestoreCallback callback =  new DatabaseManager.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LogUsageActivity.this, "Inhaler usage logged successfully", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(LogUsageActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        if (getIntent().hasExtra("uid")) {
            String childUid = getIntent().getStringExtra("uid");
            DatabaseManager.getInstance().addInhalerLog(childUid, inhalerLog, callback);
        }
        else { DatabaseManager.getInstance().addInhalerLog(inhalerLog, callback); }
    }
}
