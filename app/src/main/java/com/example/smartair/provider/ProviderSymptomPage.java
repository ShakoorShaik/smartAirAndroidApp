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

public class ProviderSymptomPage extends AppCompatActivity {

    private ProviderDataReading providerData;
    private ProviderWidgetFactory widgetFactory;
    private LinearLayout scrollContent;
    private EditText editTextDate;
    private EditText editTextSymptom;
    private Button searchButton;
    private TextView textViewDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_symptom);

        try {
            providerData = new ProviderDataReading(this);
            widgetFactory = new ProviderWidgetFactory(this);

            scrollContent = findViewById(R.id.scrollContent);
            editTextDate = findViewById(R.id.editTextDate);
            editTextSymptom = findViewById(R.id.editTextTrigger);
            searchButton = findViewById(R.id.button13);
            textViewDate = findViewById(R.id.textViewDate);

            textViewDate.setText("Date: " + DateHelper.getTodayDate());

            editTextDate.setHint("Enter date (YYYY-MM-DD)");

            setupNavigation();
            loadLinkedInfo();

            setupSearchButton();

            loadSymptomData();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupSearchButton() {
        searchButton.setOnClickListener(v -> {
            String date = editTextDate.getText().toString().trim();
            String symptomType = editTextSymptom.getText().toString().trim();

            if (!date.isEmpty()) {
                searchByDate(date);
            } else if (!symptomType.isEmpty()) {
                searchBySymptomType(symptomType);
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

        providerData.searchDateBasedByDate("symptomLogs", date, new ProviderDataReading.DateSearchCallback() {
            @Override
            public void onSuccess(List<ProviderDataReading.DateBasedDocument> documents) {
                updateDisplayWithDocuments(documents, "No symptom logs found for date: " + date);
            }

            @Override
            public void onFailure(String message) {
                showError(message);
            }
        });
    }

    private void searchBySymptomType(String symptomType) {
        if (scrollContent != null) {
            scrollContent.removeAllViews();

            TextView loading = new TextView(this);
            loading.setText("Searching for symptom: " + symptomType + "...");
            scrollContent.addView(loading);
        } else {
            TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
            textInfoDisplay.setText("Searching for symptom: " + symptomType + "...");
        }

        providerData.searchSymptomsByType(symptomType, new ProviderDataReading.DateSearchCallback() {
            @Override
            public void onSuccess(List<ProviderDataReading.DateBasedDocument> documents) {
                if (symptomType.isEmpty()) {
                    updateDisplayWithDocuments(documents, "No symptom logs available");
                } else {
                    updateDisplayWithDocuments(documents, "No symptom logs found for symptom type: " + symptomType);
                }
            }

            @Override
            public void onFailure(String message) {
                showError(message);
            }
        });
    }

    private void resetToAllData() {
        editTextDate.setText("");
        editTextSymptom.setText("");

        loadSymptomData();
    }

    private void loadSymptomData() {
        if (scrollContent != null) {
            scrollContent.removeAllViews();

            TextView loading = new TextView(this);
            loading.setText("Loading symptom logs...");
            scrollContent.addView(loading);
        } else {
            TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
            textInfoDisplay.setText("Loading symptom logs...");
        }

        providerData.getDateBasedSubcollection("symptomLogs", new ProviderDataReading.DateBasedSubcollectionCallback() {
            @Override
            public void onSuccess(List<ProviderDataReading.DateBasedDocument> documents) {
                updateDisplayWithDocuments(documents, "No symptom logs available");
            }

            @Override
            public void onFailure(String message) {
                showError(message);
            }
        });
    }

    private void updateDisplayWithDocuments(List<ProviderDataReading.DateBasedDocument> documents, String emptyMessage) {
        if (scrollContent != null) {
            scrollContent.removeAllViews();

            if (documents.isEmpty()) {
                TextView empty = new TextView(ProviderSymptomPage.this);
                empty.setText(emptyMessage);
                empty.setTextSize(16);
                scrollContent.addView(empty);
                return;
            }

            for (ProviderDataReading.DateBasedDocument doc : documents) {
                View widget = widgetFactory.createDateWidget(doc.date, doc.data, "symptomLogs");
                scrollContent.addView(widget);
            }
        } else {
            TextView textInfoDisplay = findViewById(R.id.textInfoDisplay);
            if (documents.isEmpty()) {
                textInfoDisplay.setText(emptyMessage);
                return;
            }

            StringBuilder dataText = new StringBuilder();
            dataText.append("Symptom Logs:\n\n");

            for (ProviderDataReading.DateBasedDocument doc : documents) {
                dataText.append("Date: ").append(doc.date).append("\n");
                if (doc.data != null) {
                    for (String key : doc.data.keySet()) {
                        Object value = doc.data.get(key);
                        if (value instanceof List) {
                            List<?> list = (List<?>) value;
                            dataText.append("  ").append(key).append(": ").append(list.size()).append(" entries\n");
                        } else if (value instanceof Boolean) {
                            dataText.append("  ").append(key).append(": ").append((Boolean)value ? "Yes" : "No").append("\n");
                        } else {
                            dataText.append("  ").append(key).append(": ").append(value).append("\n");
                        }
                    }
                }
                dataText.append("\n");
            }

            textInfoDisplay.setText(dataText.toString());
        }
    }

    private void showError(String message) {
        if (scrollContent != null) {
            scrollContent.removeAllViews();
            TextView error = new TextView(ProviderSymptomPage.this);
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
            Intent intent = new Intent(this, ProviderTriggerPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        right.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProviderTriagePage.class);
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