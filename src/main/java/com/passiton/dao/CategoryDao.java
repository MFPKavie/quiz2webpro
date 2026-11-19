package com.passiton.dao;

import com.passiton.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM categories";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public List<Category> findAll() throws SQLException {
        String sql = "SELECT id, name, slug FROM categories ORDER BY id";
        List<Category> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("slug")));
            }
        }
        return list;
    }

    public List<Category> findAllWithCounts() throws SQLException {
        String sql = "SELECT c.id, c.name, c.slug,"
                   + "       COUNT(l.id) AS listing_count,"
                   + "       COALESCE(SUM(l.status = 'available'), 0) AS available_count,"
                   + "       COALESCE(SUM(l.status = 'sold'), 0) AS sold_count"
                   + "  FROM categories c"
                   + "  LEFT JOIN listings l ON l.category_id = c.id"
                   + " GROUP BY c.id, c.name, c.slug"
                   + " ORDER BY listing_count DESC, c.name";
        List<Category> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Category c = new Category(rs.getInt("id"), rs.getString("name"), rs.getString("slug"));
                c.setListingCount(rs.getInt("listing_count"));
                c.setAvailableCount(rs.getInt("available_count"));
                c.setSoldCount(rs.getInt("sold_count"));
                list.add(c);
            }
        }
        return list;
    }

    public void create(String name, String slug) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("INSERT INTO categories (name, slug) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, slug);
            ps.executeUpdate();
        }
    }

    public boolean rename(int id, String name, String slug) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("UPDATE categories SET name = ?, slug = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, slug);
            ps.setInt(3, id);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM categories WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public int listingCount(int id) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM listings WHERE category_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
