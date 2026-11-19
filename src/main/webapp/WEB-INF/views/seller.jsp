<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${seller.name}"/>
<%@ include file="layout/header.jspf" %>

<section class="seller-head">
    <c:choose>
        <c:when test="${not empty seller.avatar}"><img class="avatar avatar--xl" src="${ctx}/photo/${fn:escapeXml(seller.avatar)}" alt="" width="112" height="112"></c:when>
        <c:otherwise><span class="avatar avatar--xl" aria-hidden="true"><c:out value="${seller.initial}"/></span></c:otherwise>
    </c:choose>
    <div>
        <h1 class="seller-head__name"><c:out value="${seller.name}"/></h1>
        <p class="seller-head__meta">
            <c:if test="${not empty seller.faculty}"><c:out value="${seller.faculty}"/> &middot; </c:if>
            on Pass It On since ${seller.sinceText}
        </p>
        <p class="seller-head__rating">
            <c:choose>
                <c:when test="${reviewCount > 0}">
                    <span class="stars" role="img" aria-label="Average ${avgRating} out of 5 stars"><c:forEach begin="1" end="5" var="s">${s <= avgStars ? '&#9733;' : '&#9734;'}</c:forEach></span>
                    <strong>${avgRating}</strong> &middot; ${reviewCount}${reviewCount == 1 ? ' review' : ' reviews'}
                </c:when>
                <c:otherwise>No reviews yet</c:otherwise>
            </c:choose>
        </p>
    </div>
</section>

<section class="board" aria-labelledby="seller-listings">
    <div class="board__head">
        <h2 id="seller-listings" class="section-title">On the board</h2>
        <p class="board__count">${fn:length(listings)}${fn:length(listings) == 1 ? ' listing' : ' listings'}</p>
    </div>
    <c:choose>
        <c:when test="${empty listings}">
            <p class="note-line">Nothing listed yet.</p>
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

<section class="board seller-reviews" aria-labelledby="seller-reviews">
    <h2 id="seller-reviews" class="section-title">What buyers say</h2>
    <c:choose>
        <c:when test="${empty reviews}">
            <p class="note-line">No reviews yet. Reviews appear here after a sale is finished.</p>
        </c:when>
        <c:otherwise>
            <div class="review-grid">
                <c:forEach var="r" items="${reviews}">
                    <figure class="review">
                        <p class="stars" role="img" aria-label="${r.rating} out of 5 stars">${r.stars}</p>
                        <c:if test="${not empty r.comment}">
                            <blockquote class="review__text"><c:out value="${r.comment}"/></blockquote>
                        </c:if>
                        <figcaption class="review__by">
                            <c:out value="${r.reviewerName}"/> bought
                            <a href="${ctx}/listing?id=${r.listingId}"><c:out value="${r.listingTitle}"/></a> &middot; ${r.createdText}
                        </figcaption>
                    </figure>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</section>

<%@ include file="layout/footer.jspf" %>
