package com.passiton.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Offer {
    private int id;
    private int listingId;
    private String listingTitle;
    private String listingStatus;
    private int listingPrice;
    private String coverPhoto;
    private int sellerId;
    private String sellerName;
    private String sellerLineId;
    private int buyerId;
    private String buyerName;
    private String buyerLineId;
    private int amount;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public String getAmountText() {
        return com.passiton.util.Money.rp(amount);
    }

    public String getListingPriceText() {
        return com.passiton.util.Money.price(listingPrice);
    }

    public String getStatusLabel() {
        return status == null || status.isEmpty() ? "" : Character.toUpperCase(status.charAt(0)) + status.substring(1);
    }

    public String getCreatedText() {
        return createdAt == null ? "" : DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH).format(createdAt);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getListingId() { return listingId; }
    public void setListingId(int listingId) { this.listingId = listingId; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public String getListingStatus() { return listingStatus; }
    public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }

    public int getListingPrice() { return listingPrice; }
    public void setListingPrice(int listingPrice) { this.listingPrice = listingPrice; }

    public String getCoverPhoto() { return coverPhoto; }
    public void setCoverPhoto(String coverPhoto) { this.coverPhoto = coverPhoto; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerLineId() { return sellerLineId; }
    public void setSellerLineId(String sellerLineId) { this.sellerLineId = sellerLineId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerLineId() { return buyerLineId; }
    public void setBuyerLineId(String buyerLineId) { this.buyerLineId = buyerLineId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
