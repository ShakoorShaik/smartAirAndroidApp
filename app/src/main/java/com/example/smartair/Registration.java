package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.smartair.parent.ParentDashboardWithChildrenActivity;

import com.plattysoft.leonids.ParticleSystem;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import utils.DatabaseManager;

public class Registration extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonRegister;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ProgressBar progressBar;
    TextView textView;
    Spinner spinnerAccountType;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        /*
        if (hasFocus) {
            ViewGroup mainView = (ViewGroup) findViewById(R.id.main);
            ParticleSystem particleSystem = new ParticleSystem(this, 100, R.drawable.ambient_particle, 8000);
            particleSystem.setSpeedRange(0.05f, 0.1f);
            particleSystem.setAcceleration(0.00005f, 180);
            particleSystem.emitWithGravity(mainView, Gravity.CENTER, 10);

            mainView.post(() -> {
                for (int i = 1; i < mainView.getChildCount(); i++) { // Skip index 0 if needed
                    mainView.getChildAt(i).bringToFront();
                }
                mainView.requestLayout();
            });
        }
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonRegister = findViewById(R.id.button_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivityView.class);
            startActivity(intent);
            finish();
        });
        spinnerAccountType = findViewById(R.id.spinnerAccountType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.account_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccountType.setAdapter(adapter);

        buttonRegister.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
            password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email))
            {
                Toast.makeText(Registration.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if(TextUtils.isEmpty(password))
            {
                Toast.makeText(Registration.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            String accountType = String.valueOf(spinnerAccountType.getSelectedItem());
            DatabaseManager.AccountType type = DatabaseManager.AccountType.valueOf(accountType);

            DatabaseManager.accountRegister(email, password, type, new DatabaseManager.SuccessFailCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(Registration.this, "Account created!",
                            Toast.LENGTH_SHORT).show();
                    String selectedAccountType = spinnerAccountType.getSelectedItem().toString();
                    Intent intent;
                    if ("Parent".equals(selectedAccountType)) {
                        intent = new Intent(getApplicationContext(), ParentDashboardWithChildrenActivity.class);
                    } else {
                        intent = new Intent(getApplicationContext(), LoginActivityView.class);
                    }

                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Authentication failed.";
                    if (e != null && e.getMessage() != null) {
                        String firebaseError = e.getMessage();
                        if (firebaseError.contains("EMAIL_EXISTS") || firebaseError.contains("email-already-in-use")) {
                            errorMessage = "This email is already registered. Please use a different email or try logging in.";
                        } else if (firebaseError.contains("WEAK_PASSWORD") || firebaseError.contains("weak-password")) {
                            errorMessage = "Password is too weak. Please use a stronger password (at least 6 characters).";
                        } else if (firebaseError.contains("INVALID_EMAIL") || firebaseError.contains("invalid-email")) {
                            errorMessage = "Invalid email format. Please check your email.";
                        } else {
                            errorMessage = "Registration failed: " + firebaseError;
                        }
                    }
                    Toast.makeText(Registration.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });

        });


    }
}