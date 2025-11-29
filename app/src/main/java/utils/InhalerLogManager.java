package utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InhalerLogManager {

    public interface InhalerLogDataCallback {
        void onSuccess(List<Map<String, Object>> inhalerLogData);
        void onFailure(Exception e);
    }

    /**
     * Retrieves all rescue inhaler usage data for a specific child for export purposes
     * @param childUid The UID of the child
     * @param callback Callback with list of rescue inhaler log entries
     */
    public static void getRescueInhalerLogsForChild(String childUid, InhalerLogDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (childUid == null || childUid.isEmpty()) {
            callback.onFailure(new Exception("Invalid child UID"));
            return;
        }

        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Rescue")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> rescueData = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("timestamp", document.get("timestamp"));
                        data.put("medicineName", document.getString("medicineName"));
                        data.put("dosage", document.get("dosage"));
                        data.put("medicationType", document.getString("medicationType"));
                        rescueData.add(data);
                    }
                    callback.onSuccess(rescueData);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves all control inhaler usage data for a specific child for export purposes
     * @param childUid The UID of the child
     * @param callback Callback with list of control inhaler log entries
     */
    public static void getControlInhalerLogsForChild(String childUid, InhalerLogDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (childUid == null || childUid.isEmpty()) {
            callback.onFailure(new Exception("Invalid child UID"));
            return;
        }

        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Control")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> controlData = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("timestamp", document.get("timestamp"));
                        data.put("medicineName", document.getString("medicineName"));
                        data.put("dosage", document.get("dosage"));
                        data.put("medicationType", document.getString("medicationType"));
                        controlData.add(data);
                    }
                    callback.onSuccess(controlData);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Retrieves all inhaler usage data (both rescue and control) for a specific child
     * @param childUid The UID of the child
     * @param callback Callback with list of all inhaler log entries
     */
    public static void getAllInhalerLogsForChild(String childUid, InhalerLogDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (childUid == null || childUid.isEmpty()) {
            callback.onFailure(new Exception("Invalid child UID"));
            return;
        }

        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> allData = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("timestamp", document.get("timestamp"));
                        data.put("medicineName", document.getString("medicineName"));
                        data.put("dosage", document.get("dosage"));
                        data.put("medicationType", document.getString("medicationType"));
                        allData.add(data);
                    }
                    callback.onSuccess(allData);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
