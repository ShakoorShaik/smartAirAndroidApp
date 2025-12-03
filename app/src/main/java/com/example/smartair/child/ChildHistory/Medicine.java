package com.example.smartair.child.ChildHistory;

import com.google.firebase.Timestamp;

public class Medicine {
    private Long doseCount;
    private String enteredBy;
    private String medicationType;
    private Long postDoseBreathRating;
    private String postDoseStatus;
    private Long preDoseBreathRating;
    private String preDoseStatus;

    private Timestamp timestamp;

    public Medicine(Long doseCount, String enteredBy, String medicationType, Long postDoseBreathRating, String postDoseStatus, Long preDoseBreathRating, String preDoseStatus, Timestamp timestamp){
        this.doseCount = doseCount;
        this.enteredBy = enteredBy;
        this.medicationType = medicationType;
        this.postDoseBreathRating = postDoseBreathRating;
        this.preDoseStatus = preDoseStatus;
        this.postDoseStatus = postDoseStatus;
        this.preDoseBreathRating = preDoseBreathRating;
        this.timestamp = timestamp;
    }

    public Long getDoseCount() {
        return doseCount;
    }

    public String getEnteredBy() {
        return enteredBy;
    }

    public String getMedicationType() {
        return medicationType;
    }

    public Long getPostDoseBreathRating() {
        return postDoseBreathRating;
    }

    public String getPostDoseStatus() {
        return postDoseStatus;
    }

    public Long getPreDoseBreathRating() {
        return preDoseBreathRating;
    }

    public String getPreDoseStatus() {
        return preDoseStatus;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
