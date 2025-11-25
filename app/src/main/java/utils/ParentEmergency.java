package utils;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParentEmergency {

    public static void listenEmergency(Fragment fragment){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            return;
        }
        String userId = user.getUid();
        CollectionReference userRef = db.collection("users");

        userRef.whereEqualTo("accountType", "child").whereEqualTo("parentUid", userId).addSnapshotListener((qS, e) -> {
                    if (e != null) return;
                    for (DocumentChange dc : qS.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            Boolean flag = dc.getDocument().getBoolean("emergencyFlag");
                            String name = dc.getDocument().getString("name");
                            if (flag == Boolean.FALSE){
                                emergencyPromptParent(name, fragment);
                            }
                        }
                    }
                });
    }

    public static void emergencyPromptParent(String name, Fragment fragment){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.requireContext());
        alertDialogBuilder.setTitle("Your child " + name + " is having an emergency.");
        alertDialogBuilder.setPositiveButton("DISMISS", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }
}
