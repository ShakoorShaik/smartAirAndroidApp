package com.example.smartair.child.logtrigger;

import com.example.smartair.child.checkin.DailyCheckInDataWriting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firestore.v1.Write;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChildrenTriggerDataWriting {

    public interface WriteCallback{
        void onSuccess();
        void onFailure(Exception e);
    }

    private void WriteTriggerCountsAndDate(ChildrenTriggerCountAndDates data, WriteCallback callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callBack.onFailure(new Exception("User not logged in"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String today = data.getDate();
        String triggerName = data.getTriggerName();

        DocumentReference triggerRef = db.collection("users")
                .document(user.getUid())
                .collection("trigger logs")
                .document(triggerName);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put(today, data.getCount());

        triggerRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callBack.onSuccess())
                .addOnFailureListener(e -> callBack.onFailure(e));
    }


}
