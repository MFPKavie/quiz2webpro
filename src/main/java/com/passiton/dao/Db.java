package com.passiton.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private static volatile String url;
    private static volatile String user;

    private Db() { }

    public static void init(String dbUrl, String dbUser) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "MySQL Connector/J is missing: copy mysql-connector-j-26.x.x.jar into WEB-INF/lib", e);
        }
        url = dbUrl;
        user = dbUser;
    }

    // MySQL error numbers (Lecture 5): 1062 duplicate key, 1451 delete blocked by a foreign key
    public static boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 1062;
    }

    public static boolean isStillReferenced(SQLException e) {
        return e.getErrorCode() == 1451;
    }

    public static Connection get() throws SQLException {
        if (url == null) {
            throw new SQLException("Db.init() was not called: is StartupListener running?");
        }
        String password = System.getenv("PIO_DB_PASSWORD");
        if (password == null) {
            throw new SQLException("The PIO_DB_PASSWORD environment variable is not set for Tomcat");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
