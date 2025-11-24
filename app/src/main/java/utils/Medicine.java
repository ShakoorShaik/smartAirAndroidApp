package utils;


import android.widget.ImageView;

import java.time.LocalDate;

public class Medicine {
    private String name;
    private int amount;
    private LocalDate expiry;
    private int imageId;

    public Medicine(String name, int amount, LocalDate expiry, int imageId) {
        this.name = name;
        this.amount = amount;
        this.expiry = expiry;
        this.imageId = imageId;
    }

    // Getters
    public String getName() { return name; }
    public int getAmount() { return amount; }
    public LocalDate getExpiry() { return expiry; }
    public int getImageId() { return imageId; }
}