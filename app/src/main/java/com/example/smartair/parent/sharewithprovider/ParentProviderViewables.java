package com.example.smartair.parent.sharewithprovider;

public class ParentProviderViewables {

    private boolean rescueLogs = false;
    private boolean controllerSummary = false;
    private boolean symptomLog = false;
    private boolean triggerLog = false;
    private boolean peakFlow = false;
    private boolean triageIncident = false;
    private boolean summaryChart = false;

    public ParentProviderViewables() {}

    public void SetRescues(boolean rescueLogs) {
        this.rescueLogs=rescueLogs;
    }
    public void SetController(boolean controllerSummary) {
        this.controllerSummary=controllerSummary;
    }
    public void SetSymptom(boolean symptomLog) {
        this.symptomLog = symptomLog;
    }
    public void SetTrigger(boolean triggerLog) {
        this.triggerLog = triggerLog;
    }
    public void SetPeakFlow(boolean peakFlow) {
        this.peakFlow=peakFlow;
    }
    public void SetTriage(boolean triageIncident){
        this.triageIncident=triageIncident;
    }
    public void SetSummary(boolean summaryChart){
        this.summaryChart=summaryChart;
    }

    public boolean isRescueLogs() { return rescueLogs; }
    public boolean isControllerSummary() { return controllerSummary; }
    public boolean isSymptomLog() { return symptomLog; }
    public boolean isTriggerLog() { return triggerLog; }
    public boolean isPeakFlow() { return peakFlow; }
    public boolean isTriageIncident() { return triageIncident; }
    public boolean isSummaryChart() { return summaryChart; }
}
