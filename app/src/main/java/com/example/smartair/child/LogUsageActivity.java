package com.example.smartair.child;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utils.ChildIdManager;
import utils.DatabaseManager;

public class LogUsageActivity extends AppCompatActivity {

    private RadioButton rescueRadioButton;
    private RadioButton controllerRadioButton;
    private EditText doseCountEditText;
    private TextView buttonLowCanister;
    private int prebreathRating;
    private int postbreathRating;
    private String predose;
    private String postdose;
    private String canTypeAlert;
    private int feelingIndex1 = -1;
    private int feelingIndex2 = -1;
    private int canIndex = -1;
    String childId = null;
    String parentId = null;
    String childName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_usage);

        RadioGroup medicationTypeRadioGroup = findViewById(R.id.medicationTypeRadioGroup);
        rescueRadioButton = findViewById(R.id.rescueRadioButton);
        controllerRadioButton = findViewById(R.id.controllerRadioButton);
        doseCountEditText = findViewById(R.id.doseCountEditText);
        Button saveButton = findViewById(R.id.saveButton);
        buttonLowCanister = findViewById(R.id.text_low_canister);


        AlertDialog.Builder builder = new AlertDialog.Builder(LogUsageActivity.this);
        String [] listItems = {"Bad", "Normal", "Good"};

        builder.setTitle("How do you feel?");
        builder.setSingleChoiceItems(listItems, feelingIndex1, (dialog, which) -> {
            feelingIndex1 = which;
        });
        builder.setPositiveButton("OK!", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Intent intent = new Intent(LogUsageActivity.this, ChildDashboardMainActivity.class);
            startActivity(intent);
            finish();
        });
        builder.setCancelable(false);
        AlertDialog alert = builder.create();
        alert.show();

        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if (feelingIndex1 == -1) {
                    Toast.makeText(LogUsageActivity.this, "Please Choose Rating!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    alert.dismiss();
                    predose = listItems[feelingIndex1];
                    AlertDialog.Builder builder3 = new AlertDialog.Builder(LogUsageActivity.this);
                    final EditText inputText = new EditText(LogUsageActivity.this);

                    builder3.setTitle("How do you feel?");
                    builder3.setMessage("Enter a rating of your breathing.");
                    builder3.setView(inputText);
                    builder3.setPositiveButton("OK!", null);
                    builder3.setNegativeButton("Cancel", (dialog, which) -> {
                        Intent intent = new Intent(LogUsageActivity.this, ChildDashboardMainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                    builder3.setCancelable(false);
                    AlertDialog alert3 = builder3.create();
                    alert3.show();

                    alert3.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String input = inputText.getText().toString().trim();
                            if (input.isEmpty() || Integer.parseInt(input)>10 || Integer.parseInt(input)<0) {
                                Toast.makeText(LogUsageActivity.this, "Please type Rating!",
                                        Toast.LENGTH_SHORT).show();
                            } else {

                                prebreathRating = Integer.parseInt(input);
                                alert3.dismiss();


                            }

                        }
                    });



                }


            }


        });

        buttonLowCanister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder lowCanConfirmation = new AlertDialog.Builder(LogUsageActivity.this);
                String [] canType = {"Rescue", "Controller"};

                lowCanConfirmation.setTitle("Select Canister Type to Alert Parent");
                lowCanConfirmation.setSingleChoiceItems(canType, canIndex, (dialog,which) -> {
                    canIndex = which;
                });
                lowCanConfirmation.setPositiveButton("Alert",null);
                lowCanConfirmation.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                lowCanConfirmation.setCancelable(true);

                AlertDialog lowCanisterAlert = lowCanConfirmation.create();
                lowCanisterAlert.show();

                lowCanisterAlert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (canIndex == -1) {
                            Toast.makeText(LogUsageActivity.this, "Choose Canister Type!", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        else {
                            canTypeAlert = canType[canIndex];
                            sendLowCanisterAlert();
                            lowCanisterAlert.dismiss();
                        }


                    }
                });
            }
        });





        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(LogUsageActivity.this);
                String [] listItems2 = {"Worse", "Same", "Better"};

                builder2.setTitle("How do you feel?");
                builder2.setSingleChoiceItems(listItems2, feelingIndex2, (dialog, which) -> {
                    feelingIndex2 = which;
                });
                builder2.setPositiveButton("OK!", null);
                builder2.setNegativeButton("Cancel", (dialog, which) -> {
                    Intent intent = new Intent(LogUsageActivity.this, ChildDashboardMainActivity.class);
                    startActivity(intent);
                    finish();
                });
                builder2.setCancelable(false);

                AlertDialog alert2 = builder2.create();
                alert2.show();

                alert2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v) {


                        if (feelingIndex2 == -1) {
                            Toast.makeText(LogUsageActivity.this, "Please Choose Rating!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            postdose = listItems2[feelingIndex2];
                            alert2.dismiss();
                            AlertDialog.Builder builder3 = new AlertDialog.Builder(LogUsageActivity.this);
                            final EditText inputText = new EditText(LogUsageActivity.this);

                            builder3.setTitle("Post-dose Check!");
                            builder3.setMessage("Enter a rating of your breathing.");
                            builder3.setView(inputText);
                            builder3.setPositiveButton("OK!", null);
                            builder3.setNegativeButton("Cancel", (dialog, which) -> {
                                Intent intent = new Intent(LogUsageActivity.this, ChildDashboardMainActivity.class);
                                startActivity(intent);
                                finish();
                            });
                            builder3.setCancelable(false);

                            AlertDialog alert3 = builder3.create();
                            alert3.show();

                            alert3.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String input = inputText.getText().toString().trim();
                                    if (input.isEmpty() || Integer.parseInt(input)>10 || Integer.parseInt(input)<0) {
                                        Toast.makeText(LogUsageActivity.this, "Please type Rating!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {

                                        postbreathRating = Integer.parseInt(input);
                                        saveInhalerLog();
                                        Intent intent = new Intent(
                                                LogUsageActivity.this,
                                                ChildDashboardMainActivity.class);
                                        startActivity(intent);

                                    }

                                }
                            });

                        }


                    }


                });



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
        inhalerLog.put("preDoseStatus", predose);
        inhalerLog.put("postDoseStatus", postdose);
        inhalerLog.put("preDoseBreathRating", prebreathRating);
        inhalerLog.put("postDoseBreathRating", postbreathRating);

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

        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        if (!curr_child_id.equals("NA")) {
            DatabaseManager.getInstance().addInhalerLog(curr_child_id, inhalerLog, callback);
        } else { DatabaseManager.getInstance().addInhalerLog(inhalerLog, callback); }
    }

    private void sendLowCanisterAlert() {
        ChildIdManager childIdManager = new ChildIdManager(LogUsageActivity.this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        childId = childIdManager.getChildId();

        if (!childId.equals("NA")) {

            if (user == null) { return; }

            parentId = user.getUid();
            db.collection("users").document(childId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if(documentSnapshot.exists()) {
                        childName = documentSnapshot.getString("name");

                        if(childName != null) {
                            saveLowCanAlert(childId, parentId, childName);
                        }
                    }
                }
            });


        }
        else {

            if (user == null) { return; }
            childId = user.getUid();

            db.collection("users").document(childId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    if(documentSnapshot.exists()) {
                        parentId = documentSnapshot.getString("parentUid");
                        childName = documentSnapshot.getString("name");

                        if(parentId != null && childName != null) {
                            saveLowCanAlert(childId, parentId, childName);
                        }
                    }
                }
            });



        }







    }
    private void saveLowCanAlert(String childId, String parentId, String childName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> alert = new HashMap<>();

        alert.put("childUid", childId);
        alert.put("childName", childName);
        alert.put("canType", canTypeAlert);
        alert.put("status", "unread");

        db.collection("users").document(parentId)
                .collection("low_canister_alerts")
                .add(alert)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(LogUsageActivity.this,
                                    "Alert Sent to Parent", Toast.LENGTH_LONG)
                            .show();
                });
    }
}
