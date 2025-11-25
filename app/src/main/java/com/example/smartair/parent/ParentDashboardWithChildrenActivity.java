package com.example.smartair.parent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartair.child.ChildDashboardActivity;
import com.example.smartair.Login;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.example.smartair.child.ChildHome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.ChildAccountManager;
import utils.ChildIdManager;
import utils.DatabaseManager;

public class ParentDashboardWithChildrenActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChildren;
    private TextView textViewNoChildren;
    private ProgressBar progressBar;
    private ChildrenAdapter childrenAdapter;
    private List<Map<String, Object>> childrenList;

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
            }
        });

        loadChildren();

        Button buttonLinkProvider = findViewById(R.id.button2);

        buttonLinkProvider.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentDashboardWithChildrenActivity.this, LinkAccountLayout.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (childrenList != null) {
            loadChildren();
        }
    }

    private void loadChildren() {
        if (progressBar == null || recyclerViewChildren == null || childrenList == null || childrenAdapter == null) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        textViewNoChildren.setVisibility(View.GONE);
        recyclerViewChildren.setVisibility(View.GONE);

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (progressBar == null || recyclerViewChildren == null || childrenList == null || childrenAdapter == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                childrenList.clear();
                if (children != null) {
                    childrenList.addAll(children);
                }
                childrenAdapter.notifyDataSetChanged();

                if (children == null || children.isEmpty()) {
                    if (textViewNoChildren != null) {
                        textViewNoChildren.setVisibility(View.VISIBLE);
                    }
                    recyclerViewChildren.setVisibility(View.GONE);
                } else {
                    if (textViewNoChildren != null) {
                        textViewNoChildren.setVisibility(View.GONE);
                    }
                    recyclerViewChildren.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (progressBar == null || recyclerViewChildren == null) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                String errorMessage = e != null ? e.getMessage() : "Unknown error";
                Toast.makeText(ParentDashboardWithChildrenActivity.this, "Error loading children: " + errorMessage, Toast.LENGTH_SHORT).show();
                if (textViewNoChildren != null) {
                    textViewNoChildren.setVisibility(View.VISIBLE);
                }
                recyclerViewChildren.setVisibility(View.GONE);
            }
        });
    }

    private void showDeleteConfirmation(int position) {
        Map<String, Object> child = childrenList.get(position);
        String childName = (String) child.get("name");
        String childUid = (String) child.get("uid");

        new AlertDialog.Builder(this)
                .setTitle("Unlink Child")
                .setMessage("Are you sure you want to unlink " + childName + " from your account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    unlinkChild(childUid);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void unlinkChild(String childUid) {
        progressBar.setVisibility(View.VISIBLE);
        ChildAccountManager.unlinkChildFromParent(childUid, new utils.DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ParentDashboardWithChildrenActivity.this, "Child unlinked successfully", Toast.LENGTH_SHORT).show();
                loadChildren();
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ParentDashboardWithChildrenActivity.this, "Error unlinking child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

