<%@ page contentType="text/html; charset=UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="status" value="${pageContext.errorData.statusCode}"/>
<c:set var="pageTitle" value="${status == 403 ? 'Not allowed' : 'Something went wrong'}"/>
<%@ include file="layout/header.jspf" %>

<section class="empty-state">
    <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
        <path d="M30 22h60l-4 64H34z"/>
        <path d="M44 44l10 10M54 44 44 54M66 44l10 10M76 44 66 54M48 72c8-6 16-6 24 0"/>
        <circle class="pin" cx="60" cy="20" r="6"/>
    </svg>
    <c:choose>
        <c:when test="${status == 403}">
            <h1>Not allowed</h1>
            <p>You can't do that here, or the form was open for too long.
                Go back, reload the page and try again.</p>
        </c:when>
        <c:otherwise>
            <h1>Something went wrong</h1>
            <p>Sorry, we could not finish that. Please try again in a moment.</p>
        </c:otherwise>
    </c:choose>
    <a class="btn btn--primary" href="${ctx}/">Back to the board</a>
</section>

<%@ include file="layout/footer.jspf" %>
