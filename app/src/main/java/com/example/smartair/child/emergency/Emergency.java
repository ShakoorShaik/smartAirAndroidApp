package com.example.smartair.child.emergency;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.child.inhalertechnique.InhalerTechniqueFirst;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import utils.ChildAccountManager;

public class Emergency extends AppCompatActivity {
    private CheckBox checkboxCsfs;

    private CheckBox checkboxCp;

    private CheckBox checkboxLnbg;

    private CheckBox checkboxRra;

    private CheckBox checkboxO;
    private Button buttonNextSteps;

    private Button buttonNotEmergency;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser childUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_emergency);

        checkIn10();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        childUser = mAuth.getCurrentUser();

        checkboxCsfs = findViewById(R.id.checkbox_csfs);
        checkboxCp = findViewById(R.id.checkbox_cp);
        checkboxLnbg = findViewById(R.id.checkbox_lnbg);
        checkboxRra = findViewById(R.id.checkbox_rra);
        checkboxO = findViewById(R.id.checkbox_o);

        buttonNextSteps = findViewById(R.id.button_next_steps);
        buttonNotEmergency = findViewById(R.id.button_not_emergency);

        buttonNextSteps.setOnClickListener(v -> {
            log_triage();
            checkIn10();
            if (checkboxCsfs.isChecked() || checkboxCp.isChecked() || checkboxLnbg.isChecked() || checkboxRra.isChecked()){
                escalation();
            }
            else{
                //todo make sure to forward to correct zone steps based on todays zone
                startActivity(new Intent(getApplicationContext(), InhalerTechniqueFirst.class));
            }
        });

        buttonNotEmergency.setOnClickListener(v -> {
            finish();
        });

    }

    private void escalation(){      //todo make sure this shit sends an alert to the parents
        send_parent_alert();
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

    private void log_triage(){
        ChildAccountManager.logTriage(checkboxCsfs.isChecked(), checkboxCp.isChecked(), checkboxLnbg.isChecked(), checkboxRra.isChecked(), checkboxO.isChecked());
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

    }
}
