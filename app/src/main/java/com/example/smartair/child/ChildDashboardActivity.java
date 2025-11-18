package com.example.smartair.child;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.Login;
import com.example.smartair.R;

import utils.DatabaseManager;

public class ChildDashboardActivity extends AppCompatActivity {

    private Button button_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        button_logout = findViewById(R.id.button_logout_child);

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
