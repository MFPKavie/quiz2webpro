<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Reports"/>
<c:set var="nav" value="admin"/>
<%@ include file="../layout/header.jspf" %>
<%@ include file="../layout/admin-tabs.jspf" %>

<c:choose>
    <c:when test="${empty reports}">
        <div class="empty-state">
            <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
                <path d="M30 24h60v58H30z"/>
                <path d="m44 54 10 10 22-22"/>
                <circle class="pin" cx="60" cy="22" r="6"/>
            </svg>
            <h2>All clear</h2>
            <p>Nobody has reported a listing. When someone does, it shows up here.</p>
        </div>
    </c:when>
    <c:otherwise>
        <ul class="offer-list">
            <c:forEach var="r" items="${reports}">
                <li class="offer-row admin-row offer-row--${r.status == 'open' ? 'pending' : 'declined'}">
                    <div class="offer-row__main">
                        <p class="offer-row__title">
                            <a href="${ctx}/listing?id=${r.listingId}"><c:out value="${r.listingTitle}"/></a>
                            <span class="offer-row__asking">by <c:out value="${r.sellerName}"/> &middot; ${r.listingStatus}</span>
                        </p>
                        <p class="offer-row__who">reported by <c:out value="${r.reporterName}"/> &middot; ${r.createdText}</p>
                        <p class="offer-row__msg">&ldquo;<c:out value="${r.reason}"/>&rdquo;</p>
                    </div>
                    <div class="offer-row__side">
                        <span class="chip chip--offer-${r.status == 'open' ? 'pending' : 'declined'}">${r.statusLabel}</span>
                        <c:if test="${r.status == 'open'}">
                            <div class="offer-row__actions">
                                <form method="post" action="${ctx}/admin/reports">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <input type="hidden" name="action" value="dismiss">
                                    <button type="submit" class="btn btn--ghost btn--sm">Dismiss</button>
                                </form>
                                <form method="post" action="${ctx}/admin/reports"
                                      data-confirm="Remove &ldquo;${fn:escapeXml(r.listingTitle)}&rdquo; from the board for good?">
                                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                    <input type="hidden" name="id" value="${r.id}">
                                    <input type="hidden" name="action" value="remove">
                                    <button type="submit" class="btn btn--danger btn--sm">Remove listing</button>
                                </form>
                            </div>
                        </c:if>
                    </div>
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>

<%@ include file="../layout/footer.jspf" %>
