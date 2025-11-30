package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.child.inhalertechnique.InhalerTechniqueFirst;
import com.example.smartair.child.logtriggerandsymptoms.LogTriggerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import utils.ChildEmergency;

public class ChildDashboardTasks extends AppCompatActivity {

    private ImageView Badge;
    private ImageView Streak;
    private Button buttonTechniqueHelper;
    private Button buttonRecordTrigger;
    private Button buttonTriggerHistory;
    private Button buttonRecordSymptom;

    private Button buttonEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_tasks);

        BottomNavigationView bottomNav = findViewById(R.id.navBar);
        bottomNav.setSelectedItemId(R.id.bottom_tasks);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(ChildDashboardTasks.this, ChildDashboardHome.class));
                finish();
                return true;
            } else if (itemId == R.id.bottom_tasks) {
                return true;
            } else if (itemId == R.id.bottom_settings) {
                startActivity(new Intent(ChildDashboardTasks.this, ChildDashboardSettings.class));
                finish();
                return true;
            }
            return false;
        });

        Badge = findViewById(R.id.badge);
        Badge.setImageResource(R.drawable.bronze_badge);      //todo there should be some logic to decide which badge
        Badge.setVisibility(View.VISIBLE);

        Streak = findViewById(R.id.streak);
        Streak.setImageResource(R.drawable.gold_badge);     //todo this is a placeholder for the streak, there should be a fire emoji with streak
        Streak.setVisibility(View.VISIBLE);

        buttonTechniqueHelper = findViewById(R.id.techniqueHelperButton);
        buttonRecordTrigger = findViewById(R.id.recordTriggerButton);
        buttonTriggerHistory = findViewById(R.id.historyTriggerButton);
        buttonRecordSymptom= findViewById(R.id.symptomRecordButton);
        buttonEmergency = findViewById(R.id.emergencyButton);

        buttonTechniqueHelper.setOnClickListener(v -> {
            startActivity(new Intent(this, InhalerTechniqueFirst.class));
        });

        buttonRecordTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChildDashboardTasks.this, LogTriggerActivity.class));
                finish();
            }
        });

        buttonTriggerHistory.setOnClickListener(v -> {  //todo
            Toast.makeText(this, "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonRecordSymptom.setOnClickListener(v -> {   //todo
            Toast.makeText(this, "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(this);
        });


    }

}