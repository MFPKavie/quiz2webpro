package com.passiton.dao;

import com.passiton.model.Report;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {
    private static final String SELECT =
            "SELECT r.id, r.listing_id, r.reporter_id, r.reason, r.status, r.created_at,"
          + "       l.title, l.status AS listing_status, s.name AS seller_name, u.name AS reporter_name"
          + "  FROM reports r"
          + "  JOIN listings l ON l.id = r.listing_id"
          + "  JOIN users s    ON s.id = l.seller_id"
          + "  JOIN users u    ON u.id = r.reporter_id";

    public void create(int listingId, int reporterId, String reason) throws SQLException {
        String sql = "INSERT INTO reports (listing_id, reporter_id, reason) VALUES (?, ?, ?)";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, reporterId);
            ps.setString(3, reason);
            ps.executeUpdate();
        }
    }

    public boolean hasOpen(int listingId, int reporterId) throws SQLException {
        String sql = "SELECT 1 FROM reports WHERE listing_id = ? AND reporter_id = ? AND status = 'open'";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            ps.setInt(2, reporterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<Report> findAll() throws SQLException {
        String sql = SELECT + " ORDER BY (r.status = 'open') DESC, r.created_at DESC, r.id DESC";
        List<Report> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Report findById(int id) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(SELECT + " WHERE r.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean dismiss(int id) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE reports SET status = 'dismissed' WHERE id = ? AND status = 'open'")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public int countOpen() throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM reports WHERE status = 'open'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Report map(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setId(rs.getInt("id"));
        r.setListingId(rs.getInt("listing_id"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        r.setListingTitle(rs.getString("title"));
        r.setListingStatus(rs.getString("listing_status"));
        r.setSellerName(rs.getString("seller_name"));
        r.setReporterName(rs.getString("reporter_name"));
        return r;
    }
}
