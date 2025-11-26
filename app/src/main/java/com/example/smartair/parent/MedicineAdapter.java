package com.example.smartair.parent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;
import utils.Medicine;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> medicineList;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(int position, Medicine medicine);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, Medicine medicine);
    }

    public MedicineAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_parent, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine currentMedicine = medicineList.get(position);
        holder.name.setText(currentMedicine.getName());
        holder.amt.setText(String.valueOf(currentMedicine.getAmountLeft()) + "%");
        holder.exp.setText(currentMedicine.getExpiry().toString());
        holder.img.setImageResource(currentMedicine.getImageId());
        holder.img.setAlpha(1.0f);

        holder.editButton.setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(position, currentMedicine);
            }
        });
        
        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(position, currentMedicine);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView name;
        TextView amt;
        TextView exp;
        ImageButton editButton;
        ImageButton deleteButton;

        public MedicineViewHolder(View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.imageMed);
            name = itemView.findViewById(R.id.textMedName);
            amt = itemView.findViewById(R.id.textMedAmt);
            exp = itemView.findViewById(R.id.textMedExpiry);
            editButton = itemView.findViewById(R.id.buttonEditMedicine);
            deleteButton = itemView.findViewById(R.id.buttonDeleteMedicine);
        }
    }
}

