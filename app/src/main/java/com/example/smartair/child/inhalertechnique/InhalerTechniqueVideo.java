package com.example.smartair.child.inhalertechnique;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;
import com.example.smartair.child.ChildDashboardMainActivity;
import com.example.smartair.child.ChildDashboardTasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.InhalerTechniqueManager;

public class InhalerTechniqueVideo extends AppCompatActivity {

    protected VideoView videoInhalerTechnique;
    protected Button buttonBack;
    protected Button buttonFinish;

    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        if (!curr_child_id.equals("NA")) {
            userID = curr_child_id;
        } else {
            userID = user.getUid();
        }

        setContentView(R.layout.activity_inhaler_technique_video);
        videoInhalerTechnique = findViewById(R.id.techniqueVideo);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.inhaler_video;
        Uri uri = Uri.parse(path);
        videoInhalerTechnique.setVideoURI(uri);
        videoInhalerTechnique.start();

        buttonBack = findViewById(R.id.backButton);
        buttonFinish= findViewById(R.id.finishButton);

        videoInhalerTechnique.setOnClickListener(v -> {
            if (videoInhalerTechnique.isPlaying()) {
                videoInhalerTechnique.pause();
            } else {
                videoInhalerTechnique.start();
            }
        });

        buttonBack.setOnClickListener(v -> {
            finish();
        });

        buttonFinish.setOnClickListener(v -> {
            InhalerTechniqueManager.logCorrectInhalerUse(userID);
            startActivity(new Intent(this, ChildDashboardMainActivity.class));
        });

    }
}
