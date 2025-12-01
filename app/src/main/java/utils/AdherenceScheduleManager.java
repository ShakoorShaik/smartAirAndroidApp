package utils;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for handling adherence schedule operations in Firebase
 */
public class AdherenceScheduleManager {

    private static final String COLLECTION_USERS = "users";
    private static final String FIELD_ADHERENCE_SCHEDULE = "adherenceSchedule";

    public interface ScheduleCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface LoadScheduleCallback {
        void onSuccess(Map<String, Object> schedule);
        void onFailure(Exception e);
    }

    /**
     * Save adherence schedule for a specific child
     * @param childUid The UID of the child
     * @param frequency The medication frequency (e.g., "Once Daily (1x)", "Twice Daily (2x)")
     * @param doseTimes List of dose times in "HH:MM AM/PM" format
     * @param callback Callback for success/failure
     */
    public static void saveSchedule(String childUid, String frequency, List<String> doseTimes,
                                     ScheduleCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> schedule = new HashMap<>();
        schedule.put("frequency", frequency);
        schedule.put("doseTimes", doseTimes);
        schedule.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_USERS)
                .document(childUid)
                .update(FIELD_ADHERENCE_SCHEDULE, schedule)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    // If document doesn't exist or field doesn't exist, try setting it
                    Map<String, Object> data = new HashMap<>();
                    data.put(FIELD_ADHERENCE_SCHEDULE, schedule);
                    db.collection(COLLECTION_USERS)
                            .document(childUid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid2 -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                });
    }

    /**
     * Load adherence schedule for a specific child
     * @param childUid The UID of the child
     * @param callback Callback with the schedule data
     */
    public static void loadSchedule(String childUid, LoadScheduleCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION_USERS)
                .document(childUid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> schedule = (Map<String, Object>) documentSnapshot.get(FIELD_ADHERENCE_SCHEDULE);
                        if (schedule != null) {
                            callback.onSuccess(schedule);
                        } else {
                            callback.onFailure(new Exception("No schedule found"));
                        }
                    } else {
                        callback.onFailure(new Exception("Child document not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Delete adherence schedule for a specific child
     * @param childUid The UID of the child
     * @param callback Callback for success/failure
     */
    public static void deleteSchedule(String childUid, ScheduleCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> updates = new HashMap<>();
        updates.put(FIELD_ADHERENCE_SCHEDULE, com.google.firebase.firestore.FieldValue.delete());

        db.collection(COLLECTION_USERS)
                .document(childUid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
