package com.example.smartair.child.emergency;

public class YellowZoneSteps extends ZoneSteps{
    @Override
    protected void zoneSteps(){
        steps.append("YOU ARE IN ZONE YELLOW\n\n");
        steps.append("- Use quick-relief medication (short-acting bronchodilator) as directed\n\n");
        steps.append("- Continue controller medication\n\n");
    }
}
