package com.example.smartair.parent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import utils.Medicine;
import utils.DatabaseManager;
import utils.PBManager;
import utils.ParentEmergency;
import utils.ZoneManager;
import utils.ChildAccountManager;
import utils.PEFManager;
import utils.MedicineManager;

public class ParentMedicineFragment extends Fragment {

    private static final String PREFS_NAME = "MedicineAlerts";
    private static final String PREF_ALERTS_SHOWN = "alerts_shown_";

    RecyclerView recyclerView;
    RecyclerView calendarRecyclerView;
    MedicineAdapter adapter;
    List<Medicine> medicineList;
    List<MedicineManager.MedicineWithId> medicinesWithIds;
    YearMonth currentDisplayMonth;
    TextView monthYearText;

    public ParentMedicineFragment() {
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
        if (recyclerView != null && getContext() != null) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        adapter = new MedicineAdapter(medicineList);
            adapter.setOnEditClickListener((position, medicine) -> {
                showEditDialog(position, medicine);
            });
            adapter.setOnDeleteClickListener((position, medicine) -> {
                showDeleteConfirmation(position, medicine);
            });
        recyclerView.setAdapter(adapter);
        }

        View calendarView = view.findViewById(R.id.medicineCalendarView);
        if (calendarView != null) {
            calendarRecyclerView = calendarView.findViewById(R.id.recyclerViewCalendar);
            monthYearText = calendarView.findViewById(R.id.textMonthYear);
            ImageButton buttonPrevMonth = calendarView.findViewById(R.id.buttonPrevMonth);
            ImageButton buttonNextMonth = calendarView.findViewById(R.id.buttonNextMonth);


            currentDisplayMonth = YearMonth.now();
            updateMonthDisplay();


            if (buttonPrevMonth != null) {
                buttonPrevMonth.setOnClickListener(v -> {
                    currentDisplayMonth = currentDisplayMonth.minusMonths(1);
                    updateMonthDisplay();
                    updateCalendarView();
                });
            }

            if (buttonNextMonth != null) {
                buttonNextMonth.setOnClickListener(v -> {
                    currentDisplayMonth = currentDisplayMonth.plusMonths(1);
                    updateMonthDisplay();
                    updateCalendarView();
                });
            }
        }

        FloatingActionButton addMed = view.findViewById(R.id.buttonAdd);
        Button editPEF = view.findViewById(R.id.buttonEditPEF);
        Button editPB = view.findViewById(R.id.buttonEditPB);

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
        
        editPB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPBsDialog();
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

        MedicineManager.loadMedicinesWithIds(new MedicineManager.MedicineListWithIdsCallback() {
            @Override
            public void onSuccess(List<MedicineManager.MedicineWithId> medicines) {
                medicinesWithIds = medicines;
                if (medicineList != null && adapter != null) {
                    medicineList.clear();

                    List<MedicineManager.MedicineWithId> toRemove = new ArrayList<>();
                    for (MedicineManager.MedicineWithId mwi : medicines) {
                        Medicine medicine = mwi.getMedicine();
                        if (medicine.getAmountLeft() <= 0) {

                            toRemove.add(mwi);
                        } else {
                            medicineList.add(medicine);
                        }
                    }

                    for (MedicineManager.MedicineWithId mwi : toRemove) {
                        MedicineManager.deleteMedicine(mwi.getDocumentId(), new DatabaseManager.SuccessFailCallback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    if (getContext() != null) {
                        checkMedicineAlerts();
                        scheduleExpiryReminders();
                    }
                    updateCalendarView();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (medicineList != null && adapter != null) {
                    medicineList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void showDeleteConfirmation(int position, Medicine medicine) {
        if (getContext() == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete " + medicine.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String documentId = null;
                    if (medicinesWithIds != null && medicineList != null && position < medicineList.size()) {
                        Medicine medicineToDelete = medicineList.get(position);
                        for (MedicineManager.MedicineWithId mwi : medicinesWithIds) {
                            Medicine m = mwi.getMedicine();
                            if (m.getName().equals(medicineToDelete.getName()) &&
                                m.getExpiry().equals(medicineToDelete.getExpiry()) &&
                                ((m.getChildUid() == null && medicineToDelete.getChildUid() == null) ||
                                 (m.getChildUid() != null && m.getChildUid().equals(medicineToDelete.getChildUid())))) {
                                documentId = mwi.getDocumentId();
                                break;
                            }
                        }
                    }
                    
                    if (documentId != null) {
                        MedicineManager.deleteMedicine(documentId, new DatabaseManager.SuccessFailCallback() {
                            @Override
                            public void onSuccess() {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Medicine deleted", Toast.LENGTH_SHORT).show();
                                }
                                loadMedicines();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        loadMedicines();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void scheduleExpiryReminders() {
        if (medicineList == null || getContext() == null) return;

        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);

        for (Medicine medicine : medicineList) {
            LocalDate expiry = medicine.getExpiry();
            String reminderKey3Days = "reminder_3days_" + medicine.getName() + "_" + expiry.toString();
            String reminderKeyToday = "reminder_today_" + medicine.getName() + "_" + expiry.toString();

            if (expiry.equals(threeDaysFromNow) && !prefs.getBoolean(reminderKey3Days, false)) {
                showExpiryReminder(medicine, 3);
                prefs.edit().putBoolean(reminderKey3Days, true).apply();
            }

            if (expiry.equals(today) && !prefs.getBoolean(reminderKeyToday, false)) {
                showExpiryReminder(medicine, 0);
                prefs.edit().putBoolean(reminderKeyToday, true).apply();
            }
        }
    }

    private void showExpiryReminder(Medicine medicine, int daysBefore) {
        if (getContext() == null) return;
        
        String message;
        if (daysBefore == 0) {
            message = "⚠️ " + medicine.getName() + " expires TODAY!";
        } else {
            message = "⚠️ " + medicine.getName() + " expires in " + daysBefore + " days!";
        }

        if (medicine.getChildName() != null) {
            message += " (Child: " + medicine.getChildName() + ")";
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Expiry Reminder")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateMonthDisplay() {
        if (monthYearText != null && currentDisplayMonth != null) {
            monthYearText.setText(currentDisplayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
    }

    private void updateCalendarView() {
        if (calendarRecyclerView == null || medicineList == null || currentDisplayMonth == null || getContext() == null) return;

        Map<LocalDate, List<Medicine>> expiryMap = new HashMap<>();
        for (Medicine medicine : medicineList) {
            if (medicine.getAmountLeft() > 0) {
                LocalDate expiry = medicine.getExpiry();
                if (!expiryMap.containsKey(expiry)) {
                    expiryMap.put(expiry, new ArrayList<>());
                }
                expiryMap.get(expiry).add(medicine);
            }
        }

        MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(currentDisplayMonth, expiryMap, new HashMap<>());

        androidx.recyclerview.widget.GridLayoutManager layoutManager =
                new androidx.recyclerview.widget.GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void checkMedicineAlerts() {
        if (medicineList == null || medicineList.isEmpty() || getContext() == null) {
            return;
        }

        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, 0);
        LocalDate today = LocalDate.now();
        List<String> alerts = new ArrayList<>();
        List<String> alertKeys = new ArrayList<>();

        for (Medicine medicine : medicineList) {
            String medicineKey = medicine.getName() + "_" + medicine.getExpiry().toString();

            if (medicine.getExpiry().isBefore(today)) {
                String expiredKey = PREF_ALERTS_SHOWN + medicineKey + "_expired";
                if (!prefs.getBoolean(expiredKey, false)) {
                    alerts.add("⚠️ " + medicine.getName() + " has EXPIRED (Expiry: " +
                            medicine.getExpiry().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + ")");
                    alertKeys.add(expiredKey);
                }
            }
            else if (medicine.getAmountLeft() <= 20) {
                String lowKey = PREF_ALERTS_SHOWN + medicineKey + "_low";
                if (!prefs.getBoolean(lowKey, false)) {
                    alerts.add("⚠️ " + medicine.getName() + " is LOW (" + medicine.getAmountLeft() + "% left)");
                    alertKeys.add(lowKey);
                }
            }
        }

        if (!alerts.isEmpty()) {
            StringBuilder alertMessage = new StringBuilder();
            for (int i = 0; i < alerts.size(); i++) {
                alertMessage.append(alerts.get(i));
                if (i < alerts.size() - 1) {
                    alertMessage.append("\n\n");
                }
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("⚠️ Medicine Alerts")
                    .setMessage(alertMessage.toString())
                    .setPositiveButton("OK", (dialog, which) -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        for (String key : alertKeys) {
                            editor.putBoolean(key, true);
                        }
                        editor.apply();
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    private void showEditDialog(int position, Medicine medicine) {
        if (getContext() == null) return;
        
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_edit_medicine, null);

        EditText editAmountLeft = dialogView.findViewById(R.id.editAmountLeft);
        editAmountLeft.setText(String.valueOf(medicine.getAmountLeft()));

        TextView textInitialAmount = dialogView.findViewById(R.id.textInitialAmount);
        if (textInitialAmount != null) {
            textInitialAmount.setText("Initial Amount: " + medicine.getAmount() + "%");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Edit " + medicine.getName())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        String amountLeftStr = editAmountLeft.getText().toString().trim();
                        if (amountLeftStr.isEmpty()) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Please enter amount left", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }
                        int amountLeft = Integer.parseInt(amountLeftStr);
                        if (amountLeft < 0 || amountLeft > 100) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Amount left must be between 0-100%", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        medicine.setAmountLeft(amountLeft);

                        final boolean shouldShowLowWarning = amountLeft > 0 && amountLeft <= 20 && getContext() != null;
                        String medicineKey = medicine.getName() + "_" + medicine.getExpiry().toString();
                        String lowKey = PREF_ALERTS_SHOWN + medicineKey + "_low_edit";
                        SharedPreferences prefs = getContext() != null ? getContext().getSharedPreferences(PREFS_NAME, 0) : null;
                        final boolean hasShownLowWarning = prefs != null && prefs.getBoolean(lowKey, false);

                        if (amountLeft <= 0) {
                            String documentId = null;
                            if (medicinesWithIds != null && medicineList != null && position < medicineList.size()) {
                                Medicine medicineToDelete = medicineList.get(position);
                                for (MedicineManager.MedicineWithId mwi : medicinesWithIds) {
                                    Medicine m = mwi.getMedicine();
                                    if (m.getName().equals(medicineToDelete.getName()) &&
                                        m.getExpiry().equals(medicineToDelete.getExpiry()) &&
                                        ((m.getChildUid() == null && medicineToDelete.getChildUid() == null) ||
                                         (m.getChildUid() != null && m.getChildUid().equals(medicineToDelete.getChildUid())))) {
                                        documentId = mwi.getDocumentId();
                                        break;
                                    }
                                }
                            }
                            
                            if (documentId != null) {
                                MedicineManager.deleteMedicine(documentId, new DatabaseManager.SuccessFailCallback() {
                                    @Override
                                    public void onSuccess() {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Medicine removed (0% left)", Toast.LENGTH_SHORT).show();
                                        }
                                        loadMedicines();
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Error removing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                loadMedicines();
                            }
                        } else {
                            if (shouldShowLowWarning && !hasShownLowWarning && prefs != null) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("⚠️ Low Medication Alert")
                                        .setMessage(medicine.getName() + " is LOW (" + amountLeft + "% left)")
                                        .setPositiveButton("OK", (dialog2, which2) -> {
                                            prefs.edit().putBoolean(lowKey, true).apply();
                                            saveMedicineUpdate(medicine, position);
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            } else {
                                saveMedicineUpdate(medicine, position);
                            }
                        }
                    } catch (NumberFormatException e) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveMedicineUpdate(Medicine medicine, int position) {
        String documentId = null;
        if (medicinesWithIds != null && medicineList != null && position < medicineList.size()) {
            Medicine medicineToUpdate = medicineList.get(position);
            for (MedicineManager.MedicineWithId mwi : medicinesWithIds) {
                Medicine m = mwi.getMedicine();
                if (m.getName().equals(medicineToUpdate.getName()) &&
                    m.getExpiry().equals(medicineToUpdate.getExpiry()) &&
                    ((m.getChildUid() == null && medicineToUpdate.getChildUid() == null) ||
                     (m.getChildUid() != null && m.getChildUid().equals(medicineToUpdate.getChildUid())))) {
                    documentId = mwi.getDocumentId();
                    break;
                }
            }
        }

        if (documentId != null) {
            MedicineManager.updateMedicine(documentId, medicine, new DatabaseManager.SuccessFailCallback() {
            @Override
            public void onSuccess() {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                }
                if (adapter != null) {
                    adapter.notifyItemChanged(position);
                }
                if (getContext() != null) {
                    checkMedicineAlerts();
                    scheduleExpiryReminders();
                }
                updateCalendarView();
                loadMedicines();
            }

                @Override
                public void onFailure(Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            MedicineManager.saveMedicine(medicine, new DatabaseManager.SuccessFailCallback() {
                @Override
                public void onSuccess() {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                    }
                    if (adapter != null) {
                        adapter.notifyItemChanged(position);
                    }
                    if (getContext() != null) {
                        checkMedicineAlerts();
                        scheduleExpiryReminders();
                    }
                    updateCalendarView();
                    loadMedicines();
                }

                @Override
                public void onFailure(Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showAddDialog() {
        if (getContext() == null) return;

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (getContext() == null) return;

                if (children == null || children.isEmpty()) {
                    Toast.makeText(getContext(), "No children linked. Please add a child first.", Toast.LENGTH_SHORT).show();
                    return;
                }

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_add_medicine_dialog, null);

                Spinner spinnerChild = dialogView.findViewById(R.id.spinnerChild);
        EditText medName = dialogView.findViewById(R.id.editMedName);
        EditText medAmt = dialogView.findViewById(R.id.editMedAmt);
                EditText medPurchaseDate = dialogView.findViewById(R.id.editMedPurchaseDate);
        EditText medExp = dialogView.findViewById(R.id.editMedExp);
        ImageView icon1 = dialogView.findViewById(R.id.imagePill);
        ImageView icon2 = dialogView.findViewById(R.id.imageSyrup);
        ImageView icon3 = dialogView.findViewById(R.id.imageDropper);
        ImageView icon4 = dialogView.findViewById(R.id.imageInjection);
        ImageView icon5 = dialogView.findViewById(R.id.imageInhaler);

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

                ArrayAdapter<String> childAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, childNames);
                childAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerChild.setAdapter(childAdapter);

        final int[] currentIconId = {R.drawable.pill_img};
                final ImageView[] selectedIcon = {icon1};

                icon1.setBackgroundResource(android.R.drawable.btn_default);
                icon1.setAlpha(1.0f);
                icon2.setAlpha(0.5f);
                icon3.setAlpha(0.5f);
                icon4.setAlpha(0.5f);
                icon5.setAlpha(0.5f);

        View.OnClickListener iconClickListener = v -> {
                    icon1.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                    icon2.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                    icon3.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                    icon4.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                    icon5.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                    icon1.setAlpha(0.5f);
                    icon2.setAlpha(0.5f);
                    icon3.setAlpha(0.5f);
                    icon4.setAlpha(0.5f);
                    icon5.setAlpha(0.5f);

                    if(v.getId() == R.id.imagePill) {
                        currentIconId[0] = R.drawable.pill_img;
                        selectedIcon[0] = icon1;
                        icon1.setBackgroundResource(android.R.drawable.btn_default);
                        icon1.setAlpha(1.0f);
                    } else if(v.getId() == R.id.imageSyrup) {
                        currentIconId[0] = R.drawable.syrup_img;
                        selectedIcon[0] = icon2;
                        icon2.setBackgroundResource(android.R.drawable.btn_default);
                        icon2.setAlpha(1.0f);
                    } else if(v.getId() == R.id.imageDropper) {
                        currentIconId[0] = R.drawable.dropper_img;
                        selectedIcon[0] = icon3;
                        icon3.setBackgroundResource(android.R.drawable.btn_default);
                        icon3.setAlpha(1.0f);
                    } else if(v.getId() == R.id.imageInjection) {
                        currentIconId[0] = R.drawable.syringe_img;
                        selectedIcon[0] = icon4;
                        icon4.setBackgroundResource(android.R.drawable.btn_default);
                        icon4.setAlpha(1.0f);
                    } else if(v.getId() == R.id.imageInhaler) {
                        currentIconId[0] = R.drawable.inhaler_img;
                        selectedIcon[0] = icon5;
                        icon5.setBackgroundResource(android.R.drawable.btn_default);
                        icon5.setAlpha(1.0f);
                    }
        };

        icon1.setOnClickListener(iconClickListener);
        icon2.setOnClickListener(iconClickListener);
        icon3.setOnClickListener(iconClickListener);
        icon4.setOnClickListener(iconClickListener);
        icon5.setOnClickListener(iconClickListener);

                Calendar calendar = Calendar.getInstance();
                medPurchaseDate.setOnClickListener(v -> {
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int selectedYear,
                                                      int monthOfYear, int dayOfMonth) {
                                    String formattedDate = String.format(Locale.getDefault(),
                                            "%04d-%02d-%02d", selectedYear, monthOfYear + 1, dayOfMonth);
                                    medPurchaseDate.setText(formattedDate);
                                }
                            }, year, month, day);
                    datePickerDialog.show();
                });

                medExp.setOnClickListener(v -> {
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int selectedYear,
                                                      int monthOfYear, int dayOfMonth) {
                                    String formattedDate = String.format(Locale.getDefault(),
                                            "%04d-%02d-%02d", selectedYear, monthOfYear + 1, dayOfMonth);
                                    medExp.setText(formattedDate);
                                }
                            }, year, month, day);
                    datePickerDialog.show();
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            new AlertDialog.Builder(getContext()).setTitle("Add New Medication").setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String name = medName.getText().toString().trim();
                        String amtStr = medAmt.getText().toString().trim();
                                String purchaseDateStr = medPurchaseDate.getText().toString().trim();
                        String dateStr = medExp.getText().toString().trim();

                        if(name.isEmpty()) {
                            Toast.makeText(getContext(),
                                    "Enter valid Name!", Toast.LENGTH_LONG).show();
                            return;
                        }

                                int selectedPosition = spinnerChild.getSelectedItemPosition();
                                if (selectedPosition < 0 || selectedPosition >= childUids.size()) {
                                    Toast.makeText(getContext(),
                                            "Please select a child", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String childUid = childUids.get(selectedPosition);
                                String childName = childNames.get(selectedPosition);

                        int amt;
                        LocalDate date;
                                LocalDate purchaseDate = null;
                        try {
                                    if (amtStr.isEmpty()) {
                                        amt = 100;
                                    } else {
                            amt = Integer.parseInt(amtStr);
                                        if (amt <= 0 || amt > 100) {
                                            if (amt == 0 && getContext() != null) {
                                                Toast.makeText(getContext(),
                                                        "Initial amount cannot be 0%. Medicine not added.", Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                throw new NumberFormatException();
                            }
                                    }
                                    if (dateStr.isEmpty()) {
                                        if (getContext() != null) {
                                            Toast.makeText(getContext(), "Please select an expiry date",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        return;
                                    }
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            date = LocalDate.parse(dateStr, formatter);
                                    if (!purchaseDateStr.isEmpty()) {
                                        purchaseDate = LocalDate.parse(purchaseDateStr, formatter);
                                    }
                        }
                        catch(NumberFormatException ne) {
                            if (getContext() != null) {
                            Toast.makeText(getContext(),
                                                "Enter valid amount (0-100)!", Toast.LENGTH_LONG).show();
                            }
                            return;
                        }
                        catch (DateTimeParseException de){
                            if (getContext() != null) {
                            Toast.makeText(getContext(), "Enter valid date!",
                                    Toast.LENGTH_LONG).show();
                            }
                            return;
                        }

                                Medicine med = new Medicine(name, amt, date, currentIconId[0], childUid, childName, purchaseDate, amt);
                                MedicineManager.saveMedicine(med, new DatabaseManager.SuccessFailCallback() {
                            @Override
                            public void onSuccess() {
                                if (getContext() != null) {
                                Toast.makeText(getContext(),
                                        "Added successfully", Toast.LENGTH_LONG).show();
                                }

                                if (medicineList != null) {
                                medicineList.add(med);
                                }
                                if (adapter != null) {
                                adapter.notifyDataSetChanged();
                                }
                                if (getContext() != null) {
                                    checkMedicineAlerts();
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (getContext() != null) {
                                Toast.makeText(getContext(),
                                            "Error:" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }).setNegativeButton("Cancel", null)
                    .show();
        }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error loading children: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPEFEntryDialog() {
        if (getContext() == null) return;

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (getContext() == null) return;

                if (children == null || children.isEmpty()) {
                    Toast.makeText(getContext(), "No children linked. Please add a child first.", Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_pef_entry, null);

                Spinner spinnerChild = dialogView.findViewById(R.id.spinnerChild);
                EditText editPEFValue = dialogView.findViewById(R.id.editPEFValue);
                EditText editPEFDate = dialogView.findViewById(R.id.editPEFDate);

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                editPEFDate.setText(dateFormat.format(calendar.getTime()));

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
                                    Calendar selectedDate = Calendar.getInstance();
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        selectedDate.setTime(Objects.requireNonNull(sdf.parse(dateStr)));
                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    long timestamp = selectedDate.getTimeInMillis();

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

                ZoneManager.logZoneChange(getContext(), childUid, zone, pefValue, dateStr, new DatabaseManager.SuccessFailCallback() {
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

                // Refresh other fragments that display PEF/zone data
                refreshRelatedFragments();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(),
                        "Error saving PEF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshRelatedFragments() {
        if (getActivity() == null) return;
        
        androidx.fragment.app.FragmentManager fm = getActivity().getSupportFragmentManager();
        
        // Check the currently displayed fragment and refresh it
        Fragment currentFragment = fm.findFragmentById(R.id.fragmentContainer);
        if (currentFragment instanceof ParentChildrenFragment) {
            ((ParentChildrenFragment) currentFragment).loadChildren();
        } else if (currentFragment instanceof ParentHomeFragment) {
            ((ParentHomeFragment) currentFragment).refreshZoneInfo();
        }
        
        // Also check all fragments in case they're still in memory
        List<Fragment> fragments = fm.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment != currentFragment) {
                if (fragment instanceof ParentChildrenFragment) {
                    ((ParentChildrenFragment) fragment).loadChildren();
                } else if (fragment instanceof ParentHomeFragment) {
                    ((ParentHomeFragment) fragment).refreshZoneInfo();
                }
            }
        }
    }

    private void showEditPBsDialog() {
        if (getContext() == null) return;

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (getContext() == null) return;

                if (children == null || children.isEmpty()) {
                    Toast.makeText(getContext(), "No children linked yet", Toast.LENGTH_SHORT).show();
                    return;
                }

                View dialogView = LayoutInflater.from(getContext())
                        .inflate(R.layout.dialog_edit_pbs, null);

                RecyclerView recyclerViewPBs = dialogView.findViewById(R.id.recyclerViewPBs);
                recyclerViewPBs.setLayoutManager(new LinearLayoutManager(getContext()));
                PBEditAdapter adapter = new PBEditAdapter(children);
                recyclerViewPBs.setAdapter(adapter);

                new AlertDialog.Builder(getContext())
                        .setTitle("Edit Personal Best (PB) Values")
                        .setView(dialogView)
                        .setPositiveButton("Save All", (dialog, which) -> {
                            final int[] savedCount = {0};
                            final int[] errorCount = {0};
                            for (int i = 0; i < adapter.getItemCount(); i++) {
                                String childUid = adapter.getChildUid(i);
                                if (childUid != null) {
                                    int pbValue = adapter.getPBValue(i, recyclerViewPBs);
                                    if (pbValue > 0) {
                                        PBManager.setPB(childUid, pbValue, new DatabaseManager.SuccessFailCallback() {
                                            @Override
                                            public void onSuccess() {
                                                savedCount[0]++;
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                errorCount[0]++;
                                            }
                                        });
                                    }
                                }
                            }
                            recyclerViewPBs.postDelayed(() -> {
                                if (errorCount[0] == 0) {
                                    Toast.makeText(getContext(), "PB values saved successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Some PB values could not be saved", Toast.LENGTH_SHORT).show();
                                }
                                // Refresh related fragments after PB values are updated
                                refreshRelatedFragments();
                            }, 500);
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
}

