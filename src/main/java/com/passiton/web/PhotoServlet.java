package com.passiton.web;

import com.passiton.dao.PhotoDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet("/photo/*")
public class PhotoServlet extends HttpServlet {
    // only our own random file names
    private static final Pattern NAME = Pattern.compile("[a-f0-9]{32}\\.(jpg|png|webp)");
    private final PhotoDao photoDao = new PhotoDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        String name = path == null ? "" : path.substring(1);
        if (!NAME.matcher(name).matches()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        byte[] data;
        try {
            data = photoDao.readFile(name);
        } catch (SQLException e) {
            throw new ServletException("Could not load the photo", e);
        }
        if (data == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String type = name.endsWith(".png") ? "image/png"
                    : name.endsWith(".webp") ? "image/webp"
                    : "image/jpeg";
        resp.setCharacterEncoding((String) null); // EncodingFilter set UTF-8; an image has no charset
        resp.setContentType(type);
        resp.setContentLength(data.length);
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        resp.getOutputStream().write(data);
    }
}
