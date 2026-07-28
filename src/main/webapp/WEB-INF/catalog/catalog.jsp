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

    <div class="catalog-layout">

        <!-- SIDEBAR FILTERS -->
        <aside class="catalog-sidebar">
            <div class="sidebar-header">
                <span class="sidebar-title">Filters</span>
                <button type="button" id="btnResetFilters" class="btn-reset-link">Reset all</button>
            </div>

            <form id="catalogFilterForm" onsubmit="return false;">

                <!-- Minecraft Version -->
                <div class="filter-accordion">
                    <button type="button" class="accordion-header">
                        <span class="accordion-title">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path></svg>
                            Minecraft Version
                        </span>
                        <svg class="chevron-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </button>
                    <div class="accordion-content">
                        <input type="text" id="minecraftVersion" name="minecraftVersion" class="filter-input" placeholder="Search version (e.g. 1.20)...">
                    </div>
                </div>

                <!-- Category Accordion -->
                <div class="filter-accordion">
                    <button type="button" class="accordion-header">
                        <span class="accordion-title">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
                            Category
                        </span>
                        <svg class="chevron-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </button>
                    <div class="accordion-content">
                        <select id="categoryId" name="categoryId" class="filter-input">
                            <option value="">All Categories</option>

                            <c:forEach items="${categories}" var="parent">
                                <c:if test="${empty parent.parentCategoryId}">

                                    <option value="${parent.categoryId}">${parent.categoryName}</option>

                                    <c:forEach items="${categories}" var="child">
                                        <c:if test="${child.parentCategoryId eq parent.categoryId}">
                                            <option value="${child.categoryId}">&nbsp;&nbsp;└ ${child.categoryName}</option>
                                        </c:if>
                                    </c:forEach>

                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Price Range -->
                <div class="filter-accordion">
                    <button type="button" class="accordion-header">
                        <span class="accordion-title">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
                            Price Range ($)
                        </span>
                        <svg class="chevron-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </button>
                    <div class="accordion-content">
                        <div class="range-inputs">
                            <input type="number" id="minPrice" name="minPrice" class="filter-input" placeholder="Min" min="0" step="0.50">
                            <span class="range-separator">-</span>
                            <input type="number" id="maxPrice" name="maxPrice" class="filter-input" placeholder="Max" min="0" step="0.50">
                        </div>
                    </div>
                </div>

                <!-- Rating Range -->
                <div class="filter-accordion">
                    <button type="button" class="accordion-header">
                        <span class="accordion-title">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                            Rating Range (★)
                        </span>
                        <svg class="chevron-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 12 15 18 9"></polyline></svg>
                    </button>
                    <div class="accordion-content">
                        <div class="range-inputs">
                            <input type="number" id="minRating" name="minRating" class="filter-input" placeholder="0" min="0" max="5" step="0.5">
                            <span class="range-separator">-</span>
                            <input type="number" id="maxRating" name="maxRating" class="filter-input" placeholder="5" min="0" max="5" step="0.5">
                        </div>
                    </div>
                </div>

                <!-- Checkbox On Sale -->
                <div class="filter-accordion">
                    <div class="accordion-content no-padding">
                        <label class="catalog-checkbox-container">
                            <input type="checkbox" id="onlyWithDiscount" name="onlyWithDiscount" value="true">
                            <span class="checkbox-label">On Sale Only</span>
                        </label>
                    </div>
                </div>

            </form>
        </aside>

        <!-- MAIN CATALOG CONTENT -->
        <section class="catalog-content">

            <!-- MAIN TOP SEARCH BAR -->
            <div class="search-hero-bar">
                <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                <input type="text" id="keywords" name="keywords" form="catalogFilterForm" class="main-search-input" placeholder="Search schematics, castles, redstone builds...">
            </div>

            <!-- TOOLBAR CONTROLS -->
            <div class="catalog-toolbar">
                <div class="toolbar-left">
                    <div class="results-count" id="resultsCount">Loading schematics...</div>
                    <div class="select-wrapper">
                        <label for="orderByColumn" class="select-label">Sort by:</label>
                        <select id="orderByColumn" name="orderByColumn" form="catalogFilterForm" class="toolbar-select">
                            <option value="created_at">Newest Arrivals</option>
                            <option value="price">Price</option>
                            <option value="average_rating">Rating</option>
                            <option value="product_name">Name</option>
                        </select>
                    </div>

                    <div class="select-wrapper">
                        <select id="ascending" name="ascending" form="catalogFilterForm" class="toolbar-select">
                            <option value="false">Descending</option>
                            <option value="true">Ascending</option>
                        </select>
                    </div>
                </div>

                <!-- PAGINATION IN TOOLBAR -->
                <div id="catalogPagination" class="catalog-pagination"></div>
            </div>

            <!-- PRODUCTS GRID -->
            <div id="productsGrid" class="products-grid"></div>

        </section>

    </div>

</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
