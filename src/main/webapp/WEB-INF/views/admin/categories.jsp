<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Categories"/>
<c:set var="nav" value="admin"/>
<%@ include file="../layout/header.jspf" %>
<%@ include file="../layout/admin-tabs.jspf" %>

<form class="panel add-category" method="post" action="${ctx}/admin/categories">
    <input type="hidden" name="csrf" value="${sessionScope.csrf}">
    <input type="hidden" name="action" value="add">
    <label for="new-name">New category</label>
    <div class="add-category__row">
        <input class="input" id="new-name" name="name" required minlength="2" maxlength="50">
        <button type="submit" class="btn btn--primary">Add</button>
    </div>
</form>

<div class="panel">
    <table class="data-table category-table">
        <thead>
            <tr><th scope="col">Name</th><th scope="col">Slug</th><th scope="col" class="num">Listings</th><th scope="col"><span class="visually-hidden">Delete</span></th></tr>
        </thead>
        <tbody>
            <c:forEach var="cat" items="${categories}">
                <tr>
                    <td>
                        <form class="rename-form" method="post" action="${ctx}/admin/categories">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="action" value="rename">
                            <input type="hidden" name="id" value="${cat.id}">
                            <label class="visually-hidden" for="name-${cat.id}">Name of <c:out value="${cat.name}"/></label>
                            <input class="input" id="name-${cat.id}" name="name" required minlength="2" maxlength="50" value="${fn:escapeXml(cat.name)}">
                            <button type="submit" class="btn btn--ghost btn--sm">Rename</button>
                        </form>
                    </td>
                    <td class="data-table__sub"><c:out value="${cat.slug}"/></td>
                    <td class="num">${cat.listingCount}</td>
                    <td class="num">
                        <form method="post" action="${ctx}/admin/categories" data-confirm="Delete the category &ldquo;${fn:escapeXml(cat.name)}&rdquo;?">
                            <input type="hidden" name="csrf" value="${sessionScope.csrf}">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${cat.id}">
                            <button type="submit" class="btn btn--danger btn--sm">Delete</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<%@ include file="../layout/footer.jspf" %>
