package com.example.smartair.child.checkin;

import java.util.HashMap;
import java.util.Map;

import utils.ChildIdManager;
import utils.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DailyCheckInDataWriting {

    public interface WriteCallback{
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private void autoInitializeFields(ChildCheckInDataFields data) {
        if (data.nightWaking == null || data.nightWaking.isEmpty()) {
            data.nightWaking = "Skipped";
        }

        if (data.coughWheeze == null || data.coughWheeze.isEmpty()) {
            data.coughWheeze = "Skipped";
        }

        if (data.activityLimits == null || data.activityLimits.isEmpty()) {
            data.activityLimits = "Skipped";
        }

        if (data.notes == null || data.notes.isEmpty()) {
            data.notes = "Skipped";
        }
    }

    private boolean isDataValid(ChildCheckInDataFields data) {
        return data.date != null && !data.date.isEmpty() &&
                data.userId != null && !data.userId.isEmpty();
    }

    private Map<String, Object> convertToFirebaseFormat(ChildCheckInDataFields data) {
        Map<String, Object> firebaseData = new HashMap<>();

        firebaseData.put("enteredByParent", data.enteredByParent);
        firebaseData.put("nightWaking", data.nightWaking);
        firebaseData.put("coughWheeze", data.coughWheeze);
        firebaseData.put("activityLimits", data.activityLimits);
        firebaseData.put("notes", data.notes);
        firebaseData.put("date", data.date);
        firebaseData.put("userEmail", data.userEmail);
        firebaseData.put("timestamp", System.currentTimeMillis());

        return firebaseData;
    }

    public void writeDailyCheckIn(String userID, ChildCheckInDataFields data, WriteCallback callback) {
        autoInitializeFields(data);

        Map<String, Object> firebaseData = convertToFirebaseFormat(data);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userID)
                .collection("daily_checkin_logs")
                .document(data.date)
                .set(firebaseData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Failed to save check in: " + e.getMessage()));
    }
}