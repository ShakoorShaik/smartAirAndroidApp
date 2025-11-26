package utils;


import android.widget.ImageView;

import java.time.LocalDate;

public class Medicine {
    private String name;
    private int amount;
    private LocalDate expiry;
    private int imageId;
    private String childUid;
    private String childName;
    private LocalDate purchaseDate;
    private int amountLeft;

    public Medicine(String name, int amount, LocalDate expiry, int imageId) {
        this.name = name;
        this.amount = amount;
        this.expiry = expiry;
        this.imageId = imageId;
        this.amountLeft = 100;
    }

    public Medicine(String name, int amount, LocalDate expiry, int imageId, String childUid, String childName) {
        this.name = name;
        this.amount = amount;
        this.expiry = expiry;
        this.imageId = imageId;
        this.childUid = childUid;
        this.childName = childName;
        this.amountLeft = 100;
    }

    public Medicine(String name, int amount, LocalDate expiry, int imageId, String childUid, String childName, LocalDate purchaseDate, int amountLeft) {
        this.name = name;
        this.amount = amount;
        this.expiry = expiry;
        this.imageId = imageId;
        this.childUid = childUid;
        this.childName = childName;
        this.purchaseDate = purchaseDate;
        this.amountLeft = amountLeft;
    }

    public String getName() { return name; }
    public int getAmount() { return amount; }
    public LocalDate getExpiry() { return expiry; }
    public int getImageId() { return imageId; }
    public String getChildUid() { return childUid; }
    public String getChildName() { return childName; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public int getAmountLeft() { return amountLeft; }

    public void setAmountLeft(int amountLeft) { this.amountLeft = amountLeft; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
}