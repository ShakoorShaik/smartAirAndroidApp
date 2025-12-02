package com.example.smartair.parent;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

import java.util.Objects;

import utils.DatabaseManager;

public class SetChildThresholds extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_threshold_child);
        Button buttonBack = findViewById(R.id.backButton);
        Button buttonNext = findViewById(R.id.nextButton);
        EditText thresholdController = findViewById(R.id.controller);
        EditText thresholdTechnique = findViewById(R.id.technique);
        EditText thresholdRescue = findViewById(R.id.low_rescue);
        DatabaseManager.getData("thresholdController", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {
                thresholdController.setText(Objects.requireNonNullElse(data, "7"));
            }

            @Override
            public void onFailure(Exception e) {
                thresholdController.setText("7");
            }
        });
        DatabaseManager.getData("thresholdTechnique", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {
                thresholdTechnique.setText(Objects.requireNonNullElse(data, "10"));
            }

            @Override
            public void onFailure(Exception e) {
                thresholdTechnique.setText("10");
            }
        });
        DatabaseManager.getData("thresholdRescue", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {
                thresholdRescue.setText(Objects.requireNonNullElse(data, "4"));
            }

            @Override
            public void onFailure(Exception e) {
                thresholdRescue.setText("4");
            }
        });

        buttonBack.setOnClickListener(v -> {
            finish();
        });

        buttonNext.setOnClickListener(v -> {
            if (!(TextUtils.isEmpty(thresholdController.getText()) || TextUtils.isEmpty(thresholdRescue.getText()) || TextUtils.isEmpty(thresholdTechnique.getText()))) {
                DatabaseManager.writeData("thresholdController", thresholdController.getText().toString(), new DatabaseManager.SuccessFailCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
                DatabaseManager.writeData("thresholdRescue", thresholdRescue.getText().toString(), new DatabaseManager.SuccessFailCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(Exception e) {

                    }
                });
                DatabaseManager.writeData("thresholdTechnique", thresholdTechnique.getText().toString(), new DatabaseManager.SuccessFailCallback() {
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
