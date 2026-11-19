package com.passiton.web;

import com.passiton.dao.ListingDao;
import com.passiton.dao.OfferDao;
import com.passiton.dao.PhotoDao;
import com.passiton.dao.ReviewDao;
import com.passiton.dao.WishlistDao;
import com.passiton.model.Listing;
import com.passiton.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/listing")
public class ListingDetailServlet extends HttpServlet {
    static final String VIEW = "/WEB-INF/views/listing-detail.jsp";

    private static final ListingDao LISTINGS = new ListingDao();
    private static final PhotoDao PHOTOS = new PhotoDao();
    private static final OfferDao OFFERS = new OfferDao();
    private static final WishlistDao WISHLIST = new WishlistDao();
    private static final ReviewDao REVIEWS = new ReviewDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            if (!prepare(req, id)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (SQLException e) {
            throw new ServletException("Could not read listing " + id, e);
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    static boolean prepare(HttpServletRequest req, int id) throws SQLException {
        Listing listing = LISTINGS.findById(id);
        if (listing == null) {
            return false;
        }
        req.setAttribute("listing", listing);
        req.setAttribute("photos", PHOTOS.findByListing(id));

        req.setAttribute("saveCount", WISHLIST.count(id));
        if ("sold".equals(listing.getStatus())) {
            req.setAttribute("reviews", REVIEWS.findByListing(id));
        }

        HttpSession session = req.getSession(false);
        User me = session == null ? null : (User) session.getAttribute("user");
        if (me != null) {
            if (me.getId() == listing.getSellerId()) {
                req.setAttribute("pendingOffers", OFFERS.countPending(id));
            } else {
                req.setAttribute("myOffer", OFFERS.findLatest(id, me.getId()));
                req.setAttribute("saved", WISHLIST.listingIds(me.getId()).contains(id));
                if (REVIEWS.canReview(id, me.getId())) {
                    req.setAttribute("canReview", true);
                    req.setAttribute("myReview", REVIEWS.find(id, me.getId()));
                }
            }
        }
        return true;
    }
}
