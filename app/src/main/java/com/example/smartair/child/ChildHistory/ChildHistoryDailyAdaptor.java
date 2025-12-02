package com.example.smartair.child.ChildHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.List;

class ChildHistoryDailyAdapter extends RecyclerView.Adapter<ChildHistoryDailyAdapter.MyViewHolder> {

    private List<DailyCheckIn> dailyReadingList;
    public ChildHistoryDailyAdapter(List<DailyCheckIn> dailyList) {
        this.dailyReadingList = dailyList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.daily_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        DailyCheckIn dailyItem = dailyReadingList.get(position);

        holder.DailyActivityLimits.setText("Activity limits: " + dailyItem.getActivityLimits());
        holder.DailyCoughWheeze.setText("Cough Wheeze: " + dailyItem.getCoughWheeze());
        holder.DailyEnteredByParent.setText("Entered by parent?: " + dailyItem.getEnteredByParent().toString());
        holder.DailyNightWaking.setText("Night waking?: " + dailyItem.getNightWaking());
        holder.DailyNotes.setText("Notes: " + dailyItem.getNotes());
        holder.DailyDate.setText("Date: " + dailyItem.getDate());
    }

    @Override
    public int getItemCount() {
        return dailyReadingList.size();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView DailyActivityLimits, DailyCoughWheeze, DailyEnteredByParent, DailyNightWaking, DailyNotes, DailyDate;

        public MyViewHolder(@NonNull View itemView) {

            super(itemView);

            DailyActivityLimits = itemView.findViewById(R.id.activity_limits);
            DailyCoughWheeze = itemView.findViewById(R.id.cough_wheeze);
            DailyEnteredByParent = itemView.findViewById(R.id.entered_by_parent);
            DailyNightWaking = itemView.findViewById(R.id.night_waking);
            DailyNotes = itemView.findViewById(R.id.notes);
            DailyDate = itemView.findViewById(R.id.examDate);
        }
    }

}