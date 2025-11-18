package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    public enum AccountType {
        Parent,
        Child,
        Provider
    }

    public interface SuccessFailCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public interface DataSuccessFailCallback {
        void onSuccess(String data);
        void onFailure(Exception e);
    }

    public static void accountRegister(String email, String password, AccountType type, SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String accountType = type.name();

                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            callback.onFailure(new Exception("User is null after registration"));
                            return;
                        }

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("accountType", accountType);

                        db.collection("users").document(user.getUid()).set(userMap)
                                .addOnSuccessListener(aVoid -> callback.onSuccess())
                                .addOnFailureListener(callback::onFailure);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public static void accountLogin(String email, String password, SuccessFailCallback callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public static void getData(String documentName, DataSuccessFailCallback callback)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null)
        {
            callback.onFailure(new Exception("User is null"));
            return;
        }
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            callback.onSuccess(document.getString(documentName));
                        } else {
                            callback.onFailure(task.getException());
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public static void writeData(String documentName, Object data, SuccessFailCallback callback)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null)
        {
            callback.onFailure(new Exception("User is null"));
            return;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put(documentName, data);
        db.collection("users").document(user.getUid()).set(userMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }
}
