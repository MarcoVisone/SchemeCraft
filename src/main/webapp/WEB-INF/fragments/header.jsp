<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<header class="site-header">

    <!-- Logo / Brand -->
    <a href="${pageContext.request.contextPath}/" class="site-header__brand">
        <img src="${pageContext.request.contextPath}/icons/logo.svg" alt="SchemeCraft" class="site-header__logo">
    </a>

    <!-- Main navigation -->
    <nav class="site-header__nav">

        <a href="${pageContext.request.contextPath}/" class="site-header__link site-header__link--primary-nav">Home</a>
        <a href="${pageContext.request.contextPath}/product" class="site-header__link">Catalog</a>

        <c:choose>

            <%-- Logged-in user --%>
            <c:when test="${not empty sessionScope.userSession && sessionScope.userSession.loggedIn}">

                <a href="${pageContext.request.contextPath}/cart" class="site-header__link site-header__link--cart">
                    <span>Cart</span>
                </a>

                <%-- Admin link --%>
                <c:if test="${sessionScope.userSession.account.admin}">
                    <a href="${pageContext.request.contextPath}/admin" class="site-header__link site-header__link--admin">
                        Panel
                    </a>
                </c:if>

                <a href="${pageContext.request.contextPath}/account" class="site-header__avatar" title="My account">
                    <img src="${pageContext.request.contextPath}/${sessionScope.userSession.account.profileImagePath}"
                         alt="Profile picture">
                </a>
            </c:when>

            <%-- Guest --%>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/cart" class="site-header__link site-header__link--cart">
                    <span>Cart</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth/login" class="site-header__link">Login</a>
                <a href="${pageContext.request.contextPath}/auth/register" class="site-header__link site-header__link--cta">Sign Up</a>
            </c:otherwise>

        </c:choose>

    </nav>

</header>