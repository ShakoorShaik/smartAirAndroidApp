package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import utils.PEFManager;
import utils.ZoneManager;
import com.example.smartair.child.ChildDashboardHome;
import com.example.smartair.child.ChildHistoryActivity;

import utils.ParentEmergency;

public class ParentHomeFragment extends Fragment {

    private static final String TAG = "ParentHomeFragment";

    private TextView zonePercentage;
    private CardView zoneCard;

    public ParentHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ParentEmergency.listenEmergency(this);
        View view = inflater.inflate(R.layout.fragment_parent_home, container, false);

        Button historyButton = view.findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ParentHistoryActivity.class));
                getActivity().finish();
            }
        });

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        zonePercentage = view.findViewById(R.id.zone_percentage);
        zoneCard = view.findViewById(R.id.zone_card);
        fetchChildDataAndZone();
    }

    private void fetchChildDataAndZone() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d(TAG, "Current user is null.");
            displayDefaultZone();
            return;
        }

        String parentUid = currentUser.getUid();
        Log.d(TAG, "Fetching data for parent: " + parentUid);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(parentUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Get PB from parent's document
                    Long pbLong = document.getLong("personalBestPEF");
                    Integer parentPB = (pbLong != null) ? pbLong.intValue() : null;

                    Log.d(TAG, "Parent PB: " + parentPB);

                    // Get linked children
                    List<Map<String, Object>> linkedChildren =
                        (List<Map<String, Object>>) document.get("linkedChildren");

                    if (linkedChildren != null && !linkedChildren.isEmpty()) {
                        // Get first child's UID
                        Map<String, Object> child = linkedChildren.get(0);
                        String childUid = (String) child.get("uid");

                        Log.d(TAG, "Found child with UID: " + childUid);

                        if (childUid != null && parentPB != null && parentPB > 0) {
                            updateZoneInfoWithChildPEF(childUid, parentPB);
                        } else {
                            Log.w(TAG, "Child UID or parent PB is invalid");
                            displayDefaultZone();
                        }
                    } else {
                        Log.d(TAG, "No linked children found");
                        displayDefaultZone();
                    }
                } else {
                    Log.d(TAG, "Parent document does not exist");
                    displayDefaultZone();
                }
            } else {
                Log.e(TAG, "Firestore query failed", task.getException());
                displayDefaultZone();
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

        if (getActivity() == null) {
            Log.w(TAG, "Fragment not attached to activity, cannot update UI.");
            return;
        }

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
        zoneCard.setCardBackgroundColor(cardColor);

        Log.d(TAG, "Zone tile updated: " + zoneText);
    }

    private void displayDefaultZone() {
        if (getActivity() == null) {
            return;
        }

        zonePercentage.setText("--\nNo Data");
        zoneCard.setCardBackgroundColor(0xFFBDBDBD); // Gray
        Log.d(TAG, "Displaying default zone (no data)");
    }
}
