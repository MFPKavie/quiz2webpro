package com.passiton.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class WishlistDao {
    public boolean toggle(int userId, int listingId) throws SQLException {
        try (Connection con = Db.get()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM wishlist WHERE user_id = ? AND listing_id = ?")) {
                ps.setInt(1, userId);
                ps.setInt(2, listingId);
                if (ps.executeUpdate() == 1) {
                    return false;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO wishlist (user_id, listing_id) VALUES (?, ?)")) {
                ps.setInt(1, userId);
                ps.setInt(2, listingId);
                ps.executeUpdate();
            } catch (SQLException e) {
                if (!Db.isDuplicate(e)) throw e; // a double-click already saved it
            }
            return true;
        }
    }

    public int count(int listingId) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM wishlist WHERE listing_id = ?")) {
            ps.setInt(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Set<Integer> listingIds(int userId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT listing_id FROM wishlist WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }
}
