package com.example.smartair.parent;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

import java.util.Objects;

import utils.DatabaseManager;

public class setRescueThreshold extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_rescue_threshold);
        Button buttonBack = findViewById(R.id.backButton);
        Button buttonNext = findViewById(R.id.nextButton);
        EditText thresholdRescue = findViewById(R.id.low_rescue);

        DatabaseManager.getData("rescueHourThreshold", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {
                thresholdRescue.setText(Objects.requireNonNullElse(data, "3"));
            }

            @Override
            public void onFailure(Exception e) {
                thresholdRescue.setText("3");
            }
        });

        buttonBack.setOnClickListener(v -> {
            finish();
        });

        buttonNext.setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(thresholdRescue.getText()))) {
                DatabaseManager.writeData("rescueHourThreshold", thresholdRescue.getText().toString(), new DatabaseManager.SuccessFailCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
                finish();
            }
            else{
                Toast.makeText(this, "Please enter data for all the fields.", Toast.LENGTH_SHORT).show();
            }


        });

    }
}
