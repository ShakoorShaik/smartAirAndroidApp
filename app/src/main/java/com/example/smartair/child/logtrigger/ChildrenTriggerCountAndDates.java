package com.example.smartair.child.logtrigger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ChildrenTriggerCountAndDates {
    private String triggerName;
    private int count;
    private String date;

    public String getTriggerName() {
        return triggerName;
    }

    public int getCount() {
        return count;
    }

    public String getDate() {
        return this.date;
    }

    public ChildrenTriggerCountAndDates() {}

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public ChildrenTriggerCountAndDates (String triggerName, int count) {
        this.triggerName = triggerName;
        this.count = count;
        this.date = getTodayDate();
    }
}
