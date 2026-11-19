package com.passiton.util;

public final class Json {
    private Json() { }

    public static String wishlist(boolean saved, int count) {
        return "{\"saved\":" + saved + ",\"count\":" + count + "}";
    }

    public static String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        return sb.toString();
    }
}
