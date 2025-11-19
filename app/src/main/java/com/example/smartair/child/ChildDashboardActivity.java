package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.smartair.Login;
import com.example.smartair.R;

import utils.DatabaseManager;

public class ChildDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            TextView textView = (TextView) findViewById(R.id.WelcomeMsg);
            textView.setText(user.getEmail());
        }

        Button button_logout = findViewById(R.id.button_logout_child);

        button_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager.accountLogout();
                startActivity(new Intent(ChildDashboardActivity.this, Login.class));
                finish();
            }
        });
    }
}
