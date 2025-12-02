package com.example.smartair.parent;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.LoginActivityView;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import utils.DatabaseManager;
import utils.ChildAccountManager;

public class AddChildAccountActivity extends AppCompatActivity {

    private EditText editTextChildName;
    private EditText editTextChildDob;
    private EditText editTextChildEmail;
    private EditText editTextChildPassword;
    private EditText editTextNotes;
    private Button buttonSaveChild;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_account);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextChildName = findViewById(R.id.editTextChildName);
        editTextChildDob = findViewById(R.id.editTextChildDob);
        editTextNotes = findViewById(R.id.editTextNotes);
        editTextChildEmail = findViewById(R.id.editTextChildEmail);
        editTextChildPassword = findViewById(R.id.editTextChildPassword);
        buttonSaveChild = findViewById(R.id.buttonSaveChild);
        progressBar = findViewById(R.id.progressBar);
        Button buttonReturn = findViewById(R.id.buttonReturn);

        buttonReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editTextChildDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSaveChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChildAccount();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        editTextChildDob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveChildAccount() {
        String childName = editTextChildName.getText().toString().trim();
        String childDob = editTextChildDob.getText().toString().trim();
        String email = editTextChildEmail.getText().toString().trim();
        String password = editTextChildPassword.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        if (childName.isEmpty() || childDob.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser parentUser = mAuth.getCurrentUser();
        if (parentUser == null) {
            Toast.makeText(this, "Parent not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        final String parentUid = parentUser.getUid();

        progressBar.setVisibility(View.VISIBLE);
        buttonSaveChild.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser childUser = mAuth.getCurrentUser();
                        if (childUser != null) {
                            String childUid = childUser.getUid();
                            Map<String, Object> childData = new HashMap<>();
                            childData.put("accountType", "Child");
                            childData.put("name", childName);
                            childData.put("dob", childDob);
                            childData.put("notes", notes);

                            db.collection("users").document(childUid)
                                    .set(childData, com.google.firebase.firestore.SetOptions.merge())
                                    .addOnSuccessListener(aVoid -> {
                                        ChildAccountManager.linkChildToParent(parentUid, childUid, childName, new DatabaseManager.SuccessFailCallback() {
                                            @Override
                                            public void onSuccess() {
                                                mAuth.signOut();
                                                progressBar.setVisibility(View.GONE);
                                                buttonSaveChild.setEnabled(true);
                                                Toast.makeText(AddChildAccountActivity.this, "Child account created and linked successfully. Please log in again.", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(AddChildAccountActivity.this, LoginActivityView.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                progressBar.setVisibility(View.GONE);
                                                buttonSaveChild.setEnabled(true);
                                                Toast.makeText(AddChildAccountActivity.this, "Error linking child: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        buttonSaveChild.setEnabled(true);
                                        Toast.makeText(AddChildAccountActivity.this, "Error saving child data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            buttonSaveChild.setEnabled(true);
                            Toast.makeText(AddChildAccountActivity.this, "Failed to create child account", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        buttonSaveChild.setEnabled(true);
                        Toast.makeText(AddChildAccountActivity.this, "Error creating child account: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

