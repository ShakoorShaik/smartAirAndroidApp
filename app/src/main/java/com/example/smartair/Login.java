package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.smartair.child.ChildHome;
import com.example.smartair.parent.ParentDashboardWithChildrenActivity;
import com.example.smartair.provider.ProviderDashboardActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utils.DatabaseManager;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    Button buttonForgetPassword;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            DatabaseManager.getData("accountType", new DatabaseManager.DataSuccessFailCallback() {
                @Override
                public void onSuccess(String accountType) {
                    Intent intent;
                    if ("Parent".equals(accountType)) {
                        intent = new Intent(getApplicationContext(), ParentDashboardWithChildrenActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else if ("Child".equals(accountType)) {
                        intent = new Intent(getApplicationContext(), ChildHome.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.button_login);
        buttonForgetPassword = findViewById(R.id.button_forget_password);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.registerNow);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Registration.class);
            startActivity(intent);
            finish();
        });

        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
            password = editTextPassword.getText() != null ? editTextPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email))
            {
                Toast.makeText(Login.this, "Enter email", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            if(TextUtils.isEmpty(password))
            {
                Toast.makeText(Login.this, "Enter password", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            DatabaseManager.accountLogin(email, password, new DatabaseManager.SuccessFailCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(Login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // Is there a better way to get this data?
                        DatabaseManager.getData("accountType", new DatabaseManager.DataSuccessFailCallback() {
                            @Override
                            public void onSuccess(String accountType) {
                                Intent intent;
                                if ("Parent".equals(accountType)) {
                                    // Redirect to ParentDashboard
                                    intent = new Intent(getApplicationContext(), ParentDashboardWithChildrenActivity.class);
                                } else if ("Provider".equals(accountType)){
                                    intent = new Intent(getApplicationContext(), ProviderDashboardActivity.class);
                                } else if ("Child".equals(accountType)) {
                                    intent = new Intent(getApplicationContext(), ChildHome.class);
                                } else {
                                    // TODO: in the future show other dashboards for child user types
                                    intent = new Intent(getApplicationContext(), MainActivity.class);
                                }

                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Failed to retrieve account type.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                @Override
                public void onFailure(Exception e) {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Authentication failed.";
                    if (e != null && e.getMessage() != null) {
                        String firebaseError = e.getMessage();
                        if (firebaseError.contains("INVALID_PASSWORD") || firebaseError.contains("wrong-password")) {
                            errorMessage = "Incorrect password. Please try again.";
                        } else if (firebaseError.contains("USER_NOT_FOUND") || firebaseError.contains("user-not-found")) {
                            errorMessage = "No account found with this email. Please register first.";
                        } else if (firebaseError.contains("INVALID_EMAIL") || firebaseError.contains("invalid-email")) {
                            errorMessage = "Invalid email format. Please check your email.";
                        } else if (firebaseError.contains("too-many-requests")) {
                            errorMessage = "Too many failed attempts. Please try again later.";
                        } else {
                            errorMessage = "Authentication failed: " + firebaseError;
                        }
                    }
                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });

        });

        buttonForgetPassword.setOnClickListener(v1 -> {
            Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
            startActivity(intent);
        });
    }
}
