<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>403 - Forbidden</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com/" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>
<body>

<jsp:include page="/WEB-INF/fragments/header.jsp" />

<main class="error-main">
  <div class="error-content">
    <h2>An error occurred (403):</h2>
    <p>Access Denied. You do not have permission to view this resource.</p>
  </div>

  <div class="error-actions">
    <a href="${pageContext.request.contextPath}/index.jsp">Back to home</a>
  </div>
</main>

<jsp:include page="/WEB-INF/fragments/footer.jsp" />

</body>
</html>