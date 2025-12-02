package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
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


import com.example.smartair.child.ChildDashboardMainActivity;
import com.example.smartair.parent.ParentDashboardWithChildrenActivity;
import com.example.smartair.provider.ProviderCodeLinking;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivityView extends AppCompatActivity {

    LoginActivityPresenter presenter;
    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    Button buttonForgetPassword;
    ProgressBar progressBar;
    TextView registerText;

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.checkAutoLogin();
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

        presenter = new LoginActivityPresenter(this, new LoginActivityModel());
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.button_login);
        buttonForgetPassword = findViewById(R.id.button_forget_password);
        progressBar = findViewById(R.id.progressBar);
        registerText = findViewById(R.id.registerNow);

        registerText.setOnClickListener(v -> {
            presenter.onRegisterClick();
        });

        buttonLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            presenter.attemptLogin(editTextEmail.getText(), editTextPassword.getText());


        });

        buttonForgetPassword.setOnClickListener(v1 -> {
            presenter.onForgetPasswordClick();
        });
    }

    public void sendToRegistration() {
        Intent intent = new Intent(getApplicationContext(), Registration.class);
        startActivity(intent);
        finish();
    }

    public void sendToParentHome() {
        Intent intent = new Intent(getApplicationContext(), ParentDashboardWithChildrenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendToChildHome() {
        Intent intent = new Intent(getApplicationContext(), ChildDashboardMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendToProviderCodeLinking() {
        Intent intent = new Intent(getApplicationContext(), ProviderCodeLinking.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendToPasswordReset() {
        Intent intent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
        startActivity(intent);
    }

    public void sendToast(String msg) {
        Toast.makeText(LoginActivityView.this, msg, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
    }


}
