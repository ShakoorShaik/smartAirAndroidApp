package com.example.smartair.child.ChildHistory;

import android.util.Log;

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
                .collection("pefReadings")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.isEmpty()) {
                            callback.onSuccess(new ArrayList<>());
                        }
                        List<PEFReading> pef_list = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            Long ts = doc.getLong("timestamp");
                            Long val = doc.getLong("value");
                            String date = doc.getString("date");
                            pef_list.add(new PEFReading(date, ts, val));
                        }
                        callback.onSuccess(pef_list);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
