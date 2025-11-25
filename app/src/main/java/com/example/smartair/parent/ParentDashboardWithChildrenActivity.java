package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartair.Login;
import com.example.smartair.R;
import com.example.smartair.child.ChildDashboardHome;
import com.example.smartair.parent.ChildrenAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.DatabaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentDashboardWithChildrenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard_with_children);


        BottomNavigationView bottomNav = findViewById(R.id.navBar);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                selectedFragment = new ParentHomeFragment();
            } else if (itemId == R.id.bottom_children) {
                selectedFragment = new ParentChildrenFragment();
            } else if (itemId == R.id.bottom_medicine) {
                selectedFragment = new ParentMedicineFragment();
            } else if (itemId == R.id.bottom_settings) {
                selectedFragment = new ParentSettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

                // Set default fragment to children (the original dashboard)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentChildrenFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.bottom_children);
        }
    }
}






