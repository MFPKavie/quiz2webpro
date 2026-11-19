package com.passiton.model;

public class Category {
    private int id;
    private String name;
    private String slug;
    private int listingCount;
    private int availableCount;
    private int soldCount;

    public Category() { }

    public Category(int id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public int getListingCount() { return listingCount; }
    public void setListingCount(int listingCount) { this.listingCount = listingCount; }

    public int getAvailableCount() { return availableCount; }
    public void setAvailableCount(int availableCount) { this.availableCount = availableCount; }

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
}
