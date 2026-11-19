package com.passiton.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Report {
    private int id;
    private int listingId;
    private String listingTitle;
    private String listingStatus;
    private String sellerName;
    private int reporterId;
    private String reporterName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;

    public String getCreatedText() {
        return createdAt == null ? "" : DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH).format(createdAt);
    }

    public String getStatusLabel() {
        return status == null || status.isEmpty() ? "" : Character.toUpperCase(status.charAt(0)) + status.substring(1);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getListingId() { return listingId; }
    public void setListingId(int listingId) { this.listingId = listingId; }

    public String getListingTitle() { return listingTitle; }
    public void setListingTitle(String listingTitle) { this.listingTitle = listingTitle; }

    public String getListingStatus() { return listingStatus; }
    public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
