<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft - Login</title>

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">

    <!-- Global & Page Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/src/main/webapp/css/auth.css">
</head>
<body>

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="login-page-container">

    <!-- PANNELLO MINECRAFT GUI -->
    <div class="mc-gui-box">

        <h1 class="mc-gui-title">LOGIN</h1>

        <!-- MESSAGGIO DI ERRORE (Se restituito dalla Servlet o dalla Request) -->
        <%
            String error = (String) request.getAttribute("errorMessage");
            if (error == null) {
                error = request.getParameter("error");
            }
            if (error != null) {
        %>
        <div class="mc-error-msg">
            <%= error %>
        </div>
        <% } %>

        <!-- FORM AUTENTICAZIONE -->
        <!-- Nota: Assicurati che l'action punti al pattern della tua AuthServlet (es. /login o /auth) -->
        <form action="${pageContext.request.contextPath}/login" method="post" class="mc-form">

            <div class="form-group">
                <label for="username">USERNAME</label>
                <input type="text" id="username" name="username" class="mc-input" required autocomplete="username" placeholder="Minecraft User">
            </div>

            <div class="form-group">
                <label for="password">PASSWORD</label>
                <input type="password" id="password" name="password" class="mc-input" required autocomplete="current-password" placeholder="••••••••">
            </div>

            <!-- CHECKBOX REMEMBER ME -->
            <div class="form-group-checkbox">
                <label class="mc-checkbox-container">
                    <input type="checkbox" name="rememberMe" value="true">
                    <span class="mc-checkmark"></span>
                    RICORDAMI
                </label>
            </div>

            <button type="submit" class="btn-mc btn-login">ENTRA</button>

        </form>

        <div class="mc-gui-footer">
            <span>Non hai un account?</span>
            <a href="${pageContext.request.contextPath}/signup.jsp" class="mc-link">REGISTRATI</a>
        </div>

    </div>

</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
