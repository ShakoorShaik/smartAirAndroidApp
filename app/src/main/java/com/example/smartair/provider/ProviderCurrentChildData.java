package com.example.smartair.provider;

public class ProviderCurrentChildData {
    protected String childName;
    protected String childUid;
    protected Boolean pefViewable = false;
    protected Boolean triggerViewable = false;
    protected Boolean symptomViewable = false;
    protected Boolean triageViewable = false;
    protected Boolean rescueViewable = false;
    protected Boolean adherenceViewable = false;

    public ProviderCurrentChildData() {}

    public ProviderCurrentChildData(String uid, String name) {
        this.childUid = uid;
        this.childName = name;
    }

    public void updatePermissionsFromMap(java.util.Map<String, Object> settingsMap) {
        if (settingsMap != null) {
            pefViewable = getBooleanFromMap(settingsMap, "peakFlow");
            triggerViewable = getBooleanFromMap(settingsMap, "triggerLog");
            symptomViewable = getBooleanFromMap(settingsMap, "symptomLog");
            triageViewable = getBooleanFromMap(settingsMap, "triageIncident");
            rescueViewable = getBooleanFromMap(settingsMap, "rescueLogs");
            adherenceViewable = getBooleanFromMap(settingsMap, "controllerSummary");
        }
    }

    private boolean getBooleanFromMap(java.util.Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }
        return false;
    }

    public boolean canViewPEF() { return pefViewable != null && pefViewable; }
    public boolean canViewTrigger() { return triggerViewable != null && triggerViewable; }
    public boolean canViewSymptom() { return symptomViewable != null && symptomViewable; }
    public boolean canViewTriage() { return triageViewable != null && triageViewable; }
    public boolean canViewRescue() { return rescueViewable != null && rescueViewable; }
    public boolean canViewAdherence() { return adherenceViewable != null && adherenceViewable; }
}