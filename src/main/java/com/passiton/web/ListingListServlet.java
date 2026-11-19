package com.passiton.web;

import com.passiton.util.Money;
import com.passiton.dao.CategoryDao;
import com.passiton.dao.ListingDao;
import com.passiton.dao.WishlistDao;
import com.passiton.model.Category;
import com.passiton.model.ListingFilter;
import com.passiton.model.User;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

// "" = exactly the home page; "/" would also catch every css/js/img request
@WebServlet(urlPatterns = {"", "/listings"})
public class ListingListServlet extends HttpServlet {
    static final int PAGE_SIZE = 12;
    private static final Set<String> CONDITIONS = Set.of("new", "like_new", "good", "fair");
    private static final Set<String> SORTS = Set.of("newest", "price_asc", "price_desc");

    private final ListingDao listingDao = new ListingDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final WishlistDao wishlistDao = new WishlistDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        boolean home = req.getServletPath().isEmpty();
        int page = parsePage(req.getParameter("page"));

        try {
            List<Category> categories = categoryDao.findAll();
            ListingFilter filter = home ? new ListingFilter() : readFilter(req, categories);

            int total = listingDao.count(filter);
            int pages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
            page = Math.min(page, pages);

            req.setAttribute("listings", listingDao.find(filter, PAGE_SIZE, (page - 1) * PAGE_SIZE));
            req.setAttribute("filter", filter);
            req.setAttribute("filterQuery", filterQuery(filter));
            req.setAttribute("total", total);
            req.setAttribute("page", page);
            req.setAttribute("pages", pages);
            req.setAttribute("categories", categories);
            req.setAttribute("categoryCount", categories.size());

            HttpSession session = req.getSession(false);
            User me = session == null ? null : (User) session.getAttribute("user");
            if (me != null) {
                req.setAttribute("savedIds", wishlistDao.listingIds(me.getId()));
            }
        } catch (SQLException e) {
            throw new ServletException("Could not read the listings", e);
        }
        req.setAttribute("home", home);
        req.getRequestDispatcher("/WEB-INF/views/listings.jsp").forward(req, resp);
    }

    private static ListingFilter readFilter(HttpServletRequest req, List<Category> categories) {
        ListingFilter f = new ListingFilter();

        String q = Validate.text(req.getParameter("q"));
        f.setQ(q.length() > 100 ? q.substring(0, 100) : q);

        String category = Validate.text(req.getParameter("category"));
        if (categories.stream().anyMatch(c -> c.getSlug().equals(category))) {
            f.setCategory(category);
        }

        String condition = Validate.text(req.getParameter("condition"));
        if (CONDITIONS.contains(condition)) {
            f.setCondition(condition);
        }

        Integer min = Validate.intBetween(Validate.text(req.getParameter("min")), 0, Money.MAX);
        Integer max = Validate.intBetween(Validate.text(req.getParameter("max")), 0, Money.MAX);
        if (min != null && max != null && min > max) {
            Integer t = min; min = max; max = t;
        }
        f.setMinPrice(min);
        f.setMaxPrice(max);

        String sort = Validate.text(req.getParameter("sort"));
        if (SORTS.contains(sort)) {
            f.setSort(sort);
        }
        return f;
    }

    private static String filterQuery(ListingFilter f) {
        StringBuilder sb = new StringBuilder();
        add(sb, "q", f.getQ());
        add(sb, "category", f.getCategory());
        add(sb, "condition", f.getCondition());
        add(sb, "min", f.getMinPrice() == null ? "" : f.getMinPrice().toString());
        add(sb, "max", f.getMaxPrice() == null ? "" : f.getMaxPrice().toString());
        add(sb, "sort", "newest".equals(f.getSort()) ? "" : f.getSort());
        return sb.toString();
    }

    private static void add(StringBuilder sb, String name, String value) {
        if (!value.isEmpty()) {
            sb.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8)).append('&');
        }
    }

    private static int parsePage(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
