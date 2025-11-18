package com.example.smartair.parent;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.List;
import java.util.Map;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private final List<Map<String, Object>> childrenList;
    private final OnChildClickListener listener;

    public interface OnChildClickListener {
        void onDeleteClick(int position);
    }

    public ChildrenAdapter(List<Map<String, Object>> childrenList, OnChildClickListener listener) {
        this.childrenList = childrenList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Map<String, Object> child = childrenList.get(position);
        String name = (String) child.get("name");
        Object linkedAtObj = child.get("linkedAt");

        holder.textViewChildName.setText(name != null ? name : "Unknown");

        if (linkedAtObj != null) {
            long linkedAt = ((Number) linkedAtObj).longValue();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            String dateStr = sdf.format(new java.util.Date(linkedAt));
            holder.textViewLinkedDate.setText("Linked: " + dateStr);
        } else {
            holder.textViewLinkedDate.setText("Linked: Unknown");
        }

        holder.buttonUnlink.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childrenList.size();
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName;
        TextView textViewLinkedDate;
        Button buttonUnlink;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewLinkedDate = itemView.findViewById(R.id.textViewLinkedDate);
            buttonUnlink = itemView.findViewById(R.id.buttonUnlink);
        }
    }
}

