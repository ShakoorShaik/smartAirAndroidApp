package com.example.smartair.child.emergency;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;

import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utils.ChildIdManager;

import utils.EmergencyManager;
import utils.ZoneManager;

public class Emergency extends AppCompatActivity {
    private CheckBox checkboxCsfs;

    private CheckBox checkboxCp;

    private CheckBox checkboxLnbg;

    private CheckBox checkboxRra;

    private CheckBox checkboxO;
    private Button buttonNextSteps;

    private Button buttonNotEmergency;

    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_emergency);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        if (!curr_child_id.equals("NA")) {
            userID = curr_child_id;
        } else {
            userID = user.getUid();
        }
        checkIn10();

        checkboxCsfs = findViewById(R.id.checkbox_csfs);
        checkboxCp = findViewById(R.id.checkbox_cp);
        checkboxLnbg = findViewById(R.id.checkbox_lnbg);
        checkboxRra = findViewById(R.id.checkbox_rra);
        checkboxO = findViewById(R.id.checkbox_o);

        buttonNextSteps = findViewById(R.id.button_next_steps);
        buttonNotEmergency = findViewById(R.id.button_not_emergency);

        buttonNextSteps.setOnClickListener(v -> {
            checkIn10();
            if (checkboxCsfs.isChecked() || checkboxCp.isChecked() || checkboxLnbg.isChecked() || checkboxRra.isChecked()){
                escalation();
            }
            else{
                ZoneManager.getTodayZone(userID, new ZoneManager.ZoneCallback() {
                    @Override
                    public void onSuccess(ZoneManager.Zone zone, Integer pefValue, Integer pbValue) {
                        if (zone == ZoneManager.Zone.GREEN){
                            log_triage("green zone steps");
                            startActivity(new Intent(getApplicationContext(), GreenZoneSteps.class));
                        }
                        else if (zone == ZoneManager.Zone.YELLOW){
                            log_triage("yellow zone steps");
                            startActivity(new Intent(getApplicationContext(), YellowZoneSteps.class));
                        }
                        else{
                            log_triage("red zone steps");
                            startActivity(new Intent(getApplicationContext(), RedZoneSteps.class));
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(Emergency.this, "FAILED TO GET CURRENT ZONE", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        buttonNotEmergency.setOnClickListener(v -> {
            finish();
        });

    }

    private void escalation(){
        send_parent_alert();
        log_triage("call 911");
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("CALL EMERGENCY SERVICES IMMEDIATELY");
        alertDialog.setMessage("YOUR SYMPTOMS ARE TOO SEVERE, PLEASE CALL EMERGENCY SERVICES. YOUR PARENT HAS BEEN ALERTED");
        alertDialog.setOnDismissListener( d -> {
            finish();
        });
        alertDialog.show();
    }

    private void checkIn10(){
        new Handler(Looper.getMainLooper()).postDelayed(() -> {checkUpPrompt();}, 1000 * 60 * 10);
    }

    private void log_triage(String guidance){
        EmergencyManager.logTriage(userID, checkboxCsfs.isChecked(), checkboxCp.isChecked(), checkboxLnbg.isChecked(), checkboxRra.isChecked(), checkboxO.isChecked(), guidance);
    }

    private void checkUpPrompt(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("HOW ARE YOU FEELING NOW?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Feel better", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL     , "Feel the same", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                escalation();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Feel worse", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                escalation();
            }
        });
        alertDialog.show();
    }

    private void send_parent_alert(){
        EmergencyManager.toggleEmergencyFlag(userID);
    }
}
