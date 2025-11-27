package com.example.smartair.child.emergency;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.child.ChildDashboardHome;

abstract class ZoneSteps extends AppCompatActivity {
    Button buttonBack;
    Button buttonGoHome;

    TextView steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_zone_steps);
        steps = findViewById(R.id.zoneStepsTextView);
        steps.setTextSize(20);
        steps.setTextColor(getColor(R.color.black));
        buttonBack = findViewById(R.id.backButton);
        buttonGoHome = findViewById(R.id.nextButton);

        buttonBack.setOnClickListener(v -> {
            finish();
        });

        buttonGoHome.setOnClickListener(v -> {
            startActivity(new Intent(this, ChildDashboardHome.class));
        });

        zoneSteps();
    }
    abstract protected void zoneSteps();

}
