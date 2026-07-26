<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="site-header">

    <!-- Logo / Brand (SVG already contains both icon and wordmark) -->
    <a href="${pageContext.request.contextPath}/" class="site-header__brand">
        <img src="${pageContext.request.contextPath}/icons/logo.svg" alt="SchemeCraft" class="site-header__logo">
    </a>

    <!-- Main navigation -->
    <nav class="site-header__nav">

        <a href="${pageContext.request.contextPath}/" class="site-header__link site-header__link--primary-nav">Home</a>
        <a href="${pageContext.request.contextPath}/products" class="site-header__link site-header__link--primary-nav">Catalog</a>

        <a href="${pageContext.request.contextPath}/cart" class="site-header__link site-header__link--cart">
            <span>Cart</span>
        </a>

        <c:choose>

            <%-- Logged-in user: show profile avatar linking to account hub --%>
            <c:when test="${not empty sessionScope.userSession && sessionScope.userSession.loggedIn}">
                <a href="${pageContext.request.contextPath}/account" class="site-header__avatar" title="My account">
                    <img src="${pageContext.request.contextPath}/${sessionScope.userSession.account.profileImagePath}"
                         alt="Profile picture">
                </a>
            </c:when>

            <%-- Guest: show login / signup actions --%>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/auth/login" class="site-header__link">Login</a>
                <a href="${pageContext.request.contextPath}/auth/register" class="site-header__link site-header__link--cta">Sign Up</a>
            </c:otherwise>

        </c:choose>


    </nav>

</header>
