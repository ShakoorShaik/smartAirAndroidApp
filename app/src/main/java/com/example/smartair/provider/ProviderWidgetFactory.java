package com.example.smartair.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.smartair.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProviderWidgetFactory {

    private Context context;

    public ProviderWidgetFactory(Context context) {
        this.context = context;
    }

    public View createDateWidget(String date, Map<String, Object> data, String subcollectionName) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View widgetView = inflater.inflate(R.layout.widget_date_item, null);

        TextView dateText = widgetView.findViewById(R.id.dateText);
        Button viewButton = widgetView.findViewById(R.id.viewButton);

        dateText.setText(formatDateString(date));

        viewButton.setOnClickListener(v -> {
            showDateDetailsDialog(date, data, subcollectionName);
        });

        return widgetView;
    }

    public View createTimestampWidget(Long timestamp, Map<String, Object> data, String subcollectionName, String documentId) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View widgetView = inflater.inflate(R.layout.widget_timestamp_item, null);

        TextView timestampText = widgetView.findViewById(R.id.timestampText);
        Button viewButton = widgetView.findViewById(R.id.viewButton);

        String formattedDate = formatTimestamp(timestamp);
        timestampText.setText(formattedDate);

        viewButton.setOnClickListener(v -> {
            showTimestampDetailsDialog(timestamp, data, subcollectionName, documentId);
        });

        return widgetView;
    }

    private String formatDateString(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) return "Unknown date";

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void showDateDetailsDialog(String date, Map<String, Object> data, String subcollectionName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(formatDateString(date));

        StringBuilder details = new StringBuilder();

        if (data == null || data.isEmpty()) {
            details.append("No data available for this date");
        } else {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String triggerType = entry.getKey();
                Object value = entry.getValue();

                details.append(triggerType).append(": ");

                if (value instanceof List) {
                    List<?> timestampList = (List<?>) value;
                    details.append("#entries: ").append(timestampList.size()).append("\n");

                    for (Object item : timestampList) {
                        if (item instanceof Long) {
                            Date time = new Date((Long) item);
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            details.append("- ").append(sdf.format(time)).append("\n");
                        } else {
                            details.append("- ").append(String.valueOf(item)).append("\n");
                        }
                    }
                } else {
                    details.append(String.valueOf(value));
                }

                details.append("\n");
            }
        }

        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTimestampDetailsDialog(Long timestamp, Map<String, Object> data, String subcollectionName, String documentId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if ("zoneHistory".equals(subcollectionName)) {
            builder.setTitle("Peak Flow Entry");

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedTime = sdf.format(new Date(timestamp));

            StringBuilder details = new StringBuilder();
            details.append("Time: ").append(formattedTime).append("\n\n");

            if (data != null) {
                if (data.containsKey("pefValue")) {
                    details.append("PEF Value: ").append(data.get("pefValue")).append(" L/min\n");
                }
                if (data.containsKey("zone")) {
                    details.append("Zone: ").append(data.get("zone")).append("\n");
                }
            }

            builder.setMessage(details.toString());
        } else {
            builder.setTitle("Entry Details");

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(new Date(timestamp));

            StringBuilder details = new StringBuilder();
            details.append("Time: ").append(formattedDate).append("\n\n");

            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    if (!entry.getKey().equals("timestamp")) {
                        details.append(entry.getKey()).append(": ").append(String.valueOf(entry.getValue())).append("\n");
                    }
                }
            }

            builder.setMessage(details.toString());
        }

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}