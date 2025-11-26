package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineManager {

    public interface MedicineListCallback {
        void onSuccess(List<Medicine> medicines);
        void onFailure(Exception e);
    }

    public static void saveMedicine(Medicine medicine, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        Map<String, Object> medicineData = new HashMap<>();
        medicineData.put("name", medicine.getName());
        medicineData.put("amount", medicine.getAmount());
        medicineData.put("expiry", medicine.getExpiry().format(DateTimeFormatter.ISO_LOCAL_DATE));
        medicineData.put("imageId", medicine.getImageId());
        medicineData.put("amountLeft", medicine.getAmountLeft());
        medicineData.put("timestamp", System.currentTimeMillis());

        if (medicine.getChildUid() != null) {
            medicineData.put("childUid", medicine.getChildUid());
        }
        if (medicine.getChildName() != null) {
            medicineData.put("childName", medicine.getChildName());
        }
        if (medicine.getPurchaseDate() != null) {
            medicineData.put("purchaseDate", medicine.getPurchaseDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        db.collection("users").document(user.getUid())
                .collection("medicines")
                .add(medicineData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void loadMedicines(MedicineListCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("medicines")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Medicine> medicines = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String name = document.getString("name");
                                Long amountLong = document.getLong("amount");
                                String expiryStr = document.getString("expiry");
                                Long imageIdLong = document.getLong("imageId");
                                String childUid = document.getString("childUid");
                                String childName = document.getString("childName");
                                String purchaseDateStr = document.getString("purchaseDate");
                                Long amountLeftLong = document.getLong("amountLeft");

                                if (name != null && amountLong != null && expiryStr != null && imageIdLong != null) {
                                    int amount = amountLong.intValue();
                                    LocalDate expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE);
                                    int imageId = imageIdLong.intValue();
                                    int amountLeft = amountLeftLong != null ? amountLeftLong.intValue() : 100;
                                    LocalDate purchaseDate = purchaseDateStr != null ?
                                            LocalDate.parse(purchaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE) : null;

                                    Medicine medicine = new Medicine(name, amount, expiry, imageId, childUid, childName, purchaseDate, amountLeft);
                                    medicines.add(medicine);
                                }
                            } catch (Exception e) {
                                // Skip invalid medicine entries
                            }
                        }
                        callback.onSuccess(medicines);
                    } else {
                        callback.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public static void updateMedicine(String documentId, Medicine medicine, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        Map<String, Object> medicineData = new HashMap<>();
        medicineData.put("name", medicine.getName());
        medicineData.put("amount", medicine.getAmount());
        medicineData.put("expiry", medicine.getExpiry().format(DateTimeFormatter.ISO_LOCAL_DATE));
        medicineData.put("imageId", medicine.getImageId());
        medicineData.put("amountLeft", medicine.getAmountLeft());
        medicineData.put("timestamp", System.currentTimeMillis());
        if (medicine.getChildUid() != null) {
            medicineData.put("childUid", medicine.getChildUid());
        }
        if (medicine.getChildName() != null) {
            medicineData.put("childName", medicine.getChildName());
        }
        if (medicine.getPurchaseDate() != null) {
            medicineData.put("purchaseDate", medicine.getPurchaseDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        db.collection("users").document(user.getUid())
                .collection("medicines")
                .document(documentId)
                .update(medicineData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void deleteMedicine(String documentId, DatabaseManager.SuccessFailCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("medicines")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    public static void loadMedicinesWithIds(MedicineListWithIdsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        db.collection("users").document(user.getUid())
                .collection("medicines")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MedicineWithId> medicines = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String name = document.getString("name");
                                Long amountLong = document.getLong("amount");
                                String expiryStr = document.getString("expiry");
                                Long imageIdLong = document.getLong("imageId");
                                String childUid = document.getString("childUid");
                                String childName = document.getString("childName");
                                String purchaseDateStr = document.getString("purchaseDate");
                                Long amountLeftLong = document.getLong("amountLeft");

                                if (name != null && amountLong != null && expiryStr != null && imageIdLong != null) {
                                    int amount = amountLong.intValue();
                                    LocalDate expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ISO_LOCAL_DATE);
                                    int imageId = imageIdLong.intValue();
                                    int amountLeft = amountLeftLong != null ? amountLeftLong.intValue() : 100;
                                    LocalDate purchaseDate = purchaseDateStr != null ?
                                            LocalDate.parse(purchaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE) : null;

                                    Medicine medicine = new Medicine(name, amount, expiry, imageId, childUid, childName, purchaseDate, amountLeft);
                                    medicines.add(new MedicineWithId(document.getId(), medicine));
                                }
                            } catch (Exception e) {
                                // Skip invalid medicine entries
                            }
                        }
                        callback.onSuccess(medicines);
                    } else {
                        callback.onFailure(task.getException());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public interface MedicineListWithIdsCallback {
        void onSuccess(List<MedicineWithId> medicines);
        void onFailure(Exception e);
    }

    public static class MedicineWithId {
        private final String documentId;
        private final Medicine medicine;

        public MedicineWithId(String documentId, Medicine medicine) {
            this.documentId = documentId;
            this.medicine = medicine;
        }

        public String getDocumentId() { return documentId; }
        public Medicine getMedicine() { return medicine; }
    }
}

