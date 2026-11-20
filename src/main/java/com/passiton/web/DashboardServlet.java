package com.passiton.web;

import com.passiton.dao.CategoryDao;
import com.passiton.dao.ListingDao;
import com.passiton.dao.OfferDao;
import com.passiton.dao.ReportDao;
import com.passiton.dao.ReviewDao;
import com.passiton.dao.UserDao;
import com.passiton.model.Category;
import com.passiton.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final ListingDao listingDao = new ListingDao();
    private final OfferDao offerDao = new OfferDao();
    private final ReviewDao reviewDao = new ReviewDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final UserDao userDao = new UserDao();
    private final ReportDao reportDao = new ReportDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            req.setAttribute("byStatus", listingDao.countByStatus(me.getId()));
            req.setAttribute("earned", com.passiton.util.Money.rp(offerDao.totalEarned(me.getId())));
            req.setAttribute("spent", com.passiton.util.Money.rp(offerDao.totalSpent(me.getId())));
            req.setAttribute("bought", offerDao.countBought(me.getId()));
            double[] rating = reviewDao.ratingSummary(me.getId());
            req.setAttribute("reviewCount", (int) rating[1]);
            req.setAttribute("avgRating", String.format(Locale.ENGLISH, "%.1f", rating[0]));

            if (me.isAdmin()) {
                List<Category> categories = categoryDao.findAllWithCounts();
                int max = categories.stream().mapToInt(Category::getListingCount).max().orElse(0);
                req.setAttribute("categoryStats", categories);
                req.setAttribute("maxCategoryCount", Math.max(max, 1));
                req.setAttribute("activeSellers", userDao.findActiveSellers(2));
                req.setAttribute("openReports", reportDao.countOpen());
            }
        } catch (SQLException e) {
            throw new ServletException("Could not build the dashboard", e);
        }
        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}
