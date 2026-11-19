package com.passiton.web;

import com.passiton.dao.ListingDao;
import com.passiton.dao.WishlistDao;
import com.passiton.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/wishlist")
public class WishlistServlet extends HttpServlet {
    private final ListingDao listingDao = new ListingDao();
    private final WishlistDao wishlistDao = new WishlistDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            req.setAttribute("listings", listingDao.findSavedBy(me.getId()));
            req.setAttribute("savedIds", wishlistDao.listingIds(me.getId()));
        } catch (SQLException e) {
            throw new ServletException("Could not read the wishlist", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/wishlist.jsp").forward(req, resp);
    }
}
