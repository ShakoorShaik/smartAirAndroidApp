package com.example.smartair.child.logtriggerandsymptoms;

import android.os.Build;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChildrenTriggerDataWriting {

    public interface OnTriggersLoadedListener {
        void onTriggersLoaded(Map<String, Object> triggersWithTimestamps);
        void onFailure(Exception e);
    }

    public interface WriteCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void deleteDataFields(String userID, String triggerName, Timestamp timestampToRemove, WriteCallback callBack) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put(triggerName, FieldValue.arrayRemove(timestampToRemove));

        db.collection("users")
                .document(userID)
                .collection("triggerLogs")
                .document(todayDate)
                .update(updates)
                .addOnSuccessListener(aVoid -> callBack.onSuccess())
                .addOnFailureListener(callBack::onFailure);
    }

    public void queryTodayTriggers(String userID, OnTriggersLoadedListener callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users")
                .document(userID)
                .collection("triggerLogs")
                .document(todayDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Object> triggerData = task.getResult().getData();
                        callback.onTriggersLoaded(triggerData != null ? triggerData : new HashMap<>());
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void WriteDateToSubCollection(String userID, ChildrenTriggerCountAndDates data, WriteCallback callBack) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Timestamp currTime = Timestamp.now();

        Map<String, Object> triggerTimeStamp = new HashMap<>();
        triggerTimeStamp.put(data.getTriggerName(), FieldValue.arrayUnion(currTime));

        db.collection("users")
                .document(userID)
                .collection("triggerLogs")
                .document(data.getDate())
                .set(triggerTimeStamp, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callBack.onSuccess())
                .addOnFailureListener(e -> callBack.onFailure(e));
    }
}