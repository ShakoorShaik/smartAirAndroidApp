package utils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InhalerTechniqueManager {
    public static void logCorrectInhalerUse(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference techniqueRef = db.collection("users").document(userId).collection("InhalerTechniquePracticeHistory");
        Map<String, Object> InhalerData = new HashMap<>();

        InhalerData.put("timestamp", Timestamp.now());

        techniqueRef.add(InhalerData);
    }
}
