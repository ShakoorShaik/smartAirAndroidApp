package utils;

import android.content.Context;
import android.content.SharedPreferences;

public class OnboardingManager {
    private static final String PREFS_NAME = "SmartAirPref";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String KEY_ONBOARDING_SHOWN = "onboarding_shown";

    public static boolean isOnboardingCompleted(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public static boolean isOnboardingShown(Context context) {
        if (context == null) return true;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_SHOWN, false);
    }

    public static void setOnboardingCompleted(Context context, boolean completed) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, completed);
        editor.putBoolean(KEY_ONBOARDING_SHOWN, true);
        editor.apply();
    }

    public static void checkAndSetOnboardingStatus(Context context, OnboardingCheckCallback callback) {
        if (context == null) {
            callback.onResult(true);
            return;
        }

        if (isOnboardingCompleted(context)) {
            callback.onResult(true);
            return;
        }

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(java.util.List<java.util.Map<String, Object>> children) {
                if (children != null && !children.isEmpty()) {
                    setOnboardingCompleted(context, true);
                    callback.onResult(true);
                } else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onResult(false);
            }
        });
    }

    public interface OnboardingCheckCallback {
        void onResult(boolean shouldSkipOnboarding);
    }
}

