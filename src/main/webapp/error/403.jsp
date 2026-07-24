<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page isErrorPage="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>403 - Access Denied</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/fragments/header.jsp" />

<main>
  <h2>403 - Access Denied</h2>
  <p>You do not have permission to access this resource.</p>
  <a href="${pageContext.request.contextPath}/index.jsp">Back to home</a>
</main>

<jsp:include page="/WEB-INF/fragments/footer.jsp" />

</body>
</html>