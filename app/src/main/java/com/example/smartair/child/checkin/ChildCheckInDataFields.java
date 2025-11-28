package com.example.smartair.child.checkin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChildCheckInDataFields {
    public String nightWaking;
    public String coughWheeze;
    public String activityLimits;
    public String notes;

    public String date;
    public String userEmail; //uh might not need
    public String userId;

    public ChildCheckInDataFields() {}

    public ChildCheckInDataFields (String nightWaking, String coughWheeze,
                                          String activityLimits, String notes) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            this.userEmail = user.getEmail();
            this.userId = user.getUid();
        }

        this.nightWaking = nightWaking;
        this.coughWheeze = coughWheeze;
        this.activityLimits = activityLimits;
        this.notes = notes;
        this.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}
