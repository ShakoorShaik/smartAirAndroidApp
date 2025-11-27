package com.example.smartair.child.emergency;

public class RedZoneSteps extends ZoneSteps{
    @Override
    protected void zoneSteps(){
        steps.append("YOU ARE IN ZONE RED\n\n");
        steps.append("Seek urgent medical care immediately\n\n");
    }
}
