package com.example.smartair.child.ChildHistory;

public class DailyCheckIn {
    private String date;

    private String activityLimits;

    private String coughWheeze;

    private Boolean enteredByParent;

    private String nightWaking;

    private String notes;

    private String userEmail;
    private Long timestamp;

    public DailyCheckIn(String date, Long timestamp, String al, String cw, Boolean ebp, String nw, String n, String ue) {
        this.date = date;
        this.timestamp = timestamp;
        this.activityLimits = al;
        this.coughWheeze = cw;
        this.enteredByParent = ebp;
        this.nightWaking = nw;
        this.notes = n;
        this.userEmail = ue;
    }

    public String getDate() {
        return date;
    }

    public String getActivityLimits() {
        return activityLimits;
    }

    public String getCoughWheeze() {
        return coughWheeze;
    }

    public Boolean getEnteredByParent() {
        return enteredByParent;
    }

    public String getNightWaking() {
        return nightWaking;
    }

    public String getNotes() {
        return notes;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
