package utils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RescueAlertManager {
    public static void toggleRescueFlag(String ChildUid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(ChildUid);
        docRef.get().addOnSuccessListener(dS -> {
            docRef.update("rescueFlag", false).addOnSuccessListener(v -> {
                docRef.update("rescueFlag", true);
            });
        });
    }
}
