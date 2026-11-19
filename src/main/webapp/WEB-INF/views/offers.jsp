<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Offers"/>
<c:set var="nav" value="offers"/>
<%@ include file="layout/header.jspf" %>
<c:set var="received" value="${tab == 'received'}"/>

<h1 class="section-title">Offers</h1>

<nav class="tabs" aria-label="Offer lists">
    <a href="${ctx}/offers?tab=received" aria-current="${received ? 'page' : 'false'}">
        Received<c:if test="${receivedPending > 0}"> <span class="tabs__count">${receivedPending}</span></c:if>
    </a>
    <a href="${ctx}/offers?tab=sent" aria-current="${received ? 'false' : 'page'}">
        Sent<c:if test="${sentPending > 0}"> <span class="tabs__count">${sentPending}</span></c:if>
    </a>
</nav>

<c:choose>
    <c:when test="${empty offers}">
        <div class="empty-state">
            <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
                <path d="M26 30h68v44H26z"/>
                <path d="m26 30 34 26 34-26"/>
                <circle class="pin" cx="60" cy="24" r="6"/>
            </svg>
            <c:choose>
                <c:when test="${received}">
                    <h2>No offers on your things yet</h2>
                    <p>When someone makes an offer on one of your listings, it lands here.</p>
                    <a class="btn btn--primary" href="${ctx}/listing/new">Sell something</a>
                </c:when>
                <c:otherwise>
                    <h2>You haven't made any offers</h2>
                    <p>Found something you like? Make an offer from its page.</p>
                    <a class="btn btn--primary" href="${ctx}/listings">Browse the board</a>
                </c:otherwise>
            </c:choose>
        </div>
    </c:when>
    <c:otherwise>
        <ul class="offer-list">
            <c:forEach var="o" items="${offers}">
                <li class="offer-row offer-row--${o.status}">
                    <a class="offer-row__thumb" href="${ctx}/listing?id=${o.listingId}" tabindex="-1" aria-hidden="true">
                        <c:choose>
                            <c:when test="${not empty o.coverPhoto}"><img src="${ctx}/photo/${fn:escapeXml(o.coverPhoto)}" alt="" width="120" height="90"></c:when>
                            <c:otherwise><img class="card__placeholder" src="${ctx}/img/placeholder.svg" alt="" width="120" height="90"></c:otherwise>
                        </c:choose>
                    </a>

                    <div class="offer-row__main">
                        <p class="offer-row__title">
                            <a href="${ctx}/listing?id=${o.listingId}"><c:out value="${o.listingTitle}"/></a>
                            <span class="offer-row__asking">asking ${o.listingPriceText}</span>
                        </p>
                        <p class="offer-row__who">
                            <c:choose>
                                <c:when test="${received}">from <c:out value="${o.buyerName}"/></c:when>
                                <c:otherwise>to <c:out value="${o.sellerName}"/></c:otherwise>
                            </c:choose>
                            &middot; ${o.createdText}
                        </p>
                        <c:if test="${not empty o.message}">
                            <p class="offer-row__msg">&ldquo;<c:out value="${o.message}"/>&rdquo;</p>
                        </c:if>
                        <c:if test="${o.status == 'accepted'}">
                            <c:set var="lineId" value="${received ? o.buyerLineId : o.sellerLineId}"/>
                            <p class="contact-line">
                                <c:choose>
                                    <c:when test="${not empty lineId}">Contact <c:out value="${received ? o.buyerName : o.sellerName}"/> on LINE: <strong><c:out value="${lineId}"/></strong></c:when>
                                    <c:otherwise>Agree on a time to meet and hand it over.</c:otherwise>
                                </c:choose>
                            </p>
                        </c:if>
                    </div>

                    <div class="offer-row__side">
                        <span class="offer-row__amount">${o.amountText}</span>
                        <span class="chip chip--offer-${o.status}">${o.statusLabel}</span>

                        <c:if test="${not received and o.status == 'accepted' and o.listingStatus == 'sold'}">
                            <a class="btn btn--ghost btn--sm" href="${ctx}/review/new?listingId=${o.listingId}">Review seller</a>
                        </c:if>

                        <c:if test="${o.status == 'pending'}">
                            <div class="offer-row__actions">
                                <c:choose>
                                    <c:when test="${received and o.listingStatus == 'available'}">
                                        <form method="post" action="${ctx}/offer/accept"
                                              data-confirm="Accept ${fn:escapeXml(o.amountText)} from ${fn:escapeXml(o.buyerName)}? Other pending offers on this item will be declined and it becomes reserved.">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="offerId" value="${o.id}">
                                            <button type="submit" class="btn btn--primary btn--sm">Accept</button>
                                        </form>
                                        <form method="post" action="${ctx}/offer/decline">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="offerId" value="${o.id}">
                                            <button type="submit" class="btn btn--ghost btn--sm">Decline</button>
                                        </form>
                                    </c:when>
                                    <c:when test="${received}">
                                        <span class="offer-row__note">Listing is ${o.listingStatus}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <a class="btn btn--ghost btn--sm" href="${ctx}/listing?id=${o.listingId}#offer">Edit</a>
                                        <form method="post" action="${ctx}/offer/withdraw" data-confirm="Withdraw your offer?">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="offerId" value="${o.id}">
                                            <input type="hidden" name="from" value="inbox">
                                            <button type="submit" class="btn btn--danger btn--sm">Withdraw</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:if>
                    </div>
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>

<%@ include file="layout/footer.jspf" %>
