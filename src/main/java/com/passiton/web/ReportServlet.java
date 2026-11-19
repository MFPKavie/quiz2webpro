package com.passiton.web;

import com.passiton.dao.ListingDao;
import com.passiton.dao.ReportDao;
import com.passiton.model.Listing;
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

@WebServlet("/report")
public class ReportServlet extends HttpServlet {
    private final ReportDao reportDao = new ReportDao();
    private final ListingDao listingDao = new ListingDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            Integer listingId = Validate.intBetween(req.getParameter("listingId"), 1, Integer.MAX_VALUE);
            Listing listing = listingId == null ? null : listingDao.findById(listingId);
            if (listing == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String back = req.getContextPath() + "/listing?id=" + listing.getId();
            String reason = Validate.text(req.getParameter("reason"));

            if (listing.getSellerId() == me.getId()) {
                Flash.error(req, "You can't report your own listing.");
            } else if (!Validate.length(reason, 5, 300)) {
                Flash.error(req, "Tell us what is wrong in 5 to 300 characters.");
            } else if (reportDao.hasOpen(listing.getId(), me.getId())) {
                Flash.error(req, "You already reported this listing. An admin will look at it soon.");
            } else {
                reportDao.create(listing.getId(), me.getId(), reason);
                Flash.ok(req, "Thanks for the report. An admin will look at it.");
            }
            resp.sendRedirect(back);
        } catch (SQLException e) {
            throw new ServletException("Could not save the report", e);
        }
    }
}
