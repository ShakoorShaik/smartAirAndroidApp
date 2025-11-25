package com.example.smartair.child.checkin;

import java.util.HashMap;
import java.util.Map;
import utils.DatabaseManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    private Map<String, Object> convertToFirebaseFormat (ChildCheckInDataFields data) {
        Map<String, Object> firebaseData = new HashMap<>();

        firebaseData.put("nightWaking", data.nightWaking);
        firebaseData.put("CoughWheezes", data.coughWheeze);
        firebaseData.put("activityLimits", data.activityLimits);
        firebaseData.put("notes", data.notes);

        firebaseData.put("date", data.date);
        firebaseData.put("userId", data.userId);
        firebaseData.put("userEmail", data.userEmail);

        return firebaseData;
    }

    private String generateDocumentPath (ChildCheckInDataFields data) {
        return "daily_checkins/" + data.userId + "_" + data.date;
    }

    public void writeDailyCheckIn(ChildCheckInDataFields data, WriteCallback callback) {

        autoInitializeFields(data);

        if (!isDataValid(data)) {
            callback.onFailure("Missing required user information");
            return;
        }

        Map<String, Object> firebaseData = convertToFirebaseFormat(data);
        String docPath = generateDocumentPath(data);

        DatabaseManager.writeData(docPath, firebaseData, new DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                String errorMessage = "Failed to save check in" + e.getMessage();
                callback.onFailure(errorMessage);
            }
        });
    }

    private String generateDocPathForDate(String date)  {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userId;

        if (user != null) {
            userId = user.getUid();
        } else {
            userId = "unknown";
        }
        return "daily_checkins/" + userId + "_" + date;

    }

    public interface CheckExistingCallback {
        void onCheckExistingResult(Boolean exists);
    }

    public void checkExistingCheckIn(String date, final CheckExistingCallback callback) {
        String docPath = generateDocPathForDate(date);

        DatabaseManager.getData(docPath, new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {
                boolean exist = data != null && !data.isEmpty();
                callback.onCheckExistingResult(exist);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onCheckExistingResult(false);
            }
        });
    }
}
