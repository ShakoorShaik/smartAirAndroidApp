package com.example.smartair.child.ChildHistory;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;


import com.example.smartair.R;

import java.util.List;


class ChildHistoryPefAdapter extends RecyclerView.Adapter<ChildHistoryPefAdapter.MyViewHolder> {

    private List<PEFReading> PEFreadingList;
    public ChildHistoryPefAdapter(List<PEFReading> pefList) {
        this.PEFreadingList = pefList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pef_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        PEFReading examItem = PEFreadingList.get(position);

        holder.PEFValue.setText("Value: " + examItem.getValue().toString());
        holder.PEFDate.setText("Date: " + examItem.getDate());
    }

    @Override
    public int getItemCount() {
        return PEFreadingList.size();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView PEFValue, PEFDate;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            PEFValue = itemView.findViewById(R.id.examName);
            PEFDate = itemView.findViewById(R.id.examDate);
        }
    }

}