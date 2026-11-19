<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${home ? '' : 'Browse'}"/>
<c:set var="nav" value="home"/>
<%@ include file="layout/header.jspf" %>

<c:if test="${home}">
    <section class="hero">
        <div class="hero__copy">
            <p class="hero__kicker">Second-hand, on campus</p>
            <h1 class="hero__title">Your senior's stuff, <span class="hl">your new stuff</span></h1>
            <p class="hero__lead">Buy and sell used things with other students</p>

            <form class="searchbar" action="${ctx}/listings" method="get" role="search">
                <label class="visually-hidden" for="q">Search listings</label>
                <svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.6-3.6"/></svg>
                <input id="q" name="q" type="search" maxlength="100" placeholder="Search">
                <button type="submit" class="btn btn--primary">Search</button>
            </form>
        </div>
    </section>

    <section class="categories" aria-labelledby="cat-title">
        <h2 id="cat-title" class="section-title">Browse ${categoryCount} categories</h2>
        <ul class="paper-tabs">
            <c:forEach var="cat" items="${categories}">
                <li>
                    <c:url var="catUrl" value="/listings"><c:param name="category" value="${cat.slug}"/></c:url>
                    <a class="paper-tab" href="${fn:escapeXml(catUrl)}"><c:out value="${cat.name}"/></a>
                </li>
            </c:forEach>
        </ul>
    </section>
</c:if>

<c:if test="${not home}">
    <form class="filters" action="${ctx}/listings" method="get" role="search" aria-label="Search and filter">
        <div class="filters__search">
            <label class="visually-hidden" for="f-q">Keyword</label>
            <svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.6-3.6"/></svg>
            <input class="input" id="f-q" name="q" type="search" maxlength="100"
                   placeholder="Search titles and descriptions" value="${fn:escapeXml(filter.q)}">
        </div>
        <div class="filters__field">
            <label for="f-category">Category</label>
            <select class="input" id="f-category" name="category">
                <option value="">All</option>
                <c:forEach var="cat" items="${categories}">
                    <option value="${fn:escapeXml(cat.slug)}" ${filter.category == cat.slug ? 'selected' : ''}><c:out value="${cat.name}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="filters__field">
            <label for="f-condition">Condition</label>
            <select class="input" id="f-condition" name="condition">
                <option value="">Any</option>
                <option value="new" ${filter.condition == 'new' ? 'selected' : ''}>New</option>
                <option value="like_new" ${filter.condition == 'like_new' ? 'selected' : ''}>Like new</option>
                <option value="good" ${filter.condition == 'good' ? 'selected' : ''}>Good</option>
                <option value="fair" ${filter.condition == 'fair' ? 'selected' : ''}>Fair</option>
            </select>
        </div>
        <fieldset class="filters__field filters__price">
            <legend>Price (Rp)</legend>
            <div class="filters__range">
                <input class="input" name="min" type="number" inputmode="numeric" min="0" max="20000000" step="1"
                       placeholder="Min" aria-label="Minimum price in rupiah" value="${filter.minPrice}">
                <span aria-hidden="true">&ndash;</span>
                <input class="input" name="max" type="number" inputmode="numeric" min="0" max="20000000" step="1"
                       placeholder="Max" aria-label="Maximum price in rupiah" value="${filter.maxPrice}">
            </div>
        </fieldset>
        <div class="filters__field">
            <label for="f-sort">Sort by</label>
            <select class="input" id="f-sort" name="sort">
                <option value="newest" ${filter.sort == 'newest' ? 'selected' : ''}>Newest first</option>
                <option value="price_asc" ${filter.sort == 'price_asc' ? 'selected' : ''}>Price: low to high</option>
                <option value="price_desc" ${filter.sort == 'price_desc' ? 'selected' : ''}>Price: high to low</option>
            </select>
        </div>
        <div class="filters__actions">
            <c:if test="${filter.active or filter.sort != 'newest'}">
                <a class="btn btn--ghost" href="${ctx}/listings">Clear</a>
            </c:if>
            <button type="submit" class="btn btn--primary">Apply</button>
        </div>
    </form>
</c:if>

<section class="board" aria-labelledby="board-title">
    <div class="board__head">
        <c:choose>
            <c:when test="${home}"><h2 id="board-title" class="section-title">Fresh on the board</h2></c:when>
            <c:when test="${not empty filter.q}"><h1 id="board-title" class="section-title">Results for &ldquo;<c:out value="${filter.q}"/>&rdquo;</h1></c:when>
            <c:when test="${filter.active}"><h1 id="board-title" class="section-title">Filtered listings</h1></c:when>
            <c:otherwise><h1 id="board-title" class="section-title">Everything on the board</h1></c:otherwise>
        </c:choose>
        <p class="board__count">${total}${total == 1 ? ' listing' : ' listings'}</p>
    </div>

    <c:choose>
        <c:when test="${empty listings and filter.active}">
            <div class="empty-state">
                <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
                    <circle cx="54" cy="46" r="20"/>
                    <path d="m69 61 18 18M46 40l16 12M62 40 46 52"/>
                </svg>
                <h2>No flyers match that</h2>
                <p>Try fewer filters, a wider price range or a different word.</p>
                <a class="btn btn--primary" href="${ctx}/listings">Clear filters</a>
            </div>
        </c:when>
        <c:when test="${empty listings}">
            <div class="empty-state">
                <svg class="empty-state__doodle" viewBox="0 0 120 100" aria-hidden="true">
                    <path d="M28 24h64v58H28z" stroke-dasharray="6 6"/>
                    <path d="M46 52h28M60 38v28"/>
                    <circle class="pin" cx="60" cy="22" r="6"/>
                </svg>
                <h2>Nothing pinned up yet</h2>
                <p>Be the first: clear out your shelf and pass something on.</p>
                <a class="btn btn--primary" href="${ctx}/listing/new">Sell something</a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="grid">
                <c:forEach var="l" items="${listings}">
                    <%@ include file="layout/listing-card.jspf" %>
                </c:forEach>
            </div>

            <c:if test="${pages > 1}">
                <nav class="pager" aria-label="Pages">
                    <c:choose>
                        <c:when test="${page > 1}"><a class="btn btn--ghost" rel="prev" href="${ctx}/listings?${fn:escapeXml(filterQuery)}page=${page - 1}">&larr; Previous</a></c:when>
                        <c:otherwise><span class="btn btn--ghost" aria-disabled="true">&larr; Previous</span></c:otherwise>
                    </c:choose>
                    <span class="pager__info">Page ${page} of ${pages}</span>
                    <c:choose>
                        <c:when test="${page < pages}"><a class="btn btn--ghost" rel="next" href="${ctx}/listings?${fn:escapeXml(filterQuery)}page=${page + 1}">Next &rarr;</a></c:when>
                        <c:otherwise><span class="btn btn--ghost" aria-disabled="true">Next &rarr;</span></c:otherwise>
                    </c:choose>
                </nav>
            </c:if>
        </c:otherwise>
    </c:choose>
</section>

<c:if test="${home}">
    <section class="how" aria-labelledby="how-title">
        <h2 id="how-title" class="section-title">How it works</h2>
        <ol class="notes">
            <li class="note">
                <h3>Pin it up</h3>
                <p>Add up to four photos, a price in rupiah and a meetup spot on campus.</p>
            </li>
            <li class="note">
                <h3>Pick an offer</h3>
                <p>Buyers send an offer with a short message. Accept the one that suits you.</p>
            </li>
            <li class="note">
                <h3>Meet and hand over</h3>
                <p>Mark it sold after the meetup, and the buyer can leave you a review.</p>
            </li>
        </ol>
    </section>
</c:if>

<%@ include file="layout/footer.jspf" %>
