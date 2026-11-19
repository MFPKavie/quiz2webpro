<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your profile"/>
<c:set var="nav" value="me"/>
<%@ include file="layout/header.jspf" %>

<section class="auth">
    <div class="form-card form-card--wide">
        <h1>Your profile</h1>
        <p class="form-card__lead">Logged in as <strong><c:out value="${profile.email}"/></strong>.</p>

        <ul class="quick-links">
            <li><a href="${ctx}/seller?id=${profile.id}">Your public seller page</a></li>
            <li><a href="${ctx}/dashboard">Dashboard</a></li>
            <c:if test="${profile.admin}"><li><a href="${ctx}/admin/reports">Admin</a></li></c:if>
        </ul>

        <c:if test="${not empty errors}">
            <p class="form-alert" role="alert">Please fix the highlighted fields below.</p>
        </c:if>

        <form method="post" action="${ctx}/profile" enctype="multipart/form-data">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">

            <fieldset class="field">
                <legend>Photo</legend>
                <div class="avatar-edit">
                    <c:choose>
                        <c:when test="${not empty profile.avatar}"><img class="avatar avatar--lg" src="${ctx}/photo/${fn:escapeXml(profile.avatar)}" alt="Your current photo" width="88" height="88"></c:when>
                        <c:otherwise><span class="avatar avatar--lg" aria-hidden="true"><c:out value="${profile.initial}"/></span></c:otherwise>
                    </c:choose>
                    <div class="avatar-edit__controls">
                        <label class="btn btn--ghost btn--sm avatar-edit__pick">
                            Choose a new photo
                            <input class="dropzone__input" type="file" name="avatar" accept="image/jpeg,image/png,image/webp"
                                   aria-describedby="avatar-hint avatar-error">
                        </label>
                        <c:if test="${not empty profile.avatar}">
                            <label class="photo-list__remove"><input type="checkbox" name="removeAvatar" value="1"> Remove my photo</label>
                        </c:if>
                        <p class="field__hint" id="avatar-hint">JPEG, PNG or WebP, 2 MB at most.</p>
                    </div>
                </div>
                <c:if test="${not empty errors.avatar}"><p class="field__error" id="avatar-error"><c:out value="${errors.avatar}"/></p></c:if>
            </fieldset>

            <div class="field">
                <label for="name">Full name</label>
                <input class="input" id="name" name="name" autocomplete="name" required minlength="2" maxlength="80"
                       value="${fn:escapeXml(form.name)}" aria-invalid="${not empty errors.name}" aria-describedby="name-error">
                <c:if test="${not empty errors.name}"><p class="field__error" id="name-error"><c:out value="${errors.name}"/></p></c:if>
            </div>

            <div class="field-row">
                <div class="field">
                    <label for="faculty">Faculty <span class="field__optional">(optional)</span></label>
                    <input class="input" id="faculty" name="faculty" maxlength="80" value="${fn:escapeXml(form.faculty)}"
                           aria-invalid="${not empty errors.faculty}" aria-describedby="faculty-error">
                    <c:if test="${not empty errors.faculty}"><p class="field__error" id="faculty-error"><c:out value="${errors.faculty}"/></p></c:if>
                </div>
                <div class="field">
                    <label for="lineId">LINE ID <span class="field__optional">(optional)</span></label>
                    <input class="input" id="lineId" name="lineId" maxlength="40" pattern="[A-Za-z0-9._\-]{0,40}"
                           value="${fn:escapeXml(form.lineId)}" aria-invalid="${not empty errors.lineId}"
                           aria-describedby="${not empty errors.lineId ? 'lineId-error' : 'lineId-hint'}">
                    <c:choose>
                        <c:when test="${not empty errors.lineId}"><p class="field__error" id="lineId-error"><c:out value="${errors.lineId}"/></p></c:when>
                        <c:otherwise><p class="field__hint" id="lineId-hint">Only shown to the other person once an offer is accepted.</p></c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn--primary">Save profile</button>
            </div>
        </form>

        <form class="review-delete" method="post" action="${ctx}/logout">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <button type="submit" class="btn btn--ghost btn--sm">Log out</button>
        </form>
    </div>
</section>

<%@ include file="layout/footer.jspf" %>
