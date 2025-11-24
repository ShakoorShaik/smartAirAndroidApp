package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ZoneManager {
    
    public enum Zone {
        GREEN, YELLOW, RED
    }

    public static Zone calculateZone(int pefValue, int personalBest) {
        if (personalBest <= 0) {
            return Zone.GREEN;
        }
        
        double percentage = (double) pefValue / personalBest * 100;
        
        if (percentage >= 80) {
            return Zone.GREEN;
        } else if (percentage >= 50) {
            return Zone.YELLOW;
        } else {
            return Zone.RED;
        }
    }


    public static void getTodayZone(String childUid, ZoneCallback callback) {
        PBManager.getPB(childUid, new PBManager.PBCallback() {
            @Override
            public void onSuccess(Integer pbValue) {
                if (pbValue == null) {
                    callback.onSuccess(Zone.GREEN, null);
                    return;
                }

                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
                
                PEFManager.getPEFByDate(childUid, today, new PEFManager.PEFCallback() {
                    @Override
                    public void onSuccess(Integer pefValue) {
                        if (pefValue == null) {
                            callback.onSuccess(Zone.GREEN, null);
                        } else {
                            Zone zone = calculateZone(pefValue, pbValue);
                            callback.onSuccess(zone, pefValue);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onSuccess(Zone.GREEN, null);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public static void logZoneChange(String childUid, Zone zone, int pefValue, String date, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        
        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }
        
        Map<String, Object> zoneLog = new HashMap<>();
        zoneLog.put("childUid", childUid);
        zoneLog.put("zone", zone.name());
        zoneLog.put("pefValue", pefValue);
        zoneLog.put("date", date);
        zoneLog.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .collection("zoneHistory")
                .add(zoneLog)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void checkAndAlertRedZone(String childUid, String childName, int pefValue, int personalBest, String date) {
        Zone zone = calculateZone(pefValue, personalBest);
        
        if (zone == Zone.RED) {
            logZoneChange(childUid, zone, pefValue, date, new DatabaseManager.SuccessFailCallback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(Exception e) {
                }
            });
        }
    }

    public interface ZoneCallback {
        void onSuccess(Zone zone, Integer pefValue);
        void onFailure(Exception e);
    }
}

