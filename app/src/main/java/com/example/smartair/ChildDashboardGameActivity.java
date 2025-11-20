package com.example.smartair;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChildDashboardGameActivity extends AppCompatActivity {

    private ImageView Badge;
    private ImageView Streak;
    private Button buttonTechniqueHelper;
    private Button buttonRecordTrigger;
    private Button buttonTriggerHistory;
    private Button buttonRecordSymptom;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser childUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard_game);

        Badge = findViewById(R.id.badge);
        Badge.setImageResource(R.drawable.bronze_badge);      //todo there should be some logic to decide which badge
        Badge.setVisibility(View.VISIBLE);

        Streak = findViewById(R.id.streak);
        Streak.setImageResource(R.drawable.gold_badge);     //todo this is a placeholder for the streak, there should be a fire emoji with streak
        Streak.setVisibility(View.VISIBLE);

        buttonTechniqueHelper = findViewById(R.id.techniqueHelperButton);
        buttonRecordTrigger = findViewById(R.id.recordTriggerButton);
        buttonTriggerHistory = findViewById(R.id.historyTriggerButton);
        buttonRecordSymptom= findViewById(R.id.symptomRecordButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        childUser = mAuth.getCurrentUser();

        buttonTechniqueHelper.setOnClickListener(v -> {
            finish();
        });

        buttonRecordTrigger.setOnClickListener(v -> {   //todo
            Toast.makeText(this, "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonTriggerHistory.setOnClickListener(v -> {  //todo
            Toast.makeText(this, "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonRecordSymptom.setOnClickListener(v -> {   //todo
            Toast.makeText(this, "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

        buttonRecordSymptom.setOnClickListener(v -> {   //todo
            Toast.makeText(this, "TODO NOT FUNCTIONAL", Toast.LENGTH_LONG).show();
        });

    }

}