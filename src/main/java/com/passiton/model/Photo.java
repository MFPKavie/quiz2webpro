package com.passiton.model;

public class Photo {
    private int id;
    private int listingId;
    private String fileName;
    private int sortOrder;

    public Photo() { }

    public Photo(int id, int listingId, String fileName, int sortOrder) {
        this.id = id;
        this.listingId = listingId;
        this.fileName = fileName;
        this.sortOrder = sortOrder;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getListingId() { return listingId; }
    public void setListingId(int listingId) { this.listingId = listingId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
