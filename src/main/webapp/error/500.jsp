<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>500 - Server Error</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/fragments/header.jsp" />

<main>
  <h2>500 - Server Error</h2>
  <p>An unexpected problem occurred. Please try again later.</p>
  <a href="${pageContext.request.contextPath}/index.jsp">Back to home</a>
</main>

<jsp:include page="/WEB-INF/fragments/footer.jsp" />

</body>
</html>