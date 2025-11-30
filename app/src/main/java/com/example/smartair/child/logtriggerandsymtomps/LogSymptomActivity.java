package com.example.smartair.child.logtriggerandsymtomps;

import android.os.Bundle;
import android.widget.TextView;

import com.example.smartair.R;
import com.example.smartair.child.logtriggerandsymtomps.ChildrenSymptomDataWriting;
import com.example.smartair.child.logtriggerandsymtomps.LogTriggerActivity;

public class LogSymptomActivity extends LogTriggerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView titleView = findViewById(R.id.tvTriggersTitle);
        if (titleView != null) {
            titleView.setText("Today's Symptoms:");
        }

        TextView Title = findViewById(R.id.TITLE1);
        Title.setText("Log Daily Symptoms");

        TextView Text1 = findViewById(R.id.text1);
        Text1.setText("No symptoms yet");

        TextView Text2 = findViewById(R.id.text2);
        Text2.setText("Add your first symptom above to start tracking");


        newTriggerInput.setHint("Enter new symptom");
        logTrigger.setText("Log Symptom");

        dataWriter = new ChildrenSymptomDataWriting();

        loadTodayTriggers();
    }
}