package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class ParentDashboardWithChildrenActivity extends AppCompatActivity {

    private ListenerRegistration alertListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard_with_children);
        listenForAlerts();


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

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentHomeFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.bottom_home);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment instanceof ParentChildrenFragment) {
                    ((ParentChildrenFragment) fragment).loadChildren();
                }
            }
        }
    }

    private void listenForAlerts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        alertListener = db.collection("users").document(user.getUid())
                .collection("low_canister_alerts")
                .whereEqualTo("status", "unread").addSnapshotListener((snaps, e) -> {
                    if (e != null) {
                        Log.e("ALERT", "Listen failed.", e);
                        return;
                    }

                    if (snaps != null && !snaps.isEmpty()) {
                        for (DocumentChange documentChange: snaps.getDocumentChanges()) {
                            if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                DocumentSnapshot doc = documentChange.getDocument();
                                showLowCanAlert(doc.getId(), doc.getString("childName"),
                                        doc.getString("canType"));
                            }
                        }
                    }
                });
    }

    private void showLowCanAlert(String alertId, String childName, String canType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("⚠ LOW CANISTER ALERT!");
        builder.setMessage("Your child " + childName + " has alerted you that their " + canType
        + " canister is running low. Consider replacing it.");
        builder.setPositiveButton("Got it", (dialog, which) -> {
            markAlertAsRead(alertId);
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void markAlertAsRead(String alertId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) { return; }
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid()).collection("low_canister_alerts")
                .document(alertId).update("status", "read");
    }
}






