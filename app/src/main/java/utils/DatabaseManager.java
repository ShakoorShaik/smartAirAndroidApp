package utils;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.smartair.Login;
import com.example.smartair.Registration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    final static FirebaseFirestore db = FirebaseFirestore.getInstance();
    final static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public enum AccountType {
        Parent,
        Child,
        Provider
    }

    public static boolean RegisterAccount(String email, String password, AccountType type)
    {
        boolean[] returnValue = new boolean[1];

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String accountType = "";
                            switch (type)
                            {
                                case Child:
                                    accountType = "Child";
                                case Parent:
                                    accountType = "Parent";
                                case Provider:
                                    accountType = "Provider";
                            }
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            user.put("accountType", accountType);

                            db.collection("users")
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            returnValue[0] = true;
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            returnValue[0] = false;
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            returnValue[0] = false;
                        }
                    }
                });
        return returnValue[0];
    }


}
