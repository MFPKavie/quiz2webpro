package com.passiton.util;

import java.util.regex.Pattern;

public final class Validate {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Validate() { }

    public static String text(String value) {
        return value == null ? "" : value.strip();
    }

    public static boolean length(String value, int min, int max) {
        int n = value.codePointCount(0, value.length());
        return n >= min && n <= max;
    }

    public static Integer intBetween(String value, int min, int max) {
        try {
            int n = Integer.parseInt(value);
            return n >= min && n <= max ? n : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean isEmail(String value) {
        return value.length() <= 120 && EMAIL.matcher(value).matches();
    }

    public static boolean isLocalPath(String next) {
        return next != null
                && next.startsWith("/")
                && !next.startsWith("//")
                && !next.contains("\\")
                && next.chars().noneMatch(Character::isISOControl);
    }
}
