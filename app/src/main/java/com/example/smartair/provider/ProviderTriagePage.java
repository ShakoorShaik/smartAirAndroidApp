package com.example.smartair.provider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;

public class ProviderTriagePage extends AppCompatActivity {

    private Button returnToHome;

    private Button logOut;

    private Button left;

    private Button right;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_triage);

        returnToHome = findViewById(R.id.TopLeftButton);
        returnToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderTriagePage.this, ProviderHomePage.class);
            startActivity(intent);
            finish();
        });

        logOut = findViewById(R.id.TopRightButton);
        logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(ProviderTriagePage.this, LoginActivityView.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        left = findViewById(R.id.BottomLeftButton);
        right = findViewById(R.id.BottomRightButton);
    }
}
