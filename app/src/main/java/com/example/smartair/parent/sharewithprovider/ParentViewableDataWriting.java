package com.example.smartair.parent.sharewithprovider;

import android.os.Bundle;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import utils.DatabaseManager;

public class ParentViewableDataWriting {

    public static void SaveSetting(ParentProviderViewables settingData, DatabaseManager.SuccessFailCallback callBack) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callBack.onFailure(new Exception("User is null"));
            return;
        }

        String docPath = "settings";
        Map<String, Object> nestedSettings = new HashMap<>();

        nestedSettings.put("rescueLogs", settingData.isRescueLogs());
        nestedSettings.put("controllerSummary", settingData.isControllerSummary());
        nestedSettings.put("symptomLog", settingData.isSymptomLog());
        nestedSettings.put("triggerLog", settingData.isTriggerLog());
        nestedSettings.put("peakFlow", settingData.isPeakFlow());
        nestedSettings.put("triageIncident", settingData.isTriageIncident());
        nestedSettings.put("summaryChart", settingData.isSummaryChart());

        DatabaseManager.writeData(docPath, nestedSettings, callBack);

    }

}
