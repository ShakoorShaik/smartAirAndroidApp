package com.example.smartair.child.ChildHistory;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import utils.BadgeStreakManager;

public class PEFDatabaseGetter {

    public interface PEFCallback {
        void onSuccess( List<PEFReading> pef_list);
        void onFailure(Exception e);
    }
    public static void getPEFList(String childUid, PEFCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Controller")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        callback.onSuccess(new ArrayList<>());
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
