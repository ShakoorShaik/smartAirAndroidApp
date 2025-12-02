package com.example.smartair.provider;

import java.util.List;

public class AdherenceSummary {
    private List<String> doseTimes;
    private String frequency;
    private long updatedAt;

    public AdherenceSummary() {
    }

    public AdherenceSummary(List<String> doseTimes, String frequency, long updatedAt) {
        this.doseTimes = doseTimes;
        this.frequency = frequency;
        this.updatedAt = updatedAt;
    }

    public List<String> getDoseTimes() { return doseTimes; }
    public void setDoseTimes(List<String> doseTimes) { this.doseTimes = doseTimes; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String formatDoseTimes() {
        if (doseTimes == null || doseTimes.isEmpty()) {
            return "No dose times set";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < doseTimes.size(); i++) {
            builder.append(doseTimes.get(i));
            if (i < doseTimes.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    public String getFormattedUpdatedTime() {
        if (updatedAt <= 0) return "Never updated";

        long currentTime = System.currentTimeMillis();
        long diff = currentTime - updatedAt;

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }
}