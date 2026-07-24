<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>404 - Page Not Found</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/fragments/header.jsp" />

<main>
    <h2>404 - Page Not Found</h2>
    <p>The requested resource does not exist or has been moved.</p>
    <a href="${pageContext.request.contextPath}/index.jsp">Back to home</a>
</main>

<jsp:include page="/WEB-INF/fragments/footer.jsp" />

</body>
</html>