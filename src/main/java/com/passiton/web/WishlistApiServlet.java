package com.passiton.web;

import com.passiton.dao.ListingDao;
import com.passiton.dao.WishlistDao;
import com.passiton.model.Listing;
import com.passiton.model.User;
import com.passiton.util.Json;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/wishlist")
public class WishlistApiServlet extends HttpServlet {
    private final WishlistDao wishlistDao = new WishlistDao();
    private final ListingDao listingDao = new ListingDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        resp.setContentType("application/json");
        resp.setHeader("Cache-Control", "no-store");

        try {
            Integer listingId = Validate.intBetween(req.getParameter("listingId"), 1, Integer.MAX_VALUE);
            Listing listing = listingId == null ? null : listingDao.findById(listingId);
            if (listing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(Json.error("No such listing."));
                return;
            }
            if (listing.getSellerId() == me.getId()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(Json.error("You can't save your own listing."));
                return;
            }
            boolean saved = wishlistDao.toggle(me.getId(), listing.getId());
            resp.getWriter().write(Json.wishlist(saved, wishlistDao.count(listing.getId())));
        } catch (SQLException e) {
            throw new ServletException("Could not update the wishlist", e);
        }
    }
}
