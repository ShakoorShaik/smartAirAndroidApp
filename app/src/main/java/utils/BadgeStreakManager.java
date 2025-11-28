package utils;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BadgeStreakManager {

    public interface BSMCallback {
        void onSuccess(Integer streak);
        void onFailure(Exception e);
    }

    public static void getParentInhalerTechniqueThreshold(String childUid, BSMCallback callback){
        getThreshold(childUid, "thresholdTechnique", 10, callback);
    }
    public static void getInhalerTechniqueNumber(String childUid, BSMCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("InhalerTechniquePracticeHistory")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {;
                        int streak = task.getResult().size();
                        callback.onSuccess(streak);
                    } else {
                        callback.onSuccess(0);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getParentRescueNumberThreshold(String childUid, BSMCallback callback){
        getThreshold(childUid, "thresholdRescue", 4, callback);
    }

    public static void getRescueNumber(String childUid, BSMCallback callback){
        long thirtyDays = 30L * 24L * 60L * 60L * 1000L;
        Timestamp thirtyDaysAgo = new Timestamp(new Date(System.currentTimeMillis() - thirtyDays));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Rescue")
                .whereGreaterThanOrEqualTo("timestamp", thirtyDaysAgo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        int streak = task.getResult().size();
                        callback.onSuccess(streak);
                    } else {
                        callback.onSuccess(-1);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getParentControllerNumberThreshold(String childUid, BSMCallback callback){
        getThreshold(childUid, "thresholdController", 7, callback);
    }

    public static void getControllerNumber(String childUid, BSMCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Controller")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        int streak = getLongestStreak(documents);
                        callback.onSuccess(streak);
                    } else {
                        callback.onSuccess(0);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getControllerStreak(String childUid, BSMCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Controller")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        int streak = getCurrentStreak(documents);
                        callback.onSuccess(streak);
                    } else {
                        callback.onSuccess(0);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getInhalerTechniqueStreak(String childUid, BSMCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("InhalerTechniquePracticeHistory")
                .orderBy("timestamp",  Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {;
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        int streak = getCurrentStreak(documents);
                        callback.onSuccess(streak);
                    } else {
                        callback.onSuccess(0);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    private static int getCurrentStreak(List<DocumentSnapshot> documents) {
        if (documents.isEmpty()) {
            return 0;
        }

        int count = 0;
        long exact_moment = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(exact_moment);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long today = cal.getTimeInMillis();

        long oneDay = (24L * 60L * 60L * 1000L);
        long yesterday = today - oneDay;
        boolean flag = false;

        for (DocumentSnapshot doc : documents) {
            long docTime = doc.getTimestamp("timestamp").toDate().getTime();
            if (today < docTime && docTime <= exact_moment) {
                if (!flag){
                    count++;
                    flag = true;
                }
            }
        }
        long sixMonthsAgo = today - 6L * 30L * 24L * 60L * 60L * 1000L;
        while (today > sixMonthsAgo) {
            flag = false;
            for (DocumentSnapshot doc : documents) {
                long docTime = doc.getTimestamp("timestamp").toDate().getTime();
                if (yesterday <= docTime && docTime <= today) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return count;
            }
            count++;
            today = yesterday;
            yesterday = yesterday - oneDay;

        }
        return count;
    }

    private static int getLongestStreak(List<DocumentSnapshot> documents) {
        if (documents.isEmpty()) {
            return 0;
        }
        int max = 0;
        int count = 0;
        long exact_moment = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(exact_moment);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long today = cal.getTimeInMillis();

        long oneDay = (24L * 60L * 60L * 1000L);
        long yesterday = today - oneDay;
        boolean flag = false;

        for (DocumentSnapshot doc : documents) {
            long docTime = doc.getTimestamp("timestamp").toDate().getTime();
            if (today < docTime && docTime <= exact_moment) {
                if (!flag){
                    count++;
                    flag = true;
                }
            }
        }
        max = count;

        long sixMonthsAgo = today - 6L * 30L * 24L * 60L * 60L * 1000L;
        while (today > sixMonthsAgo) {
            flag = false;
            for (DocumentSnapshot doc : documents) {
                long docTime = doc.getTimestamp("timestamp").toDate().getTime();
                if (yesterday <= docTime && docTime <= today) {
                    flag = true;
                    break;
                }
            }
            count++;
            if (!flag) {
                count = 0;
            }
            if (count > max){
                max = count;
            }
            today = yesterday;
            yesterday = yesterday - oneDay;

        }
        return max;
    }

    public static void getThreshold(String childUid, String Threshold, int default_threshold, BSMCallback callback){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DatabaseManager.getData("parentUid", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {    // the case where we logged in through login as a child
                if (data == null){
                    DatabaseManager.getData(Threshold, new DatabaseManager.DataSuccessFailCallback() { // the case where we logged in through dashboard hence we are still logged in as parent
                        @Override
                        public void onSuccess(String data) {
                            if (data == null){
                                callback.onSuccess(default_threshold);
                            }
                            else {
                                callback.onSuccess(Integer.valueOf(data));
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                }
                else {
                    db.collection("users").document(data).get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String X = document.getString(Threshold);
                                        if (X != null){
                                            callback.onSuccess(Integer.valueOf(X));
                                        }
                                        callback.onSuccess(default_threshold);
                                    } else {
                                        callback.onFailure(task.getException());
                                    }
                                } else {
                                    callback.onFailure(task.getException());
                                }
                            });
                }
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });

    }


}
