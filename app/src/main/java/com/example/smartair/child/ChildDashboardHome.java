package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartair.Login;
import com.example.smartair.R;
import com.example.smartair.child.emergency.Emergency;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utils.ChildEmergency;

public class ChildDashboardHome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(ChildDashboardHome.this, Login.class));
            finish();
            return;
        }

        BottomNavigationView bottomNav = findViewById(R.id.navBar);
        bottomNav.setSelectedItemId(R.id.bottom_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                return true;
            } else if (itemId == R.id.bottom_tasks) {
                startActivity(new Intent(ChildDashboardHome.this, ChildDashboardTasks.class));
                finish();
                return true;
            } else if (itemId == R.id.bottom_settings) {
                startActivity(new Intent(ChildDashboardHome.this, ChildDashboardSettings.class));
                finish();
                return true;
            }
            return false;
        });

        Button buttonEmergency = findViewById(R.id.emergencyButton);
        Button logInhalerButton = findViewById(R.id.button5);
        logInhalerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChildDashboardHome.this, LogUsageActivity.class));
                finish();
            }
        });
        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(this);
        });

        Button zoneButton = findViewById(R.id.zoneButton);
        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChildDashboardHome.this, ChildHistoryActivity.class));
                finish();
            }
        });
    }
}
