package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChildDashboardMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(ChildDashboardMainActivity.this, LoginActivityView.class));
            finish();
            return;
        }

        utils.ChildIdManager manager = new utils.ChildIdManager(this);
        String storedChildId = manager.getChildId();
        if (!storedChildId.equals("NA") && !storedChildId.equals(user.getUid())) {
            manager.clearChildId();
        }

        utils.ChildOnboardingManager.checkAndSetOnboardingStatus(this, new utils.ChildOnboardingManager.ChildOnboardingCheckCallback() {
            @Override
            public void onResult(boolean shouldSkipOnboarding) {
                if (shouldSkipOnboarding) {
                    initializeDashboard();
                } else {
                    Intent intent = new Intent(ChildDashboardMainActivity.this, ChildOnboardingActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void initializeDashboard() {
        setContentView(R.layout.activity_child_dashboard_main);

        BottomNavigationView bottomNav = findViewById(R.id.navBar);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                selectedFragment = new ChildHomeFragment();
            } else if (itemId == R.id.bottom_tasks) {
                selectedFragment = new ChildTasksFragment();
            } else if (itemId == R.id.bottom_settings) {
                selectedFragment = new ChildSettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ChildHomeFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.bottom_home);
        }
    }
}

