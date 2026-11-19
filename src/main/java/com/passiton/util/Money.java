package com.passiton.util;

import java.util.Locale;

public final class Money {
    public static final int MAX = 20_000_000;

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");

    private Money() { }

    public static String rp(int amount) {
        return "Rp " + number(amount);
    }

    public static String number(int amount) {
        return String.format(INDONESIA, "%,d", amount);
    }

    public static String price(int amount) {
        return amount == 0 ? "FREE" : rp(amount);
    }
}
