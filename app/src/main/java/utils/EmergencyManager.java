package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;
import com.google.firebase.Timestamp;

public class EmergencyManager {
    public static void logTriage(String userId, boolean csfs, boolean cp, boolean lnbg, boolean rra, boolean o, String guidance) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        CollectionReference triageRef = db.collection("users").document(userId).collection("TriageHistory");

        Map<String, Object> triageData = new HashMap<>();
        triageData.put("I cannot speak full sentences.", csfs);
        triageData.put("I am having chest pulls.", cp);
        triageData.put("My lips/nails are blue/grey.", lnbg);
        triageData.put("Have you taken rescue medicine in the past 20 minutes?", rra);
        triageData.put("Other symptoms", o);
        triageData.put("guidance", o);
        triageData.put("timestamp", Timestamp.now());

        triageRef.add(triageData);
    }
    public static void toggleEmergencyFlag(String userId){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(dS -> {
            docRef.update("emergencyFlag", false).addOnSuccessListener(v -> {
                docRef.update("emergencyFlag", true);
            });
        });
    }

}
