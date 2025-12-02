package com.example.smartair;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartair.child.ChildDashboardHome;
import com.example.smartair.parent.ParentDashboardWithChildrenActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import utils.DatabaseManager;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        if(user == null)
        {
            Intent intent = new Intent(getApplicationContext(), LoginActivityView.class);
            startActivity(intent);
            finish();
        }
        else
        {
            DatabaseManager.getData("accountType", new DatabaseManager.DataSuccessFailCallback() {
                @Override
                public void onSuccess(String data) {
                    if (data == null) {
                        textView.setText("Account type not found");
                        return;
                    }

                    if (data.equals("Parent")) {
                        Intent intent = new Intent(getApplicationContext(), ParentDashboardWithChildrenActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (data.equals("Child")) {
                        Intent intent = new Intent(getApplicationContext(), ChildDashboardHome.class);
                        startActivity(intent);
                        finish();
                    } else {
                        textView.setText("Account Type: " + data);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getApplicationContext(), LoginActivityView.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivityView.class);
            startActivity(intent);
            finish();
        });
    }
}