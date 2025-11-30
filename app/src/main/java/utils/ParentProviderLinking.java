package utils;

import static utils.CodeGeneration.generateCode;
import static utils.DatabaseManager.getData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;
import com.google.firebase.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ParentProviderLinking{

    public interface RedeemCallback {
        void onSuccess(String parentEmail);
        void onFailure(Exception e);
    }
    public static void generateLinkCode(DatabaseManager.SuccessFailCallback callback) {
        generateUniqueCode(5, callback);
    }

    private static void generateUniqueCode(int attempts, DatabaseManager.SuccessFailCallback callback) {

        if (attempts <= 0) {
            callback.onFailure(new Exception("Failed to generate unique code after multiple attempts"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User is null"));
            return;
        }
        Instant sevenDays = Instant.now().plus(7, ChronoUnit.DAYS);
        Timestamp expiresAt = new Timestamp(Date.from(sevenDays));

        String code = generateCode(7);

        db.collection("users")
                .whereEqualTo("linkingCode.referralCode", code)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {

                        Map<String, Object> body = new HashMap<>();
                        body.put("email", user.getEmail());
                        body.put("referralCodeUsed", false);
                        body.put("referralCode", code);
                        body.put("referralExpires", expiresAt);

                        DatabaseManager.writeData("linkingCode", body, callback);
                    } else {
                        generateUniqueCode(attempts-1, callback);
                    }

                }).addOnFailureListener(e -> {generateUniqueCode(attempts-1, callback);});
    }

    public static void InvalidateCode(DatabaseManager.SuccessFailCallback callBack) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callBack.onFailure(new Exception("User is null"));
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("referralCodeUsed", true);

        DatabaseManager.writeData("linkingCode", body, callBack);
    }

    public static void redeemCode(String code, RedeemCallback callback) {

        FirebaseUser provider = FirebaseAuth.getInstance().getCurrentUser();

        if (provider == null) {
            callback.onFailure(new Exception("User is null"));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();


        db.collection("users")
                .whereEqualTo("linkingCode.referralCode", code)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        callback.onFailure(new Exception("Invalid code"));
                        return;
                    }

                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) query.getDocuments().get(0);
                    String parentUid = document.getId();

                    Map<String, Object> linkingCode = (Map<String, Object>) document.get("linkingCode");

                    if (linkingCode == null) {
                        callback.onFailure(new Exception("Malformed code data - no liking code info"));
                        return;
                    }

                    try {
                        Timestamp expiresAt = (Timestamp) linkingCode.get("referralExpires");
                        String parentEmail = (String) linkingCode.get("email");
                        boolean used = (boolean) linkingCode.get("referralCodeUsed");

                        if (Instant.now().isAfter(expiresAt.toDate().toInstant())) {
                            callback.onFailure(new Exception("Code expired"));
                            return;
                        }

                        if (used) {
                            callback.onFailure(new Exception("Code already used"));
                            return;
                        }

                        Map<String, Object> linkData = new HashMap<>();
                        linkData.put("email", parentEmail);
                        linkData.put("linkedAt", System.currentTimeMillis());

                        String providerPath = "users/" + provider.getUid() + "/linkedParents/" + parentUid;

                        db.document(providerPath)
                                .set(linkData)
                                .addOnSuccessListener(a -> {

                                    db.collection("users")
                                            .document(parentUid)
                                            .update("linkingCode.referralCodeUsed", true)
                                            .addOnSuccessListener(b -> callback.onSuccess(parentEmail))
                                            .addOnFailureListener(callback::onFailure);
                                })
                                .addOnFailureListener(callback::onFailure);

                    } catch (Exception e) {
                        callback.onFailure(new Exception("Malformed code data"));
                    }

                }).addOnFailureListener(callback::onFailure);
    }
}
