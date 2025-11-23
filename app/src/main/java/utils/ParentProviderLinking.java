package utils;

import static utils.CodeGeneration.generateCode;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ParentProviderLinking extends DatabaseManager {

    private static final long EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

    public interface CodeCallback {
        void onSuccess(String code);
        void onFailure(Exception e);
    }

    public interface RedeemCallback {
        void onSuccess(String parentEmail);
        void onFailure(Exception e);
    }

    public static void generateLinkCode(DataSuccessFailCallback callback) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User is null"));
            return;
        }

        String code = generateCode(7);

        db.collection("users")
                .whereEqualTo("referralCode", code)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        callback.onFailure(task.getException());
                        return;
                    }

                    boolean exists = false;
                    for (QueryDocumentSnapshot ignored : task.getResult()) {
                        exists = true;
                        break;
                    }

                    if (exists) {
                        generateLinkCode(callback);
                        return;
                    }

                    long expiration = System.currentTimeMillis() + (60 * 60 * 1000);

                    Map<String, Object> body = new HashMap<>();
                    body.put("referralCode", code);
                    body.put("referralExpires", expiration);

                    db.collection("users")
                            .document(user.getUid())
                            .set(body, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess(code))
                            .addOnFailureListener(callback::onFailure);

                });
    }

    public static void redeemCode(String code, RedeemCallback callback) {

        FirebaseUser provider = FirebaseAuth.getInstance().getCurrentUser();

        if (provider == null) {
            callback.onFailure(new Exception("Provider not logged in"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("referralCode", code)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onFailure(new Exception("Invalid code"));
                        return;
                    }

                    Map<String, Object> data = query.getDocuments().get(0).getData();
                    String parentUid = query.getDocuments().get(0).getId();

                    try {
                        long expiresAt = (long) data.get("referralExpires");
                        String parentEmail = (String) data.get("email");

                        if (System.currentTimeMillis() > expiresAt) {
                            callback.onFailure(new Exception("Code expired"));
                            return;
                        }

                        Map<String, Object> linkData = new HashMap<>();
                        linkData.put("email", parentEmail);
                        linkData.put("linkedAt", System.currentTimeMillis());

                        String providerPath = "users/" + provider.getUid() + "/linkedParents/" + parentUid;

                        db.document(providerPath)
                                .set(linkData)
                                .addOnSuccessListener(a -> callback.onSuccess(parentEmail))
                                .addOnFailureListener(callback::onFailure);

                    } catch (Exception e) {
                        callback.onFailure(new Exception("Malformed code data"));
                    }

                })
                .addOnFailureListener(callback::onFailure);
    }

    private static String generateRandomCode(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public interface DataMapCallback {
        void onSuccess(Map<String, Object> data);
        void onFailure(Exception e);
    }

    public static void getDataMap(String documentPath, DataMapCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.document(documentPath).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess(doc.getData());
                    } else {
                        callback.onFailure(new Exception("Document not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}
