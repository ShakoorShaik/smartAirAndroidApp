package utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriggersManager {

    public interface TriggersDataCallback {
        void onSuccess(List<Map<String, Object>> triggersData);
        void onFailure(Exception e);
    }

    /**
     * Retrieves all trigger data for a specific child for export purposes
     * @param childUid The UID of the child
     * @param callback Callback with list of trigger entries
     */
    public static void getAllTriggersForChild(String childUid, TriggersDataCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (childUid == null || childUid.isEmpty()) {
            callback.onFailure(new Exception("Invalid child UID"));
            return;
        }

        db.collection("users").document(childUid)
                .collection("triggers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> triggersData = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("timestamp", document.getLong("timestamp"));
                        data.put("triggerType", document.getString("triggerType"));
                        data.put("description", document.getString("description"));
                        triggersData.add(data);
                    }
                    callback.onSuccess(triggersData);
                })
                .addOnFailureListener(e -> {
                    // Triggers collection might not exist, return empty list
                    callback.onSuccess(new ArrayList<>());
                });
    }
}
