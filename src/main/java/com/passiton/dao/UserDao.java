package com.passiton.dao;

import com.passiton.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String COLUMNS =
            "id, name, email, password_hash, faculty, line_id, avatar, role, created_at";

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE email = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM users WHERE id = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int create(String name, String email, String passwordHash, String faculty) throws SQLException {
        String sql = "INSERT INTO users (name, email, password_hash, faculty, role) VALUES (?, ?, ?, ?, 'student')";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, faculty);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public void updateProfile(int id, String name, String faculty, String lineId, String avatar) throws SQLException {
        String sql = "UPDATE users SET name = ?, faculty = ?, line_id = ?, avatar = ? WHERE id = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, faculty);
            ps.setString(3, lineId);
            ps.setString(4, avatar);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public List<User> findActiveSellers(int min) throws SQLException {
        String sql = "SELECT u.id, u.name, u.faculty, COUNT(l.id) AS listing_count,"
                   + "       SUM(l.status = 'sold') AS sold_count"
                   + "  FROM users u JOIN listings l ON l.seller_id = u.id"
                   + " GROUP BY u.id, u.name, u.faculty"
                   + " HAVING COUNT(l.id) >= ?"
                   + " ORDER BY listing_count DESC, u.name";
        List<User> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, min);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setName(rs.getString("name"));
                    u.setFaculty(rs.getString("faculty"));
                    u.setListingCount(rs.getInt("listing_count"));
                    u.setSoldCount(rs.getInt("sold_count"));
                    list.add(u);
                }
            }
        }
        return list;
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFaculty(rs.getString("faculty"));
        u.setLineId(rs.getString("line_id"));
        u.setAvatar(rs.getString("avatar"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return u;
    }
}
