<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign up"/>
<c:set var="nav" value="me"/>
<%@ include file="layout/header.jspf" %>

<section class="auth">
    <div class="form-card">
        <h1>Join the board</h1>
        <p class="form-card__lead">One account to sell your old things and pick up someone else's.</p>

        <form method="post" action="${ctx}/register">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">

            <div class="field">
                <label for="name">Full name</label>
                <input class="input" id="name" name="name" autocomplete="name" required minlength="2" maxlength="80"
                       value="${fn:escapeXml(name)}" aria-invalid="${not empty errors.name}"
                       <c:if test="${not empty errors.name}">aria-describedby="name-error"</c:if>>
                <c:if test="${not empty errors.name}">
                    <p class="field__error" id="name-error"><c:out value="${errors.name}"/></p>
                </c:if>
            </div>

            <div class="field">
                <label for="email">Email</label>
                <input class="input" id="email" name="email" type="email" autocomplete="email" required maxlength="120"
                       value="${fn:escapeXml(email)}" aria-invalid="${not empty errors.email}"
                       <c:if test="${not empty errors.email}">aria-describedby="email-error"</c:if>>
                <c:if test="${not empty errors.email}">
                    <p class="field__error" id="email-error"><c:out value="${errors.email}"/></p>
                </c:if>
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input class="input" id="password" name="password" type="password" autocomplete="new-password"
                       required minlength="8" maxlength="128" aria-invalid="${not empty errors.password}"
                       aria-describedby="${not empty errors.password ? 'password-error' : 'password-hint'}">
                <c:choose>
                    <c:when test="${not empty errors.password}">
                        <p class="field__error" id="password-error"><c:out value="${errors.password}"/></p>
                    </c:when>
                    <c:otherwise>
                        <p class="field__hint" id="password-hint">At least 8 characters.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="field">
                <label for="faculty">Faculty <span class="field__optional">(optional)</span></label>
                <input class="input" id="faculty" name="faculty" maxlength="80"
                       value="${fn:escapeXml(faculty)}" aria-invalid="${not empty errors.faculty}"
                       <c:if test="${not empty errors.faculty}">aria-describedby="faculty-error"</c:if>>
                <c:if test="${not empty errors.faculty}">
                    <p class="field__error" id="faculty-error"><c:out value="${errors.faculty}"/></p>
                </c:if>
            </div>

            <button type="submit" class="btn btn--primary btn--block">Create account</button>
        </form>

        <p class="form-card__switch">Already have an account? <a href="${ctx}/login">Log in</a></p>
    </div>
</section>

<%@ include file="layout/footer.jspf" %>
