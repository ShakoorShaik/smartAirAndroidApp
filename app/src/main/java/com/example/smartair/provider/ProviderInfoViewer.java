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
        Button Logout = findViewById(R.id.TopRightButton);

        Button LinkNewAcc = findViewById(R.id.TopLeftButton);

        if (parentEmail != null) {
            linkedText.setText("Currently linked with: " + parentEmail);

        }

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProviderInfoViewer.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        LinkNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProviderInfoViewer.this, ProviderDashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
