<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Dashboard"/>
<c:set var="nav" value="dashboard"/>
<%@ include file="layout/header.jspf" %>

<h1 class="section-title">Hi, <c:out value="${sessionScope.user.name}"/></h1>

<section aria-labelledby="selling-title">
    <h2 id="selling-title" class="dash-title">Selling</h2>
    <div class="tiles">
        <div class="tile"><p class="tile__label">Available</p><p class="tile__value">${byStatus.available}</p></div>
        <div class="tile"><p class="tile__label">Reserved</p><p class="tile__value">${byStatus.reserved}</p></div>
        <div class="tile"><p class="tile__label">Sold</p><p class="tile__value">${byStatus.sold}</p></div>
        <div class="tile tile--accent"><p class="tile__label">Total earned</p><p class="tile__value">${earned}</p></div>
        <div class="tile">
            <p class="tile__label">Average rating</p>
            <c:choose>
                <c:when test="${reviewCount > 0}">
                    <p class="tile__value">${avgRating}<span class="tile__unit"> / 5</span></p>
                    <p class="tile__note">${reviewCount}${reviewCount == 1 ? ' review' : ' reviews'}</p>
                </c:when>
                <c:otherwise><p class="tile__value tile__value--muted">&ndash;</p><p class="tile__note">No reviews yet</p></c:otherwise>
            </c:choose>
        </div>
    </div>
</section>

<section aria-labelledby="buying-title">
    <h2 id="buying-title" class="dash-title">Buying</h2>
    <div class="tiles">
        <div class="tile"><p class="tile__label">Things bought</p><p class="tile__value">${bought}</p></div>
        <div class="tile"><p class="tile__label">Total spent</p><p class="tile__value">${spent}</p></div>
    </div>
    <p class="dash-links">
        <a class="btn btn--ghost btn--sm" href="${ctx}/offers">Your offers</a>
        <a class="btn btn--ghost btn--sm" href="${ctx}/seller?id=${sessionScope.user.id}">Your seller page</a>
        <a class="btn btn--primary btn--sm" href="${ctx}/listing/new">Sell something</a>
    </p>
</section>

<c:if test="${sessionScope.user.admin}">
    <section class="dash-admin" aria-labelledby="admin-title">
        <h2 id="admin-title" class="section-title">Admin overview</h2>
        <div class="tiles">
            <a class="tile tile--link" href="${ctx}/admin/reports">
                <p class="tile__label">Open reports</p><p class="tile__value">${openReports}</p>
            </a>
        </div>

        <div class="dash-tables">
            <div class="panel">
                <h3 class="panel__title">Listings per category</h3>
                <table class="data-table">
                    <thead>
                        <tr><th scope="col">Category</th><th scope="col" class="data-table__bar-col">Listings</th><th scope="col" class="num">Available</th><th scope="col" class="num">Sold</th></tr>
                    </thead>
                    <tbody>
                        <c:forEach var="cat" items="${categoryStats}">
                            <tr>
                                <th scope="row"><c:out value="${cat.name}"/></th>
                                <td class="data-table__bar-col">
                                    <span class="bar">
                                        <span class="bar__fill" style="width: ${cat.listingCount * 100 / maxCategoryCount}%"></span>
                                        <span class="bar__value">${cat.listingCount}</span>
                                    </span>
                                </td>
                                <td class="num">${cat.availableCount}</td>
                                <td class="num">${cat.soldCount}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="panel">
                <h3 class="panel__title">Active sellers <span class="panel__sub">(2 or more listings)</span></h3>
                <c:choose>
                    <c:when test="${empty activeSellers}"><p class="note-line">No seller has 2 listings yet.</p></c:when>
                    <c:otherwise>
                        <table class="data-table">
                            <thead><tr><th scope="col">Seller</th><th scope="col" class="num">Listings</th><th scope="col" class="num">Sold</th></tr></thead>
                            <tbody>
                                <c:forEach var="s" items="${activeSellers}">
                                    <tr>
                                        <th scope="row"><a href="${ctx}/seller?id=${s.id}"><c:out value="${s.name}"/></a>
                                            <c:if test="${not empty s.faculty}"><span class="data-table__sub"><c:out value="${s.faculty}"/></span></c:if></th>
                                        <td class="num">${s.listingCount}</td>
                                        <td class="num">${s.soldCount}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>
</c:if>

<%@ include file="layout/footer.jspf" %>
