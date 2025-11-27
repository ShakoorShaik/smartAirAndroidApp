package com.example.smartair.parent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import utils.Medicine;
import utils.MedicineManager;
import utils.TriageHistoryManager;
import utils.ZoneHistoryManager;

import utils.ParentEmergency;

public class ParentHomeFragment extends Fragment {

    private RecyclerView calendarRecyclerView;
    private TextView monthYearText;
    private YearMonth currentDisplayMonth;
    private TextView lastRescue;
    private Date lastRescueDate = null;

    public ParentHomeFragment() {
    }

    @Override
    public void onStart(){
        super.onStart();
        ParentEmergency.listenEmergency(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ParentEmergency.listenEmergency(this);
        View view = inflater.inflate(R.layout.fragment_parent_home, container, false);
        lastRescue = view.findViewById(R.id.last_rescue_date);

        View calendarView = view.findViewById(R.id.mainCalendarView);
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
                    loadCalendarData();
                });
            }


            if (buttonNextMonth != null) {
                buttonNextMonth.setOnClickListener(v -> {
                    currentDisplayMonth = currentDisplayMonth.plusMonths(1);
                    updateMonthDisplay();
                    loadCalendarData();
                });
            }

            loadCalendarData();
        }

        getMostRecentRescue();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (calendarRecyclerView != null) {
            loadCalendarData();
        }
    }

    private void updateMonthDisplay() {
        if (monthYearText != null && currentDisplayMonth != null) {
            monthYearText.setText(currentDisplayMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
    }

    private void loadCalendarData() {
        MedicineManager.loadMedicines(new MedicineManager.MedicineListCallback() {
            @Override
            public void onSuccess(List<Medicine> medicines) {
                ZoneHistoryManager.loadRedZoneDates(getContext(), new ZoneHistoryManager.RedZoneDatesCallback() {
                    @Override
                    public void onSuccess(Map<LocalDate, Boolean> redZoneMap) {
                        TriageHistoryManager.loadTriageDates(getContext(), new TriageHistoryManager.TriageDatesCallback() {
                            @Override
                            public void onSuccess(Map<LocalDate, Boolean> triageMap) {
                                Map<LocalDate, List<Medicine>> expiryMap = new HashMap<>();
                                for (Medicine medicine : medicines) {
                                    if (medicine.getAmountLeft() > 0) {
                                        LocalDate expiry = medicine.getExpiry();
                                        if (!expiryMap.containsKey(expiry)) {
                                            expiryMap.put(expiry, new ArrayList<>());
                                        }
                                        Objects.requireNonNull(expiryMap.get(expiry)).add(medicine);
                                    }
                                }

                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, expiryMap, redZoneMap, triageMap);

                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Map<LocalDate, List<Medicine>> expiryMap = new HashMap<>();
                                for (Medicine medicine : medicines) {
                                    if (medicine.getAmountLeft() > 0) {
                                        LocalDate expiry = medicine.getExpiry();
                                        if (!expiryMap.containsKey(expiry)) {
                                            expiryMap.put(expiry, new ArrayList<>());
                                        }
                                        expiryMap.get(expiry).add(medicine);
                                    }
                                }
                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, expiryMap, redZoneMap, new HashMap<>());
                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        TriageHistoryManager.loadTriageDates(getContext(), new TriageHistoryManager.TriageDatesCallback() {
                            @Override
                            public void onSuccess(Map<LocalDate, Boolean> triageMap) {
                                Map<LocalDate, List<Medicine>> expiryMap = new HashMap<>();
                                for (Medicine medicine : medicines) {
                                    if (medicine.getAmountLeft() > 0) {
                                        LocalDate expiry = medicine.getExpiry();
                                        if (!expiryMap.containsKey(expiry)) {
                                            expiryMap.put(expiry, new ArrayList<>());
                                        }
                                        expiryMap.get(expiry).add(medicine);
                                    }
                                }
                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, expiryMap, new HashMap<>(), triageMap);
                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Map<LocalDate, List<Medicine>> expiryMap = new HashMap<>();
                                for (Medicine medicine : medicines) {
                                    if (medicine.getAmountLeft() > 0) {
                                        LocalDate expiry = medicine.getExpiry();
                                        if (!expiryMap.containsKey(expiry)) {
                                            expiryMap.put(expiry, new ArrayList<>());
                                        }
                                        expiryMap.get(expiry).add(medicine);
                                    }
                                }
                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, expiryMap, new HashMap<>(), new HashMap<>());
                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                                }
                            }
                        });
            }
        });
    }

            @Override
            public void onFailure(Exception e) {
                ZoneHistoryManager.loadRedZoneDates(getContext(), new ZoneHistoryManager.RedZoneDatesCallback() {
                    @Override
                    public void onSuccess(Map<LocalDate, Boolean> redZoneMap) {
                        TriageHistoryManager.loadTriageDates(getContext(), new TriageHistoryManager.TriageDatesCallback() {
                            @Override
                            public void onSuccess(Map<LocalDate, Boolean> triageMap) {
                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, new HashMap<>(), redZoneMap, triageMap);
                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                }
            }

            @Override
            public void onFailure(Exception e) {
                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, new HashMap<>(), redZoneMap, new HashMap<>());
                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                                }
            }
        });
    }

                    @Override
                    public void onFailure(Exception e) {
                        MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                currentDisplayMonth, new HashMap<>(), new HashMap<>(), new HashMap<>());
                        if (getContext() != null && calendarRecyclerView != null) {
                            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                            calendarRecyclerView.setLayoutManager(layoutManager);
                            calendarRecyclerView.setAdapter(calendarAdapter);
                        }
                    }
                });
            }
        });
    }

    public void getMostRecentRescue() {
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currUser == null) { return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = currUser.getUid();

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                // get linked children
                List<Map<String, Object>> linkedChildren =
                        (List<Map<String, Object>>) document.get("linkedChildren");

                if (linkedChildren != null && !linkedChildren.isEmpty()) {

                    // extract children uid's to perform query
                    for (Map<String, Object> child : linkedChildren) {
                        String childUid = (String) child.get("uid");
                        if (childUid != null) {
                            getChildMostRecentRescue(childUid);
                        }
                    }

                }
            }

        });
    }

    private void getChildMostRecentRescue(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("inhaler_log")
                .whereEqualTo("medicationType",  "Rescue")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        Timestamp timestamp = doc.getTimestamp("timestamp");

                        if (timestamp != null) {
                            updateRescueCard(timestamp.toDate());
                        }
                    }

                }).addOnFailureListener(e -> {
                    Log.e("FIX_ME", "------------------------------------------------");
                    Log.e("FIX_ME", "ERROR TYPE: " + e.getMessage());
                    Log.e("FIX_ME", "------------------------------------------------");
                });
    }

    private void updateRescueCard(Date date) {
        if (lastRescueDate == null || date.after(lastRescueDate)) {

            lastRescueDate = date;

            if (getActivity() != null) {
                CharSequence timeAgo = android.text.format.DateUtils
                        .getRelativeTimeSpanString(lastRescueDate.getTime(),
                                System.currentTimeMillis(),
                                android.text.format.DateUtils.MINUTE_IN_MILLIS);
                lastRescue.setText(timeAgo);
            }
        }
    }
}

