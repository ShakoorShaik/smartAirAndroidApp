package com.example.smartair.provider;

public class ProviderCurrentParentData {

    protected String parentUid;

    protected  String parentEmail;


    public ProviderCurrentParentData() {}

    public ProviderCurrentParentData(String parentUid, String parentEmail) {
        this.parentUid = parentUid;
        this.parentEmail = parentEmail;
    }
}
