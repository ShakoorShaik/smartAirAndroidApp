package utils;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TriageHistoryManager {

    public interface TriageDatesCallback {
        void onSuccess(Map<LocalDate, Boolean> triageDates);
        void onFailure(Exception e);
    }

    public static void loadTriageDates(Context context, TriageDatesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        ChildAccountManager.getLinkedChildren(new ChildAccountManager.ChildrenListCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> children) {
                if (children == null || children.isEmpty()) {
                    callback.onSuccess(new HashMap<>());
                    return;
                }

                Map<LocalDate, Boolean> triageDates = new HashMap<>();
                final int[] completedCount = {0};
                final int totalChildren = children.size();

                if (totalChildren == 0) {
                    callback.onSuccess(triageDates);
                    return;
                }

                for (Map<String, Object> child : children) {
                    String childUid = (String) child.get("uid");
                    if (childUid == null || childUid.isEmpty()) {
                        completedCount[0]++;
                        if (completedCount[0] == totalChildren) {
                            callback.onSuccess(triageDates);
                        }
                        continue;
                    }

                    db.collection("users").document(childUid)
                            .collection("TriageHistory")
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        try {
                                            Long timestamp = document.getLong("timestamp");
                                            if (timestamp != null) {
                                                LocalDate date = new java.util.Date(timestamp)
                                                        .toInstant()
                                                        .atZone(ZoneId.systemDefault())
                                                        .toLocalDate();
                                                triageDates.put(date, true);
                                            }
                                        } catch (Exception e) {
                                            // Skip invalid entries
                                        }
                                    }
                                }
                                completedCount[0]++;
                                if (completedCount[0] == totalChildren) {
                                    callback.onSuccess(triageDates);
                                }
                            })
                            .addOnFailureListener(e -> {
                                completedCount[0]++;
                                if (completedCount[0] == totalChildren) {
                                    callback.onSuccess(triageDates);
                                }
                            });
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}

