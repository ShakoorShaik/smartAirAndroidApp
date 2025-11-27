package com.example.smartair.child;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;
import static utils.ZoneManager.getTodayZone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.smartair.Login;
import com.example.smartair.R;
import com.example.smartair.child.emergency.Emergency;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import utils.ChildEmergency;
import utils.ChildIdManager;
import utils.PEFManager;
import utils.ZoneManager;

public class ChildDashboardHome extends AppCompatActivity {
    CardView todayZone;
    TextView zonePercentage;
    Button buttonEmergency;
    Button logInhalerButton;

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

        buttonEmergency = findViewById(R.id.emergencyButton);
        logInhalerButton = findViewById(R.id.button5);
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

        todayZone = findViewById(R.id.zone_card);
        zonePercentage = findViewById(R.id.zone_percentage);

        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();
        if (curr_user == null) {
            displayDefaultZone();
            return;
        } else if (curr_child_id.equals("NA")) {
            curr_child_id = curr_user.getUid();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String finalCurr_child_id = curr_child_id;
        db.collection("users").document(curr_child_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();

                if (doc.exists()) {
                    Long pb = doc.getLong("personalBestPEF");
                    int myPB = (pb != null) ? pb.intValue() : 0;

                    if (myPB > 0) {
                        updateZoneInfoWithChildPEF(finalCurr_child_id, myPB);
                    } else {
                        Toast.makeText(this, "PB not set for this Child.", Toast.LENGTH_LONG);
                        displayDefaultZone();
                    }

                } else {
                    displayDefaultZone();
                }
            } else {
                Log.e(TAG, "Firestore query failed", task.getException());
                displayDefaultZone();
            }
        });

        Button historyButton = findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChildDashboardHome.this, ChildHistoryActivity.class));
                finish();
            }
        });

        todayZone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

        private void updateZoneInfoWithChildPEF(String childUid, int parentPB) {
            Log.d(TAG, "Getting most recent PEF for child: " + childUid);

            PEFManager.getMostRecentPEF(childUid, new PEFManager.PEFCallback() {
                @Override
                public void onSuccess(Integer pefValue) {
                    if (pefValue == null || pefValue <= 0) {
                        Log.d(TAG, "No valid PEF reading found for child");
                        displayDefaultZone();
                    } else {
                        Log.d(TAG, "PEF value: " + pefValue + ", Parent PB: " + parentPB);
                        ZoneManager.Zone zone = ZoneManager.calculateZone(pefValue, parentPB);
                        displayZoneInfo(zone, pefValue, parentPB);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Error getting PEF", e);
                    displayDefaultZone();
                }
            });
        }

        private void displayZoneInfo(ZoneManager.Zone zone, int pefValue, int pbValue) {
            Log.d(TAG, "displayZoneInfo: zone=" + zone + ", pefValue=" + pefValue + ", pbValue=" + pbValue);

            // Calculate percentage
            int percentage = (int) (((double) pefValue / pbValue) * 100);

            // Format zone name
            String zoneName = zone.toString().substring(0, 1).toUpperCase()
                    + zone.toString().substring(1).toLowerCase();

            // Set zone text
            String zoneText = String.format("%s %d%%", zoneName, percentage);
            zonePercentage.setText(zoneText);

            // Set card background color based on zone
            int cardColor;
            switch (zone) {
                case GREEN:
                    cardColor = 0xFF4CAF50; // Green
                    break;
                case YELLOW:
                    cardColor = 0xFFFFC107; // Yellow/Amber
                    break;
                case RED:
                    cardColor = 0xFFF44336; // Red
                    break;
                default:
                    cardColor = 0xFFBDBDBD; // Gray for unknown
                    break;
            }
            todayZone.setCardBackgroundColor(cardColor);

            Log.d(TAG, "Zone tile updated: " + zoneText);
        }

        private void displayDefaultZone() {

            zonePercentage.setText("--\nNo Data");
            todayZone.setCardBackgroundColor(0xFFBDBDBD); // Gray
            Log.d(TAG, "Displaying default zone (no data)");
        }







}
