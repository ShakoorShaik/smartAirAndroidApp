package com.example.smartair.child.ChildHistory;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import utils.ChildIdManager;

public class ChildHistoryPEFActivity extends AppCompatActivity {
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_history);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        ChildIdManager manager = new ChildIdManager(this);
        String curr_child_id = manager.getChildId();
        if (!curr_child_id.equals("NA")) {
            userID = curr_child_id;
        } else {
            userID = user.getUid();
        }

        Button returnButton = findViewById(R.id.returnButton);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Set LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<PEFReading> pef_list = new ArrayList<>();

        pef_list.add(new PEFReading("Math Exam", 300L, 200L));

        pef_list.add(new PEFReading("Science Exam", 300L, 200L));

        // Set Adapter
        ChildHistoryPefAdapter adapter = new ChildHistoryPefAdapter(pef_list);
        recyclerView.setAdapter(adapter);



    }
}