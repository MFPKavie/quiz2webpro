package com.passiton.dao;

import com.passiton.model.Offer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OfferDao {
    private static final String SELECT =
            "SELECT o.id, o.listing_id, o.buyer_id, o.amount, o.message, o.status, o.created_at,"
          + "       l.title, l.status AS listing_status, l.price, l.seller_id,"
          + "       s.name AS seller_name, s.line_id AS seller_line_id,"
          + "       b.name AS buyer_name,  b.line_id AS buyer_line_id,"
          + "       (SELECT p.file_name FROM listing_photos p WHERE p.listing_id = l.id"
          + "         ORDER BY p.sort_order, p.id LIMIT 1) AS cover_photo"
          + "  FROM offers o"
          + "  JOIN listings l ON l.id = o.listing_id"
          + "  JOIN users s    ON s.id = l.seller_id"
          + "  JOIN users b    ON b.id = o.buyer_id";

    private static final String ORDER = " ORDER BY (o.status = 'pending') DESC, o.created_at DESC, o.id DESC";

    public List<Offer> findSent(int buyerId) throws SQLException {
        return list(SELECT + " WHERE o.buyer_id = ?" + ORDER, buyerId);
    }

    public List<Offer> findReceived(int sellerId) throws SQLException {
        return list(SELECT + " WHERE l.seller_id = ?" + ORDER, sellerId);
    }

    public Offer findById(int id) throws SQLException {
        List<Offer> one = list(SELECT + " WHERE o.id = ?", id);
        return one.isEmpty() ? null : one.get(0);
    }

    public Offer findLatest(int listingId, int buyerId) throws SQLException {
        String sql = SELECT + " WHERE o.listing_id = ? AND o.buyer_id = ? ORDER BY o.created_at DESC, o.id DESC LIMIT 1";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int countPending(int listingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM offers WHERE listing_id = ? AND status = 'pending'";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public int totalEarned(int sellerId) throws SQLException {
        return sum("SELECT COALESCE(SUM(o.amount), 0) FROM offers o JOIN listings l ON l.id = o.listing_id"
                 + " WHERE l.seller_id = ? AND l.status = 'sold' AND o.status = 'accepted'", sellerId);
    }

    public int totalSpent(int buyerId) throws SQLException {
        return sum("SELECT COALESCE(SUM(o.amount), 0) FROM offers o JOIN listings l ON l.id = o.listing_id"
                 + " WHERE o.buyer_id = ? AND l.status = 'sold' AND o.status = 'accepted'", buyerId);
    }

    public int countBought(int buyerId) throws SQLException {
        return sum("SELECT COUNT(*) FROM offers o JOIN listings l ON l.id = o.listing_id"
                 + " WHERE o.buyer_id = ? AND l.status = 'sold' AND o.status = 'accepted'", buyerId);
    }

    private int sum(String sql, int param) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void create(int listingId, int buyerId, int amount, String message) throws SQLException {
        String sql = "INSERT INTO offers (listing_id, buyer_id, amount, message) VALUES (?, ?, ?, ?)";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, buyerId);
            ps.setInt(3, amount);
            ps.setString(4, message);
            ps.executeUpdate();
        }
    }

    public boolean updatePending(int offerId, int buyerId, int amount, String message) throws SQLException {
        String sql = "UPDATE offers SET amount = ?, message = ? WHERE id = ? AND buyer_id = ? AND status = 'pending'";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setString(2, message);
            ps.setInt(3, offerId);
            ps.setInt(4, buyerId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean withdraw(int offerId, int buyerId) throws SQLException {
        String sql = "UPDATE offers SET status = 'withdrawn' WHERE id = ? AND buyer_id = ? AND status = 'pending'";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            ps.setInt(2, buyerId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean decline(int offerId, int sellerId) throws SQLException {
        String sql = "UPDATE offers SET status = 'declined'"
                   + " WHERE id = ? AND status = 'pending'"
                   + "   AND listing_id IN (SELECT id FROM listings WHERE seller_id = ?)";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            ps.setInt(2, sellerId);
            return ps.executeUpdate() == 1;
        }
    }

    // one transaction (rule 4); each UPDATE checks its own condition and 0 rows changed means rollback, so two accepts can't both win
    public boolean accept(int offerId, int sellerId) throws SQLException {
        try (Connection con = Db.get()) {
            con.setAutoCommit(false);
            try {
                int listingId;
                try (PreparedStatement ps = con.prepareStatement("SELECT listing_id FROM offers WHERE id = ?")) {
                    ps.setInt(1, offerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                        listingId = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE listings SET status = 'reserved' WHERE id = ? AND seller_id = ? AND status = 'available'")) {
                    ps.setInt(1, listingId);
                    ps.setInt(2, sellerId);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE offers SET status = 'accepted' WHERE id = ? AND status = 'pending'")) {
                    ps.setInt(1, offerId);
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE offers SET status = 'declined' WHERE listing_id = ? AND id <> ? AND status = 'pending'")) {
                    ps.setInt(1, listingId);
                    ps.setInt(2, offerId);
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private List<Offer> list(String sql, int param) throws SQLException {
        List<Offer> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private Offer map(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setId(rs.getInt("id"));
        o.setListingId(rs.getInt("listing_id"));
        o.setBuyerId(rs.getInt("buyer_id"));
        o.setAmount(rs.getInt("amount"));
        o.setMessage(rs.getString("message"));
        o.setStatus(rs.getString("status"));
        o.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        o.setListingTitle(rs.getString("title"));
        o.setListingStatus(rs.getString("listing_status"));
        o.setListingPrice(rs.getInt("price"));
        o.setSellerId(rs.getInt("seller_id"));
        o.setSellerName(rs.getString("seller_name"));
        o.setSellerLineId(rs.getString("seller_line_id"));
        o.setBuyerName(rs.getString("buyer_name"));
        o.setBuyerLineId(rs.getString("buyer_line_id"));
        o.setCoverPhoto(rs.getString("cover_photo"));
        return o;
    }
}
