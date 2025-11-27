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
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

        loadZoneInfo();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (todayZone == null) {
            todayZone = findViewById(R.id.zone_card);
        }
        if (zonePercentage == null) {
            zonePercentage = findViewById(R.id.zone_percentage);
        }
        if (todayZone != null && zonePercentage != null) {
            loadZoneInfo();
        }
    }

    private void loadZoneInfo() {
        if (todayZone == null) {
            todayZone = findViewById(R.id.zone_card);
        }
        if (zonePercentage == null) {
            zonePercentage = findViewById(R.id.zone_percentage);
        }

        if (todayZone == null || zonePercentage == null) {
            Log.e(TAG, "Zone views not found in layout");
            return;
        }

        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();
        if (curr_user == null) {
            Log.d(TAG, "No current user, displaying default zone");
            displayDefaultZone();
            return;
        } else if (curr_child_id.equals("NA")) {
            curr_child_id = curr_user.getUid();
        }

        Log.d(TAG, "Loading zone info for child: " + curr_child_id);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String finalCurr_child_id = curr_child_id;
        db.collection("users").document(curr_child_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot doc = task.getResult();

                if (doc.exists()) {
                    Long pb = doc.getLong("personalBestPEF");
                    int myPB = (pb != null) ? pb.intValue() : 0;
                    Log.d(TAG, "Personal Best PEF: " + myPB);

                    if (myPB > 0) {
                        updateZoneInfoWithChildPEF(finalCurr_child_id, myPB);
                    } else {
                        Log.d(TAG, "PB not set, displaying default zone");
                        displayDefaultZone();
                    }

                } else {
                    Log.d(TAG, "Document does not exist, displaying default zone");
                    displayDefaultZone();
                }
            } else {
                Log.e(TAG, "Firestore query failed", task.getException());
                displayDefaultZone();
            }
        });
    }

    private void updateZoneInfoWithChildPEF(String childUid, int parentPB) {
        Log.d(TAG, "Getting today's PEF for child: " + childUid + ", PB: " + parentPB);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        Log.d(TAG, "Looking for PEF reading for date: " + today);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("pefReadings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String dateStr = doc.getString("date");
                        Long pefValueLong = doc.getLong("value");

                        Log.d(TAG, "Most recent PEF - Date: " + dateStr + ", Today: " + today + ", Value: " + pefValueLong);

                        if (dateStr != null && today.equals(dateStr) && pefValueLong != null) {
                            int pefValue = pefValueLong.intValue();
                            if (pefValue > 0) {
                                Log.d(TAG, "Found today's PEF: " + pefValue + ", PB: " + parentPB);
                                ZoneManager.Zone zone = ZoneManager.calculateZone(pefValue, parentPB);
                                displayZoneInfo(zone, pefValue, parentPB);
                            } else {
                                Log.d(TAG, "PEF value is 0 or negative");
                                displayDefaultZone();
                            }
                        } else {
                            Log.d(TAG, "Most recent PEF is not from today (date: " + dateStr + ", today: " + today + ")");
                            displayDefaultZone();
                        }
                    } else {
                        Log.d(TAG, "No PEF readings found for child");
                        displayDefaultZone();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying PEF readings", e);
                    displayDefaultZone();
                });
    }

    private void displayZoneInfo(ZoneManager.Zone zone, int pefValue, int pbValue) {
        Log.d(TAG, "displayZoneInfo: zone=" + zone + ", pefValue=" + pefValue + ", pbValue=" + pbValue);

        if (zonePercentage == null || todayZone == null) {
            Log.e(TAG, "Zone views not initialized");
            return;
        }

        runOnUiThread(() -> {
            int percentage = (int) (((double) pefValue / pbValue) * 100);

            String zoneName = zone.toString().substring(0, 1).toUpperCase()
                    + zone.toString().substring(1).toLowerCase();

            String zoneText = String.format("%s %d%%", zoneName, percentage);
            zonePercentage.setText(zoneText);

            int cardColor;
            switch (zone) {
                case GREEN:
                    cardColor = 0xFF4CAF50;
                    break;
                case YELLOW:
                    cardColor = 0xFFFFC107;
                    break;
                case RED:
                    cardColor = 0xFFF44336;
                    break;
                default:
                    cardColor = 0xFFBDBDBD;
                    break;
            }
            todayZone.setCardBackgroundColor(cardColor);

            Log.d(TAG, "Zone tile updated: " + zoneText);
        });
    }

    private void displayDefaultZone() {
        if (zonePercentage == null || todayZone == null) {
            Log.e(TAG, "Zone views not initialized for default display");
            return;
        }

        runOnUiThread(() -> {
            zonePercentage.setText("--");
            todayZone.setCardBackgroundColor(0xFFBDBDBD);
            Log.d(TAG, "Displaying default zone (no data)");
        });
    }







}
