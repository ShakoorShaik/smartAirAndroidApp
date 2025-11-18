package com.example.smartair.parent;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartair.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddChildActivity extends AppCompatActivity {

    private EditText editTextChildName;
    private EditText editTextChildDob;
    private EditText editTextChildUsername;
    private EditText editTextChildPassword;
    private EditText editTextNotes;
    private Button buttonSaveChild;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        editTextChildName = findViewById(R.id.editTextChildName);
        editTextChildDob = findViewById(R.id.editTextChildDob);
        editTextNotes = findViewById(R.id.editTextNotes);
        editTextChildUsername = findViewById(R.id.editTextChildUsername);
        editTextChildPassword = findViewById(R.id.editTextChildPassword);
        buttonSaveChild = findViewById(R.id.buttonSaveChild);

        editTextChildDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        buttonSaveChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChildProfile();
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
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        editTextChildDob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveChildProfile() {
        // Get all data
        String childName = editTextChildName.getText().toString().trim();
        String childDob = editTextChildDob.getText().toString().trim();
        String username = editTextChildUsername.getText().toString().trim();
        String password = editTextChildPassword.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();

        // Data validation
        if (childName.isEmpty() || childDob.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String parentUid = mAuth.getCurrentUser().getUid();

        Map<String, Object> child = new HashMap<>();
        child.put("name", childName);
        child.put("dob", childDob);
        child.put("username", username);
        child.put("password", password);
        child.put("notes", notes);
        System.out.println(child);
        System.out.println(parentUid);
        db.collection("users").document(parentUid)
                .update("children", FieldValue.arrayUnion(child))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddChildActivity.this, "Child profile saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddChildActivity.this, "Error saving child profile", Toast.LENGTH_SHORT).show();
                });
    }
}
