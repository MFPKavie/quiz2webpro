<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Log in"/>
<c:set var="nav" value="me"/>
<%@ include file="layout/header.jspf" %>

<section class="auth">
    <div class="form-card">
        <h1>Welcome back</h1>
        <p class="form-card__lead">Log in to sell, make offers and save things you like.</p>

        <c:if test="${not empty error}">
            <p class="form-alert" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <form method="post" action="${ctx}/login">
            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
            <input type="hidden" name="next" value="${fn:escapeXml(next)}">

            <div class="field">
                <label for="email">Email</label>
                <input class="input" id="email" name="email" type="email" autocomplete="username"
                       required maxlength="120" value="${fn:escapeXml(email)}">
            </div>

            <div class="field">
                <label for="password">Password</label>
                <input class="input" id="password" name="password" type="password"
                       autocomplete="current-password" required maxlength="128">
            </div>

            <button type="submit" class="btn btn--primary btn--block">Log in</button>
        </form>

        <p class="form-card__switch">New here? <a href="${ctx}/register">Create an account</a></p>
    </div>
</section>

<%@ include file="layout/footer.jspf" %>
