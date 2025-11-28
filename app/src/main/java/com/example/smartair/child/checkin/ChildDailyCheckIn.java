package com.example.smartair.child.checkin;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChildDailyCheckIn extends AppCompatActivity {


    /*
    implement child entered daily check in or parent entered daily check in
     */


    private ChildCheckInDataFields checkInData;
    private DailyCheckInDataWriting dataWriter;

    private LinearLayout selectedSleepOption = null;
    private LinearLayout selectedCoughOption = null;
    private LinearLayout selectedLimitsOption = null;

    private EditText notesEditText;
    private Button submitButton;

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

        // Initialize data writer
        dataWriter = new DailyCheckInDataWriting();

        checkInData = new ChildCheckInDataFields();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            checkInData.userId = user.getUid();
            checkInData.userEmail = user.getEmail();
        }

        // Set current date
        checkInData.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        submitButton = findViewById(R.id.btnSubmit);
        notesEditText = findViewById(R.id.notesEditText);

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
        // 0 = bad
        // 1 = ok
        // 2 = good
        // _______________________________________

        // _______________________________________
        // QUESTION 1
        // _______________________________________
        goodSleep.setOnClickListener(v -> {
            handleSleepSelection(goodSleep, "2");
        });

        okSleep.setOnClickListener(v -> {
            handleSleepSelection(okSleep, "1");
        });

        badSleep.setOnClickListener(v -> {
            handleSleepSelection(badSleep, "0");
        });

        // _______________________________________
        // QUESTION 2
        // _______________________________________
        goodCough.setOnClickListener(v -> {
            handleCoughSelection(goodCough, "2");
        });

        okCough.setOnClickListener(v -> {
            handleCoughSelection(okCough, "1");
        });

        badCough.setOnClickListener(v -> {
            handleCoughSelection(badCough, "0");
        });

        // _______________________________________
        // QUESTION 3
        // _______________________________________
        goodLimits.setOnClickListener(v -> {
            handleLimitsSelection(goodLimits, "2");
        });

        okLimits.setOnClickListener(v -> {
            handleLimitsSelection(okLimits, "1");
        });

        badLimits.setOnClickListener(v -> {
            handleLimitsSelection(badLimits, "0");
        });

        submitButton.setOnClickListener(v -> {
            checkInData.notes = notesEditText.getText().toString();

            dataWriter.writeDailyCheckIn(checkInData, new DailyCheckInDataWriting.WriteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(ChildDailyCheckIn.this, "Check-in submitted!", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity
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