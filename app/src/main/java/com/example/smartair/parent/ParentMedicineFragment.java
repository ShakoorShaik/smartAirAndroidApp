package com.example.smartair.parent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import utils.Medicine;
import utils.DatabaseManager;
import utils.PBManager;
import utils.ParentEmergency;
import utils.ZoneManager;
import utils.ChildAccountManager;
import utils.PEFManager;

public class ParentMedicineFragment extends Fragment {

    RecyclerView recyclerView;
    MedicineAdapter adapter;
    List<Medicine> medicineList;

    public ParentMedicineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart(){
        super.onStart();
        ParentEmergency.listenEmergency(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_medicine, container, false);

        medicineList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerMeds);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new MedicineAdapter(medicineList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton addMed = view.findViewById(R.id.buttonAdd);
        Button editPEF = view.findViewById(R.id.buttonEditPEF);

        addMed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        editPEF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPEFEntryDialog();
            }
        });

        loadMedicines();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMedicines();
    }

    private void loadMedicines() {
        // Load medicines from database
        DatabaseManager.getData("Medicine", new DatabaseManager.DataSuccessFailCallback() {
            @Override
            public void onSuccess(String data) {
                // Medicine data is stored as a single object, not a list
                // This would need to be adjusted based on actual data structure
                // For now, we'll keep the list empty and let users add medicines
            }

            @Override
            public void onFailure(Exception e) {
                // Medicine data doesn't exist yet, which is fine
            }
        });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(getContext())
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
            new AlertDialog.Builder(getContext()).setTitle("Add New Medication").setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String name = medName.getText().toString().trim();
                        String amtStr = medAmt.getText().toString().trim();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        String dateStr = medExp.getText().toString().trim();

                        if(name.isEmpty()) {
                            Toast.makeText(getContext(),
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
                            Toast.makeText(getContext(),
                                    "Enter valid amount!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        catch (DateTimeParseException de){
                            Toast.makeText(getContext(), "Enter valid date!",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Medicine med = new Medicine(name, amt, date, currentIconId[0]);
                        DatabaseManager.writeData("Medicine", med, new DatabaseManager.SuccessFailCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(),
                                        "Added successfully", Toast.LENGTH_LONG).show();

                                medicineList.add(med);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(getContext(),
                                        "Error:" + e, Toast.LENGTH_LONG).show();
                            }
                        });
                    }).setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void showPEFEntryDialog() {
        // First, load children list
        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (children == null || children.isEmpty()) {
                    Toast.makeText(getContext(), "No children linked. Please add a child first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create dialog view
                View dialogView = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_pef_entry, null);

                Spinner spinnerChild = dialogView.findViewById(R.id.spinnerChild);
                EditText editPEFValue = dialogView.findViewById(R.id.editPEFValue);
                EditText editPEFDate = dialogView.findViewById(R.id.editPEFDate);

                // Set default date to today
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                editPEFDate.setText(dateFormat.format(calendar.getTime()));

                // Date picker for PEF date
                editPEFDate.setOnClickListener(v -> {
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int selectedYear,
                                                      int monthOfYear, int dayOfMonth) {
                                    Calendar selectedDate = Calendar.getInstance();
                                    selectedDate.set(selectedYear, monthOfYear, dayOfMonth);
                                    editPEFDate.setText(dateFormat.format(selectedDate.getTime()));
                                }
                            }, year, month, day);
                    datePickerDialog.show();
                });

                // Populate spinner with children
                List<String> childNames = new ArrayList<>();
                List<String> childUids = new ArrayList<>();
                for (Map<String, Object> child : children) {
                    String name = (String) child.get("name");
                    String uid = (String) child.get("uid");
                    if (name != null && uid != null) {
                        childNames.add(name);
                        childUids.add(uid);
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, childNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerChild.setAdapter(adapter);

                new AlertDialog.Builder(getContext())
                        .setTitle("Enter PEF Reading")
                        .setView(dialogView)
                        .setPositiveButton("Save", (dialog, which) -> {
                            String pefStr = editPEFValue.getText().toString().trim();
                            String dateStr = editPEFDate.getText().toString().trim();

                            if (pefStr.isEmpty()) {
                                Toast.makeText(getContext(), "Please enter a PEF value", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (dateStr.isEmpty()) {
                                Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            try {
                                int pefValue = Integer.parseInt(pefStr);
                                if (pefValue < 0 || pefValue > 1000) {
                                    Toast.makeText(getContext(), "Please enter a valid PEF value (0-1000)", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int selectedPosition = spinnerChild.getSelectedItemPosition();
                                if (selectedPosition >= 0 && selectedPosition < childUids.size()) {
                                    String childUid = childUids.get(selectedPosition);
                                    String childName = childNames.get(selectedPosition);

                                    // Parse the date and create timestamp
                                    Calendar selectedDate = Calendar.getInstance();
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        selectedDate.setTime(sdf.parse(dateStr));
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    long timestamp = selectedDate.getTimeInMillis();

                                    // First, check if PB exists, if not, set it to highest PEF
                                    PBManager.getPB(childUid, new PBManager.PBCallback() {
                                        @Override
                                        public void onSuccess(Integer currentPB) {
                                            if (currentPB == null) {
                                                PEFManager.getHighestPEF(childUid, new PEFManager.PEFCallback() {
                                                    @Override
                                                    public void onSuccess(Integer highestPEF) {
                                                        int pbToSet = (highestPEF != null && highestPEF > pefValue) ? highestPEF : pefValue;
                                                        PBManager.setPB(childUid, pbToSet, new DatabaseManager.SuccessFailCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                savePEFAndCheckZone(childUid, childName, pefValue, dateStr, timestamp, pbToSet);
                                                            }

                                                            @Override
                                                            public void onFailure(Exception e) {
                                                                savePEFAndCheckZone(childUid, childName, pefValue, dateStr, timestamp, pefValue);
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        PBManager.setPB(childUid, pefValue, new DatabaseManager.SuccessFailCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                savePEFAndCheckZone(childUid, childName, pefValue, dateStr, timestamp, pefValue);
                                                            }

                                                            @Override
                                                            public void onFailure(Exception e2) {
                                                                savePEFAndCheckZone(childUid, childName, pefValue, dateStr, timestamp, pefValue);
                                                            }
                                                        });
                                                    }
                                                });
                                            } else {
                                                savePEFAndCheckZone(childUid, childName, pefValue, dateStr, timestamp, currentPB);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            savePEFAndCheckZone(childUid, childName, pefValue, dateStr, timestamp, pefValue);
                                        }
                                    });
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePEFAndCheckZone(String childUid, String childName, int pefValue, String dateStr, long timestamp, int personalBest) {
        PEFManager.savePEFReading(childUid, pefValue, dateStr, timestamp, new DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                ZoneManager.Zone zone = ZoneManager.calculateZone(pefValue, personalBest);

                ZoneManager.logZoneChange(childUid, zone, pefValue, dateStr, new DatabaseManager.SuccessFailCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });

                if (zone == ZoneManager.Zone.RED) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("⚠️ Red Zone Alert")
                            .setMessage(childName + " has entered the RED ZONE!\n\n" +
                                    "PEF: " + pefValue + " L/min\n" +
                                    "PB: " + personalBest + " L/min\n" +
                                    "Percentage: " + String.format("%.1f", (double) pefValue / personalBest * 100) + "%\n\n" +
                                    "Please take immediate action.")
                            .setPositiveButton("OK", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                Toast.makeText(getContext(),
                        "PEF reading saved for " + childName, Toast.LENGTH_SHORT).show();

                if (getActivity() != null) {
                    androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
                    Fragment childrenFragment = fm.findFragmentById(R.id.fragmentContainer);
                    if (childrenFragment instanceof ParentChildrenFragment) {
                        ((ParentChildrenFragment) childrenFragment).loadChildren();
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(),
                        "Error saving PEF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

