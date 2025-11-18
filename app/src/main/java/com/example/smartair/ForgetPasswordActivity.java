package com.example.smartair;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button buttonSendRecovery;

    private Button buttonReturn;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.inputEmail);
        buttonSendRecovery = findViewById(R.id.sendRecoveryButton);
        buttonReturn = findViewById(R.id.backButton);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonSendRecovery.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            sendLink(email);
        });

        buttonReturn.setOnClickListener(v1 -> {
            finish();
        });


    }

    private void sendLink(String email) {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "A reset link has been sent, given an account exists..", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to send reset email. Try again please.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
