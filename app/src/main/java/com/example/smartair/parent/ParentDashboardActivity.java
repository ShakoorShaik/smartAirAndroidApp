package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;

public class ParentDashboardActivity extends AppCompatActivity {

    private Button buttonAddChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        buttonAddChild = findViewById(R.id.buttonAddChild);

        buttonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ParentDashboardActivity.this, AddChildActivity.class));
            }
        });
    }
}
