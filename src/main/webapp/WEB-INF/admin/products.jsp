<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin - Products</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-products.css">

</head>
<body>
<%@ include file="/WEB-INF/fragments/header_admin.jsp" %>
<main class="admin-container">
    <div class="admin-products">
        <!-- Header area -->
        <div class="admin-products__header">
            <h1 class="admin-products__title">Products</h1>
        </div>

        <a href="${pageContext.request.contextPath}/admin/products/new" class="admin-products__fab">
            <img src="${pageContext.request.contextPath}/icons/crafting_table.webp"
                 alt="New Product Icon"
                 class="admin-products__fab-icon" />
            <span class="admin-products__fab-text">Craft Product</span>
        </a>

        <!-- Filters bar -->
        <div class="filters">
            <div class="filters__search">
                <div class="filters__search-input-wrapper">
                    <img class="filters__search-icon"
                         src="${pageContext.request.contextPath}/icons/compass.png"
                         alt="Search Icon" />
                    <input
                            type="text"
                            id="filter-search"
                            class="filters__search-input"
                            placeholder="Search products..."
                            autocomplete="off"
                    />
                </div>
                <!-- Tasto Search (SENZA icona) -->
                <button id="btn-search" class="filters__search-btn" type="button">
                    Search
                </button>
            </div>

            <div class="filters__controls">
                <div class="filters__group">
                    <label for="filter-status" class="filters__label">Status</label>
                    <select id="filter-status" class="filters__select">
                        <option value="all">All</option>
                        <option value="active">Active</option>
                        <option value="inactive">Inactive</option>
                    </select>
                </div>

                <div class="filters__group">
                    <label for="filter-sort" class="filters__label">Sort by</label>
                    <select id="filter-sort" class="filters__select">
                        <option value="newest">Newest</option>
                        <option value="oldest">Oldest</option>
                        <option value="price-asc">Price (Low-High)</option>
                        <option value="price-desc">Price (High-Low)</option>
                        <option value="name-asc">Name (A-Z)</option>
                        <option value="name-desc">Name (Z-A)</option>
                        <option value="most-downloaded">Most Downloaded</option>
                        <option value="highest-rated">Highest Rated</option>
                    </select>
                </div>
            </div>
        </div>

        <!-- Products table -->
        <div class="admin-products__table-wrapper">
            <table class="data-table" id="products-table">
                <thead class="data-table__head">
                <tr class="data-table__row data-table__row--header">
                    <th class="data-table__cell data-table__cell--header data-table__cell--id">ID</th>
                    <th class="data-table__cell data-table__cell--header">Name</th>
                    <th class="data-table__cell data-table__cell--header data-table__cell--created">Created</th>
                    <th class="data-table__cell data-table__cell--header">Price</th>
                    <th class="data-table__cell data-table__cell--header">Stock</th>
                    <th class="data-table__cell data-table__cell--header">Categories</th>
                    <th class="data-table__cell data-table__cell--header">Rating</th>
                    <th class="data-table__cell data-table__cell--header">Downloads</th>
                    <th class="data-table__cell data-table__cell--header">Status</th>
                    <th class="data-table__cell data-table__cell--header data-table__cell--actions">Actions</th>
                </tr>
                </thead>
                <tbody class="data-table__body" id="products-tbody">
                <!-- Rows rendered by JS -->
                </tbody>
            </table>

            <!-- Loading state -->
            <div class="admin-products__state admin-products__state--loading" id="state-loading">
                <div class="admin-products__spinner"></div>
                <p class="admin-products__state-text">Loading products...</p>
            </div>

            <!-- Empty state -->
            <div class="admin-products__state admin-products__state--empty" id="state-empty">
                <i data-lucide="package-open" class="admin-products__state-icon"></i>
                <p class="admin-products__state-text">No products found</p>
            </div>

            <!-- Error state -->
            <div class="admin-products__state admin-products__state--error" id="state-error">
                <i data-lucide="alert-circle" class="admin-products__state-icon"></i>
                <p class="admin-products__state-text" id="error-message">Something went wrong</p>
                <button id="btn-retry" class="admin-products__retry-btn" type="button">
                    <i data-lucide="refresh-cw" class="admin-products__retry-icon"></i>
                    <span>Retry</span>
                </button>
            </div>
        </div>

        <!-- Pagination -->
        <div class="pagination" id="pagination">
            <div class="pagination__info">
                <span id="pagination-info">Showing 0-0 of 0 products</span>
            </div>
            <div class="pagination__controls" id="pagination-controls">
                <!-- Buttons rendered by JS -->
            </div>
        </div>

        <!-- Confirmation modal (replaces native confirm()/alert(), reused by toggle and delete actions) -->
        <div class="confirm-modal" id="confirm-modal" hidden>
            <div class="confirm-modal__backdrop" id="confirm-modal-backdrop"></div>
            <div class="confirm-modal__dialog" role="alertdialog" aria-modal="true" aria-labelledby="confirm-modal-title">
                <p class="confirm-modal__title" id="confirm-modal-title">Are you sure?</p>
                <div class="confirm-modal__actions">
                    <button type="button" class="confirm-modal__btn confirm-modal__btn--cancel" id="confirm-modal-cancel">Cancel</button>
                    <button type="button" class="confirm-modal__btn confirm-modal__btn--confirm" id="confirm-modal-confirm">Confirm</button>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
    // Exposes the app's context path to plain JS files, so API calls
    // built in admin-products.js resolve correctly regardless of deploy path.
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/admin/products.js"></script>
</body>
</html>
