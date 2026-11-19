package com.passiton.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            chain.doFilter(req, resp);
            return;
        }

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length());

        if (path.startsWith("/api/")) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"login\"}");
        } else if ("GET".equals(req.getMethod())) {
            String next = path + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
            resp.sendRedirect(ctx + "/login?next=" + URLEncoder.encode(next, StandardCharsets.UTF_8));
        } else {
            resp.sendRedirect(ctx + "/login");
        }
    }
}
