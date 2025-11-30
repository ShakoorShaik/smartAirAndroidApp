package com.example.smartair.child.logtriggerandsymtomps;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChildrenTriggerCountAndDates {
    private String triggerName;
    private String date;

    public String getTriggerName() {
        return triggerName;
    }


    public String getDate() {
        return this.date;
    }

    public ChildrenTriggerCountAndDates() {}

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public ChildrenTriggerCountAndDates (String triggerName) {
        this.triggerName = triggerName;
        this.date = getTodayDate();
    }
}
