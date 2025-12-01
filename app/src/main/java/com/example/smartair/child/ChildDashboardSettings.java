package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.LoginActivityView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.smartair.R;

import utils.ChildEmergency;
import utils.ChildIdManager;
import utils.DatabaseManager;

public class ChildDashboardSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_settings);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView textView = (TextView) findViewById(R.id.WelcomeMsg);
            textView.setText(user.getEmail());
        }

        BottomNavigationView bottomNav = findViewById(R.id.navBar);
        bottomNav.setSelectedItemId(R.id.bottom_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                startActivity(new Intent(ChildDashboardSettings.this, ChildDashboardHome.class));
                finish();
                return true;
            } else if (itemId == R.id.bottom_tasks) {
                startActivity(new Intent(ChildDashboardSettings.this, ChildDashboardTasks.class));
                finish();
                return true;
            } else return itemId == R.id.bottom_settings;
        });

        Button button_logout = findViewById(R.id.button_logout_child);
        Button buttonEmergency = findViewById(R.id.emergencyButton);
        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager.accountLogout();
                ChildIdManager manager = new ChildIdManager(ChildDashboardSettings.this);
                manager.clearChildId();
                startActivity(new Intent(ChildDashboardSettings.this, LoginActivityView.class));
                finish();
            }
        });
        buttonEmergency.setOnClickListener(v -> {
            ChildEmergency.emergencyPrompt(this);
        });
    }
}
