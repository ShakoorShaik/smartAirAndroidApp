package com.example.smartair.provider;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.Login;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;


public class ProviderInfoViewer extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_viewinfo);

        TextView linkedText = findViewById(R.id.linkedText);
        String parentEmail = getIntent().getStringExtra("parentEmail");

        if (parentEmail != null) {
            linkedText.setText("Currently linked with: " + parentEmail);

        }

    }
}
