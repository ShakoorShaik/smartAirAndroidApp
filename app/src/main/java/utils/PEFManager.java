package utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class PEFManager {

    public interface PEFCallback {
        void onSuccess(Integer pefValue);
        void onFailure(Exception e);
    }

    public static void savePEFReading(String childUid, int pefValue, String date, long timestamp, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> pefReading = new HashMap<>();
        pefReading.put("value", pefValue);
        pefReading.put("timestamp", timestamp);
        pefReading.put("date", date);

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .add(pefReading)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void getHighestPEF(String childUid, PEFCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .orderBy("value", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Long pefValue = document.getLong("value");
                        if (pefValue != null) {
                            callback.onSuccess(pefValue.intValue());
                        } else {
                            callback.onSuccess(null);
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getMostRecentPEF(String childUid, PEFCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Long pefValue = document.getLong("value");
                        if (pefValue != null) {
                            callback.onSuccess(pefValue.intValue());
                        } else {
                            callback.onSuccess(null);
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getPEFByDate(String childUid, String date, PEFCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .whereEqualTo("date", date)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Long pefValue = document.getLong("value");
                        if (pefValue != null) {
                            callback.onSuccess(pefValue.intValue());
                        } else {
                            callback.onSuccess(null);
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface PEFDocumentCallback {
        void onSuccess(String documentId, Integer pefValue);
        void onFailure(Exception e);
    }

    public static void getPEFDocumentIdByDate(String childUid, String date, PEFDocumentCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .whereEqualTo("date", date)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Long pefValue = document.getLong("value");
                        String documentId = document.getId();
                        if (pefValue != null) {
                            callback.onSuccess(documentId, pefValue.intValue());
                        } else {
                            callback.onSuccess(documentId, null);
                        }
                    } else {
                        callback.onSuccess(null, null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void updatePEFReading(String childUid, String documentId, int pefValue, String date, long timestamp, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> pefReading = new HashMap<>();
        pefReading.put("value", pefValue);
        pefReading.put("timestamp", timestamp);
        pefReading.put("date", date);

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .document(documentId)
                .set(pefReading)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}

