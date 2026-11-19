<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="editing" value="${not empty listing}"/>
<c:set var="pageTitle" value="${editing ? 'Edit listing' : 'Sell something'}"/>
<c:set var="nav" value="sell"/>
<%@ include file="layout/header.jspf" %>
<c:set var="formAction" value="${editing ? ctx += '/listing/edit?id=' += listing.id : ctx += '/listing/new'}"/>

<section class="auth">
    <div class="form-card form-card--wide">
        <h1>${editing ? 'Edit your listing' : 'Pin something up'}</h1>
        <p class="form-card__lead">${editing ? 'Change anything except a sold listing.' : 'A clear photo and an honest description sell things fastest.'}</p>

        <c:if test="${not empty errors}">
            <p class="form-alert" role="alert">Please fix the highlighted fields below.</p>
        </c:if>

        <form method="post" action="${formAction}" enctype="multipart/form-data">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">

            <fieldset class="field">
                <legend>Photos</legend>
                <p class="field__hint" id="photos-hint">Up to ${maxPhotos} photos, JPEG, PNG or WebP, 2 MB each. The first one is the cover.</p>

                <c:if test="${not empty photos}">
                    <ul class="photo-list">
                        <c:forEach var="p" items="${photos}" varStatus="st">
                            <li>
                                <img src="${ctx}/photo/${fn:escapeXml(p.fileName)}" alt="Current photo ${st.count}" width="160" height="120">
                                <label class="photo-list__remove">
                                    <input type="checkbox" name="removePhoto" value="${p.id}"
                                           <c:if test="${not empty removed and removed.contains(p.id)}">checked</c:if>>
                                    Remove
                                </label>
                            </li>
                        </c:forEach>
                    </ul>
                </c:if>

                <label class="dropzone">
                    <input class="dropzone__input" id="photos" name="photos" type="file" multiple
                           accept="image/jpeg,image/png,image/webp" aria-describedby="photos-hint photos-error">
                    <svg class="icon dropzone__icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M4 16.5V19a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-2.5M12 15V4M7.5 8.5 12 4l4.5 4.5"/></svg>
                    <span class="dropzone__text">Drop photos here or <span class="dropzone__pick">choose files</span></span>
                </label>
                <ul class="photo-previews" aria-live="polite"></ul>
                <p class="field__error" id="photos-error" data-photo-error ${empty errors.photos ? 'hidden' : ''}><c:out value="${errors.photos}"/></p>
                <c:if test="${reselectPhotos}">
                    <p class="field__hint">For your safety, browsers forget chosen files when a form comes back. Please choose your new photos again.</p>
                </c:if>
            </fieldset>

            <div class="field">
                <label for="title">Title</label>
                <input class="input" id="title" name="title" required minlength="5" maxlength="100"
                       value="${fn:escapeXml(form.title)}"
                       aria-invalid="${not empty errors.title}" aria-describedby="title-error">
                <c:if test="${not empty errors.title}"><p class="field__error" id="title-error"><c:out value="${errors.title}"/></p></c:if>
            </div>

            <div class="field-row">
                <div class="field">
                    <label for="price">Price</label>
                    <div class="input-prefix">
                        <span aria-hidden="true">Rp</span>
                        <input class="input" id="price" name="price" type="number" inputmode="numeric" required
                               min="0" max="20000000" step="1" value="${fn:escapeXml(form.price)}"
                               aria-invalid="${not empty errors.price}" aria-describedby="price-hint price-error">
                    </div>
                    <c:choose>
                        <c:when test="${not empty errors.price}"><p class="field__error" id="price-error"><c:out value="${errors.price}"/></p></c:when>
                        <c:otherwise><p class="field__hint" id="price-hint">Whole rupiah. 0 means free.</p></c:otherwise>
                    </c:choose>
                </div>

                <div class="field">
                    <label for="categoryId">Category</label>
                    <select class="input" id="categoryId" name="categoryId" required
                            aria-invalid="${not empty errors.categoryId}" aria-describedby="categoryId-error">
                        <option value="">Choose one</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}" ${form.categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.name}"/></option>
                        </c:forEach>
                    </select>
                    <c:if test="${not empty errors.categoryId}"><p class="field__error" id="categoryId-error"><c:out value="${errors.categoryId}"/></p></c:if>
                </div>
            </div>

            <fieldset class="field">
                <legend>Condition</legend>
                <div class="choice-chips">
                    <label class="choice"><input type="radio" name="itemCondition" value="new" ${form.itemCondition == 'new' ? 'checked' : ''}><span>New</span></label>
                    <label class="choice"><input type="radio" name="itemCondition" value="like_new" ${form.itemCondition == 'like_new' ? 'checked' : ''}><span>Like new</span></label>
                    <label class="choice"><input type="radio" name="itemCondition" value="good" ${form.itemCondition == 'good' ? 'checked' : ''}><span>Good</span></label>
                    <label class="choice"><input type="radio" name="itemCondition" value="fair" ${form.itemCondition == 'fair' ? 'checked' : ''}><span>Fair</span></label>
                </div>
                <c:if test="${not empty errors.itemCondition}"><p class="field__error"><c:out value="${errors.itemCondition}"/></p></c:if>
            </fieldset>

            <div class="field">
                <label for="description">Description</label>
                <textarea class="input" id="description" name="description" rows="6" required minlength="10" maxlength="2000"
                          placeholder="What is it, how old, anything wrong with it?"
                          aria-invalid="${not empty errors.description}" aria-describedby="description-error"><c:out value="${form.description}"/></textarea>
                <c:if test="${not empty errors.description}"><p class="field__error" id="description-error"><c:out value="${errors.description}"/></p></c:if>
            </div>

            <div class="field">
                <label for="meetupSpot">Meetup spot <span class="field__optional">(optional)</span></label>
                <input class="input" id="meetupSpot" name="meetupSpot" maxlength="100"
                       value="${fn:escapeXml(form.meetupSpot)}" aria-invalid="${not empty errors.meetupSpot}" aria-describedby="meetupSpot-error">
                <c:if test="${not empty errors.meetupSpot}"><p class="field__error" id="meetupSpot-error"><c:out value="${errors.meetupSpot}"/></p></c:if>
            </div>

            <div class="form-actions">
                <a class="btn btn--ghost" href="${editing ? ctx += '/listing?id=' += listing.id : ctx += '/'}">Cancel</a>
                <button type="submit" class="btn btn--primary">${editing ? 'Save changes' : 'Pin it to the board'}</button>
            </div>
        </form>
    </div>
</section>

<%@ include file="layout/footer.jspf" %>
