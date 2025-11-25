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

import utils.ZoneManager;

public class ParentHomeFragment extends Fragment {

    private static final String TAG = "ParentHomeFragment";

    private TextView zonePercentage;

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
        ZoneManager.getTodayZone(childUid, new ZoneManager.ZoneCallback() {
            @Override
            public void onSuccess(ZoneManager.Zone zone, Integer pefValue, Integer pbValue) {
                displayZoneInfo(zone, pefValue, pbValue);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error getting zone info", e);
                displayZoneInfo(ZoneManager.Zone.UNKNOWN, null, null);
            }
        });
    }

    private void displayZoneInfo(ZoneManager.Zone zone, Integer pefValue, Integer pbValue) {
        if (getActivity() == null) return; // Fragment not attached

        String zoneText;
        if (zone == ZoneManager.Zone.UNKNOWN) {
            zoneText = "Zone: --";
        } else {
            int percentage = (int) (((double) pefValue / pbValue) * 100);
            String zoneName = zone.toString().substring(0, 1).toUpperCase() + zone.toString().substring(1).toLowerCase();
            zoneText = String.format("%s Zone (%d%%)", zoneName, percentage);
        }
        zonePercentage.setText(zoneText);
    }
}
