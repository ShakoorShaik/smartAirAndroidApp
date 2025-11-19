package com.example.smartair.provider;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.Login;
import com.example.smartair.R;
import com.example.smartair.parent.LinkAccountLayout;
import com.example.smartair.parent.ParentDashboardWithChildrenActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ProviderDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_dashboard);

        Button Logout = findViewById(R.id.buttonLogout1);
        Button Link = findViewById(R.id.button4);

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProviderDashboardActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
