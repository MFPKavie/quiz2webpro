package com.passiton.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class Flash {
    private Flash() { }

    public static void ok(HttpServletRequest req, String message) {
        put(req, "ok", message);
    }

    public static void error(HttpServletRequest req, String message) {
        put(req, "error", message);
    }

    private static void put(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flash", message);
        session.setAttribute("flashType", type);
    }
}
