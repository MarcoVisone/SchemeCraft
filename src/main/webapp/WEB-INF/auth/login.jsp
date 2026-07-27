<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft - Log In</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">

    <script src="${pageContext.request.contextPath}/js/header.js" defer></script>
</head>
<body class="auth-body">

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="auth-container">
    <div class="auth-card theme-step-1">

        <div class="auth-header">
            <h1 class="auth-title">Welcome Back</h1>
            <p class="auth-subtitle">Log in to access your Minecraft schematics</p>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="auth-error-alert" style="border-color: rgba(34, 197, 94, 0.5); color: #4ade80; background: rgba(34, 197, 94, 0.15);">
                <span>${successMessage}</span>
            </div>
        </c:if>

        <c:if test="${not empty infoMessage}">
            <div class="auth-error-alert" style="border-color: rgba(59, 130, 246, 0.5); color: #60a5fa; background: rgba(59, 130, 246, 0.15);">
                <span>${infoMessage}</span>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="auth-error-alert">
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/login"
              method="POST"
              class="auth-form">

            <div class="form-step">

                <div class="form-group">
                    <label for="usernameOrEmail">Username or Email <span class="required">*</span></label>
                    <input type="text" id="usernameOrEmail" name="usernameOrEmail" class="auth-input"
                           value="${param.usernameOrEmail}" placeholder="e.g. SteveCraft or steve@schemecraft.com"
                           required autocomplete="username">
                </div>

                <div class="form-group">
                    <label for="password">Password <span class="required">*</span></label>
                    <input type="password" id="password" name="password" class="auth-input"
                           placeholder="Enter your password" required autocomplete="current-password">
                </div>

                <div class="form-group">
                    <label class="checkbox-container" style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
                        <input type="checkbox" id="rememberMe" name="rememberMe" value="true" style="width: 18px; height: 18px; accent-color: #22c55e;">
                        <span style="font-size: 0.9rem; color: #ccc;">Remember Me</span>
                    </label>
                </div>

                <button type="submit" class="btn-primary btn-block">
                    Log In &rarr;
                </button>

            </div>

        </form>

        <div class="auth-footer">
            <p>Don't have an account? <a href="${pageContext.request.contextPath}/auth/register">Register here</a></p>
        </div>

    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
