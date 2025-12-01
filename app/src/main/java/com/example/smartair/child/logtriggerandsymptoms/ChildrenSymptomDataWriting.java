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
public class ChildrenSymptomDataWriting extends ChildrenTriggerDataWriting{

    @Override
    public void deleteDataFields(String triggerName, Timestamp timestampToRemove, WriteCallback callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            callBack.onFailure(new Exception("User not logged in"));
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put(triggerName, FieldValue.arrayRemove(timestampToRemove));

        db.collection("users")
                .document(user.getUid())
                .collection("symptomLogs")
                .document(todayDate)
                .update(updates)
                .addOnSuccessListener(aVoid -> callBack.onSuccess())
                .addOnFailureListener(callBack::onFailure);
    }

    @Override
    public void queryTodayTriggers(OnTriggersLoadedListener callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        db.collection("users")
                .document(user.getUid())
                .collection("symptomLogs")
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

    @Override
    public void WriteDateToSubCollection(ChildrenTriggerCountAndDates data, WriteCallback callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            callBack.onFailure(new Exception("User not logged in"));
            return;
        }

        Timestamp currTime = Timestamp.now();

        Map<String, Object> triggerTimeStamp = new HashMap<>();
        triggerTimeStamp.put(data.getTriggerName(), FieldValue.arrayUnion(currTime));

        db.collection("users")
                .document(user.getUid())
                .collection("symptomLogs")
                .document(data.getDate())
                .set(triggerTimeStamp, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callBack.onSuccess())
                .addOnFailureListener(e -> callBack.onFailure(e));
    }
}
