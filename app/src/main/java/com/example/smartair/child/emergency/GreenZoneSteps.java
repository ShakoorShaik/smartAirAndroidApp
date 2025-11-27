package com.example.smartair.child.emergency;

public class GreenZoneSteps extends ZoneSteps{
    @Override
    protected void zoneSteps(){
        steps.append("YOU ARE IN ZONE GREEN\n\n");
        steps.append("- Continue daily controller medication\n\n");
        steps.append("- Avoid known triggers\n\n");

    }

}
