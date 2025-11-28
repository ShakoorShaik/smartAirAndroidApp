package com.example.smartair.child.logtrigger;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/*
check for caps and lower case
 */


public class LogTriggerActivity extends AppCompatActivity {

    private EditText newTriggerInput;
    private LinearLayout triggersContainer;
    private LinearLayout emptyStateView;
    private ScrollView triggersScrollView;
    private TextView dateDisplay;
    private Button submitButton;

    private Map<String, Integer> triggerCounts = new HashMap<>();
    private Map<String, TextView> countDisplayViews = new HashMap<>();

    private String currentDate;

    private void resetCounters() {
        String today = ChildrenTriggerCountAndDates.getTodayDate();
        if (!today.equals(currentDate)) {
            for (String triggerName : triggerCounts.keySet()) {
                triggerCounts.put(triggerName, 0);
                TextView countView = countDisplayViews.get(triggerName);
                if (countView != null) {
                    countView.setText("< 0 >");
                }
            }
            currentDate = today;
            setupDateDisplay();
            showToast("New day - counters reset to zero");
        }
    }

    private void showTriggersList() {
        emptyStateView.setVisibility(android.view.View.GONE);
        triggersScrollView.setVisibility(android.view.View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setupViews() {
        newTriggerInput = findViewById(R.id.etNewTrigger);
        triggersContainer = findViewById(R.id.triggersListContainer);
        emptyStateView = findViewById(R.id.emptyState);
        triggersScrollView = findViewById(R.id.scrollViewTriggers);
        dateDisplay = findViewById(R.id.tvDate);
        submitButton = findViewById(R.id.button6);
    }

    private void setupDateDisplay() {
        currentDate = ChildrenTriggerCountAndDates.getTodayDate();
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String displayDate = displayFormat.format(new Date());
        dateDisplay.setText("Today: " + displayDate);
    }

    private void handleAddTrigger() {
        String triggerName = newTriggerInput.getText().toString().trim();

        if (triggerName.isEmpty()) {
            showToast("Please enter a trigger name");
            return;
        }

        if (triggerCounts.containsKey(triggerName)) {
            showToast("Trigger already exists");
            return;
        }

        triggerCounts.put(triggerName, 0);
        createTriggerUI(triggerName, 0);
        newTriggerInput.setText("");
        showTriggersList();
    }

    private void createTriggerUI(String triggerName, int count) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 0, 0, 16);
        row.setLayoutParams(rowParams);
        row.setPadding(32, 24, 32, 24);
        row.setBackgroundResource(R.drawable.edittext_background);

        TextView nameView = new TextView(this);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f
        );
        nameView.setLayoutParams(nameParams);
        nameView.setText(triggerName);
        nameView.setTextSize(18);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);

        LinearLayout counterSection = new LinearLayout(this);
        counterSection.setOrientation(LinearLayout.HORIZONTAL);
        counterSection.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.END);
        LinearLayout.LayoutParams counterParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f
        );
        counterSection.setLayoutParams(counterParams);

        Button decreaseBtn = createCounterButton("-");
        decreaseBtn.setOnClickListener(v -> decreaseCount(triggerName));

        TextView countView = new TextView(this);
        countView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        countView.setText("< " + count + " >");
        countView.setTextSize(18);
        countView.setTypeface(null, android.graphics.Typeface.BOLD);
        countView.setPadding(32, 0, 32, 0);
        countView.setGravity(android.view.Gravity.CENTER);

        countDisplayViews.put(triggerName, countView);

        Button increaseBtn = createCounterButton("+");
        increaseBtn.setOnClickListener(v -> increaseCount(triggerName));

        counterSection.addView(decreaseBtn);
        counterSection.addView(countView);
        counterSection.addView(increaseBtn);

        row.addView(nameView);
        row.addView(counterSection);

        triggersContainer.addView(row);
    }

    private Button createCounterButton(String text) {
        Button button = new Button(this);
        int size = (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()
        );
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        button.setLayoutParams(params);
        button.setText(text);
        button.setTextSize(18);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        return button;
    }

    private void increaseCount(String triggerName) {
        resetCounters();
        int current = triggerCounts.getOrDefault(triggerName, 0);
        updateCount(triggerName, current + 1);
    }

    private void decreaseCount(String triggerName) {
        resetCounters();
        int current = triggerCounts.getOrDefault(triggerName, 0);
        updateCount(triggerName, Math.max(0, current - 1));
    }

    private void updateCount(String triggerName, int newCount) {
        triggerCounts.put(triggerName, newCount);
        TextView countView = countDisplayViews.get(triggerName);
        if (countView != null) {
            countView.setText("< " + newCount + " >");
        }
    }

    private void saveTriggerData(ChildrenTriggerCountAndDates data, ChildrenTriggerDataWriting.WriteCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("Not signed in"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String today = data.getDate();
        String triggerName = data.getTriggerName();

        DocumentReference docRef = db.collection("users")
                .document(user.getUid())
                .collection("trigger logs")
                .document(triggerName);

        Map<String, Object> dataToSave = new HashMap<>();
        dataToSave.put(today, data.getCount());

        docRef.set(dataToSave, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    private void handleSubmitTriggers() {
        if (triggerCounts.isEmpty()) {
            showToast("No triggers to submit");
            return;
        }

        boolean hasData = false;
        final int[] completed = {0};
        final int total = triggerCounts.size();

        for (Map.Entry<String, Integer> entry : triggerCounts.entrySet()) {
            String name = entry.getKey();
            int count = entry.getValue();

            if (count > 0) {
                hasData = true;
            }

            ChildrenTriggerCountAndDates data = new ChildrenTriggerCountAndDates(name, count);

            saveTriggerData(data, new ChildrenTriggerDataWriting.WriteCallback() {
                @Override
                public void onSuccess() {
                    completed[0]++;
                    if (completed[0] == total) {
                        runOnUiThread(() -> {
                            showToast("Triggers saved successfully!");
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    completed[0]++;
                    android.util.Log.e("TriggerSave", "Failed to save trigger", e);
                    if (completed[0] == total) {
                        runOnUiThread(() -> {
                            showToast("Some triggers didn't save properly");
                        });
                    }
                }
            });
        }

        if (!hasData) {
            showToast("All triggers are at zero");
        }
    }

    private void setupClickListeners() {
        Button addTriggerButton = findViewById(R.id.btnAddTrigger);
        addTriggerButton.setOnClickListener(v -> handleAddTrigger());

        submitButton.setOnClickListener(v -> handleSubmitTriggers());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_triggers);

        setupViews();
        setupClickListeners();
        setupDateDisplay();

        Button returnButton = findViewById(R.id.button9);
        returnButton.setOnClickListener(v -> finish());

    }

    @Override
    protected void onResume() {
        super.onResume();
        resetCounters();
    }
}