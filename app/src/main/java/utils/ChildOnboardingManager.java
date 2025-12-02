package utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ChildOnboardingManager {
    private static final String PREFS_NAME = "SmartAirPref";
    private static final String KEY_CHILD_ONBOARDING_COMPLETED = "child_onboarding_completed_";
    private static final String KEY_CHILD_ONBOARDING_SHOWN = "child_onboarding_shown_";

    private static String getOnboardingCompletedKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return KEY_CHILD_ONBOARDING_COMPLETED + user.getUid();
        }
        return KEY_CHILD_ONBOARDING_COMPLETED;
    }

    private static String getOnboardingShownKey() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return KEY_CHILD_ONBOARDING_SHOWN + user.getUid();
        }
        return KEY_CHILD_ONBOARDING_SHOWN;
    }

    public static boolean isOnboardingCompleted(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(getOnboardingCompletedKey(), false);
    }

    public static boolean isOnboardingShown(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(getOnboardingShownKey(), false);
    }

    public static void setOnboardingCompleted(Context context, boolean completed) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getOnboardingCompletedKey(), completed);
        editor.putBoolean(getOnboardingShownKey(), true);
        editor.apply();
    }


    public static void checkAndSetOnboardingStatus(Context context, ChildOnboardingCheckCallback callback) {
        if (context == null) {
            callback.onResult(true);
            return;
        }

        if (isOnboardingCompleted(context)) {
            callback.onResult(true);
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onResult(false);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String childUid = user.getUid();

        db.collection("users").document(childUid)
                .collection("pefReadings")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        setOnboardingCompleted(context, true);
                        callback.onResult(true);
                    } else {
                        db.collection("users").document(childUid)
                                .collection("inhaler_log")
                                .limit(1)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && task2.getResult() != null && !task2.getResult().isEmpty()) {
                                        setOnboardingCompleted(context, true);
                                        callback.onResult(true);
                                    } else {
                                        callback.onResult(false);
                                    }
                                })
                                .addOnFailureListener(e2 -> {
                                    callback.onResult(false);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    db.collection("users").document(childUid)
                            .collection("inhaler_log")
                            .limit(1)
                            .get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && task2.getResult() != null && !task2.getResult().isEmpty()) {
                                    setOnboardingCompleted(context, true);
                                    callback.onResult(true);
                                } else {
                                    callback.onResult(false);
                                }
                            })
                            .addOnFailureListener(e2 -> {
                                callback.onResult(false);
                            });
                });
    }

    public interface ChildOnboardingCheckCallback {
        void onResult(boolean shouldSkipOnboarding);
    }
}

