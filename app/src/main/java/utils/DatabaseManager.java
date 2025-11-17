package utils;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.smartair.Login;
import com.example.smartair.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public enum AccountType {
        Parent,
        Child,
        Provider
    }

    public interface SuccessFailCallback {
        void onSuccess();

        void onFailure(Exception e);
    }

    public static void accountRegister(String email, String password, AccountType type, SuccessFailCallback callback) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String accountType = type.name();

                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                callback.onFailure(new Exception("User is null after registration"));
                                return;
                            }

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", email);
                            userMap.put("accountType", accountType);

                            db.collection("users").document(user.getUid()).set(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            callback.onSuccess();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            callback.onFailure(e);
                                        }
                                    });
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    public static void accountLogin(String email, String password, SuccessFailCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }
}
