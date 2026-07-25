<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft - Build Your World</title>

    <!-- Google Fonts (Optional) -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=Press+Start+2P&display=swap"
          rel="stylesheet">

    <!-- Global & Page Styles -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
</head>
<body>

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main>
    <section class="hero-section">
        <video class="hero-video"
               autoplay
               muted
               loop
               playsinline
               poster="${pageContext.request.contextPath}/images/hero-fallback.png"
               onerror="this.style.display='none';">
            <source src="${pageContext.request.contextPath}/media/hero-bg.mp4s" type="video/mp4">
            Your browser does not support HTML5 video.
        </video>

        <div class="hero-overlay"></div>

        <div class="hero-content">
            <h1 class="hero-title">Build<br>your world</h1>
            <a href="${pageContext.request.contextPath}/catalog" class="btn-mc">EXPLORE</a>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
