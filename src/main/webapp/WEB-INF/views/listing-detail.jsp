<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${listing.title}"/>
<%@ include file="layout/header.jspf" %>
<c:set var="me" value="${sessionScope.user}"/>

<p class="crumbs">
    <a href="${ctx}/listings">All listings</a>
    <span aria-hidden="true">/</span>
    <c:url var="catUrl" value="/listings"><c:param name="category" value="${listing.categorySlug}"/></c:url>
    <a href="${fn:escapeXml(catUrl)}"><c:out value="${listing.categoryName}"/></a>
</p>

<article class="detail detail--${listing.status}">
    <div class="gallery">
        <div class="gallery__main">
            <c:choose>
                <c:when test="${not empty photos}">
                    <img id="gallery-main" src="${ctx}/photo/${fn:escapeXml(photos[0].fileName)}"
                         alt="Photo 1 of ${fn:escapeXml(listing.title)}" width="800" height="600">
                </c:when>
                <c:otherwise>
                    <img class="card__placeholder" src="${ctx}/img/placeholder.svg" alt="No photo yet" width="800" height="600">
                </c:otherwise>
            </c:choose>
            <c:if test="${listing.status != 'available'}">
                <span class="ribbon ribbon--lg ribbon--${listing.status}">${listing.statusLabel}</span>
            </c:if>
        </div>
        <c:if test="${fn:length(photos) > 1}">
            <ul class="gallery__thumbs">
                <c:forEach var="p" items="${photos}" varStatus="st">
                    <li>
                        <button type="button" class="gallery__thumb" aria-pressed="${st.first}"
                                data-src="${ctx}/photo/${fn:escapeXml(p.fileName)}"
                                data-alt="Photo ${st.count} of ${fn:escapeXml(listing.title)}">
                            <img src="${ctx}/photo/${fn:escapeXml(p.fileName)}" alt="Show photo ${st.count}" width="96" height="72">
                        </button>
                    </li>
                </c:forEach>
            </ul>
        </c:if>
    </div>

    <div class="detail__info">
        <h1 class="detail__title"><c:out value="${listing.title}"/></h1>

        <div class="detail__pricing">
            <span class="sticker sticker--lg"><c:choose><c:when test="${listing.price == 0}">FREE</c:when><c:otherwise><small class="sticker__cur">Rp</small>${listing.priceNumber}</c:otherwise></c:choose></span>
            <span class="status status--${listing.status}">${listing.statusLabel}</span>
            <span class="save">
                <c:choose>
                    <c:when test="${empty me}">
                        <button type="button" class="heart heart--inline" data-login aria-label="Log in to save this listing">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 20.5s-7.5-4.6-9.3-9.4A4.9 4.9 0 0 1 12 6.6a4.9 4.9 0 0 1 9.3 4.5c-1.8 4.8-9.3 9.4-9.3 9.4z"/></svg>
                        </button>
                    </c:when>
                    <c:when test="${me.id != listing.sellerId}">
                        <button type="button" class="heart heart--inline" data-listing="${listing.id}"
                                aria-pressed="${saved}" aria-label="Save to wishlist">
                            <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 20.5s-7.5-4.6-9.3-9.4A4.9 4.9 0 0 1 12 6.6a4.9 4.9 0 0 1 9.3 4.5c-1.8 4.8-9.3 9.4-9.3 9.4z"/></svg>
                        </button>
                    </c:when>
                </c:choose>
                <span><span data-save-count="${listing.id}">${saveCount}</span> saved</span>
            </span>
        </div>

        <dl class="facts">
            <div><dt>Condition</dt><dd><span class="chip chip--${listing.itemCondition}">${listing.conditionLabel}</span></dd></div>
            <div><dt>Category</dt><dd><c:out value="${listing.categoryName}"/></dd></div>
            <c:if test="${not empty listing.meetupSpot}">
                <div><dt>Meet at</dt><dd><c:out value="${listing.meetupSpot}"/></dd></div>
            </c:if>
            <div><dt>Posted</dt><dd>${listing.postedText}</dd></div>
        </dl>

        <div class="detail__desc"><c:out value="${listing.description}"/></div>

        <section class="seller-card" aria-label="Seller">
            <c:choose>
                <c:when test="${not empty listing.sellerAvatar}"><img class="avatar" src="${ctx}/photo/${fn:escapeXml(listing.sellerAvatar)}" alt="" width="48" height="48"></c:when>
                <c:otherwise><span class="avatar" aria-hidden="true"><c:out value="${listing.sellerInitial}"/></span></c:otherwise>
            </c:choose>
            <div>
                <p class="seller-card__name"><a href="${ctx}/seller?id=${listing.sellerId}"><c:out value="${listing.sellerName}"/></a></p>
                <p class="seller-card__meta">
                    <c:if test="${not empty listing.sellerFaculty}"><c:out value="${listing.sellerFaculty}"/> &middot; </c:if>
                    on Pass It On since ${listing.sellerSinceText}
                </p>
            </div>
        </section>

        <div class="detail__actions">
            <c:choose>
                <c:when test="${not empty me and me.id == listing.sellerId}">
                    <section class="owner-panel" aria-labelledby="owner-title">
                        <h2 id="owner-title" class="owner-panel__title">This is your listing</h2>
                        <c:choose>
                            <c:when test="${listing.status == 'sold'}">
                                <p class="owner-panel__text">It's sold, so it can no longer be edited or deleted.</p>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${pendingOffers > 0}">
                                    <p class="owner-panel__text">
                                        <a href="${ctx}/offers?tab=received">${pendingOffers}${pendingOffers == 1 ? ' offer is' : ' offers are'} waiting for your answer</a>
                                    </p>
                                </c:if>
                                <div class="owner-panel__buttons">
                                    <c:if test="${listing.status == 'available'}">
                                        <form method="post" action="${ctx}/listing/status">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="id" value="${listing.id}">
                                            <input type="hidden" name="to" value="reserved">
                                            <button type="submit" class="btn btn--ghost">Mark as reserved</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${listing.status == 'reserved'}">
                                        <form method="post" action="${ctx}/listing/status">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="id" value="${listing.id}">
                                            <input type="hidden" name="to" value="sold">
                                            <button type="submit" class="btn btn--primary">Mark as sold</button>
                                        </form>
                                        <form method="post" action="${ctx}/listing/status"
                                              data-confirm="Put it back on the board? The accepted offer will be declined.">
                                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                            <input type="hidden" name="id" value="${listing.id}">
                                            <input type="hidden" name="to" value="available">
                                            <button type="submit" class="btn btn--ghost">Back to available</button>
                                        </form>
                                    </c:if>
                                    <a class="btn btn--ghost" href="${ctx}/listing/edit?id=${listing.id}">Edit</a>
                                    <form method="post" action="${ctx}/listing/delete"
                                          data-confirm="Take this listing off the board for good? This can't be undone.">
                                        <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                        <input type="hidden" name="id" value="${listing.id}">
                                        <button type="submit" class="btn btn--danger">Delete</button>
                                    </form>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:when>
                <c:when test="${myOffer.status == 'accepted'}">
                    <section class="offer-box offer-box--accepted">
                        <c:choose>
                            <c:when test="${listing.status == 'sold'}">
                                <h2 class="offer-box__title">You bought this</h2>
                                <p>You paid <strong>${myOffer.amountText}</strong> to <c:out value="${listing.sellerName}"/>.</p>
                                <c:choose>
                                    <c:when test="${canReview and empty myReview}">
                                        <a class="btn btn--primary btn--block" href="${ctx}/review/new?listingId=${listing.id}">Review the seller</a>
                                    </c:when>
                                    <c:when test="${not empty myReview}">
                                        <a class="btn btn--ghost btn--block" href="${ctx}/review/edit?id=${myReview.id}">Edit your review</a>
                                    </c:when>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <h2 class="offer-box__title">Your offer was accepted</h2>
                                <p>You offered <strong>${myOffer.amountText}</strong> and <c:out value="${listing.sellerName}"/> said yes.
                                    <c:if test="${not empty listing.meetupSpot}">Meet at <c:out value="${listing.meetupSpot}"/>.</c:if></p>
                                <c:if test="${not empty myOffer.sellerLineId}">
                                    <p class="contact-line">LINE ID: <strong><c:out value="${myOffer.sellerLineId}"/></strong></p>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </c:when>
                <c:when test="${listing.status == 'sold'}">
                    <p class="note-line">This item has been sold.</p>
                </c:when>
                <c:when test="${listing.status == 'reserved'}">
                    <p class="note-line">Reserved: the seller has accepted an offer.</p>
                </c:when>
                <c:when test="${empty me}">
                    <c:url var="loginUrl" value="/login"><c:param name="next" value="/listing?id=${listing.id}"/></c:url>
                    <a class="btn btn--primary btn--block" href="${fn:escapeXml(loginUrl)}">Log in to make an offer</a>
                </c:when>
                <c:otherwise>
                    <c:set var="pending" value="${myOffer.status == 'pending'}"/>
                    <c:choose>
                        <c:when test="${not empty offerForm}"><c:set var="amountValue" value="${offerForm.amount}"/><c:set var="messageValue" value="${offerForm.message}"/></c:when>
                        <c:when test="${pending}"><c:set var="amountValue" value="${myOffer.amount}"/><c:set var="messageValue" value="${myOffer.message}"/></c:when>
                        <c:otherwise><c:set var="amountValue" value="${listing.price > 0 ? listing.price : 1}"/><c:set var="messageValue" value=""/></c:otherwise>
                    </c:choose>
                    <section class="offer-box" id="offer" aria-labelledby="offer-title">
                        <h2 id="offer-title" class="offer-box__title">${pending ? 'Your offer' : 'Make an offer'}</h2>
                        <c:choose>
                            <c:when test="${pending}">
                                <p class="offer-box__status"><span class="chip chip--offer-pending">Pending</span>
                                    You offered <strong>${myOffer.amountText}</strong>. The seller hasn't answered yet.</p>
                            </c:when>
                            <c:when test="${myOffer.status == 'declined'}">
                                <p class="offer-box__status">Your last offer of ${myOffer.amountText} was declined. You can try a new one.</p>
                            </c:when>
                            <c:when test="${myOffer.status == 'withdrawn'}">
                                <p class="offer-box__status">You withdrew your last offer. You can make a new one.</p>
                            </c:when>
                        </c:choose>

                        <form method="post" action="${ctx}/offer/${pending ? 'edit' : 'create'}">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="listingId" value="${listing.id}">
                            <c:if test="${pending}"><input type="hidden" name="offerId" value="${myOffer.id}"></c:if>

                            <div class="field">
                                <label for="amount">Your offer</label>
                                <div class="input-prefix">
                                    <span aria-hidden="true">Rp</span>
                                    <input class="input" id="amount" name="amount" type="number" inputmode="numeric"
                                           required min="1" max="20000000" step="1" value="${fn:escapeXml(amountValue)}"
                                           aria-invalid="${not empty offerErrors.amount}" aria-describedby="amount-error">
                                </div>
                                <c:if test="${not empty offerErrors.amount}"><p class="field__error" id="amount-error"><c:out value="${offerErrors.amount}"/></p></c:if>
                            </div>
                            <div class="field">
                                <label for="message">Message <span class="field__optional">(optional)</span></label>
                                <textarea class="input offer-box__message" id="message" name="message" rows="3" maxlength="300"
                                          aria-invalid="${not empty offerErrors.message}" aria-describedby="message-error"><c:out value="${messageValue}"/></textarea>
                                <c:if test="${not empty offerErrors.message}"><p class="field__error" id="message-error"><c:out value="${offerErrors.message}"/></p></c:if>
                            </div>
                            <button type="submit" class="btn btn--primary btn--block">${pending ? 'Update offer' : 'Send offer'}</button>
                        </form>

                        <c:if test="${pending}">
                            <form class="offer-box__withdraw" method="post" action="${ctx}/offer/withdraw" data-confirm="Withdraw your offer?">
                                <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                                <input type="hidden" name="offerId" value="${myOffer.id}">
                                <button type="submit" class="btn btn--danger btn--block">Withdraw offer</button>
                            </form>
                        </c:if>
                    </section>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${not empty me and me.id != listing.sellerId}">
            <details class="report">
                <summary>Report this listing</summary>
                <form method="post" action="${ctx}/report">
                    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                    <input type="hidden" name="listingId" value="${listing.id}">
                    <div class="field">
                        <label for="reason">What is wrong with it?</label>
                        <textarea class="input" id="reason" name="reason" rows="3" required minlength="5" maxlength="300"></textarea>
                    </div>
                    <button type="submit" class="btn btn--ghost btn--sm">Send report</button>
                </form>
            </details>
        </c:if>

        <c:if test="${not empty reviews}">
            <section class="reviews" aria-labelledby="reviews-title">
                <h2 id="reviews-title" class="reviews__title">Buyer's review</h2>
                <c:forEach var="r" items="${reviews}">
                    <figure class="review">
                        <p class="stars" role="img" aria-label="${r.rating} out of 5 stars">${r.stars}</p>
                        <c:if test="${not empty r.comment}">
                            <blockquote class="review__text"><c:out value="${r.comment}"/></blockquote>
                        </c:if>
                        <figcaption class="review__by"><c:out value="${r.reviewerName}"/> &middot; ${r.createdText}</figcaption>
                    </figure>
                </c:forEach>
            </section>
        </c:if>
    </div>
</article>

<%@ include file="layout/footer.jspf" %>
