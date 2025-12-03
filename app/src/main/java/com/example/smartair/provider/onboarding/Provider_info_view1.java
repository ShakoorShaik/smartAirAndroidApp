package com.example.smartair.provider.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.LoginActivityView;
import com.example.smartair.R;

public class Provider_info_view1 extends AppCompatActivity {

    private Button right;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_infoview1);

        right = findViewById(R.id.BottomRightButton);

        right.setOnClickListener(v -> {
            Intent intent = new Intent(this, Provider_info_view2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
