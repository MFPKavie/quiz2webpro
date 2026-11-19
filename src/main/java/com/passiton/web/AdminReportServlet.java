package com.passiton.web;

import com.passiton.dao.Db;
import com.passiton.dao.ListingDao;
import com.passiton.dao.PhotoDao;
import com.passiton.dao.ReportDao;
import com.passiton.model.Photo;
import com.passiton.model.Report;
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

@WebServlet("/admin/reports")
public class AdminReportServlet extends HttpServlet {
    private final ReportDao reportDao = new ReportDao();
    private final ListingDao listingDao = new ListingDao();
    private final PhotoDao photoDao = new PhotoDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("reports", reportDao.findAll());
            req.setAttribute("openReports", reportDao.countOpen());
        } catch (SQLException e) {
            throw new ServletException("Could not read the reports", e);
        }
        req.setAttribute("adminTab", "reports");
        req.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String back = req.getContextPath() + "/admin/reports";
        try {
            Integer id = Validate.intBetween(req.getParameter("id"), 1, Integer.MAX_VALUE);
            Report report = id == null ? null : reportDao.findById(id);
            if (report == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String action = Validate.text(req.getParameter("action"));
            if ("dismiss".equals(action)) {
                if (reportDao.dismiss(report.getId())) {
                    Flash.ok(req, "Report dismissed. The listing stays up.");
                } else {
                    Flash.error(req, "That report is already closed.");
                }
            } else if ("remove".equals(action)) {
                List<Photo> photos = photoDao.findByListing(report.getListingId());
                try {
                    if (listingDao.adminDelete(report.getListingId())) {
                        photos.forEach(p -> Uploads.delete(p.getFileName()));
                        Flash.ok(req, "“" + report.getListingTitle() + "” was removed, and its reports were closed.");
                    } else {
                        Flash.error(req, "That listing is already gone.");
                    }
                } catch (SQLException e) {
                    if (!Db.isStillReferenced(e)) throw e;
                    Flash.error(req, "This listing has a buyer's review, so it can't be removed. Dismiss the report instead.");
                }
            } else {
                Flash.error(req, "Unknown action.");
            }
            resp.sendRedirect(back);
        } catch (SQLException e) {
            throw new ServletException("Could not update the report", e);
        }
    }
}
