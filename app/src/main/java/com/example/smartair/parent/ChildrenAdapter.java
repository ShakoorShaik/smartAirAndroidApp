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

import utils.PEFManager;
import utils.PBManager;

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
        String childUid = (String) child.get("uid");
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

        // Load and display most recent PEF
        if (childUid != null) {
            PEFManager.getMostRecentPEF(childUid, new PEFManager.PEFCallback() {
                @Override
                public void onSuccess(Integer pefValue) {
                    if (pefValue != null) {
                        holder.textViewPEF.setText("PEF: " + pefValue + " L/min");
                    } else {
                        holder.textViewPEF.setText("PEF: --");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    holder.textViewPEF.setText("PEF: --");
                }
            });

            // Load and display PB
            PBManager.getPB(childUid, new PBManager.PBCallback() {
                @Override
                public void onSuccess(Integer pbValue) {
                    if (pbValue != null) {
                        holder.textViewPB.setText("PB: " + pbValue + " L/min");
                    } else {
                        holder.textViewPB.setText("PB: --");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    holder.textViewPB.setText("PB: --");
                }
            });
        } else {
            holder.textViewPEF.setText("PEF: --");
            holder.textViewPB.setText("PB: --");
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
        TextView textViewPEF;
        TextView textViewPB;
        Button buttonUnlink;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewLinkedDate = itemView.findViewById(R.id.textViewLinkedDate);
            textViewPEF = itemView.findViewById(R.id.textViewPEF);
            textViewPB = itemView.findViewById(R.id.textViewPB);
            buttonUnlink = itemView.findViewById(R.id.buttonUnlink);
        }
    }
}

