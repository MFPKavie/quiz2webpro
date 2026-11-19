package com.passiton.web;

import com.passiton.dao.ListingDao;
import com.passiton.dao.ReviewDao;
import com.passiton.dao.UserDao;
import com.passiton.dao.WishlistDao;
import com.passiton.model.User;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

@WebServlet("/seller")
public class SellerServlet extends HttpServlet {
    private final UserDao userDao = new UserDao();
    private final ListingDao listingDao = new ListingDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final WishlistDao wishlistDao = new WishlistDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = Validate.intBetween(req.getParameter("id"), 1, Integer.MAX_VALUE);
        try {
            User seller = id == null ? null : userDao.findById(id);
            if (seller == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            seller.setPasswordHash(null);
            double[] rating = reviewDao.ratingSummary(seller.getId());
            req.setAttribute("seller", seller);
            req.setAttribute("listings", listingDao.findBySeller(seller.getId()));
            req.setAttribute("reviews", reviewDao.findForSeller(seller.getId()));
            req.setAttribute("reviewCount", (int) rating[1]);
            req.setAttribute("avgRating", String.format(Locale.ENGLISH, "%.1f", rating[0]));
            req.setAttribute("avgStars", (int) Math.round(rating[0]));

            HttpSession session = req.getSession(false);
            User me = session == null ? null : (User) session.getAttribute("user");
            if (me != null) {
                req.setAttribute("savedIds", wishlistDao.listingIds(me.getId()));
            }
        } catch (SQLException e) {
            throw new ServletException("Could not read the seller", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/seller.jsp").forward(req, resp);
    }
}
