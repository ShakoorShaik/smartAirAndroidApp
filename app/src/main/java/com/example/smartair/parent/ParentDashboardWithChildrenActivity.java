package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smartair.R;
import com.example.smartair.child.ChildHome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.DatabaseManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ParentDashboardWithChildrenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard_with_children);

        Button buttonAddChild = findViewById(R.id.buttonAddChild);
        Button buttonLogout = findViewById(R.id.buttonLogout);

        recyclerViewChildren = findViewById(R.id.recyclerViewChildren);
        textViewNoChildren = findViewById(R.id.textViewNoChildren);
        progressBar = findViewById(R.id.progressBar);

        childrenList = new ArrayList<>();
        childrenAdapter = new ChildrenAdapter(childrenList, new ChildrenAdapter.OnChildClickListener() {
            @Override
            public void onDeleteClick(int position) {
                showDeleteConfirmation(position);
            }

            @Override
            public void onClick(int position) {
                Map<String, Object> currentChild = childrenList.get(position);

                ChildIdManager manager = new ChildIdManager(getApplicationContext());
                manager.SaveChildId((String) currentChild.get("uid"));

                Intent intent = new Intent(ParentDashboardWithChildrenActivity.this, ChildHome.class);



                startActivity(intent);
                finish();
            }


        });


        recyclerViewChildren.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChildren.setAdapter(childrenAdapter);

        buttonAddChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentDashboardWithChildrenActivity.this, AddChildAccountActivity.class);
                startActivityForResult(intent, 1);
            }
        });



        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseManager.accountLogout();
                Intent intent = new Intent(ParentDashboardWithChildrenActivity.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        BottomNavigationView bottomNav = findViewById(R.id.navBar);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.bottom_home) {
                selectedFragment = new ParentHomeFragment();
            } else if (itemId == R.id.bottom_children) {
                selectedFragment = new ParentChildrenFragment();
            } else if (itemId == R.id.bottom_medicine) {
                selectedFragment = new ParentMedicineFragment();
            } else if (itemId == R.id.bottom_settings) {
                selectedFragment = new ParentSettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        // Set default fragment to children (the original dashboard)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ParentChildrenFragment())
                    .commit();
            bottomNav.setSelectedItemId(R.id.bottom_children);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Notify the children fragment to reload if we're returning from add child activities
        if (requestCode == 1 || requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
                if (fragment instanceof ParentChildrenFragment) {
                    ((ParentChildrenFragment) fragment).loadChildren();
                }
            }
        }
    }
}
