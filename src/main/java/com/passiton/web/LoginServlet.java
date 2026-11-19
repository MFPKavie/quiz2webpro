package com.passiton.web;

import com.passiton.dao.UserDao;
import com.passiton.filter.CsrfFilter;
import com.passiton.model.User;
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
import java.util.Locale;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/login.jsp";
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/");
            return;
        }
        req.setAttribute("next", safeNext(req.getParameter("next")));
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = Validate.text(req.getParameter("email")).toLowerCase(Locale.ROOT);
        String password = req.getParameter("password");
        String next = safeNext(req.getParameter("next"));

        User user;
        try {
            user = userDao.findByEmail(email);
        } catch (SQLException e) {
            throw new ServletException("Could not read the user", e);
        }

        boolean passwordOk = Passwords.verify(password, user == null ? Passwords.DUMMY_HASH : user.getPasswordHash());

        if (user == null || !passwordOk) {
            // same message for unknown email and wrong password
            req.setAttribute("error", "Email or password is incorrect.");
            req.setAttribute("email", email);
            req.setAttribute("next", next);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.getRequestDispatcher(VIEW).forward(req, resp);
            return;
        }

        req.changeSessionId(); // new session id at login: stops session fixation
        HttpSession session = req.getSession();
        user.setPasswordHash(null);
        session.setAttribute("user", user);
        session.setAttribute("csrf", CsrfFilter.newToken());

        Flash.ok(req, "Welcome back, " + user.getName() + ".");
        resp.sendRedirect(req.getContextPath() + (next.isEmpty() ? "/" : next));
    }

    private static boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    private static String safeNext(String next) {
        return Validate.isLocalPath(next) ? next : "";
    }
}
