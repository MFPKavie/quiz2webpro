package com.passiton.model;

public class ListingFilter {
    private String q = "";
    private String category = "";
    private String condition = "";
    private Integer minPrice;
    private Integer maxPrice;
    private String sort = "newest";

    public boolean isActive() {
        return !q.isEmpty() || !category.isEmpty() || !condition.isEmpty()
                || minPrice != null || maxPrice != null;
    }

    public String getQ() { return q; }
    public void setQ(String q) { this.q = q; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }

    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }

    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
