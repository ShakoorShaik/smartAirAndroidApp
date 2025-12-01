package utils;

import com.google.firebase.firestore.FirebaseFirestore;

public class History {

    public static void getInhalerTechniqueNumber(String childUid, BadgeStreakManager.BSMCallback callback){
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
}
