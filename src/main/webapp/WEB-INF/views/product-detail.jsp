<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${product.productName} - SchemeCraft</title>

    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <!-- Stylesheets -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/product-detail.css">

    <!-- Script -->
    <script src="${pageContext.request.contextPath}/js/header.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/product-detail.js" defer></script>
</head>
<body>

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="product-detail-container"
      data-context-path="${pageContext.request.contextPath}"
      data-product-id="${product.productId}">

    <header class="product-header">
        <div class="header-left">
            <div class="product-icon-wrapper">
                <c:choose>
                    <c:when test="${not empty images}">
                        <img src="${pageContext.request.contextPath}/${images[0].imagePath}" alt="${product.productName}">
                    </c:when>
                    <c:otherwise>
                        <div class="icon-placeholder">🎮</div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="header-meta">
                <h1 class="product-name" title="${product.productName}">${product.productName}</h1>

                <div class="product-stats-row">
                    <span class="stat-item">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                        ${product.totalDownloads} downloads
                    </span>
                    <span class="dot-separator">•</span>
                    <span class="stat-item">★ <fmt:formatNumber value="${product.averageRating}" pattern="0.00"/></span>

                    <div class="tags-container">
                        <c:forEach items="${categories}" var="cat">
                            <span class="tag-badge" title="${cat.categoryName}">${cat.categoryName}</span>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <div class="header-actions">
            <c:choose>
                <c:when test="${isPurchased || product.price == 0}">
                    <c:choose>
                        <c:when test="${not empty versions}">
                            <a href="${pageContext.request.contextPath}/product/download?productId=${product.productId}&versionId=${versions[0].versionId}" class="btn-action btn-primary-action" id="btnDownload">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                                Download
                            </a>
                        </c:when>
                        <c:otherwise>
                            <button type="button" class="btn-action btn-primary-action" disabled>
                                No versions available
                            </button>
                        </c:otherwise>
                    </c:choose>
                </c:when>

                <c:otherwise>
                    <button type="button" class="btn-action btn-primary-action" id="btnBuyNow">
                        Buy - $<fmt:formatNumber value="${product.price}" pattern="0.00"/>
                    </button>
                    <button type="button" class="btn-action btn-cart-action" id="btnAddToCart" title="Add to Cart">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
                        Add to Cart
                    </button>
                </c:otherwise>
            </c:choose>

            <button type="button" class="btn-favorite ${(isFavorite || isWishlisted) ? 'active' : ''}" id="btnFavorite" title="Add to Favorites">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path></svg>
            </button>
        </div>
    </header>

    <nav class="product-tabs">
        <button class="tab-link active" data-tab="tab-description">Description</button>
        <button class="tab-link" data-tab="tab-gallery">Gallery</button>
        <button class="tab-link" data-tab="tab-versions">Versions</button>
        <button class="tab-link" data-tab="tab-reviews">
            Reviews <c:if test="${not empty reviews}">(${fn:length(reviews)})</c:if>
        </button>
    </nav>

    <!-- LAYOUT PRINCIPALE -->
    <div class="product-layout">

        <!-- TAB CONTENT -->
        <section class="tab-container">

            <!-- TAB DESCRIPTION -->
            <div id="tab-description" class="tab-content active">
                <c:if test="${not empty images}">
                    <div class="hero-banner">
                        <img src="${pageContext.request.contextPath}/${images[0].imagePath}" alt="${product.productName}">
                    </div>
                </c:if>

                <div class="description-section">
                    <h2>About ${product.productName}</h2>
                    <p class="description-text">${product.description}</p>
                </div>
            </div>

            <!-- TAB GALLERY -->
            <div id="tab-gallery" class="tab-content">
                <c:choose>
                    <c:when test="${not empty images}">
                        <div class="gallery-grid">
                            <c:forEach items="${images}" var="img">
                                <div class="gallery-item">
                                    <img src="${pageContext.request.contextPath}/${img.imagePath}" alt="${product.productName}">
                                </div>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="empty-msg">No gallery images uploaded for this product.</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- TAB VERSIONS -->
            <div id="tab-versions" class="tab-content">
                <div class="versions-list">
                    <c:choose>
                        <c:when test="${not empty versions}">
                            <c:forEach items="${versions}" var="ver">
                                <div class="version-card">
                                    <div class="version-info">
                                        <div class="version-header">
                                            <span class="version-title">v${ver.version}</span>
                                            <span class="badge-subtle">MC ${ver.minecraftVersion}</span>
                                        </div>
                                        <div class="version-meta-row">
                                            <span class="version-meta">
                                                Uploaded on ${fn:replace(fn:substring(ver.createdAt, 0, 19), 'T', ' ')}
                                            </span>
                                            <span class="dot-separator">•</span>
                                            <span class="version-meta highlight">${ver.downloadCount} downloads</span>
                                        </div>
                                        <c:if test="${not empty ver.changelog}">
                                            <p class="version-changelog">${ver.changelog}</p>
                                        </c:if>
                                    </div>

                                    <div class="version-action">
                                        <c:choose>
                                            <c:when test="${isPurchased || product.price == 0}">
                                                <a href="${pageContext.request.contextPath}/product/download?productId=${product.productId}&versionId=${ver.versionId}" class="btn-download-file">
                                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                                                    Download .schem
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="version-locked">Purchase product to unlock</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p class="empty-msg">No schematic versions uploaded yet.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div id="tab-reviews" class="tab-content">

                <div class="review-form-wrapper">
                    <c:choose>
                        <c:when test="${not empty sessionScope.account || not empty sessionScope.userSession}">
                            <form id="reviewForm" class="review-form">
                                <h3>${not empty userReview ? 'Edit Your Review' : 'Write a Review'}</h3>

                                <div class="rating-picker">
                                    <span class="rating-label">Rating:</span>
                                    <div class="star-rating" id="starPicker">
                                        <span class="star-btn" data-value="1">★</span>
                                        <span class="star-btn" data-value="2">★</span>
                                        <span class="star-btn" data-value="3">★</span>
                                        <span class="star-btn" data-value="4">★</span>
                                        <span class="star-btn" data-value="5">★</span>
                                    </div>
                                    <input type="hidden" name="rating" id="ratingInput" value="${not empty userReview ? userReview.rating : '5'}">
                                </div>

                                <div class="form-group">
                                    <textarea name="comment" id="reviewComment" rows="3"
                                        placeholder="Write your opinion about this schematic..."
                                        required>${not empty userReview ? userReview.comment : ''}</textarea>
                                </div>

                                <div class="review-form-actions">
                                    <button type="submit" class="btn-action btn-primary-action" id="btnSubmitReview">
                                            ${not empty userReview ? 'Update Review' : 'Submit Review'}
                                    </button>
                                    <c:if test="${not empty userReview}">
                                        <button type="button" class="btn-action btn-danger-action" id="btnDeleteReview">
                                            Delete Review
                                        </button>
                                    </c:if>
                                </div>

                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="review-login-prompt">
                                <p>You must be logged in to post a review.</p>
                                <a href="${pageContext.request.contextPath}/login" class="btn-action btn-cart-action">Log in to Review</a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="reviews-list">
                    <c:choose>
                        <c:when test="${not empty reviews}">
                            <c:forEach items="${reviews}" var="rev">
                                <div class="review-card">
                                    <div class="review-header">
                                        <div class="review-author-info">
                                            <img src="${pageContext.request.contextPath}/${not empty rev.authorAvatar ? rev.authorAvatar : 'images/default-avatar.png'}"
                                                 alt="${rev.authorUsername}" class="review-avatar">
                                            <div>
                                                <div class="author-name-row">
                                                    <span class="author-name">${rev.authorUsername}</span>

                                                    <!-- FLAG VERIFIED PURCHASE -->
                                                    <c:if test="${rev.verifiedPurchase || rev.isVerifiedPurchase}">
                                                        <span class="badge-verified" title="Verified Purchase">
                                                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><polyline points="20 6 9 17 4 12"></polyline></svg>
                                                            Verified Purchase
                                                        </span>
                                                    </c:if>
                                                </div>
                                                <span class="review-date">
                                                        ${fn:replace(fn:substring(rev.createdAt, 0, 19), 'T', ' ')}
                                                </span>
                                            </div>
                                        </div>

                                        <div class="review-rating-stars">
                                            <c:forEach var="i" begin="1" end="5">
                                                <span class="star ${i <= rev.rating ? 'filled' : ''}">★</span>
                                            </c:forEach>
                                        </div>
                                    </div>

                                    <div class="review-body">
                                        <p>${rev.comment}</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p class="empty-msg">No reviews yet. Be the first to leave a review!</p>
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>

        </section>

        <aside class="product-sidebar">

            <div class="sidebar-box">
                <h3>Compatibility</h3>
                <div class="compat-item">
                    <span class="compat-label">Minecraft Engine</span>
                    <span class="compat-value">
                        Java Edition
                        <c:if test="${not empty versions}">
                            (${versions[0].minecraftVersion})
                        </c:if>
                    </span>
                </div>
            </div>

            <div class="sidebar-box">
                <h3>Tags</h3>
                <div class="tags-cloud">
                    <c:choose>
                        <c:when test="${not empty categories}">
                            <c:forEach items="${categories}" var="cat">
                                <span class="sidebar-tag">${cat.categoryName}</span>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <span class="text-muted">No categories associated.</span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="sidebar-box">
                <h3>Creators</h3>
                <c:choose>
                    <c:when test="${not empty creator}">
                        <div class="creator-card">
                            <img class="creator-avatar"
                                 src="${pageContext.request.contextPath}/${creator.profileImagePath}"
                                 alt="${creator.username}">
                            <div class="creator-details">
                                <span class="creator-name">${creator.username}</span>
                                <span class="creator-role">Author</span>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted">Creator information unavailable.</p>
                    </c:otherwise>
                </c:choose>
            </div>

        </aside>

    </div>

</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
