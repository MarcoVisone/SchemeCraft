<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft - Catalog</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">

    <script src="${pageContext.request.contextPath}/js/header.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/catalog.js" defer></script>
</head>
<body class="catalog-body">

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="catalog-container" data-context-path="${pageContext.request.contextPath}">

    <section class="catalog-hero">
        <h1 class="catalog-title">Schematic Catalog</h1>
        <p class="catalog-subtitle">Explore, filter, and download high-quality Minecraft creations</p>
    </section>

    <div class="catalog-layout">

        <aside class="catalog-sidebar">
            <div class="filter-card">
                <div class="filter-header">
                    <h3 class="filter-title">Filters</h3>
                    <button type="button" id="btnResetFilters" class="btn-reset-link">Reset All</button>
                </div>

                <form id="catalogFilterForm" onsubmit="return false;">

                    <div class="filter-group">
                        <label for="keywords">Search</label>
                        <input type="text" id="keywords" name="keywords" class="filter-input" placeholder="e.g. Castle, Farm, Redstone...">
                    </div>

                    <div class="filter-group">
                        <label for="categoryId">Category</label>
                        <select id="categoryId" name="categoryId" class="filter-select">
                            <option value="">All Categories</option>
                            <c:forEach items="${categories}" var="category">
                                <option value="${category.categoryId}"
                                        <c:if test="${not empty category.parentCategoryId}">style="color: var(--text-secondary);"</c:if>>
                                    <c:if test="${not empty category.parentCategoryId}">&nbsp;&nbsp;&nbsp;&nbsp;↳ </c:if>
                                        ${category.categoryName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label for="minecraftVersion">Minecraft Version</label>
                        <input type="text" id="minecraftVersion" name="minecraftVersion" class="filter-input" placeholder="e.g. 1.20.4, 1.19...">
                    </div>

                    <div class="filter-group">
                        <label>Price Range ($)</label>
                        <div class="range-inputs">
                            <input type="number" id="minPrice" name="minPrice" class="filter-input" placeholder="Min" min="0" step="0.50">
                            <span class="range-separator">-</span>
                            <input type="number" id="maxPrice" name="maxPrice" class="filter-input" placeholder="Max" min="0" step="0.50">
                        </div>
                    </div>

                    <div class="filter-group">
                        <label>Rating Range (0 - 5 ★)</label>
                        <div class="range-inputs">
                            <input type="number" id="minRating" name="minRating" class="filter-input" placeholder="Min" min="0" max="5" step="0.5">
                            <span class="range-separator">-</span>
                            <input type="number" id="maxRating" name="maxRating" class="filter-input" placeholder="Max" min="0" max="5" step="0.5">
                        </div>
                    </div>

                    <div class="filter-group filter-checkbox-group">
                        <label class="catalog-checkbox-container">
                            <input type="checkbox" id="onlyWithDiscount" name="onlyWithDiscount" value="true">
                            <span class="checkbox-label">On Sale Only</span>
                        </label>
                    </div>

                    <hr class="filter-divider">

                    <div class="filter-group">
                        <label for="orderByColumn">Sort By</label>
                        <select id="orderByColumn" name="orderByColumn" class="filter-select">
                            <option value="created_at">Newest Arrivals</option>
                            <option value="price">Price</option>
                            <option value="average_rating">Rating</option>
                            <option value="product_name">Name</option>
                        </select>
                    </div>

                    <div class="filter-group">
                        <label for="ascending">Order Direction</label>
                        <select id="ascending" name="ascending" class="filter-select">
                            <option value="false">Descending (High to Low)</option>
                            <option value="true">Ascending (Low to High)</option>
                        </select>
                    </div>

                </form>
            </div>
        </aside>

        <section class="catalog-content">

            <div class="catalog-toolbar">
                <span id="resultsCount" class="results-count">Loading schematics...</span>
            </div>

            <div id="productsGrid" class="products-grid">
            </div>

            <div id="pagination" class="catalog-pagination hidden">
                <button type="button" id="btnPrevPage" class="btn-pagination" disabled>&larr; Previous</button>
                <span id="currentPageDisplay" class="page-indicator">Page 1</span>
                <button type="button" id="btnNextPage" class="btn-pagination">Next &rarr;</button>
            </div>

        </section>

    </div>

</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
