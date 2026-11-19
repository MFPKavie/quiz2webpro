package com.passiton.web;

import com.passiton.dao.CategoryDao;
import com.passiton.dao.Db;
import com.passiton.util.Flash;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;

@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {
    private final CategoryDao categoryDao = new CategoryDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            req.setAttribute("categories", categoryDao.findAllWithCounts());
            req.setAttribute("openReports", new com.passiton.dao.ReportDao().countOpen());
        } catch (SQLException e) {
            throw new ServletException("Could not read the categories", e);
        }
        req.setAttribute("adminTab", "categories");
        req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = Validate.text(req.getParameter("action"));
        String name = Validate.text(req.getParameter("name"));
        Integer id = Validate.intBetween(req.getParameter("id"), 1, Integer.MAX_VALUE);
        try {
            switch (action) {
                case "add" -> {
                    if (validName(req, name)) {
                        categoryDao.create(name, slug(name));
                        Flash.ok(req, "Category “" + name + "” added.");
                    }
                }
                case "rename" -> {
                    if (id != null && validName(req, name)) {
                        categoryDao.rename(id, name, slug(name));
                        Flash.ok(req, "Category renamed to “" + name + "”.");
                    }
                }
                case "delete" -> {
                    if (id != null) {
                        try {
                            categoryDao.delete(id);
                            Flash.ok(req, "Category deleted.");
                        } catch (SQLException e) {
                            if (!Db.isStillReferenced(e)) throw e;
                            int n = categoryDao.listingCount(id);
                            Flash.error(req, "This category still has " + n + (n == 1 ? " listing" : " listings")
                                    + ". Move or delete them first.");
                        }
                    }
                }
                default -> Flash.error(req, "Unknown action.");
            }
        } catch (SQLException e) {
            if (Db.isDuplicate(e)) {
                Flash.error(req, "A category with that name already exists.");
            } else {
                throw new ServletException("Could not change the category", e);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/categories");
    }

    private static boolean validName(HttpServletRequest req, String name) {
        if (!Validate.length(name, 2, 50) || slug(name).isEmpty()) {
            Flash.error(req, "A category name needs 2 to 50 characters, with at least one letter or number.");
            return false;
        }
        return true;
    }

    static String slug(String name) {
        // drop accents only from Latin letters (Café -> cafe); Thai vowel and tone marks are part of the word
        String plain = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("(?<=\\p{IsLatin})\\p{M}+", "");
        plain = Normalizer.normalize(plain, Normalizer.Form.NFC);
        String s = plain.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}\\p{M}]+", "-").replaceAll("(^-|-$)", "");
        return s.length() > 50 ? s.substring(0, 50) : s;
    }
}
