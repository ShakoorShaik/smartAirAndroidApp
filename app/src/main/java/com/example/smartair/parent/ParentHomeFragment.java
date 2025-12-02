package com.example.smartair.parent;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import utils.Medicine;
import utils.MedicineManager;
import utils.ParentRescue;
import utils.SnippetManager;
import utils.TriageHistoryManager;
import utils.ZoneHistoryManager;
import utils.PBManager;
import utils.PEFManager;
import utils.ZoneManager;
import utils.ChildAccountManager;
import com.github.mikephil.charting.charts.LineChart;


import utils.ParentEmergency;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ParentHomeFragment extends Fragment {

    private RecyclerView calendarRecyclerView;
    private TextView monthYearText;
    private YearMonth currentDisplayMonth;
    private TextView lastRescue;
    private Date lastRescueDate = null;
    private int rescue_count;
    private List<Map<String, Object>> linkedChildren;
    private int currentChildIndex = 0;
    private CardView zoneCard;
    private TextView zonePercentage;
    private TextView childNameText;
    LineChart lineChart;

    boolean isSevenDaySnippet;

    public ParentHomeFragment() {
    }

    @Override
    public void onStart(){
        super.onStart();
        ParentEmergency.listenEmergency(this);
        ParentRescue.listenRescue(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        loadZoneInfo(view);
        loadWeeklyRescues(view);

        lineChart = view.findViewById(R.id.historyButton);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        setSevenDaySnippet();
        isSevenDaySnippet = true;

        View historyBtn = view.findViewById(R.id.historyButton);
        if (historyBtn != null) {
            historyBtn.setOnClickListener(v -> {
                if (getContext() != null) {
                    toggleSnippet();
                }
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (calendarRecyclerView != null) {
            loadCalendarData();
        }
        View view = getView();
        if (view != null) {
            loadZoneInfo(view);
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
                        TriageHistoryManager.loadTriageDates(getContext(), new TriageHistoryManager.TriageDatesCallback() {
                            @Override
                            public void onSuccess(Map<LocalDate, Boolean> triageMap) {
                                MedicineCalendarAdapter calendarAdapter = new MedicineCalendarAdapter(
                                        currentDisplayMonth, new HashMap<>(), new HashMap<>(), triageMap);
                                if (getContext() != null && calendarRecyclerView != null) {
                                    GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
                                    calendarRecyclerView.setLayoutManager(layoutManager);
                                    calendarRecyclerView.setAdapter(calendarAdapter);
                                }
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
                List<Map<String, Object>> linkedChildren =
                        (List<Map<String, Object>>) document.get("linkedChildren");

                if (linkedChildren != null && !linkedChildren.isEmpty()) {

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
                CharSequence timeAgo = DateUtils
                        .getRelativeTimeSpanString(lastRescueDate.getTime(),
                                System.currentTimeMillis(),
                                DateUtils.MINUTE_IN_MILLIS);
                lastRescue.setText(timeAgo);
            }
        }
    }

    private void loadZoneInfo(View view) {
        zonePercentage = view.findViewById(R.id.zone_percentage);
        zoneCard = view.findViewById(R.id.zone_card);
        childNameText = view.findViewById(R.id.child_name_text);

        if (zonePercentage == null || zoneCard == null) {
            return;
        }

        if (childNameText != null) {
            childNameText.setVisibility(View.GONE);
        }

        currentChildIndex = 0;

        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currUser == null) {
            displayDefaultZone();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                linkedChildren = (List<Map<String, Object>>) document.get("linkedChildren");

                if (linkedChildren != null && !linkedChildren.isEmpty()) {
                    if (linkedChildren.size() > 1 && childNameText != null) {
                        childNameText.setVisibility(View.VISIBLE);
                    }

                    if (currentChildIndex >= linkedChildren.size()) {
                        currentChildIndex = 0;
                    }
                    
                    String childUid = (String) linkedChildren.get(currentChildIndex).get("uid");
                    if (childUid != null) {
                        updateZoneInfoForChild(childUid, currentChildIndex);
                        setupClickListener();
                    } else {
                        displayDefaultZone();
                    }
                } else {
                    displayDefaultZone();
                }
            } else {
                displayDefaultZone();
            }
        });
    }

    private void setupClickListener() {
        if (zoneCard == null || linkedChildren == null || linkedChildren.size() <= 1) {
            return;
        }

        zoneCard.setOnClickListener(v -> {
            if (linkedChildren != null && !linkedChildren.isEmpty()) {
                currentChildIndex = (currentChildIndex + 1) % linkedChildren.size();
                String childUid = (String) linkedChildren.get(currentChildIndex).get("uid");
                if (childUid != null) {
                    updateZoneInfoForChild(childUid, currentChildIndex);
                }
            }
        });
    }

    private void updateZoneInfoForChild(String childUid, int childIndex) {
        if (childNameText != null && linkedChildren != null && childIndex < linkedChildren.size()) {
            String childName = (String) linkedChildren.get(childIndex).get("name");
            if (childName != null) {
                childNameText.setText(childName);
            }
        }

        PBManager.getPB(childUid, new PBManager.PBCallback() {
            @Override
            public void onSuccess(Integer pbValue) {
                if (pbValue == null || pbValue <= 0) {
                    displayDefaultZone();
                    return;
                }

                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());

                PEFManager.getPEFByDate(childUid, today, new PEFManager.PEFCallback() {
                    @Override
                    public void onSuccess(Integer pefValue) {
                        if (pefValue == null || pefValue <= 0) {
                            displayDefaultZone();
                        } else {
                            ZoneManager.Zone zone = ZoneManager.calculateZone(pefValue, pbValue);
                            displayZoneInfo(zone, pefValue, pbValue);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        displayDefaultZone();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                displayDefaultZone();
            }
        });
    }

    private void displayZoneInfo(ZoneManager.Zone zone, int pefValue, int pbValue) {
        if (zonePercentage == null || zoneCard == null) return;
        
        int percentage = (int) (((double) pefValue / pbValue) * 100);

        String zoneName = zone.toString().substring(0, 1).toUpperCase()
                + zone.toString().substring(1).toLowerCase();

        String zoneText = String.format("%s %d%%", zoneName, percentage);
        zonePercentage.setText(zoneText);

        int cardColor;
        switch (zone) {
            case GREEN:
                cardColor = 0xFF4CAF50;
                break;
            case YELLOW:
                cardColor = 0xFFFFC107;
                break;
            case RED:
                cardColor = 0xFFF44336;
                break;
            default:
                cardColor = 0xFFBDBDBD;
                break;
        }
        zoneCard.setCardBackgroundColor(cardColor);
    }

    private void displayDefaultZone() {
        if (zonePercentage == null || zoneCard == null) return;
        
        zonePercentage.setText("--");
        zoneCard.setCardBackgroundColor(0xFFBDBDBD);
    }

    public void refreshZoneInfo() {
        View view = getView();
        if (view != null) {
            loadZoneInfo(view);
        }
    }

    private void loadWeeklyRescues(View v) {
        TextView num_weekly_rescues = v.findViewById(R.id.text_num_rescues);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null || num_weekly_rescues == null) { return; }

        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot documentSnapshot = task.getResult();

                List<Map<String, Object>> linkedChildren =
                        (List<Map<String, Object>>) documentSnapshot.get("linkedChildren");
                if (linkedChildren != null) {
                    for (Map<String, Object> child : linkedChildren) {
                        if (child.get("uid") != null) {
                            updateNumWeeklyRescues(num_weekly_rescues, (String) child.get("uid"));
                        }

                    }

                }
            }
        });



    }

    private void updateNumWeeklyRescues(TextView num_weekly_rescues, String childId) {

        LocalDate today = LocalDate.now();
        LocalDate localWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate localWeekEnd = localWeekStart.plusWeeks(1);
        Timestamp weekStart = new Timestamp(
                Date.from(localWeekStart.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        Timestamp weekEnd = new Timestamp(
                Date.from(localWeekEnd.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        Log.d("DEBUG_QUERY", "Start: " + weekStart.toDate().toString());
        Log.d("DEBUG_QUERY", "End: " + weekEnd.toDate().toString());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childId)
                .collection("inhaler_log").whereGreaterThanOrEqualTo("timestamp", weekStart)
                .whereLessThan("timestamp", weekEnd).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Log.d("DEBUG_QUERY", "task successful");
                        QuerySnapshot query = task.getResult();
                        Log.d("DEBUG_QUERY", "task result");

                        if (!query.isEmpty()) {
                            Log.d("DEBUG_QUERY", "query nonempty");
                            for (DocumentSnapshot doc: query) {
                                Log.d("DEBUG_QUERY", "doc");
                                if ("Rescue".equals(doc.getString("medicationType"))) {
                                    Log.d("DEBUG_QUERY", "rescue attempt");
                                    rescue_count++;
                                    num_weekly_rescues.setText(rescue_count + " times");
                                }
                            }
                        }
                    }
                });



    }
    private void toggleSnippet() {
        if (isSevenDaySnippet){
            setThirtyDaySnippet();
            isSevenDaySnippet = false;
        }
        else{
            setSevenDaySnippet();
            isSevenDaySnippet = true;
        }
    }

    private void setSevenDaySnippet(){
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(6f);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(7, true);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setGranularityEnabled(true);

        yAxisLeft.setLabelCount(10, false);

        SnippetManager.set7dayData(7, new SnippetManager.SnippetCallback() {
            @Override
            public void onSuccess(LineData lineData) {
                lineChart.setData(lineData);
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }

    private void setThirtyDaySnippet(){

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(29f);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        xAxis.setLabelCount(7, false);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setGranularityEnabled(true);

        yAxisLeft.setLabelCount(10, false);

        SnippetManager.set7dayData(30, new SnippetManager.SnippetCallback() {
            @Override
            public void onSuccess(LineData lineData) {
                lineChart.setData(lineData);
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });


    }

}

