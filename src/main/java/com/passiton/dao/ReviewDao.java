package com.passiton.dao;

import com.passiton.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    private static final String SELECT =
            "SELECT r.id, r.listing_id, r.reviewer_id, r.rating, r.comment, r.created_at,"
          + "       l.title, l.seller_id, s.name AS seller_name, u.name AS reviewer_name"
          + "  FROM reviews r"
          + "  JOIN listings l ON l.id = r.listing_id"
          + "  JOIN users s    ON s.id = l.seller_id"
          + "  JOIN users u    ON u.id = r.reviewer_id";

    public boolean canReview(int listingId, int userId) throws SQLException {
        String sql = "SELECT 1 FROM listings l JOIN offers o ON o.listing_id = l.id"
                   + " WHERE l.id = ? AND l.status = 'sold' AND o.buyer_id = ? AND o.status = 'accepted'";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Review find(int listingId, int reviewerId) throws SQLException {
        List<Review> list = query(SELECT + " WHERE r.listing_id = ? AND r.reviewer_id = ?", listingId, reviewerId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Review findById(int id) throws SQLException {
        List<Review> list = query(SELECT + " WHERE r.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Review> findByListing(int listingId) throws SQLException {
        return query(SELECT + " WHERE r.listing_id = ? ORDER BY r.created_at DESC", listingId);
    }

    public List<Review> findForSeller(int sellerId) throws SQLException {
        return query(SELECT + " WHERE l.seller_id = ? ORDER BY r.created_at DESC, r.id DESC", sellerId);
    }

    public double[] ratingSummary(int sellerId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(r.rating), 0), COUNT(r.id)"
                   + "  FROM reviews r JOIN listings l ON l.id = r.listing_id"
                   + " WHERE l.seller_id = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new double[] {rs.getDouble(1), rs.getInt(2)};
            }
        }
    }

    public void create(int listingId, int reviewerId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO reviews (listing_id, reviewer_id, rating, comment) VALUES (?, ?, ?, ?)";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, reviewerId);
            ps.setInt(3, rating);
            ps.setString(4, comment);
            ps.executeUpdate();
        }
    }

    public boolean update(int id, int reviewerId, int rating, String comment) throws SQLException {
        String sql = "UPDATE reviews SET rating = ?, comment = ? WHERE id = ? AND reviewer_id = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setInt(3, id);
            ps.setInt(4, reviewerId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id, int reviewerId) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM reviews WHERE id = ? AND reviewer_id = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, reviewerId);
            return ps.executeUpdate() == 1;
        }
    }

    private List<Review> query(String sql, int... params) throws SQLException {
        List<Review> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review r = new Review();
                    r.setId(rs.getInt("id"));
                    r.setListingId(rs.getInt("listing_id"));
                    r.setReviewerId(rs.getInt("reviewer_id"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                    r.setListingTitle(rs.getString("title"));
                    r.setSellerId(rs.getInt("seller_id"));
                    r.setSellerName(rs.getString("seller_name"));
                    r.setReviewerName(rs.getString("reviewer_name"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}
