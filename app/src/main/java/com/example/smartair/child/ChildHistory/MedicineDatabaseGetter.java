package com.example.smartair.child.ChildHistory;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MedicineDatabaseGetter {
    public interface MedicineCallback {
        void onSuccess( List<Medicine> daily_list);
        void onFailure(Exception e);
    }
    public static void getMedicineList(String childUid, MedicineCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.isEmpty()) {
                            callback.onSuccess(new ArrayList<>());
                        }
                        List<Medicine> medicine_list = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            Long dc = doc.getLong("doseCount");
                            String eb = doc.getString("enteredBy");
                            String mt = doc.getString("medicationType");
                            String postds = doc.getString("postDoseStatus");
                            Long postdr = doc.getLong("postDoseBreathRating");
                            String preds = doc.getString("preDoseStatus");
                            Long predr = doc.getLong("preDoseBreathRating");
                            Timestamp ts = doc.getTimestamp("timestamp");
                            medicine_list.add(new Medicine(dc, eb, mt, postdr, postds, predr, preds, ts));
                        }
                        callback.onSuccess(medicine_list);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
