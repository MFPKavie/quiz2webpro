<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Page not found"/>
<%@ include file="layout/header.jspf" %>

<section class="empty-state">
    <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
        <path d="M28 24h64v58H28z" stroke-dasharray="6 6"/>
        <path d="M46 52h28"/>
        <circle class="pin" cx="60" cy="22" r="6"/>
    </svg>
    <h1>Page not found</h1>
    <p>This flyer has been taken off the board.</p>
    <a class="btn btn--primary" href="${ctx}/">Back to the board</a>
</section>

<%@ include file="layout/footer.jspf" %>
