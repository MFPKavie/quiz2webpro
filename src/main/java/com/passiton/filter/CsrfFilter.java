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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

public class CsrfFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/img/") || path.startsWith("/photo/")) {
            chain.doFilter(req, resp);
            return;
        }

        HttpSession session = req.getSession();
        String token = (String) session.getAttribute("csrf");
        if (token == null) {
            token = newToken();
            session.setAttribute("csrf", token);
        }

        // GET never changes data (PRG), so only POSTs need the token
        if ("POST".equals(req.getMethod())) {
            String sent = req.getHeader("X-CSRF-Token");
            if (sent == null) {
                sent = req.getParameter("csrf");
            }
            if (!sameToken(token, sent)) {
                if (path.startsWith("/api/")) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.setContentType("application/json");
                    resp.getWriter().write("{\"error\":\"csrf\"}");
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }
        }
        chain.doFilter(req, resp);
    }

    private static boolean sameToken(String expected, String sent) {
        return sent != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8));
    }
}
