package com.passiton.web;

import com.passiton.dao.Db;
import com.passiton.dao.ListingDao;
import com.passiton.dao.ReviewDao;
import com.passiton.model.Listing;
import com.passiton.model.Review;
import com.passiton.model.User;
import com.passiton.util.Flash;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/review/*")
public class ReviewServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/review-form.jsp";

    private final ReviewDao reviewDao = new ReviewDao();
    private final ListingDao listingDao = new ListingDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        String action = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            if ("/new".equals(action)) {
                Listing listing = reviewableListing(req, resp, me);
                if (listing == null) return;
                Review existing = reviewDao.find(listing.getId(), me.getId());
                if (existing != null) {
                    Flash.error(req, "You have already reviewed this sale. You can edit your review instead.");
                    resp.sendRedirect(req.getContextPath() + "/review/edit?id=" + existing.getId());
                    return;
                }
                Map<String, String> form = new HashMap<>();
                form.put("rating", "5");
                form.put("comment", "");
                show(req, resp, listing, null, form, Map.of());
            } else if ("/edit".equals(action)) {
                Review review = ownReview(req, resp, me);
                if (review == null) return;
                Map<String, String> form = new HashMap<>();
                form.put("rating", String.valueOf(review.getRating()));
                form.put("comment", review.getComment() == null ? "" : review.getComment());
                show(req, resp, listingDao.findById(review.getListingId()), review, form, Map.of());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Could not open the review", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        String action = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            switch (action) {
                case "/new" -> create(req, resp, me);
                case "/edit" -> update(req, resp, me);
                case "/delete" -> delete(req, resp, me);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Could not save the review", e);
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, ServletException, IOException {
        Listing listing = reviewableListing(req, resp, me);
        if (listing == null) return;
        Map<String, String> form = readForm(req);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            show(req, resp, listing, null, form, errors);
            return;
        }
        String back = req.getContextPath() + "/listing?id=" + listing.getId();
        try {
            reviewDao.create(listing.getId(), me.getId(), Integer.parseInt(form.get("rating")), comment(form));
        } catch (SQLException e) {
            if (!Db.isDuplicate(e)) throw e;
            Flash.error(req, "You have already reviewed this sale.");
            resp.sendRedirect(back);
            return;
        }
        Flash.ok(req, "Thanks! Your review helps other students.");
        resp.sendRedirect(back);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, ServletException, IOException {
        Review review = ownReview(req, resp, me);
        if (review == null) return;
        Map<String, String> form = readForm(req);
        Map<String, String> errors = validate(form);
        if (!errors.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            show(req, resp, listingDao.findById(review.getListingId()), review, form, errors);
            return;
        }
        reviewDao.update(review.getId(), me.getId(), Integer.parseInt(form.get("rating")), comment(form));
        Flash.ok(req, "Review updated.");
        resp.sendRedirect(req.getContextPath() + "/listing?id=" + review.getListingId());
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Review review = ownReview(req, resp, me);
        if (review == null) return;
        reviewDao.delete(review.getId(), me.getId());
        Flash.ok(req, "Review deleted.");
        resp.sendRedirect(req.getContextPath() + "/listing?id=" + review.getListingId());
    }

    private Listing reviewableListing(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Integer id = Validate.intBetween(req.getParameter("listingId"), 1, Integer.MAX_VALUE);
        Listing listing = id == null ? null : listingDao.findById(id);
        if (listing == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (!reviewDao.canReview(listing.getId(), me.getId())) {
            Flash.error(req, "Only the buyer can review, after the item is marked sold.");
            resp.sendRedirect(req.getContextPath() + "/listing?id=" + listing.getId());
            return null;
        }
        return listing;
    }

    private Review ownReview(HttpServletRequest req, HttpServletResponse resp, User me)
            throws SQLException, IOException {
        Integer id = Validate.intBetween(req.getParameter("id"), 1, Integer.MAX_VALUE);
        Review review = id == null ? null : reviewDao.findById(id);
        if (review == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (review.getReviewerId() != me.getId()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return review;
    }

    private static Map<String, String> readForm(HttpServletRequest req) {
        Map<String, String> form = new HashMap<>();
        form.put("rating", Validate.text(req.getParameter("rating")));
        form.put("comment", Validate.text(req.getParameter("comment")));
        return form;
    }

    private static Map<String, String> validate(Map<String, String> form) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (Validate.intBetween(form.get("rating"), 1, 5) == null) {
            errors.put("rating", "Choose 1 to 5 stars.");
        }
        if (!Validate.length(form.get("comment"), 0, 500)) {
            errors.put("comment", "Keep the comment under 500 characters.");
        }
        return errors;
    }

    private static String comment(Map<String, String> form) {
        return form.get("comment").isEmpty() ? null : form.get("comment");
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, Listing listing, Review review,
                      Map<String, String> form, Map<String, String> errors)
            throws ServletException, IOException {
        req.setAttribute("listing", listing);
        req.setAttribute("review", review);
        req.setAttribute("form", form);
        req.setAttribute("errors", errors);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
