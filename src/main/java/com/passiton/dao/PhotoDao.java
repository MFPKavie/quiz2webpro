package com.passiton.dao;

import com.passiton.model.Photo;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhotoDao {
    public List<Photo> findByListing(int listingId) throws SQLException {
        String sql = "SELECT id, listing_id, file_name, sort_order FROM listing_photos"
                   + " WHERE listing_id = ? ORDER BY sort_order, id";
        List<Photo> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, listingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Photo(rs.getInt("id"), rs.getInt("listing_id"),
                            rs.getString("file_name"), rs.getInt("sort_order")));
                }
            }
        }
        return list;
    }

    // the image bytes live in MySQL, not on disk: free hosts wipe their disk on every restart
    public void saveFile(String fileName, InputStream data) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("INSERT INTO photo_files (file_name, data) VALUES (?, ?)")) {
            ps.setString(1, fileName);
            ps.setBinaryStream(2, data);
            ps.executeUpdate();
        }
    }

    public byte[] readFile(String fileName) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("SELECT data FROM photo_files WHERE file_name = ?")) {
            ps.setString(1, fileName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBytes("data") : null;
            }
        }
    }

    public void deleteFile(String fileName) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM photo_files WHERE file_name = ?")) {
            ps.setString(1, fileName);
            ps.executeUpdate();
        }
    }
}
