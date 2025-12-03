package com.example.smartair.child.checkin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import utils.ChildIdManager;

public class ChildDailyCheckIn extends AppCompatActivity {

    private ChildCheckInDataFields checkInData;
    private DailyCheckInDataWriting dataWriter;

    private LinearLayout selectedSleepOption = null;
    private LinearLayout selectedCoughOption = null;
    private LinearLayout selectedLimitsOption = null;

    private Switch enteredByParent = null;

    private EditText notesEditText;
    private Button submitButton;

    private String userID;
    private void handleSleepSelection (LinearLayout selectedOption, String value) {
        if(selectedSleepOption != null) {
            selectedSleepOption.setSelected(false);
        }

        selectedOption.setSelected(true);
        selectedSleepOption = selectedOption;
        checkInData.nightWaking = value;
    }

    private void handleCoughSelection(LinearLayout selectedOption, String value) {
        if (selectedCoughOption != null) {
            selectedCoughOption.setSelected(false);
        }
        selectedOption.setSelected(true);
        selectedCoughOption = selectedOption;
        checkInData.coughWheeze = value;
    }

    private void handleLimitsSelection(LinearLayout selectedOption, String value) {
        if (selectedLimitsOption != null) {
            selectedLimitsOption.setSelected(false);
        }
        selectedOption.setSelected(true);
        selectedLimitsOption = selectedOption;
        checkInData.activityLimits = value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_daily_checkin);


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // REPLACE THIS with the specific User UID from your screenshot (JsBvfr...)
        // or use FirebaseAuth.getInstance().getCurrentUser().getUid() if logged in.
        String userId = "JsBvfrQw1WVKMQj0mZpoZvFQII82";

        // Reference to the subcollection shown in your image
        String collectionPath = "users/" + userId + "/daily_checkin_logs";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Random random = new Random();

        // 1. Setup Time Range (4 to 6 months ago)
        Calendar cal = Calendar.getInstance();

        // Max date = 4 months ago
        cal.add(Calendar.MONTH, -4);
        long maxMillis = cal.getTimeInMillis();

        // Min date = 6 months ago (subtract 2 more months from the 4-month mark)
        cal.add(Calendar.MONTH, -2);
        long minMillis = cal.getTimeInMillis();

        // 2. Generate 20 random entries
        for (int i = 0; i < 20; i++) {
            // Generate random timestamp
            long randomTime = minMillis + (long) (random.nextDouble() * (maxMillis - minMillis));

            // Create Calendar object for this time to get the string format
            Calendar randomCal = Calendar.getInstance();
            randomCal.setTimeInMillis(randomTime);
            String dateString = sdf.format(randomCal.getTime());

            // 3. Match the EXACT fields from your screenshot
            Map<String, Object> data = new HashMap<>();
            data.put("activityLimits", "ok");
            data.put("coughWheeze", "ok");
            data.put("date", dateString);        // Matches document ID
            data.put("enteredByParent", true);
            data.put("nightWaking", "ok");
            data.put("notes", "Skipped");
            data.put("timestamp", randomTime);   // The random long calculated above
            data.put("userEmail", null);         // Explicit null

            // 4. Write to Firestore
            // Using .document(dateString) so the ID looks like "2025-06-15"
            db.collection(collectionPath)
                    .document(dateString)
                    .set(data)
                    .addOnSuccessListener(aVoid -> System.out.println("Success: " + dateString))
                    .addOnFailureListener(e -> System.err.println("Error: " + e.getMessage()));
        }

        dataWriter = new DailyCheckInDataWriting();

        checkInData = new ChildCheckInDataFields();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        if (!curr_child_id.equals("NA")) {
            userID = curr_child_id;
        } else {
            userID = user.getUid();
        }

        checkInData.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        submitButton = findViewById(R.id.btnSubmit);
        notesEditText = findViewById(R.id.notesEditText);
        enteredByParent = findViewById(R.id.switch1);

        //Question 1
        LinearLayout badSleep = findViewById(R.id.emojiBad);
        LinearLayout goodSleep = findViewById(R.id.emojiSleepy);
        LinearLayout okSleep = findViewById(R.id.emojiOk);

        //Question 2
        LinearLayout badCough = findViewById(R.id.emojiMask);
        LinearLayout okCough = findViewById(R.id.emojiOk1);
        LinearLayout goodCough = findViewById(R.id.emojiGood);

        //Question 3
        LinearLayout badLimits = findViewById(R.id.emojiStanding);
        LinearLayout okLimits = findViewById(R.id.emojiWalk);
        LinearLayout goodLimits = findViewById(R.id.emojiRun);

        // _______________________________________
        // QUESTION 1
        // _______________________________________
        goodSleep.setOnClickListener(v -> {
            handleSleepSelection(goodSleep, "good");
        });

        okSleep.setOnClickListener(v -> {
            handleSleepSelection(okSleep, "ok");
        });

        badSleep.setOnClickListener(v -> {
            handleSleepSelection(badSleep, "bad");
        });

        // _______________________________________
        // QUESTION 2
        // _______________________________________
        goodCough.setOnClickListener(v -> {
            handleCoughSelection(goodCough, "good");
        });

        okCough.setOnClickListener(v -> {
            handleCoughSelection(okCough, "ok");
        });

        badCough.setOnClickListener(v -> {
            handleCoughSelection(badCough, "bad");
        });

        // _______________________________________
        // QUESTION 3
        // _______________________________________
        goodLimits.setOnClickListener(v -> {
            handleLimitsSelection(goodLimits, "good");
        });

        okLimits.setOnClickListener(v -> {
            handleLimitsSelection(okLimits, "ok");
        });

        badLimits.setOnClickListener(v -> {
            handleLimitsSelection(badLimits, "bad");
        });

        Boolean isChecked = enteredByParent.isChecked();

        enteredByParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkInData.enteredByParent = true;
                }
            }
        });


        submitButton.setOnClickListener(v -> {
            checkInData.notes = notesEditText.getText().toString();

            dataWriter.writeDailyCheckIn(userID, checkInData, new DailyCheckInDataWriting.WriteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(ChildDailyCheckIn.this, "Check-in submitted!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChildDailyCheckIn.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }
}