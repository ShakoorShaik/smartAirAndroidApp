package com.example.smartair.parent.sharewithprovider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParentViewableDataWriting {

    private FirebaseFirestore firestore;

    public ParentViewableDataWriting() {
        firestore = FirebaseFirestore.getInstance();
    }

    public interface ChildrenLoadCallback {
        void onSuccess(List<Map<String, Object>> children);
        void onFailure(Exception e);
    }

    public interface SettingsLoadCallback {
        void onSuccess(ParentProviderViewables settings);
        void onFailure(Exception e);
    }

    public interface SettingsSaveCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void getLinkedChildren(String parentUid, ChildrenLoadCallback callback) {
        firestore.collection("users").document(parentUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> children = (List<Map<String, Object>>) document.get("linkedChildren");
                            if (children == null) {
                                children = new ArrayList<>();
                            }
                            callback.onSuccess(children);
                        } else {
                            callback.onFailure(new Exception("Document does not exist"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void loadChildSettings(String childUid, SettingsLoadCallback callback) {
        if (childUid == null || childUid.isEmpty()) {
            callback.onSuccess(new ParentProviderViewables());
            return;
        }

        firestore.collection("users").document(childUid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            try {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> settingsMap = (Map<String, Object>) document.get("settings");
                                ParentProviderViewables settings = mapToParentProviderViewables(settingsMap);
                                callback.onSuccess(settings);
                            } catch (Exception e) {
                                callback.onSuccess(new ParentProviderViewables());
                            }
                        } else {
                            callback.onSuccess(new ParentProviderViewables());
                        }
                    } else {
                        callback.onSuccess(new ParentProviderViewables());
                    }
                });
    }

    public void saveChildSettings(String childUid, ParentProviderViewables settings,
                                  SettingsSaveCallback callback) {
        if (childUid == null || childUid.isEmpty()) {
            callback.onFailure(new Exception("Invalid child UID"));
            return;
        }

        Map<String, Object> settingsMap = parentProviderViewablesToMap(settings);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("settings", settingsMap);

        firestore.collection("users").document(childUid)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    private Map<String, Object> parentProviderViewablesToMap(ParentProviderViewables settings) {
        Map<String, Object> map = new HashMap<>();
        map.put("rescueLogs", settings.isRescueLogs());
        map.put("controllerSummary", settings.isControllerSummary());
        map.put("symptomLog", settings.isSymptomLog());
        map.put("triggerLog", settings.isTriggerLog());
        map.put("peakFlow", settings.isPeakFlow());
        map.put("triageIncident", settings.isTriageIncident());
        map.put("summaryChart", settings.isSummaryChart());
        return map;
    }

    private ParentProviderViewables mapToParentProviderViewables(Map<String, Object> map) {
        ParentProviderViewables settings = new ParentProviderViewables();

        if (map == null) return settings;

        try {
            if (map.containsKey("rescueLogs")) {
                Object rescueLogsObj = map.get("rescueLogs");
                if (rescueLogsObj instanceof Boolean) {
                    settings.SetRescues((Boolean) rescueLogsObj);
                }
            }
            if (map.containsKey("controllerSummary")) {
                Object controllerSummaryObj = map.get("controllerSummary");
                if (controllerSummaryObj instanceof Boolean) {
                    settings.SetController((Boolean) controllerSummaryObj);
                }
            }
            if (map.containsKey("symptomLog")) {
                Object symptomLogObj = map.get("symptomLog");
                if (symptomLogObj instanceof Boolean) {
                    settings.SetSymptom((Boolean) symptomLogObj);
                }
            }
            if (map.containsKey("triggerLog")) {
                Object triggerLogObj = map.get("triggerLog");
                if (triggerLogObj instanceof Boolean) {
                    settings.SetTrigger((Boolean) triggerLogObj);
                }
            }
            if (map.containsKey("peakFlow")) {
                Object peakFlowObj = map.get("peakFlow");
                if (peakFlowObj instanceof Boolean) {
                    settings.SetPeakFlow((Boolean) peakFlowObj);
                }
            }
            if (map.containsKey("triageIncident")) {
                Object triageIncidentObj = map.get("triageIncident");
                if (triageIncidentObj instanceof Boolean) {
                    settings.SetTriage((Boolean) triageIncidentObj);
                }
            }
            if (map.containsKey("summaryChart")) {
                Object summaryChartObj = map.get("summaryChart");
                if (summaryChartObj instanceof Boolean) {
                    settings.SetSummary((Boolean) summaryChartObj);
                }
            }
        } catch (Exception e) {
            // Keep empty catch block as in original style
        }

        return settings;
    }
}