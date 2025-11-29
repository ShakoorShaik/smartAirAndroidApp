package com.example.smartair.parent;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

public class ParentExportHistoryActivity extends AppCompatActivity {

    private Spinner spinnerChildSelector;
    private RadioGroup radioGroupFormat;
    private RadioButton radioCsv;
    private RadioButton radioPdf;
    private Button buttonExport;
    private Button buttonReturn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_export_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Export History");
        }

        spinnerChildSelector = findViewById(R.id.spinnerChildSelector);
        radioGroupFormat = findViewById(R.id.radioGroupFormat);
        radioCsv = findViewById(R.id.radioCsv);
        radioPdf = findViewById(R.id.radioPdf);
        buttonExport = findViewById(R.id.buttonExport);
        buttonReturn = findViewById(R.id.buttonReturn);
        buttonReturn.setOnClickListener(v -> finish());

        // TODO: Add children list to dropdown+ "All Children" option
        // TODO: implement CSV/PDF generation
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
