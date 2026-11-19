package com.passiton.web;

import com.passiton.dao.Db;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class StartupListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext app = event.getServletContext();
        String dbUrl = setting("PIO_DB_URL", app.getInitParameter("dbUrl"));
        Db.init(dbUrl, setting("PIO_DB_USER", app.getInitParameter("dbUser")));
        // changes on every deploy; header.jspf adds it to the CSS/JS links so browsers drop old cached copies
        app.setAttribute("assetVersion", System.currentTimeMillis());
        app.log("Pass It On started; database = " + dbUrl.replaceFirst("\\?.*", ""));
    }

    // a host sets environment variables; web.xml keeps the local defaults
    private static String setting(String envName, String fallback) {
        String value = System.getenv(envName);
        return value == null || value.isBlank() ? fallback : value;
    }
}
