<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Wishlist"/>
<c:set var="nav" value="wishlist"/>
<%@ include file="layout/header.jspf" %>

<section class="board wishlist" aria-labelledby="wish-title">
    <div class="board__head">
        <h1 id="wish-title" class="section-title">Your wishlist</h1>
        <p class="board__count">${fn:length(listings)}${fn:length(listings) == 1 ? ' saved listing' : ' saved listings'}</p>
    </div>

    <c:choose>
        <c:when test="${empty listings}">
            <div class="empty-state">
                <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
                    <path d="M60 84S28 64 21 44a17 17 0 0 1 39-16 17 17 0 0 1 39 16c-7 20-39 40-39 40z" stroke-dasharray="7 6"/>
                    <path d="M98 18l6-6M104 26h8M90 12V4"/>
                </svg>
                <h2>Nothing saved yet</h2>
                <p>Tap the heart on anything you like and it waits for you here.</p>
                <a class="btn btn--primary" href="${ctx}/listings">Browse the board</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="grid">
                <c:forEach var="l" items="${listings}">
                    <%@ include file="layout/listing-card.jspf" %>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="layout/footer.jspf" %>
