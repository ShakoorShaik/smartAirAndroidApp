package com.example.smartair.provider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;

public class ProviderAdherencePage extends AppCompatActivity {

    private Button returnToHome;
    private Button logOut;
    private Button left;
    private Button right;

    private TextView tvFrequency;
    private TextView tvDoseTimes;
    private TextView tvLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_adherence);

        returnToHome = findViewById(R.id.TopLeftButton);
        logOut = findViewById(R.id.TopRightButton);
        left = findViewById(R.id.BottomLeftButton);
        right = findViewById(R.id.BottomRightButton);

        tvFrequency = findViewById(R.id.tvFrequency);
        tvDoseTimes = findViewById(R.id.tvDoseTimes);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);

        setupButtonListeners();
        
        ProviderDataReading providerData = new ProviderDataReading(this);

        TextView linkedText = findViewById(R.id.linkedText);
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

        TextView linkedText1 = findViewById(R.id.linkedText1);
        String currentChildName = providerData.getCurrentChildName();
        linkedText1.setText("Viewing: " + currentChildName);

        loadAdherenceSchedule(providerData);
    }

    private void setupButtonListeners() {
        returnToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderAdherencePage.this, ProviderHomePage.class);
            startActivity(intent);
            finish();
        });

        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProviderAdherencePage.this, LoginActivityView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        right.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderAdherencePage.this, ProviderHomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        left.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderAdherencePage.this, ProviderPEFPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void loadAdherenceSchedule(ProviderDataReading providerData) {
        providerData.getAdherenceSchedule(new ProviderDataReading.AdherenceScheduleCallback() {
            @Override
            public void onSuccess(AdherenceSummary schedule) {
                if (schedule.getFrequency() != null && !schedule.getFrequency().isEmpty()) {
                    tvFrequency.setText(schedule.getFrequency());
                } else {
                    tvFrequency.setText("Not specified");
                }

                tvDoseTimes.setText(schedule.formatDoseTimes());

                tvLastUpdated.setText(schedule.getFormattedUpdatedTime());
            }

            @Override
            public void onFailure(String message) {
                tvFrequency.setText("Error loading data");
                tvDoseTimes.setText("Could not load dose times: " + message);
                tvLastUpdated.setText("Error");
            }
        });
    }
}