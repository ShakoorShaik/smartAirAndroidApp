package com.example.smartair.provider;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class ProviderRescuesPage extends AppCompatActivity {

    private ProviderDataReading providerData;
    private ProviderWidgetFactory widgetFactory;
    private LinearLayout scrollContent;
    private EditText editTextDate;
    private Button searchButton;
    private TextView textViewDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_rescues);

        try {
            providerData = new ProviderDataReading(this);
            widgetFactory = new ProviderWidgetFactory(this);

            editTextDate = findViewById(R.id.editTextDate);
            searchButton = findViewById(R.id.button13);
            textViewDate = findViewById(R.id.textViewDate);

            textViewDate.setText("Date: " + DateHelper.getTodayDate());

            scrollContent = findViewById(R.id.scrollContent);

            if (scrollContent == null) {
                TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
                textInfoDisplay.setText("Checking permission...");
                testPermissionSimple();
            } else {
                testPermission();
            }

            setupNavigation();
            loadLinkedInfo();

            setupSearchButton();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupSearchButton() {
        searchButton.setOnClickListener(v -> {
            String date = editTextDate.getText().toString().trim();

            if (!date.isEmpty()) {
                searchByDate(date);
            } else {
                resetToAllData();
            }
        });
    }

    private void searchByDate(String date) {
        if (scrollContent != null) {
            scrollContent.removeAllViews();

            TextView loading = new TextView(this);
            loading.setText("Searching for date: " + date + "...");
            scrollContent.addView(loading);
        } else {
            TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
            textInfoDisplay.setText("Searching for date: " + date + "...");
        }

        providerData.searchTimestampByDate("inhaler_log", date, new ProviderDataReading.TimestampSearchCallback() {
            @Override
            public void onSuccess(List<ProviderDataReading.TimestampDocument> documents) {
                updateDisplayWithTimestampDocuments(documents, "No rescue logs found for date: " + date);
            }

            @Override
            public void onFailure(String message) {
                showError(message);
            }
        });
    }

    private void resetToAllData() {
        editTextDate.setText("");

        if (scrollContent != null) {
            loadRescueData();
        } else {
            loadRescueDataSimple();
        }
    }

    private void testPermission() {
        providerData.checkChildPermission("rescueLogs", new ProviderDataReading.PermissionCallback() {
            @Override
            public void onPermissionResult(boolean hasPermission) {
                if (hasPermission) {
                    loadRescueData();
                } else {
                    showPermissionText("Permission DENIED\nYou cannot view rescue logs.");
                }
            }

            @Override
            public void onError(String message) {
                showPermissionText("Error: " + message);
            }
        });
    }

    private void testPermissionSimple() {
        TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
        providerData.checkChildPermission("rescueLogs", new ProviderDataReading.PermissionCallback() {
            @Override
            public void onPermissionResult(boolean hasPermission) {
                if (hasPermission) {
                    textInfoDisplay.setText("Permission GRANTED\nLoading rescue logs...");
                    loadRescueDataSimple();
                } else {
                    textInfoDisplay.setText("Permission DENIED\nYou cannot view rescue logs.");
                }
            }

            @Override
            public void onError(String message) {
                textInfoDisplay.setText("Error: " + message);
            }
        });
    }

    private void loadRescueData() {
        scrollContent.removeAllViews();

        TextView loading = new TextView(this);
        loading.setText("Loading rescue logs...");
        scrollContent.addView(loading);

        providerData.getTimestampBasedSubcollection("inhaler_log", new ProviderDataReading.TimestampDocumentCallback() {
            @Override
            public void onSuccess(List<ProviderDataReading.TimestampDocument> documents) {
                updateDisplayWithTimestampDocuments(documents, "No rescue logs available");
            }

            @Override
            public void onFailure(String message) {
                showError(message);
            }
        });
    }

    private void loadRescueDataSimple() {
        TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);

        providerData.getTimestampBasedSubcollection("inhaler_log", new ProviderDataReading.TimestampDocumentCallback() {
            @Override
            public void onSuccess(List<ProviderDataReading.TimestampDocument> documents) {
                updateDisplayWithTimestampDocumentsSimple(documents, "No rescue logs available");
            }

            @Override
            public void onFailure(String message) {
                textInfoDisplay.setText("Error loading rescue logs: " + message);
            }
        });
    }

    private void updateDisplayWithTimestampDocuments(List<ProviderDataReading.TimestampDocument> documents, String emptyMessage) {
        scrollContent.removeAllViews();

        if (documents.isEmpty()) {
            TextView empty = new TextView(ProviderRescuesPage.this);
            empty.setText(emptyMessage);
            empty.setTextSize(16);
            scrollContent.addView(empty);
            return;
        }

        for (ProviderDataReading.TimestampDocument doc : documents) {
            View widget = widgetFactory.createTimestampWidget(doc.timestamp, doc.data, "inhaler_log", doc.documentId);
            scrollContent.addView(widget);
        }
    }

    private void updateDisplayWithTimestampDocumentsSimple(List<ProviderDataReading.TimestampDocument> documents, String emptyMessage) {
        TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
        if (documents.isEmpty()) {
            textInfoDisplay.setText(emptyMessage);
            return;
        }

        StringBuilder dataText = new StringBuilder();
        dataText.append("Rescue Logs:\n\n");

        for (ProviderDataReading.TimestampDocument doc : documents) {
            String formattedTime = DateHelper.formatTimestampToDateTime(doc.timestamp);
            dataText.append("Time: ").append(formattedTime).append("\n");

            if (doc.data != null) {
                if (doc.data.containsKey("medicationType")) {
                    dataText.append("Medication: ").append(doc.data.get("medicationType")).append("\n");
                }
                if (doc.data.containsKey("doseCount")) {
                    dataText.append("Dose Count: ").append(doc.data.get("doseCount")).append("\n");
                }
                if (doc.data.containsKey("enteredBy")) {
                    dataText.append("Entered By: ").append(doc.data.get("enteredBy")).append("\n");
                }
                if (doc.data.containsKey("preDoseStatus")) {
                    dataText.append("Before: ").append(doc.data.get("preDoseStatus")).append("\n");
                }
                if (doc.data.containsKey("postDoseStatus")) {
                    dataText.append("After: ").append(doc.data.get("postDoseStatus")).append("\n");
                }
            }
            dataText.append("\n");
        }

        textInfoDisplay.setText(dataText.toString());
    }

    private void showPermissionText(String message) {
        if (scrollContent != null) {
            scrollContent.removeAllViews();

            TextView text = new TextView(this);
            text.setText(message);
            text.setTextSize(16);
            text.setPadding(0, 50, 0, 0);
            text.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            scrollContent.addView(text);
        } else {
            TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
            textInfoDisplay.setText(message);
        }
    }

    private void showError(String message) {
        if (scrollContent != null) {
            scrollContent.removeAllViews();
            TextView error = new TextView(ProviderRescuesPage.this);
            error.setText("Error: " + message);
            error.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            scrollContent.addView(error);
        } else {
            TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
            textInfoDisplay.setText("Error: " + message);
        }
    }

    private void setupNavigation() {
        Button returnToHome = findViewById(R.id.TopLeftButton);
        Button logOut = findViewById(R.id.TopRightButton);
        Button left = findViewById(R.id.BottomLeftButton);
        Button right = findViewById(R.id.BottomRightButton);

        returnToHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderHomePage.class);
            startActivity(intent);
            finish();
        });

        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivityView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        left.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderTriagePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        right.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderPEFPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void loadLinkedInfo() {
        TextView linkedText = findViewById(R.id.linkedText);
        TextView currentChildText = findViewById(R.id.linkedText1);

        providerData.getParentUid(new ProviderDataReading.ParentUidCallback() {
            @Override
            public void onSuccess(String parentUid, String parentEmail) {
                linkedText.setText("Linked with: " + parentEmail);
            }

            @Override
            public void onFailure(String message) {
                linkedText.setText("Not linked to parent");
            }
        });

        String currentChildName = providerData.getCurrentChildName();
        currentChildText.setText("Viewing: " + currentChildName);
    }
}