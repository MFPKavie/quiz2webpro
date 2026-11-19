package com.passiton.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Listing {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    private int id;
    private int sellerId;
    private String sellerName;
    private String sellerFaculty;
    private String sellerAvatar;
    private LocalDateTime sellerSince;
    private int categoryId;
    private String categoryName;
    private String categorySlug;
    private String title;
    private String description;
    private int price;
    private String itemCondition;
    private String status;
    private String meetupSpot;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String coverPhoto;

    public String getPriceText() {
        return com.passiton.util.Money.price(price);
    }

    public String getPriceNumber() {
        return com.passiton.util.Money.number(price);
    }

    public String getConditionLabel() {
        if (itemCondition == null) return "";
        return switch (itemCondition) {
            case "new" -> "New";
            case "like_new" -> "Like new";
            case "good" -> "Good";
            case "fair" -> "Fair";
            default -> itemCondition;
        };
    }

    public String getStatusLabel() {
        if (status == null || status.isEmpty()) return "";
        return Character.toUpperCase(status.charAt(0)) + status.substring(1);
    }

    public String getPostedText() {
        return createdAt == null ? "" : DATE.format(createdAt);
    }

    public String getSellerSinceText() {
        return sellerSince == null ? "" : DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH).format(sellerSince);
    }

    public String getSellerInitial() {
        return sellerName == null || sellerName.isEmpty() ? "?" : sellerName.substring(0, sellerName.offsetByCodePoints(0, 1)).toUpperCase(Locale.ROOT);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerFaculty() { return sellerFaculty; }
    public void setSellerFaculty(String sellerFaculty) { this.sellerFaculty = sellerFaculty; }

    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }

    public LocalDateTime getSellerSince() { return sellerSince; }
    public void setSellerSince(LocalDateTime sellerSince) { this.sellerSince = sellerSince; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getCategorySlug() { return categorySlug; }
    public void setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getItemCondition() { return itemCondition; }
    public void setItemCondition(String itemCondition) { this.itemCondition = itemCondition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMeetupSpot() { return meetupSpot; }
    public void setMeetupSpot(String meetupSpot) { this.meetupSpot = meetupSpot; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }
}
