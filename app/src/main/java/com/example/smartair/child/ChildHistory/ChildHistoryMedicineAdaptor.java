package com.example.smartair.child.ChildHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.List;

class ChildHistoryMedicineAdapter extends RecyclerView.Adapter<ChildHistoryMedicineAdapter.MyViewHolder> {

    private List<Medicine> medicineReadingList;
    public ChildHistoryMedicineAdapter(List<Medicine> medList) {
        this.medicineReadingList = medList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medicine_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Medicine medItem = medicineReadingList.get(position);

        holder.DoseCount.setText("Dose count: " + medItem.getDoseCount().toString());
        holder.EnteredBy.setText("Entered by: " + medItem.getEnteredBy());
        holder.MedicationType.setText("Medication Type: " + medItem.getMedicationType());
        holder.PostDoseStatus.setText("Date: " + medItem.getPostDoseStatus());
        holder.PreDoseStatus.setText("Date: " + medItem.getPreDoseStatus());
        holder.PreDoseBreathRating.setText("Date: " + medItem.getPreDoseBreathRating().toString());
        holder.PostDoseBreathRating.setText("Date: " + medItem.getPostDoseBreathRating().toString());
        holder.Date.setText("Date: " + medItem.getTimestamp().toDate());
    }

    @Override
    public int getItemCount() {
        return medicineReadingList.size();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView DoseCount, EnteredBy, MedicationType, PostDoseBreathRating, PostDoseStatus, PreDoseBreathRating, PreDoseStatus, Date;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            DoseCount = itemView.findViewById(R.id.dose_count);
            EnteredBy = itemView.findViewById(R.id.entered_by);
            MedicationType = itemView.findViewById(R.id.medication_type);
            PostDoseBreathRating = itemView.findViewById(R.id.post_dose_br);
            PostDoseStatus = itemView.findViewById(R.id.post_dose_s);
            PreDoseStatus = itemView.findViewById(R.id.pre_dose_s);
            PreDoseBreathRating = itemView.findViewById(R.id.pre_dose_br);
            Date = itemView.findViewById(R.id.date);

        }
    }

}
