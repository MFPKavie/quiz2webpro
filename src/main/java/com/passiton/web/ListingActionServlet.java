package com.passiton.web;

import com.passiton.dao.Db;
import com.passiton.dao.ListingDao;
import com.passiton.dao.PhotoDao;
import com.passiton.model.Listing;
import com.passiton.model.Photo;
import com.passiton.model.User;
import com.passiton.util.Flash;
import com.passiton.util.Uploads;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet({"/listing/delete", "/listing/status"})
public class ListingActionServlet extends HttpServlet {
    private static final Map<String, List<String>> ALLOWED = Map.of(
            "available", List.of("reserved"),
            "reserved", List.of("sold", "available"),
            "sold", List.of());

    private final ListingDao listingDao = new ListingDao();
    private final PhotoDao photoDao = new PhotoDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            Integer id = Validate.intBetween(req.getParameter("id"), 1, Integer.MAX_VALUE);
            Listing listing = id == null ? null : listingDao.findById(id);
            if (listing == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            if (listing.getSellerId() != me.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if ("/listing/delete".equals(req.getServletPath())) {
                delete(req, resp, listing, me);
            } else {
                changeStatus(req, resp, listing, me);
            }
        } catch (SQLException e) {
            throw new ServletException("Could not change the listing", e);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp, Listing listing, User me)
            throws SQLException, IOException {
        String back = req.getContextPath() + "/listing?id=" + listing.getId();
        if ("sold".equals(listing.getStatus())) {
            Flash.error(req, "A sold listing can't be deleted.");
            resp.sendRedirect(back);
            return;
        }
        List<Photo> photos = photoDao.findByListing(listing.getId());
        try {
            if (!listingDao.delete(listing.getId(), me.getId())) {
                Flash.error(req, "This listing could not be deleted.");
                resp.sendRedirect(back);
                return;
            }
        } catch (SQLException e) {
            if (!Db.isStillReferenced(e)) throw e;
            Flash.error(req, "This listing has a review, so it can't be deleted.");
            resp.sendRedirect(back);
            return;
        }
        photos.forEach(p -> Uploads.delete(p.getFileName()));
        Flash.ok(req, "“" + listing.getTitle() + "” was taken off the board.");
        resp.sendRedirect(req.getContextPath() + "/");
    }

    private void changeStatus(HttpServletRequest req, HttpServletResponse resp, Listing listing, User me)
            throws SQLException, IOException {
        String back = req.getContextPath() + "/listing?id=" + listing.getId();
        String from = listing.getStatus();
        String to = Validate.text(req.getParameter("to"));

        if (!ALLOWED.getOrDefault(from, List.of()).contains(to)) {
            Flash.error(req, "That status change is not possible.");
        } else if (!listingDao.changeStatus(listing.getId(), me.getId(), from, to)) {
            Flash.error(req, "The listing changed meanwhile. Please try again.");
        } else {
            Flash.ok(req, switch (to) {
                case "reserved" -> "Marked as reserved.";
                case "sold" -> "Marked as sold. Nice one!";
                default -> "Back on the board as available.";
            });
        }
        resp.sendRedirect(back);
    }
}
