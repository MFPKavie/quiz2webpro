package com.passiton.web;

import com.passiton.dao.Db;
import com.passiton.dao.UserDao;
import com.passiton.util.Flash;
import com.passiton.util.Passwords;
import com.passiton.util.Validate;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/register.jsp";
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = Validate.text(req.getParameter("name"));
        String email = Validate.text(req.getParameter("email")).toLowerCase(Locale.ROOT);
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String faculty = Validate.text(req.getParameter("faculty"));

        Map<String, String> errors = new LinkedHashMap<>();
        if (!Validate.length(name, 2, 80)) {
            errors.put("name", "Enter your name (2 to 80 characters).");
        }
        if (!Validate.isEmail(email)) {
            errors.put("email", "Enter a valid email address.");
        }
        if (!Validate.length(password, 8, 128)) {
            errors.put("password", "Use at least 8 characters.");
        }
        if (!Validate.length(faculty, 0, 80)) {
            errors.put("faculty", "Keep the faculty under 80 characters.");
        }

        try {
            if (!errors.containsKey("email") && userDao.emailExists(email)) {
                errors.put("email", "This email already has an account. Try logging in.");
            }
            if (errors.isEmpty()) {
                userDao.create(name, email, Passwords.hash(password), faculty.isEmpty() ? null : faculty);
            }
        } catch (SQLException e) {
            if (Db.isDuplicate(e)) {
                errors.put("email", "This email already has an account. Try logging in.");
            } else {
                throw new ServletException("Could not create the account", e);
            }
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("faculty", faculty);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        Flash.ok(req, "Account created. Log in with your email and password.");
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
