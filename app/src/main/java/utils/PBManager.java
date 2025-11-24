package utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PBManager {

    public static void getPB(String childUid, PBCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("users").document(childUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Long pb = task.getResult().getLong("personalBestPEF");
                        if (pb != null) {
                            callback.onSuccess(pb.intValue());
                        } else {
                            callback.onSuccess(null);
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void setPB(String childUid, int pbValue, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("personalBestPEF", pbValue);
        
        db.collection("users").document(childUid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public interface PBCallback {
        void onSuccess(Integer pbValue);
        void onFailure(Exception e);
    }
}

