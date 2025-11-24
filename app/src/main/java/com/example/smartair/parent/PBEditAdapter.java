package com.example.smartair.parent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartair.R;

import java.util.List;
import java.util.Map;

import utils.PBManager;
import utils.PEFManager;

public class PBEditAdapter extends RecyclerView.Adapter<PBEditAdapter.PBEditViewHolder> {

    private final List<Map<String, Object>> childrenList;

    public PBEditAdapter(List<Map<String, Object>> childrenList) {
        this.childrenList = childrenList;
    }

    @NonNull
    @Override
    public PBEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pb_edit, parent, false);
        return new PBEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PBEditViewHolder holder, int position) {
        Map<String, Object> child = childrenList.get(position);
        String childName = (String) child.get("name");
        String childUid = (String) child.get("uid");

        holder.textViewChildName.setText(childName != null ? childName : "Unknown");

        // Load current PB
        if (childUid != null) {
            PBManager.getPB(childUid, new PBManager.PBCallback() {
                @Override
                public void onSuccess(Integer pbValue) {
                    if (pbValue != null) {
                        holder.textViewCurrentPB.setText("Current PB: " + pbValue + " L/min");
                        holder.editPBValue.setText(String.valueOf(pbValue));
                    } else {
                        PEFManager.getHighestPEF(childUid, new PEFManager.PEFCallback() {
                            @Override
                            public void onSuccess(Integer highestPEF) {
                                if (highestPEF != null) {
                                    holder.textViewCurrentPB.setText("Highest PEF: " + highestPEF + " L/min (will be set as PB)");
                                    holder.editPBValue.setText(String.valueOf(highestPEF));
                                } else {
                                    holder.textViewCurrentPB.setText("Current PB: Not set");
                                    holder.editPBValue.setText("");
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                holder.textViewCurrentPB.setText("Current PB: Not set");
                                holder.editPBValue.setText("");
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    holder.textViewCurrentPB.setText("Current PB: Not set");
                    holder.editPBValue.setText("");
                }
            });
        }

        holder.itemView.setTag(childUid);
    }

    @Override
    public int getItemCount() {
        return childrenList.size();
    }

    public String getChildUid(int position) {
        if (position >= 0 && position < childrenList.size()) {
            return (String) childrenList.get(position).get("uid");
        }
        return null;
    }

    public int getPBValue(int position, RecyclerView recyclerView) {
        PBEditViewHolder holder = (PBEditViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            String valueStr = holder.editPBValue.getText().toString().trim();
            try {
                return Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    public static class PBEditViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName;
        TextView textViewCurrentPB;
        EditText editPBValue;

        public PBEditViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewCurrentPB = itemView.findViewById(R.id.textViewCurrentPB);
            editPBValue = itemView.findViewById(R.id.editPBValue);
        }
    }
}

