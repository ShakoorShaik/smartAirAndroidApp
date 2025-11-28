package utils;

import android.app.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class ParentRescue {

    private static final Integer defaultThreshold = 3;
    public interface PRCallback {
        void onSuccess(Integer number);
        void onFailure(Exception e);
    }
    public static void listenRescue(Fragment fragment){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            return;
        }
        String userId = user.getUid();
        CollectionReference userRef = db.collection("users");

        userRef.whereEqualTo("accountType", "Child").whereEqualTo("parentUid", userId).addSnapshotListener((qS, e) -> {
            if (e != null) return;
            for (DocumentChange dc : qS.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.MODIFIED) {
                    DocumentSnapshot doc = dc.getDocument();
                    String ChildUid = doc.getId();
                    Boolean flag = dc.getDocument().getBoolean("rescueFlag");
                    String name = dc.getDocument().getString("name");
                    if (flag == Boolean.FALSE){
                        countRescueAttempts(ChildUid, new PRCallback() {
                            @Override
                            public void onSuccess(Integer number) {
                                Integer rescueCount = number;
                                DatabaseManager.getData("rescueHourThreshold", new DatabaseManager.DataSuccessFailCallback() {
                                    @Override
                                    public void onSuccess(String data) {
                                        Integer threshold = Integer.getInteger(data);
                                        if (threshold == null){
                                            if (rescueCount >= defaultThreshold){
                                                rescuePromptParent(name, rescueCount, defaultThreshold, fragment);
                                            }
                                        }
                                        else {
                                            if (rescueCount >= threshold) {
                                            rescuePromptParent(name, rescueCount, threshold, fragment);
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        if (rescueCount >= defaultThreshold){
                                            rescuePromptParent(name, rescueCount, defaultThreshold, fragment);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                }
            }
        });
    }

    public static void countRescueAttempts(String childUid, PRCallback callback){
        long threeHours = 3L * 60L * 60L * 1000L;
        Timestamp threeHoursTimestamp = new Timestamp(new Date(System.currentTimeMillis() - threeHours));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(childUid)
                .collection("inhaler_log")
                .whereEqualTo("medicationType", "Rescue")
                .whereGreaterThanOrEqualTo("timestamp", threeHoursTimestamp)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {;
                        int count = task.getResult().size();
                        callback.onSuccess(count);
                    } else {
                        callback.onSuccess(0);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    public static void rescuePromptParent(String name, Integer rescueCount, Integer rescueThreshold,Fragment fragment){
        Activity activity = fragment.getActivity();
        if (activity != null && !activity.isFinishing()) {
            AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
            alertDialog.setTitle("Your child " + name + " attempted " + rescueCount + " rescues, greater than " + rescueThreshold + " threshold in the span of 3 hours.");
            alertDialog.show();
        }
    }
}
