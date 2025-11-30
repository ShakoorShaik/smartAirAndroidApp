package com.example.smartair.child.logtrigger;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LogTriggerActivity extends AppCompatActivity {

    private EditText newTriggerInput;
    private LinearLayout triggersContainer;
    private LinearLayout emptyStateView;
    private ScrollView triggersScrollView;
    private TextView dateDisplay;
    private Button logTrigger;
    private Button returnButton;

    private ChildrenTriggerDataWriting dataWriter;
    private List<TriggerOccurrence> currentTriggers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_triggers);

        setupViews();
        setupClickListeners();

        dataWriter = new ChildrenTriggerDataWriting();
        dateDisplay.setText("Today's Date: " + ChildrenTriggerCountAndDates.getTodayDate());

        loadTodayTriggers();
    }

    private void setupViews() {
        newTriggerInput = findViewById(R.id.etNewTrigger);
        dateDisplay = findViewById(R.id.tvDate);
        logTrigger = findViewById(R.id.button11);
        returnButton = findViewById(R.id.button9);
        triggersContainer = findViewById(R.id.triggersListContainer);
        emptyStateView = findViewById(R.id.emptyState);
        triggersScrollView = findViewById(R.id.scrollViewTriggers);
    }

    private void setupClickListeners() {
        logTrigger.setOnClickListener(v -> logNewTrigger());
        returnButton.setOnClickListener(v -> finish());
    }

    private void logNewTrigger() {
        String triggerName = newTriggerInput.getText().toString().trim();

        if (TextUtils.isEmpty(triggerName)) {
            Toast.makeText(this, "Please enter a trigger name", Toast.LENGTH_SHORT).show();
            return;
        }

        ChildrenTriggerCountAndDates data = new ChildrenTriggerCountAndDates(triggerName);

        dataWriter.WriteDateToSubCollection(data, new ChildrenTriggerDataWriting.WriteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(LogTriggerActivity.this, "Trigger logged", Toast.LENGTH_SHORT).show();
                    newTriggerInput.setText("");
                    loadTodayTriggers();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(LogTriggerActivity.this, "Failed to log", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadTodayTriggers() {
        dataWriter.queryTodayTriggers(new ChildrenTriggerDataWriting.OnTriggersLoadedListener() {
            @Override
            public void onTriggersLoaded(Map<String, Object> triggersData) {
                runOnUiThread(() -> updateTriggersList(triggersData));
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(LogTriggerActivity.this, "Failed to load", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateTriggersList(Map<String, Object> triggersData) {
        currentTriggers.clear();
        triggersContainer.removeAllViews();

        if (triggersData == null || triggersData.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Map.Entry<String, Object> entry : triggersData.entrySet()) {
            String triggerName = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                List<?> timestamps = (List<?>) value;
                for (Object timestampObj : timestamps) {
                    if (timestampObj instanceof Timestamp) {
                        currentTriggers.add(new TriggerOccurrence(triggerName, (Timestamp) timestampObj));
                    }
                }
            }
        }

        currentTriggers.sort((o1, o2) -> o2.timestamp.compareTo(o1.timestamp));

        if (currentTriggers.isEmpty()) {
            showEmptyState();
        } else {
            showTriggersList();
            for (TriggerOccurrence occurrence : currentTriggers) {
                triggersContainer.addView(createTriggerItem(occurrence));
            }
        }
    }

    private View createTriggerItem(TriggerOccurrence occurrence) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        item.setPadding(16, 16, 16, 16);
        item.setBackgroundResource(R.drawable.edittext_background);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) item.getLayoutParams();
        params.setMargins(0, 0, 0, 8);
        item.setLayoutParams(params);

        TextView nameView = new TextView(this);
        nameView.setText(occurrence.triggerName);
        nameView.setTextSize(16);
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView timeView = new TextView(this);
        timeView.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(occurrence.timestamp.toDate()));
        timeView.setTextSize(14);
        timeView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        Button deleteBtn = new Button(this);
        deleteBtn.setText("Delete");
        deleteBtn.setBackgroundColor(getColor(android.R.color.holo_red_dark));
        deleteBtn.setTextColor(getColor(android.R.color.white));


        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        deleteParams.setMargins(48, 0, 0, 0);
        deleteBtn.setLayoutParams(deleteParams);

        deleteBtn.setOnClickListener(v -> removeTrigger(occurrence));

        item.addView(nameView);
        item.addView(timeView);
        item.addView(deleteBtn);

        return item;
    }

    private void removeTrigger(TriggerOccurrence occurrence) {
        dataWriter.deleteDataFields(occurrence.triggerName, occurrence.timestamp, new ChildrenTriggerDataWriting.WriteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(LogTriggerActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                    loadTodayTriggers();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> Toast.makeText(LogTriggerActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showEmptyState() {
        emptyStateView.setVisibility(View.VISIBLE);
        triggersScrollView.setVisibility(View.GONE);
    }

    private void showTriggersList() {
        emptyStateView.setVisibility(View.GONE);
        triggersScrollView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayTriggers();
    }

    private static class TriggerOccurrence {
        String triggerName;
        Timestamp timestamp;

        TriggerOccurrence(String triggerName, Timestamp timestamp) {
            this.triggerName = triggerName;
            this.timestamp = timestamp;
        }
    }
}