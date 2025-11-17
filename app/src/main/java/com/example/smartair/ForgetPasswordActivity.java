package com.example.smartair;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button buttonSendRecovery;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.inputEmail);
        buttonSendRecovery = findViewById(R.id.sendRecoveryButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonSendRecovery.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            sendLink(email);
        });

    }

    private void sendLink(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "If an account exists for this email, a reset link has been sent.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to send reset email. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
