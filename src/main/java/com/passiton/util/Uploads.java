package com.passiton.util;

import com.passiton.dao.PhotoDao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Pattern;

public final class Uploads {
    public static final long MAX_BYTES = 2L * 1024 * 1024;
    public static final int MAX_FILES = 4;
    public static final String FIELD = "photos";

    private static final Pattern OUR_NAME = Pattern.compile("[a-f0-9]{32}\\.(jpg|png|webp)");
    private static final SecureRandom RANDOM = new SecureRandom();

    private Uploads() { }

    public static List<Part> photoParts(HttpServletRequest req) throws IOException, jakarta.servlet.ServletException {
        return parts(req, FIELD);
    }

    public static List<Part> parts(HttpServletRequest req, String field) throws IOException, jakarta.servlet.ServletException {
        List<Part> list = new ArrayList<>();
        for (Part part : req.getParts()) {
            if (field.equals(part.getName()) && part.getSize() > 0) {
                list.add(part);
            }
        }
        return list;
    }

    public static String check(Part part) throws IOException {
        String name = part.getSubmittedFileName() == null ? "A photo" : "“" + part.getSubmittedFileName() + "”";
        if (part.getSize() > MAX_BYTES) {
            return name + " is larger than 2 MB.";
        }
        String type = part.getContentType();
        String detected = detectType(part);
        if (detected == null || !detected.equals(type)) {
            return name + " is not a JPEG, PNG or WebP image.";
        }
        return null;
    }

    public static String save(Part part) throws IOException, SQLException {
        String ext = switch (detectType(part)) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
        byte[] random = new byte[16];
        RANDOM.nextBytes(random);
        String fileName = HexFormat.of().formatHex(random) + "." + ext;

        try (InputStream in = part.getInputStream()) {
            new PhotoDao().saveFile(fileName, in);
        }
        return fileName;
    }

    public static void delete(String fileName) {
        if (fileName == null || !OUR_NAME.matcher(fileName).matches()) {
            return;
        }
        try {
            new PhotoDao().deleteFile(fileName);
        } catch (SQLException e) {
            // best effort: a leftover photo row wastes space but breaks nothing
        }
    }

    // trust the file's first bytes, not its name or the browser's content type
    private static String detectType(Part part) throws IOException {
        byte[] head = new byte[12];
        int n;
        try (InputStream in = part.getInputStream()) {
            n = in.readNBytes(head, 0, head.length);
        }
        if (n >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (n >= 8 && Arrays.equals(Arrays.copyOf(head, 8),
                new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A})) {
            return "image/png";
        }
        if (n >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}
