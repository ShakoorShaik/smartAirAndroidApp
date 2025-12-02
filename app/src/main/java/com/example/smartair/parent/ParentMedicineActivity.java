package com.example.smartair.parent;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import utils.Medicine;
import utils.DatabaseManager;

public class ParentMedicineActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    MedicineAdapter adapter;
    List<Medicine> medicineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_medicine);

        medicineList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerMeds);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new MedicineAdapter(medicineList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addMed = findViewById(R.id.buttonAdd);
        Button editPEF = findViewById(R.id.buttonEditPEF);

        addMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        editPEF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.item_add_medicine_dialog, null);

        EditText medName = dialogView.findViewById(R.id.editMedName);
        EditText medAmt = dialogView.findViewById(R.id.editMedAmt);
        EditText medExp = dialogView.findViewById(R.id.editMedExp);
        ImageView icon1 = dialogView.findViewById(R.id.imagePill);
        ImageView icon2 = dialogView.findViewById(R.id.imageSyrup);
        ImageView icon3 = dialogView.findViewById(R.id.imageDropper);
        ImageView icon4 = dialogView.findViewById(R.id.imageInjection);
        ImageView icon5 = dialogView.findViewById(R.id.imageInhaler);
        final int[] currentIconId = {R.drawable.pill_img};

        View.OnClickListener iconClickListener = v -> {
            if(v.getId() == R.id.imagePill) { currentIconId[0] = R.drawable.pill_img; }
            else if(v.getId() == R.id.imageSyrup) { currentIconId[0] = R.drawable.pill_img; }
            else if(v.getId() == R.id.imageDropper) { currentIconId[0] = R.drawable.pill_img; }
            else if(v.getId() == R.id.imageInjection) { currentIconId[0] = R.drawable.pill_img; }
            else if(v.getId() == R.id.imageInhaler) { currentIconId[0] = R.drawable.pill_img; }
        };

        icon1.setOnClickListener(iconClickListener);
        icon2.setOnClickListener(iconClickListener);
        icon3.setOnClickListener(iconClickListener);
        icon4.setOnClickListener(iconClickListener);
        icon5.setOnClickListener(iconClickListener);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new AlertDialog.Builder(this).setTitle("Add New Medication").setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> {

                        String name = medName.getText().toString().trim();
                        String amtStr = medAmt.getText().toString().trim();
                        DateTimeFormatter formatter = null;
                        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String dateStr = medExp.getText().toString().trim();

                        if(name.isEmpty()) {
                            Toast.makeText(ParentMedicineActivity.this,
                                    "Enter valid Name!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int amt;
                        LocalDate date;
                        try {

                            amt = Integer.parseInt(amtStr);
                            if (amt < 0 || amt > 100) {
                                throw new NumberFormatException();
                            }

                            date = LocalDate.parse(dateStr, formatter);

                        }
                        catch(NumberFormatException ne) {
                            Toast.makeText(ParentMedicineActivity.this,
                                    "Enter valid amount!", Toast.LENGTH_LONG).show();
                            return;
                        }


                        catch (DateTimeParseException de){
                            Toast.makeText(ParentMedicineActivity.this, "Enter valid date!",
                                    Toast.LENGTH_LONG).show();
                            return;

                        }

                        Medicine med = new Medicine(name, amt, date, currentIconId[0]);
                        DatabaseManager.writeData("Medicine", med, new DatabaseManager.SuccessFailCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ParentMedicineActivity.this,
                                        "Added successfully", Toast.LENGTH_LONG).show();

                                medicineList.add(med);

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(ParentMedicineActivity.this,
                                        "Error:" + e, Toast.LENGTH_LONG).show();
                            }
                        });
                    }).setNegativeButton("Cancel", null)
                    .show();
        }

    }


}




