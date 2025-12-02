package com.example.smartair.provider.utils;

public class ProviderCurrentChildData {


    protected String childName;

    protected String childUid; //uid of current child

    //viewable toggles
    protected Boolean pefViewable = false;

    protected Boolean triggerSymptomViewable = false;

    protected Boolean triageViewable = false;

    protected Boolean rescueViewable = false;

    protected Boolean adherenceViewable = false;

    public ProviderCurrentChildData() {}

    public ProviderCurrentChildData(String uid, String name) {
        this.childUid = uid;
        this.childName = name;
    }

}
