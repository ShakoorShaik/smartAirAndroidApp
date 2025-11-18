package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildAccountManager {
    public interface ChildrenListCallback {
        void onSuccess(List<Map<String, Object>> children);
        void onFailure(Exception e);
    }

    public static void linkChildToParent(String parentUid, String childUid, String childName, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> childInfo = new HashMap<>();
        childInfo.put("uid", childUid);
        childInfo.put("name", childName);
        childInfo.put("linkedAt", System.currentTimeMillis());
        db.collection("users").document(parentUid)
                .update("linkedChildren", com.google.firebase.firestore.FieldValue.arrayUnion(childInfo))
                .addOnSuccessListener(aVoid -> {
                    Map<String, Object> parentRef = new HashMap<>();
                    parentRef.put("parentUid", parentUid);
                    db.collection("users").document(childUid)
                            .set(parentRef, SetOptions.merge())
                            .addOnSuccessListener(aVoid1 -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void getLinkedChildren(ChildrenListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("User is null"));
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> children = (List<Map<String, Object>>) document.get("linkedChildren");
                            if (children == null) {
                                children = new ArrayList<>();
                            }
                            callback.onSuccess(children);
                        } else {
                            callback.onFailure(new Exception("Document does not exist"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public static void unlinkChildFromParent(String childUid, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure(new Exception("User is null"));
            return;
        }

        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> children = (List<Map<String, Object>>) document.get("linkedChildren");
                            if (children != null) {
                                Map<String, Object> childToRemove = null;
                                for (Map<String, Object> child : children) {
                                    if (childUid.equals(child.get("uid"))) {
                                        childToRemove = child;
                                        break;
                                    }
                                }
                                if (childToRemove != null) {
                                    children.remove(childToRemove);
                                    db.collection("users").document(user.getUid())
                                            .update("linkedChildren", children)
                                            .addOnSuccessListener(aVoid -> {
                                                Map<String, Object> updates = new HashMap<>();
                                                updates.put("parentUid", com.google.firebase.firestore.FieldValue.delete());
                                                db.collection("users").document(childUid)
                                                        .update(updates)
                                                        .addOnSuccessListener(aVoid1 -> callback.onSuccess())
                                                        .addOnFailureListener(callback::onFailure);
                                            })
                                            .addOnFailureListener(callback::onFailure);
                                } else {
                                    callback.onFailure(new Exception("Child not found in linked children"));
                                }
                            } else {
                                callback.onFailure(new Exception("No linked children found"));
                            }
                        } else {
                            callback.onFailure(new Exception("Document does not exist"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}

