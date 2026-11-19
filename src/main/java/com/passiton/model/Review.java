package com.passiton.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Review {
    private int id;
    private int listingId;
    private String listingTitle;
    private int sellerId;
    private String sellerName;
    private int reviewerId;
    private String reviewerName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public String getStars() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    public String getCreatedText() {
        return createdAt == null ? "" : DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH).format(createdAt);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getListingId() { return listingId; }
    public void setListingId(int listingId) { this.listingId = listingId; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public int getReviewerId() { return reviewerId; }
    public void setReviewerId(int reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
