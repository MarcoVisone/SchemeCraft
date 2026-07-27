<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - Page Not Found</title>
    <!-- Keep existing fonts: Inter for body, Press Start 2P for Game UI -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">

    <script src="${pageContext.request.contextPath}/js/header.js"></script>
</head>
<body>

<jsp:include page="/WEB-INF/fragments/header.jsp" />

<main class="error-main">
    <!-- 1. Top Section: Error text on dirt -->
    <div class="error-content">
        <!-- White title text -->
        <h2>An error occurred (404):</h2>
        <!-- Red details text -->
        <p>Could not find page: The requested resource does not exist or has been moved.</p>
    </div>

    <!-- 2. Bottom Section: The action button, centered -->
    <div class="error-actions">
        <!-- Stylized Minecraft button -->
        <a href="${pageContext.request.contextPath}/index.jsp">Back to home</a>
    </div>
</main>

<jsp:include page="/WEB-INF/fragments/footer.jsp" />

</body>
</html>