<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="editing" value="${not empty review}"/>
<c:set var="pageTitle" value="${editing ? 'Edit review' : 'Review the seller'}"/>
<%@ include file="layout/header.jspf" %>
<c:set var="formAction" value="${editing ? ctx += '/review/edit?id=' += review.id : ctx += '/review/new?listingId=' += listing.id}"/>

<section class="auth">
    <div class="form-card">
        <h1>${editing ? 'Edit your review' : 'How did it go?'}</h1>
        <p class="form-card__lead">
            You bought <a href="${ctx}/listing?id=${listing.id}"><c:out value="${listing.title}"/></a>
            from <c:out value="${listing.sellerName}"/>.
        </p>

        <form method="post" action="${formAction}">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">

            <fieldset class="field">
                <legend>Rating</legend>
                <div class="stars-input">
                    <c:forEach var="n" begin="1" end="5">
                        <c:set var="star" value="${6 - n}"/>
                        <input type="radio" id="star${star}" name="rating" value="${star}" ${form.rating == star ? 'checked' : ''}>
                        <label for="star${star}" title="${star} ${star == 1 ? 'star' : 'stars'}">
                            <span aria-hidden="true">&#9733;</span><span class="visually-hidden">${star} ${star == 1 ? 'star' : 'stars'}</span>
                        </label>
                    </c:forEach>
                </div>
                <c:if test="${not empty errors.rating}"><p class="field__error"><c:out value="${errors.rating}"/></p></c:if>
            </fieldset>

            <div class="field">
                <label for="comment">Comment <span class="field__optional">(optional)</span></label>
                <textarea class="input" id="comment" name="comment" rows="4" maxlength="500"
                          placeholder="Was it as described? Was the meetup easy?"
                          aria-invalid="${not empty errors.comment}" aria-describedby="comment-error"><c:out value="${form.comment}"/></textarea>
                <c:if test="${not empty errors.comment}"><p class="field__error" id="comment-error"><c:out value="${errors.comment}"/></p></c:if>
            </div>

            <div class="form-actions">
                <a class="btn btn--ghost" href="${ctx}/listing?id=${listing.id}">Cancel</a>
                <button type="submit" class="btn btn--primary">${editing ? 'Save review' : 'Post review'}</button>
            </div>
        </form>

        <c:if test="${editing}">
            <form class="review-delete" method="post" action="${ctx}/review/delete" data-confirm="Delete your review?">
                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                <input type="hidden" name="id" value="${review.id}">
                <button type="submit" class="btn btn--danger btn--sm">Delete review</button>
            </form>
        </c:if>
    </div>
</section>

<%@ include file="layout/footer.jspf" %>
