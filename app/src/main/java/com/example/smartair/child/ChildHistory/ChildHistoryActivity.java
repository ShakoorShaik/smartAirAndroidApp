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

import java.util.List;

import utils.ChildIdManager;

public class ChildHistoryActivity extends AppCompatActivity {
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

        RecyclerView recyclerView = findViewById(R.id.recyclerViewPEF);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PEFDatabaseGetter.getPEFList(userID, new PEFDatabaseGetter.PEFCallback() {
            @Override
            public void onSuccess(List<PEFReading> pef_list) {
                ChildHistoryPefAdapter adapter = new ChildHistoryPefAdapter(pef_list);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        RecyclerView recyclerView2 = findViewById(R.id.recyclerViewDaily);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this));
        DailyDatabaseGetter.getDailyList(userID, new DailyDatabaseGetter.DailyCallback() {
            @Override
            public void onSuccess(List<DailyCheckIn> daily_list) {
                ChildHistoryDailyAdapter adapter = new ChildHistoryDailyAdapter(daily_list);
                recyclerView2.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });

        RecyclerView recyclerView3 = findViewById(R.id.recyclerViewMedicine);
        recyclerView3.setLayoutManager(new LinearLayoutManager(this));
        MedicineDatabaseGetter.getMedicineList(userID, new MedicineDatabaseGetter.MedicineCallback() {
            @Override
            public void onSuccess(List<Medicine> medicine_list) {
                ChildHistoryMedicineAdapter adapter = new ChildHistoryMedicineAdapter(medicine_list);
                recyclerView3.setAdapter(adapter);
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
}