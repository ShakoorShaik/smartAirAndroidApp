package com.example.smartair.provider.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

public class Provider_info_view2 extends AppCompatActivity {

    private Button right;

    private Button left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_infoview2);

        right = findViewById(R.id.BottomRightButton);

        right.setOnClickListener(v -> {
            Intent intent = new Intent(this, Provider_info_view3.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        left = findViewById(R.id.BottomLeftButton);

        left.setOnClickListener(v->{
            Intent intent = new Intent(this, Provider_info_view1.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
