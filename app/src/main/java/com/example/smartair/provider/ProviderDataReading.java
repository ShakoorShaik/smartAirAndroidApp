package com.example.smartair.provider;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderDataReading {

    private Context context;
    private static final String PREFS_NAME = "ProviderPrefs";
    private static final String CURRENT_CHILD_UID = "current_child_uid";
    private static final String CURRENT_CHILD_NAME = "current_child_name";

    public interface DateSearchCallback {
        void onSuccess(List<DateBasedDocument> documents);
        void onFailure(String message);
    }

    public interface TimestampSearchCallback {
        void onSuccess(List<TimestampDocument> documents);
        void onFailure(String message);
    }

    public interface AdherenceScheduleCallback {
        void onSuccess(AdherenceSummary schedule);
        void onFailure(String message);
    }

    public interface ParentUidCallback {
        void onSuccess(String parentUid, String parentEmail);
        void onFailure(String message);
    }

    public interface ChildrenListCallback {
        void onSuccess(List<ProviderCurrentChildData> children);
        void onFailure(String message);
    }

    public interface DateBasedSubcollectionCallback {
        void onSuccess(List<DateBasedDocument> documents);
        void onFailure(String message);
    }

    public interface TimestampDocumentCallback {
        void onSuccess(List<TimestampDocument> documents);
        void onFailure(String message);
    }

    public interface PermissionCallback {
        void onPermissionResult(boolean hasPermission);
        void onError(String message);
    }

    public static class DateBasedDocument {
        public String date;
        public Map<String, Object> data;

        public DateBasedDocument(String date, Map<String, Object> data) {
            this.date = date;
            this.data = data;
        }
    }

    public static class TimestampDocument {
        public String documentId;
        public Long timestamp;
        public Map<String, Object> data;

        public TimestampDocument(String documentId, Long timestamp, Map<String, Object> data) {
            this.documentId = documentId;
            this.timestamp = timestamp;
            this.data = data;
        }
    }

    public ProviderDataReading(Context context) {
        this.context = context;
    }

    public void getParentUid(ParentUidCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null) {
            callback.onFailure("User is null");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("linkedParents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot parentDoc = task.getResult().getDocuments().get(0);
                        String parentUid = parentDoc.getId();
                        String parentEmail = parentDoc.getString("email");

                        if (parentEmail != null) {
                            callback.onSuccess(parentUid, parentEmail);
                        } else {
                            callback.onFailure("Email not found");
                        }
                    } else {
                        callback.onFailure("No parent linked");
                    }
                });
    }

    public void getChildrenForParent(String parentUid, ChildrenListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(parentUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot parentDoc = task.getResult();

                        if (parentDoc.exists()) {
                            Object childrenObj = parentDoc.get("linkedChildren");

                            if (childrenObj instanceof List) {
                                List<Map<String, Object>> linkedChildren = (List<Map<String, Object>>) childrenObj;
                                List<ProviderCurrentChildData> children = new ArrayList<>();

                                for (Map<String, Object> childMap : linkedChildren) {
                                    String uid = (String) childMap.get("uid");
                                    String name = (String) childMap.get("name");

                                    if (uid != null && name != null) {
                                        children.add(new ProviderCurrentChildData(uid, name));
                                    }
                                }

                                callback.onSuccess(children);
                            } else {
                                callback.onFailure("No children array found");
                            }
                        } else {
                            callback.onFailure("Parent not found");
                        }
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void setCurrentChild(String uid, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CURRENT_CHILD_UID, uid);
        editor.putString(CURRENT_CHILD_NAME, name);
        editor.apply();
    }

    public String getCurrentChildName() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(CURRENT_CHILD_NAME, "No Child Selected");
    }

    public String getCurrentChildUid() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(CURRENT_CHILD_UID, null);
    }

    public void getDateBasedSubcollection(String subcollectionName, DateBasedSubcollectionCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .collection(subcollectionName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DateBasedDocument> documents = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                documents.add(new DateBasedDocument(document.getId(), data));
                            }
                        }

                        documents.sort((d1, d2) -> d2.date.compareTo(d1.date));
                        callback.onSuccess(documents);
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void getTimestampBasedSubcollection(String subcollectionName, TimestampDocumentCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .collection(subcollectionName)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TimestampDocument> documents = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            Long timestamp = null;
                            Map<String, Object> data = document.getData();

                            Object timestampObj = document.get("timestamp");

                            if (timestampObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp firestoreTimestamp = (com.google.firebase.Timestamp) timestampObj;
                                timestamp = firestoreTimestamp.toDate().getTime();
                            } else if (timestampObj instanceof Long) {
                                timestamp = (Long) timestampObj;
                            } else if (timestampObj instanceof Integer) {
                                timestamp = ((Integer) timestampObj).longValue();
                            }

                            if (timestamp != null && data != null) {
                                documents.add(new TimestampDocument(document.getId(), timestamp, data));
                            }
                        }

                        callback.onSuccess(documents);
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void checkChildPermission(String permissionKey, PermissionCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onError("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot childDoc = task.getResult();
                        if (childDoc.exists()) {
                            Map<String, Object> settings = (Map<String, Object>) childDoc.get("settings");

                            if (settings != null && settings.containsKey(permissionKey)) {
                                Object value = settings.get(permissionKey);
                                boolean hasPermission = false;

                                if (value instanceof Boolean) {
                                    hasPermission = (Boolean) value;
                                } else if (value instanceof String) {
                                    hasPermission = Boolean.parseBoolean((String) value);
                                }

                                callback.onPermissionResult(hasPermission);
                            } else {
                                callback.onPermissionResult(false);
                            }
                        } else {
                            callback.onError("Child document not found");
                        }
                    } else {
                        callback.onError("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void getAdherenceSchedule(AdherenceScheduleCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot childDoc = task.getResult();
                        if (childDoc.exists()) {
                            Map<String, Object> adherenceScheduleData = (Map<String, Object>) childDoc.get("adherenceSchedule");

                            if (adherenceScheduleData != null) {
                                AdherenceSummary schedule = new AdherenceSummary();

                                if (adherenceScheduleData.containsKey("doseTimes")) {
                                    Object doseTimesObj = adherenceScheduleData.get("doseTimes");
                                    if (doseTimesObj instanceof List) {
                                        List<String> doseTimes = (List<String>) doseTimesObj;
                                        schedule.setDoseTimes(doseTimes);
                                    }
                                }

                                if (adherenceScheduleData.containsKey("frequency")) {
                                    Object frequencyObj = adherenceScheduleData.get("frequency");
                                    if (frequencyObj instanceof String) {
                                        schedule.setFrequency((String) frequencyObj);
                                    }
                                }

                                callback.onSuccess(schedule);
                            } else {
                                callback.onFailure("No adherence schedule found for this child");
                            }
                        } else {
                            callback.onFailure("Child document not found");
                        }
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void searchDateBasedByDate(String subcollectionName, String date, DateSearchCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (date == null || date.isEmpty()) {
            getDateBasedSubcollection(subcollectionName, new DateBasedSubcollectionCallback() {
                @Override
                public void onSuccess(List<DateBasedDocument> documents) {
                    callback.onSuccess(documents);
                }

                @Override
                public void onFailure(String message) {
                    callback.onFailure(message);
                }
            });
            return;
        }

        db.collection("users")
                .document(childUid)
                .collection(subcollectionName)
                .document(date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            List<DateBasedDocument> documents = new ArrayList<>();
                            documents.add(new DateBasedDocument(date, data));
                            callback.onSuccess(documents);
                        } else {
                            callback.onFailure("No data found for date: " + date);
                        }
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void searchTriggersByType(String triggerType, DateSearchCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .collection("triggerLogs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DateBasedDocument> filteredDocs = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            if (data != null && (triggerType.isEmpty() || data.containsKey(triggerType))) {
                                if (triggerType.isEmpty()) {
                                    filteredDocs.add(new DateBasedDocument(document.getId(), data));
                                } else {
                                    Map<String, Object> filteredData = new HashMap<>();
                                    filteredData.put(triggerType, data.get(triggerType));
                                    filteredDocs.add(new DateBasedDocument(document.getId(), filteredData));
                                }
                            }
                        }

                        filteredDocs.sort((d1, d2) -> d2.date.compareTo(d1.date));
                        callback.onSuccess(filteredDocs);
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void searchSymptomsByType(String symptomType, DateSearchCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .collection("symptomLogs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DateBasedDocument> filteredDocs = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            if (data != null && (symptomType.isEmpty() || data.containsKey(symptomType))) {
                                if (symptomType.isEmpty()) {
                                    filteredDocs.add(new DateBasedDocument(document.getId(), data));
                                } else {
                                    Map<String, Object> filteredData = new HashMap<>();
                                    filteredData.put(symptomType, data.get(symptomType));
                                    filteredDocs.add(new DateBasedDocument(document.getId(), filteredData));
                                }
                            }
                        }

                        filteredDocs.sort((d1, d2) -> d2.date.compareTo(d1.date));
                        callback.onSuccess(filteredDocs);
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }

    public void searchTimestampByDate(String subcollectionName, String date, TimestampSearchCallback callback) {
        String childUid = getCurrentChildUid();

        if (childUid == null) {
            callback.onFailure("No child selected");
            return;
        }

        if (date == null || date.isEmpty()) {
            getTimestampBasedSubcollection(subcollectionName, new TimestampDocumentCallback() {
                @Override
                public void onSuccess(List<TimestampDocument> documents) {
                    callback.onSuccess(documents);
                }

                @Override
                public void onFailure(String message) {
                    callback.onFailure(message);
                }
            });
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(childUid)
                .collection(subcollectionName)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TimestampDocument> filteredDocs = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            Long timestamp = null;
                            Map<String, Object> data = document.getData();

                            Object timestampObj = document.get("timestamp");
                            if (timestampObj instanceof com.google.firebase.Timestamp) {
                                com.google.firebase.Timestamp firestoreTimestamp = (com.google.firebase.Timestamp) timestampObj;
                                timestamp = firestoreTimestamp.toDate().getTime();
                            } else if (timestampObj instanceof Long) {
                                timestamp = (Long) timestampObj;
                            } else if (timestampObj instanceof Integer) {
                                timestamp = ((Integer) timestampObj).longValue();
                            }

                            if (timestamp != null && data != null) {
                                String documentDate = DateHelper.formatTimestampToDate(timestamp);
                                if (documentDate.equals(date)) {
                                    filteredDocs.add(new TimestampDocument(document.getId(), timestamp, data));
                                }
                            }
                        }

                        callback.onSuccess(filteredDocs);
                    } else {
                        callback.onFailure("Error: " + task.getException().getMessage());
                    }
                });
    }
}