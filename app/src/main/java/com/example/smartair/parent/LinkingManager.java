package com.example.smartair.parent;

import com.example.smartair.parent.CodeGeneration;
import utils.DatabaseManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class LinkingManager {

    private static final long ms_expiration = 60 * 60 * 1000; //1 hour expiration

    public static void generateReferralCode(DatabaseManager.DataSuccessFailCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User is null"));
            return;
        }

        String code = CodeGeneration.generateCode(7);

        db.collection("user")
                .whereEqualTo("referralCode", code)
                .get()
                .addOnCompleteListener(task ->{
                    if (!task.isSuccessful()) {
                        callback.onFailure(task.getException());
                        return;
                    }

                    boolean found = false;

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        found = true;
                    }

                    if (found) {
                        generateReferralCode(callback);
                        return;
                    }

                    long expiration = System.currentTimeMillis() + ms_expiration;

                    Map<String, Object> data = new HashMap<>();
                    data.put("referralCode", code);
                    data.put("referralExpires", expiration);

                    FirebaseFirestore db2 = FirebaseFirestore.getInstance();
                    db2.collection("users")
                            .document(user.getUid())
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess(code))
                            .addOnFailureListener(callback::onFailure);
                });
    }
}