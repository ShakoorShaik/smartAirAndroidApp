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

import java.util.Calendar;

public class AddChildActivity extends AppCompatActivity {

    private EditText editTextChildName;
    private EditText editTextChildDob;
    private EditText editTextChildUsername;
    private EditText editTextChildPassword;
    private EditText editTextNotes;
    private Button buttonSaveChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

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

        // TODO: integrate with database to save child login data
        String message = "Child Profile Saved:\nName: " + childName + "\nDOB: " + childDob + "\nUsername: " + username + "\nNotes: " + notes;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();


        finish();
    }
}
