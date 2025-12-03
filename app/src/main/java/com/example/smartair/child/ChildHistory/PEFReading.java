package com.example.smartair.child.ChildHistory;

public class PEFReading {
    private String date;
    private Long timestamp;
    private Long value;

    public PEFReading(String date, Long timestamp, Long value) {
        this.date = date;
        this.timestamp = timestamp;
        this.value = value;
    }

    public String getDate() {
        return date;
    }
    public Long getTimestamp() {
        return timestamp;
    }
    public Long getValue() {
        return value;
    }
}
