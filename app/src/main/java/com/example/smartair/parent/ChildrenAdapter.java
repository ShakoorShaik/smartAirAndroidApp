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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import utils.PEFManager;
import utils.PBManager;
import utils.ZoneManager;

public class ChildrenAdapter extends RecyclerView.Adapter<ChildrenAdapter.ChildViewHolder> {

    private final List<Map<String, Object>> childrenList;
    private final OnChildClickListener listener;

    public interface OnChildClickListener {
        void onDeleteClick(int position);

        void onClick(int position);
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

    @SuppressLint({"SetTextI18n", "RecyclerView"})
    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Map<String, Object> child = childrenList.get(position);
        String name = (String) child.get("name");
        String childUid = (String) child.get("uid");
        Object linkedAtObj = child.get("linkedAt");

        holder.currentChildUid = childUid;

        holder.textViewChildName.setText(name != null ? name : "Unknown");

        if (linkedAtObj != null) {
            long linkedAt = ((Number) linkedAtObj).longValue();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            String dateStr = sdf.format(new java.util.Date(linkedAt));
            holder.textViewLinkedDate.setText("Linked: " + dateStr);
        } else {
            holder.textViewLinkedDate.setText("Linked: Unknown");
        }

        holder.textViewPEF.setText("PEF: --");
        holder.textViewPB.setText("PB: --");
        holder.textViewZone.setText("Zone: --");
        holder.textViewZone.setTextColor(0xFF757575);

        if (childUid != null) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().getTime());

            PEFManager.getPEFByDate(childUid, today, new PEFManager.PEFCallback() {
                @Override
                public void onSuccess(Integer pefValue) {
                    if (holder.currentChildUid != null && holder.currentChildUid.equals(childUid)) {
                        if (pefValue != null && pefValue > 0) {
                            holder.textViewPEF.setText("PEF: " + pefValue + " L/min");
                            holder.currentPEF = pefValue;
                            updateZoneForChild(holder, childUid, pefValue);
                        } else {
                            holder.textViewPEF.setText("PEF: --");
                            holder.currentPEF = null;
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (holder.currentChildUid != null && holder.currentChildUid.equals(childUid)) {
                        holder.textViewPEF.setText("PEF: --");
                        holder.currentPEF = null;
                    }
                }
            });

            PBManager.getPB(childUid, new PBManager.PBCallback() {
                @Override
                public void onSuccess(Integer pbValue) {
                    if (holder.currentChildUid != null && holder.currentChildUid.equals(childUid)) {
                        if (pbValue != null && pbValue > 0) {
                            holder.textViewPB.setText("PB: " + pbValue + " L/min");
                            holder.currentPB = pbValue;
                            if (holder.currentPEF != null && holder.currentPEF > 0) {
                                updateZoneForChild(holder, childUid, holder.currentPEF);
                            }
                        } else {
                            holder.textViewPB.setText("PB: --");
                            holder.currentPB = null;
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (holder.currentChildUid != null && holder.currentChildUid.equals(childUid)) {
                        holder.textViewPB.setText("PB: --");
                        holder.currentPB = null;
                    }
                }
            });
        } else {
            holder.currentChildUid = null;
            holder.currentPEF = null;
            holder.currentPB = null;
        }

        holder.buttonUnlink.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });

        holder.buttonGoToChild.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return childrenList.size();
    }

    private void updateZoneForChild(ChildViewHolder holder, String childUid, int pefValue) {
        if (holder.currentChildUid == null || !holder.currentChildUid.equals(childUid)) {
            return;
        }

        if (holder.currentPB != null && holder.currentPB > 0) {
            ZoneManager.Zone zone = ZoneManager.calculateZone(pefValue, holder.currentPB);
            int percentage = (int) (((double) pefValue / holder.currentPB) * 100);
            String zoneName = zone.toString().substring(0, 1).toUpperCase()
                    + zone.toString().substring(1).toLowerCase();
            String zoneText = String.format("Zone: %s %d%%", zoneName, percentage);
            holder.textViewZone.setText(zoneText);

            int zoneColor;
            switch (zone) {
                case GREEN:
                    zoneColor = 0xFF4CAF50;
                    break;
                case YELLOW:
                    zoneColor = 0xFFFFC107;
                    break;
                case RED:
                    zoneColor = 0xFFF44336;
                    break;
                default:
                    zoneColor = 0xFF757575;
                    break;
            }
            holder.textViewZone.setTextColor(zoneColor);
        } else {
            PBManager.getPB(childUid, new PBManager.PBCallback() {
                @Override
                public void onSuccess(Integer pbValue) {
                    if (holder.currentChildUid != null && holder.currentChildUid.equals(childUid)) {
                        if (pbValue != null && pbValue > 0) {
                            holder.currentPB = pbValue;
                            ZoneManager.Zone zone = ZoneManager.calculateZone(pefValue, pbValue);
                            int percentage = (int) (((double) pefValue / pbValue) * 100);
                            String zoneName = zone.toString().substring(0, 1).toUpperCase()
                                    + zone.toString().substring(1).toLowerCase();
                            String zoneText = String.format("Zone: %s %d%%", zoneName, percentage);
                            holder.textViewZone.setText(zoneText);

                            int zoneColor;
                            switch (zone) {
                                case GREEN:
                                    zoneColor = 0xFF4CAF50;
                                    break;
                                case YELLOW:
                                    zoneColor = 0xFFFFC107;
                                    break;
                                case RED:
                                    zoneColor = 0xFFF44336;
                                    break;
                                default:
                                    zoneColor = 0xFF757575;
                                    break;
                            }
                            holder.textViewZone.setTextColor(zoneColor);
                        } else {
                            holder.textViewZone.setText("Zone: --");
                            holder.textViewZone.setTextColor(0xFF757575);
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (holder.currentChildUid != null && holder.currentChildUid.equals(childUid)) {
                        holder.textViewZone.setText("Zone: --");
                        holder.textViewZone.setTextColor(0xFF757575);
                    }
                }
            });
        }
    }

    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChildName;
        TextView textViewLinkedDate;
        TextView textViewPEF;
        TextView textViewPB;
        TextView textViewZone;
        Button buttonUnlink;
        Button buttonGoToChild;
        
        String currentChildUid;
        Integer currentPEF;
        Integer currentPB;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChildName = itemView.findViewById(R.id.textViewChildName);
            textViewLinkedDate = itemView.findViewById(R.id.textViewLinkedDate);
            textViewPEF = itemView.findViewById(R.id.textViewPEF);
            textViewPB = itemView.findViewById(R.id.textViewPB);
            textViewZone = itemView.findViewById(R.id.textViewZone);
            buttonUnlink = itemView.findViewById(R.id.buttonUnlink);
            buttonGoToChild = itemView.findViewById(R.id.buttonGoToChild);
        }
    }
}

