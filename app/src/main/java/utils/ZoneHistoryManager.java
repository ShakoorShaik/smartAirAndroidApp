package utils;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ZoneHistoryManager {

    public interface RedZoneDatesCallback {
        void onSuccess(Map<LocalDate, Boolean> redZoneDates);
        void onFailure(Exception e);
    }

    public static void loadRedZoneDates(Context context, RedZoneDatesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String targetUid;
        if (context != null) {
            ChildIdManager manager = new ChildIdManager(context);
            String curr_child_id = manager.getChildId();
            if (!curr_child_id.equals("NA")) {
                targetUid = curr_child_id;
            } else {
                targetUid = user.getUid();
            }
        } else {
            targetUid = user.getUid();
        }

        db.collection("users").document(targetUid)
                .collection("zoneHistory")
                .whereEqualTo("zone", "RED")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<LocalDate, Boolean> redZoneDates = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String dateStr = document.getString("date");

                                if (dateStr != null) {
                                    LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                                    redZoneDates.put(date, true);
                                }
                            } catch (Exception e) {
                                // Skip invalid entries
                            }
                        }
                        callback.onSuccess(redZoneDates);
                    } else {
                        callback.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface ZoneHistoryDataCallback {
        void onSuccess(java.util.List<java.util.Map<String, Object>> zoneHistoryData);
        void onFailure(Exception e);
    }

    /**
     * Retrieves all zone history data for a specific child for export purposes
     * @param childUid The UID of the child
     * @param callback Callback with list of zone history entries
     */
    public static void getAllZoneHistoryForChild(String childUid, ZoneHistoryDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (childUid == null || childUid.isEmpty()) {
            callback.onFailure(new Exception("Invalid child UID"));
            return;
        }

        db.collection("users").document(childUid)
                .collection("zoneHistory")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<java.util.Map<String, Object>> zoneData = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("date", document.getString("date"));
                        data.put("zone", document.getString("zone"));
                        data.put("pefValue", document.get("pefValue"));
                        zoneData.add(data);
                    }
                    callback.onSuccess(zoneData);
                })
                .addOnFailureListener(callback::onFailure);
    }
}

