<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft - Build Your World</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
</head>
<body>

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main>
    <section class="hero-container">

        <video class="hero-video hero-video-desktop"
               autoplay loop muted playsinline preload="auto"
               poster="${pageContext.request.contextPath}/images/hero-fallback.png"
               onerror="this.style.display='none';">
            <source src="${pageContext.request.contextPath}/media/hero-bg.mp4" type="video/mp4">
        </video>

        <video class="hero-video hero-video-mobile"
               autoplay loop muted playsinline preload="auto"
               poster="${pageContext.request.contextPath}/images/hero-fallback.png"
               onerror="this.style.display='none';">
            <source src="${pageContext.request.contextPath}/media/hero-bg-mobile.mp4" type="video/mp4">
        </video>

        <div class="hero-content-wrapper">
            <div class="hero-content-inner">
                <h1 class="hero-title">Build<br>your world</h1>
                <a href="${pageContext.request.contextPath}/catalog" class="btn-mc">EXPLORE</a>
            </div>
        </div>

    </section>

</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
