package com.passiton.web;

import com.passiton.dao.UserDao;
import com.passiton.model.User;
import com.passiton.util.Flash;
import com.passiton.util.Uploads;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@WebServlet("/profile")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10L * 1024 * 1024, maxRequestSize = 12L * 1024 * 1024)
public class ProfileServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/profile.jsp";
    private static final Pattern LINE_ID = Pattern.compile("[A-Za-z0-9._-]{0,40}");

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            User fresh = userDao.findById(me.getId());
            Map<String, String> form = new HashMap<>();
            form.put("name", fresh.getName());
            form.put("faculty", fresh.getFaculty() == null ? "" : fresh.getFaculty());
            form.put("lineId", fresh.getLineId() == null ? "" : fresh.getLineId());
            show(req, resp, fresh, form, Map.of());
        } catch (SQLException e) {
            throw new ServletException("Could not read the profile", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User me = (User) req.getSession().getAttribute("user");
        try {
            User fresh = userDao.findById(me.getId());
            Map<String, String> form = new HashMap<>();
            form.put("name", Validate.text(req.getParameter("name")));
            form.put("faculty", Validate.text(req.getParameter("faculty")));
            form.put("lineId", Validate.text(req.getParameter("lineId")));

            Map<String, String> errors = new LinkedHashMap<>();
            if (!Validate.length(form.get("name"), 2, 80)) {
                errors.put("name", "Enter your name (2 to 80 characters).");
            }
            if (!Validate.length(form.get("faculty"), 0, 80)) {
                errors.put("faculty", "Keep the faculty under 80 characters.");
            }
            if (!LINE_ID.matcher(form.get("lineId")).matches()) {
                errors.put("lineId", "A LINE ID uses letters, numbers, . _ or - (40 at most).");
            }
            List<Part> avatarParts;
            try {
                avatarParts = Uploads.parts(req, "avatar");
            } catch (IllegalStateException e) {
                avatarParts = List.of();
                errors.put("avatar", "That photo is too large. Use 2 MB or less.");
            }
            if (avatarParts.size() > 1) {
                errors.put("avatar", "Choose one photo.");
            } else if (avatarParts.size() == 1) {
                String problem = Uploads.check(avatarParts.get(0));
                if (problem != null) {
                    errors.put("avatar", problem + " Use JPEG, PNG or WebP, 2 MB at most.");
                }
            }

            if (!errors.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                show(req, resp, fresh, form, errors);
                return;
            }

            String oldAvatar = fresh.getAvatar();
            String avatar = oldAvatar;
            if (avatarParts.size() == 1) {
                avatar = Uploads.save(avatarParts.get(0));
            } else if (req.getParameter("removeAvatar") != null) {
                avatar = null;
            }

            userDao.updateProfile(me.getId(), form.get("name"),
                    form.get("faculty").isEmpty() ? null : form.get("faculty"),
                    form.get("lineId").isEmpty() ? null : form.get("lineId"), avatar);
            if (oldAvatar != null && !oldAvatar.equals(avatar)) {
                Uploads.delete(oldAvatar);
            }

            User updated = userDao.findById(me.getId());
            updated.setPasswordHash(null);
            req.getSession().setAttribute("user", updated);

            Flash.ok(req, "Profile saved.");
            resp.sendRedirect(req.getContextPath() + "/profile");
        } catch (SQLException e) {
            throw new ServletException("Could not save the profile", e);
        }
    }

    private void show(HttpServletRequest req, HttpServletResponse resp, User profile,
                      Map<String, String> form, Map<String, String> errors)
            throws ServletException, IOException {
        req.setAttribute("profile", profile);
        req.setAttribute("form", form);
        req.setAttribute("errors", errors);
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }
}
