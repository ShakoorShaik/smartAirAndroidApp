package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static DatabaseManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private DatabaseManager() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public interface FirestoreCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public void addInhalerLog(Map<String, Object> inhalerLog, FirestoreCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).collection("inhaler_log")
                    .add(inhalerLog)
                    .addOnSuccessListener(documentReference -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } else {
            callback.onError("User not logged in");
        }
    }

    public void addInhalerLog(String uid, Map<String, Object> inhalerLog,
                              FirestoreCallback callback) {
            db.collection("users").document(uid).collection("inhaler_log")
                    .add(inhalerLog)
                    .addOnSuccessListener(documentReference -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));

    }
    

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

    /**
     * A function that handles account registration
     * @param email The email to sign up with
     * @param password The password to sign up with
     * @param type The type of account you're signing up for
     * @param callback Callback on completion
     */
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

    /**
     * A function that handles account logins
     * @param email The email to login with
     * @param password The password to login with
     * @param callback Callback on completion
     */
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

    /**
     * A function that handles retrieval of data from a document
     * @param documentName The name of the document to retrieve from
     * @param callback Callback on completion with retrieved data
     */
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

    /**
     * A function that handles adding of data from a document
     * @param documentName The name of the document to create
     * @param data The data that goes in that document
     * @param callback Callback on completion with retrieved data
     */
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

    public static void accountLogout() {
        FirebaseAuth.getInstance().signOut();
    }
}
