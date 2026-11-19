package com.passiton.dao;

import com.passiton.util.Money;
import com.passiton.model.Listing;
import com.passiton.model.ListingFilter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListingDao {
    private static final String SELECT =
            "SELECT l.id, l.seller_id, l.category_id, l.title, l.description, l.price, l.item_condition,"
          + "       l.status, l.meetup_spot, l.created_at, l.updated_at,"
          + "       u.name AS seller_name, u.faculty AS seller_faculty, u.created_at AS seller_since, u.avatar AS seller_avatar,"
          + "       c.name AS category_name, c.slug AS category_slug,"
          + "       (SELECT p.file_name FROM listing_photos p WHERE p.listing_id = l.id"
          + "         ORDER BY p.sort_order, p.id LIMIT 1) AS cover_photo"
          + "  FROM listings l"
          + "  JOIN users u      ON u.id = l.seller_id"
          + "  JOIN categories c ON c.id = l.category_id";

    private static final String WHERE =
            " WHERE (? = '' OR l.title LIKE ? ESCAPE '!' OR l.description LIKE ? ESCAPE '!')"
          + "   AND (? = '' OR c.slug = ?)"
          + "   AND (? = '' OR l.item_condition = ?)"
          + "   AND l.price BETWEEN ? AND ?";

    // ORDER BY cannot use a ? placeholder, so user input only picks one of these fixed strings
    private static final Map<String, String> ORDER_BY = Map.of(
            "newest", " ORDER BY l.created_at DESC, l.id DESC",
            "price_asc", " ORDER BY l.price ASC, l.id DESC",
            "price_desc", " ORDER BY l.price DESC, l.id DESC");

    public List<Listing> find(ListingFilter f, int limit, int offset) throws SQLException {
        String sql = SELECT + WHERE + ORDER_BY.getOrDefault(f.getSort(), ORDER_BY.get("newest"))
                   + " LIMIT ? OFFSET ?";
        List<Listing> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = bindFilter(ps, f);
            ps.setInt(i++, limit);
            ps.setInt(i, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public int count(ListingFilter f) throws SQLException {
        String sql = "SELECT COUNT(*) FROM listings l JOIN categories c ON c.id = l.category_id" + WHERE;
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bindFilter(ps, f);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private int bindFilter(PreparedStatement ps, ListingFilter f) throws SQLException {
        // escape % and _ so a search treats them as plain characters (ESCAPE '!')
        String like = "%" + f.getQ().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        int i = 1;
        ps.setString(i++, f.getQ());
        ps.setString(i++, like);
        ps.setString(i++, like);
        ps.setString(i++, f.getCategory());
        ps.setString(i++, f.getCategory());
        ps.setString(i++, f.getCondition());
        ps.setString(i++, f.getCondition());
        ps.setInt(i++, f.getMinPrice() == null ? 0 : f.getMinPrice());
        ps.setInt(i++, f.getMaxPrice() == null ? Money.MAX : f.getMaxPrice());
        return i;
    }

    public List<Listing> findBySeller(int sellerId) throws SQLException {
        String sql = SELECT + " WHERE l.seller_id = ? ORDER BY (l.status = 'available') DESC, l.created_at DESC, l.id DESC";
        List<Listing> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Map<String, Integer> countByStatus(int sellerId) throws SQLException {
        String sql = "SELECT status, COUNT(*) FROM listings WHERE seller_id = ? GROUP BY status";
        Map<String, Integer> counts = new HashMap<>(Map.of("available", 0, "reserved", 0, "sold", 0));
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString(1), rs.getInt(2));
                }
            }
        }
        return counts;
    }

    public boolean adminDelete(int id) throws SQLException {
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM listings WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    public List<Listing> findSavedBy(int userId) throws SQLException {
        String sql = SELECT + " JOIN wishlist w ON w.listing_id = l.id WHERE w.user_id = ? ORDER BY w.created_at DESC, l.id DESC";
        List<Listing> list = new ArrayList<>();
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Listing findById(int id) throws SQLException {
        String sql = SELECT + " WHERE l.id = ?";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int create(Listing l, List<String> photoNames) throws SQLException {
        String sqlListing = "INSERT INTO listings (seller_id, category_id, title, description, price, item_condition, meetup_spot)"
                          + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Db.get()) {
            con.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = con.prepareStatement(sqlListing, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, l.getSellerId());
                    ps.setInt(2, l.getCategoryId());
                    ps.setString(3, l.getTitle());
                    ps.setString(4, l.getDescription());
                    ps.setInt(5, l.getPrice());
                    ps.setString(6, l.getItemCondition());
                    ps.setString(7, l.getMeetupSpot());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                insertPhotos(con, id, photoNames, 0);
                con.commit();
                return id;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public boolean update(Listing l, List<Integer> removePhotoIds, List<String> newPhotoNames) throws SQLException {
        String sqlListing = "UPDATE listings SET category_id = ?, title = ?, description = ?, price = ?,"
                          + " item_condition = ?, meetup_spot = ?"
                          + " WHERE id = ? AND seller_id = ? AND status <> 'sold'";
        try (Connection con = Db.get()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlListing)) {
                    ps.setInt(1, l.getCategoryId());
                    ps.setString(2, l.getTitle());
                    ps.setString(3, l.getDescription());
                    ps.setInt(4, l.getPrice());
                    ps.setString(5, l.getItemCondition());
                    ps.setString(6, l.getMeetupSpot());
                    ps.setInt(7, l.getId());
                    ps.setInt(8, l.getSellerId());
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM listing_photos WHERE id = ? AND listing_id = ?")) {
                    for (int photoId : removePhotoIds) {
                        ps.setInt(1, photoId);
                        ps.setInt(2, l.getId());
                        ps.executeUpdate();
                    }
                }
                int nextOrder;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COALESCE(MAX(sort_order) + 1, 0) FROM listing_photos WHERE listing_id = ?")) {
                    ps.setInt(1, l.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        nextOrder = rs.getInt(1);
                    }
                }
                insertPhotos(con, l.getId(), newPhotoNames, nextOrder);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public boolean delete(int id, int sellerId) throws SQLException {
        String sql = "DELETE FROM listings WHERE id = ? AND seller_id = ? AND status <> 'sold'";
        try (Connection con = Db.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, sellerId);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean changeStatus(int id, int sellerId, String from, String to) throws SQLException {
        try (Connection con = Db.get()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE listings SET status = ? WHERE id = ? AND seller_id = ? AND status = ?")) {
                    ps.setString(1, to);
                    ps.setInt(2, id);
                    ps.setInt(3, sellerId);
                    ps.setString(4, from);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }
                // business rule 5: back to available declines the accepted offer
                if ("reserved".equals(from) && "available".equals(to)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE offers SET status = 'declined' WHERE listing_id = ? AND status = 'accepted'")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
                // a listing that is reserved or sold by hand can't take the offers still waiting on it
                if ("reserved".equals(to) || "sold".equals(to)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE offers SET status = 'declined' WHERE listing_id = ? AND status = 'pending'")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void insertPhotos(Connection con, int listingId, List<String> names, int start) throws SQLException {
        String sql = "INSERT INTO listing_photos (listing_id, file_name, sort_order) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int order = start;
            for (String name : names) {
                ps.setInt(1, listingId);
                ps.setString(2, name);
                ps.setInt(3, order++);
                ps.executeUpdate();
            }
        }
    }

    private Listing map(ResultSet rs) throws SQLException {
        Listing l = new Listing();
        l.setId(rs.getInt("id"));
        l.setSellerId(rs.getInt("seller_id"));
        l.setCategoryId(rs.getInt("category_id"));
        l.setTitle(rs.getString("title"));
        l.setDescription(rs.getString("description"));
        l.setPrice(rs.getInt("price"));
        l.setItemCondition(rs.getString("item_condition"));
        l.setStatus(rs.getString("status"));
        l.setMeetupSpot(rs.getString("meetup_spot"));
        l.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        l.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        l.setSellerName(rs.getString("seller_name"));
        l.setSellerFaculty(rs.getString("seller_faculty"));
        l.setSellerAvatar(rs.getString("seller_avatar"));
        l.setSellerSince(rs.getObject("seller_since", LocalDateTime.class));
        l.setCategoryName(rs.getString("category_name"));
        l.setCategorySlug(rs.getString("category_slug"));
        l.setCoverPhoto(rs.getString("cover_photo"));
        return l;
    }
}
