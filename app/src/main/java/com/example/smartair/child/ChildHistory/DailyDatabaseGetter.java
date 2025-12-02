package com.example.smartair.child.ChildHistory;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DailyDatabaseGetter {
    public interface DailyCallback {
        void onSuccess( List<DailyCheckIn> medicine_list);
        void onFailure(Exception e);
    }

    public static void getDailyList(String childUid, DailyCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("daily_checkin_logs")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        if (documents.isEmpty()) {
                            callback.onSuccess(new ArrayList<>());
                        }
                        List<DailyCheckIn> daily_list = new ArrayList<>();
                        for (DocumentSnapshot doc : documents) {
                            Long ts = doc.getLong("timestamp");
                            String date = doc.getString("date");
                            String al = doc.getString("activityLimits");
                            String cw = doc.getString("coughWheeze");
                            Boolean ebp = doc.getBoolean("enteredByParent");
                            String nw = doc.getString("nightWaking");
                            String n = doc.getString("notes");
                            String ue = doc.getString("userEmail");
                            daily_list.add(new DailyCheckIn(date, ts, al, cw, ebp, nw, n, ue));
                        }
                        callback.onSuccess(daily_list);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
