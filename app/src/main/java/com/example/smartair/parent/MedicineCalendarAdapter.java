package com.example.smartair.parent;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import utils.Medicine;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineCalendarAdapter extends RecyclerView.Adapter<MedicineCalendarAdapter.CalendarViewHolder> {

    private final YearMonth currentMonth;
    private final Map<LocalDate, List<Medicine>> medicineMap;
    private final Map<LocalDate, Boolean> redZoneDates;
    private final Map<LocalDate, Boolean> triageDates;
    private final Map<String, Integer> childColors = new HashMap<>();
    private final int[] colorPalette = {
            0xFFE91E63,
            0xFF2196F3,
            0xFF4CAF50,
            0xFFFF9800,
            0xFF9C27B0,
            0xFF00BCD4,
            0xFFFFEB3B,
            0xFF795548
    };
    private int colorIndex = 0;
    private final List<LocalDate> datesInMonth = new ArrayList<>();

    public MedicineCalendarAdapter(YearMonth month, Map<LocalDate, List<Medicine>> medicineMap) {
        this.currentMonth = month;
        this.medicineMap = medicineMap;
        this.redZoneDates = new HashMap<>();
        this.triageDates = new HashMap<>();
        initializeDates();
    }

    public MedicineCalendarAdapter(YearMonth month, Map<LocalDate, List<Medicine>> medicineMap, Map<LocalDate, Boolean> redZoneDates) {
        this.currentMonth = month;
        this.medicineMap = medicineMap;
        this.redZoneDates = redZoneDates != null ? redZoneDates : new HashMap<>();
        this.triageDates = new HashMap<>();
        initializeDates();
    }

    public MedicineCalendarAdapter(YearMonth month, Map<LocalDate, List<Medicine>> medicineMap, Map<LocalDate, Boolean> redZoneDates, Map<LocalDate, Boolean> triageDates) {
        this.currentMonth = month;
        this.medicineMap = medicineMap;
        this.redZoneDates = redZoneDates != null ? redZoneDates : new HashMap<>();
        this.triageDates = triageDates != null ? triageDates : new HashMap<>();
        initializeDates();
    }

    private void initializeDates() {
        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();

        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < firstDayOfWeek; i++) {
            datesInMonth.add(null);
        }

        LocalDate date = firstDay;
        while (!date.isAfter(lastDay)) {
            datesInMonth.add(date);
            date = date.plusDays(1);
        }
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = datesInMonth.get(position);

        if (date == null) {
            holder.dayNumber.setText("");
            holder.medicineIndicators.removeAllViews();
            holder.itemView.setAlpha(0.3f);
            return;
        }

        holder.itemView.setAlpha(1.0f);
        List<Medicine> medicines = new ArrayList<>();
        if (YearMonth.from(date).equals(currentMonth)) {
            medicines = medicineMap.getOrDefault(date, new ArrayList<>());
        }

        boolean isRedZoneDay = redZoneDates != null && redZoneDates.containsKey(date) && redZoneDates.get(date);

        boolean isTriageDay = triageDates != null && triageDates.containsKey(date) && triageDates.get(date);

        holder.dayNumber.setText(String.valueOf(date.getDayOfMonth()));

        LocalDate today = LocalDate.now();
        if (isRedZoneDay) {
            holder.cardView.setCardBackgroundColor(0xFFFFEBEE);
            holder.dayNumber.setTextColor(0xFFFF0000);
            holder.dayNumber.getPaint().setFakeBoldText(true);
        } else if (date.isBefore(today)) {
            holder.dayNumber.setTextColor(0xFFCCCCCC);
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF);
            holder.dayNumber.getPaint().setFakeBoldText(false);
        } else if (date.equals(today)) {
            holder.dayNumber.setTextColor(0xFFFF9800);
            holder.dayNumber.getPaint().setFakeBoldText(true);
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF);
        } else {
            holder.dayNumber.setTextColor(0xFF333333);
            holder.cardView.setCardBackgroundColor(0xFFFFFFFF);
            holder.dayNumber.getPaint().setFakeBoldText(false);
        }

        holder.medicineIndicators.removeAllViews();

        assert medicines != null;
        if (!medicines.isEmpty()) {
            for (Medicine medicine : medicines) {
                String childName = medicine.getChildName() != null ? medicine.getChildName() : "?";
                String initial = !childName.isEmpty() ? childName.substring(0, 1).toUpperCase() : "?";

                if (!childColors.containsKey(childName)) {
                    childColors.put(childName, colorPalette[colorIndex % colorPalette.length]);
                    colorIndex++;
                }
                int childColor = childColors.get(childName);
                View circleView = createCircleView(holder.itemView.getContext(), initial, childColor, date.isBefore(today));
                holder.medicineIndicators.addView(circleView);
            }
        }

        if (isTriageDay) {
            View triageIcon = createTriageIcon(holder.itemView.getContext());
            holder.medicineIndicators.addView(triageIcon);
        }
    }

    private View createCircleView(android.content.Context context, String initial, int color, boolean isExpired) {
        TextView circle = new TextView(context);
        circle.setText(initial);
        circle.setTextColor(Color.WHITE);
        circle.setTextSize(8);
        circle.setGravity(android.view.Gravity.CENTER);
        circle.setPadding(0, 0, 0, 0);

        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        drawable.setColor(isExpired ? 0xFFCCCCCC : color);
        drawable.setSize(24, 24);
        circle.setBackground(drawable);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(24, 24);
        params.setMargins(2, 0, 2, 0);
        circle.setLayoutParams(params);

        return circle;
    }

    private View createTriageIcon(android.content.Context context) {
        TextView triageIcon = new TextView(context);
        triageIcon.setText("T");
        triageIcon.setTextColor(Color.WHITE);
        triageIcon.setTextSize(10);
        triageIcon.setGravity(android.view.Gravity.CENTER);
        triageIcon.setPadding(0, 0, 0, 0);
        triageIcon.getPaint().setFakeBoldText(true);

        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setColor(0xFFFF5722);
        drawable.setCornerRadius(4);
        drawable.setSize(24, 24);
        triageIcon.setBackground(drawable);

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(24, 24);
        params.setMargins(2, 0, 2, 0);
        triageIcon.setLayoutParams(params);

        return triageIcon;
    }

    @Override
    public int getItemCount() {
        return datesInMonth.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumber;
        ViewGroup medicineIndicators;
        CardView cardView;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCalendarDay);
            dayNumber = itemView.findViewById(R.id.textDayNumber);
            medicineIndicators = itemView.findViewById(R.id.medicineIndicators);
        }
    }
}

