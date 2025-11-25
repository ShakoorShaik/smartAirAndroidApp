package com.example.smartair.parent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import utils.PBManager;
import utils.ZoneManager;

public class ParentHomeFragment extends Fragment {

    private static final String TAG = "ParentHomeFragment";

    private TextView zonePercentage;

    // Defines the zones based on Personal Best PEF percentage
    public enum Zone {
        GREEN, // >= 80% of PB
        YELLOW, // 50-79% of PB
        RED,   // < 50% of PB
        UNKNOWN // PEF or PB not available
    }

    public ParentHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_parent_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        zonePercentage = view.findViewById(R.id.zone_percentage);
        fetchChildData();
    }

    private void fetchChildData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String parentUid = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(parentUid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<Map<String, Object>> linkedChildren = (List<Map<String, Object>>) document.get("linkedChildren");
                        if (linkedChildren != null && !linkedChildren.isEmpty()) {
                            // For now, let's take the first child
                            Map<String, Object> child = linkedChildren.get(0);
                            String childUid = (String) child.get("uid");
                            if (childUid != null) {
                                updateZoneInfo(childUid);
                            }
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
    }

    private void updateZoneInfo(String childUid) {
        // We get the PEF from a source that provides today's value, here represented by ZoneManager
        ZoneManager.getTodayZone(childUid, new ZoneManager.ZoneCallback() {
            @Override
            public void onSuccess(ZoneManager.Zone ignoredZone, Integer pefValue) {
                // With PEF, we can now get the personal best to determine the zone
                getPBAndDetermineZone(childUid, pefValue);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error getting today's PEF value", e);
                displayZoneInfo(Zone.UNKNOWN, null, null);
            }
        });
    }

    private void getPBAndDetermineZone(String childUid, Integer pefValue) {
        if (pefValue == null) {
            displayZoneInfo(Zone.UNKNOWN, null, null);
            return;
        }

        PBManager.getPB(childUid, new PBManager.PBCallback() {
            @Override
            public void onSuccess(Integer pbValue) {
                Zone zone = determineZone(pefValue, pbValue);
                displayZoneInfo(zone, pefValue, pbValue);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error getting Personal Best", e);
                // We have a PEF but no PB, so the zone is UNKNOWN
                displayZoneInfo(Zone.UNKNOWN, pefValue, null);
            }
        });
    }

    private Zone determineZone(Integer pefValue, Integer pbValue) {
        if (pefValue == null || pbValue == null || pbValue == 0) {
            return Zone.UNKNOWN;
        }
        double percentage = ((double) pefValue / pbValue) * 100;
        if (percentage >= 80) {
            return Zone.GREEN;
        } else if (percentage >= 50) {
            return Zone.YELLOW;
        } else {
            return Zone.RED;
        }
    }

    private void displayZoneInfo(Zone zone, Integer pefValue, Integer pbValue) {
        if(getActivity() == null) return; // Fragment not attached

        String zoneText;
        if (zone == Zone.UNKNOWN) {
            zoneText = "Zone: --";
        } else {
            // Because of the check in determineZone, pefValue and pbValue are non-null and pbValue > 0.
            int percentage = (int) (((double) pefValue / pbValue) * 100);
            String zoneName = zone.toString().substring(0, 1).toUpperCase() + zone.toString().substring(1).toLowerCase();
            zoneText = String.format("%s Zone (%d%%)", zoneName, percentage);
        }
        zonePercentage.setText(zoneText);

        // Example of how you might change colors based on zone
        // CardView zoneCard = getView().findViewById(R.id.zone_card);
        // int color = getResources().getColor(R.color.grey); // Default color
        // if (zone != null) {
        //     switch (zone) {
        //         case GREEN:
        //             color = getResources().getColor(R.color.green);
        //             break;
        //         case YELLOW:
        //             color = getResources().getColor(R.color.yellow);
        //             break;
        //         case RED:
        //             color = getResources().getColor(R.color.red);
        //             break;
        //     }
        // }
        // zoneCard.setCardBackgroundColor(color);
    }
}
