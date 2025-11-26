package com.example.smartair.parent;

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
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpiryCalendarAdapter extends RecyclerView.Adapter<ExpiryCalendarAdapter.ExpiryViewHolder> {

    private final List<ExpiryEntry> expiryEntries;
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

    public ExpiryCalendarAdapter(List<ExpiryEntry> expiryEntries) {
        this.expiryEntries = expiryEntries;
    }

    @NonNull
    @Override
    public ExpiryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expiry_calendar, parent, false);
        return new ExpiryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpiryViewHolder holder, int position) {
        ExpiryEntry entry = expiryEntries.get(position);
        LocalDate date = entry.getDate();
        List<Medicine> medicines = entry.getMedicines();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        holder.dateText.setText(date.format(formatter));

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            holder.dateText.setTextColor(0xFFFF0000);
        } else if (date.equals(today)) {
            holder.dateText.setTextColor(0xFFFF9800);
        } else if (date.isBefore(today.plusDays(4))) {
            holder.dateText.setTextColor(0xFFFF9800);
        } else {
            holder.dateText.setTextColor(0xFF666666);
        }

        StringBuilder medicineList = new StringBuilder();
        for (int i = 0; i < medicines.size(); i++) {
            Medicine medicine = medicines.get(i);
            String childName = medicine.getChildName() != null ? medicine.getChildName() : "Unknown";

            if (!childColors.containsKey(childName)) {
                childColors.put(childName, colorPalette[colorIndex % colorPalette.length]);
                colorIndex++;
            }
            int childColor = childColors.get(childName);

            if (i > 0) {
                medicineList.append("\n");
            }
            medicineList.append("● ").append(medicine.getName());
            medicineList.append(" (").append(childName).append(")");
        }

        holder.medicinesText.setText(medicineList.toString());

        if (!medicines.isEmpty()) {
            String firstChildName = medicines.get(0).getChildName() != null ? medicines.get(0).getChildName() : "Unknown";
            if (!childColors.containsKey(firstChildName)) {
                childColors.put(firstChildName, colorPalette[colorIndex % colorPalette.length]);
                colorIndex++;
            }
            int color = childColors.get(firstChildName);
            holder.cardView.setCardBackgroundColor((color & 0x00FFFFFF) | 0x0A000000);
        }
    }

    @Override
    public int getItemCount() {
        return expiryEntries.size();
    }

    public static class ExpiryViewHolder extends RecyclerView.ViewHolder {
        TextView dateText;
        TextView medicinesText;
        CardView cardView;

        public ExpiryViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExpiry);
            dateText = itemView.findViewById(R.id.textExpiryDate);
            medicinesText = itemView.findViewById(R.id.textMedicines);
        }
    }

    public static class ExpiryEntry {
        private final LocalDate date;
        private final List<Medicine> medicines;

        public ExpiryEntry(LocalDate date, List<Medicine> medicines) {
            this.date = date;
            this.medicines = medicines;
        }

        public LocalDate getDate() { return date; }
        public List<Medicine> getMedicines() { return medicines; }
    }
}

