<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="admin-header">
  <div class="admin-header__container">

    <!-- Brand / Logo -->
    <a href="${pageContext.request.contextPath}/admin" class="admin-header__brand">
      <img src="${pageContext.request.contextPath}/icons/command_block.gif" alt="SchemeCraft Admin" class="admin-header__logo">
      <img src="${pageContext.request.contextPath}/icons/logo_text.svg" alt="SchemeCraft Admin" class="admin-header__logo">
      <span class="admin-header__badge-admin">ADMIN</span>
    </a>

    <!-- Hamburger toggle button (mobile only) -->
    <button type="button" class="admin-header__toggle" id="adminHeaderToggle" aria-expanded="false" aria-controls="adminHeaderMenu" aria-label="Toggle admin menu">
      <span class="admin-header__toggle-bar"></span>
      <span class="admin-header__toggle-bar"></span>
      <span class="admin-header__toggle-bar"></span>
    </button>

    <!-- Collapsible menu: nav + controls -->
    <div class="admin-header__menu" id="adminHeaderMenu">

      <!-- Main Navigation -->
      <nav class="admin-header__nav">
        <a href="${pageContext.request.contextPath}/admin/products"
           class="admin-header__link ${pageContext.request.requestURI.endsWith('/products') || pageContext.request.requestURI.endsWith('/admin') || pageContext.request.requestURI.endsWith('/admin/') ? 'active' : ''}">
          Products
        </a>
        <a href="${pageContext.request.contextPath}/admin/orders"
           class="admin-header__link ${pageContext.request.requestURI.endsWith('/orders') ? 'active' : ''}">
          Orders
        </a>
        <a href="${pageContext.request.contextPath}/admin/users"
           class="admin-header__link ${pageContext.request.requestURI.endsWith('/users') ? 'active' : ''}">
          Users
        </a>
      </nav>

      <!-- Right Side Controls -->
      <div class="admin-header__controls">
        <a href="${pageContext.request.contextPath}/" class="admin-header__link admin-header__link--store" title="Go to Storefront">
          &larr; Back to Store
        </a>

        <c:if test="${not empty sessionScope.userSession && sessionScope.userSession.loggedIn}">
          <a href="${pageContext.request.contextPath}/account" class="admin-header__avatar" title="Admin Account">
            <img src="${pageContext.request.contextPath}/${sessionScope.userSession.account.profileImagePath}"
                 alt="${sessionScope.userSession.account.username}">
          </a>
        </c:if>
      </div>

    </div>

  </div>
</header>

<script src="${pageContext.request.contextPath}/js/admin-header.js" defer></script>