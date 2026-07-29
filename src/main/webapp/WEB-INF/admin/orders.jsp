<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin - Orders</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-orders.css">

</head>
<body>
<%@ include file="/WEB-INF/fragments/header_admin.jsp" %>
<main class="admin-container">
    <div class="admin-orders">
        <!-- Header -->
        <div class="admin-orders__header">
            <h1 class="admin-orders__title">Orders</h1>
        </div>

        <!-- Filters -->
        <div class="filters">
            <div class="filters__search">
                <div class="filters__search-input-wrapper">
                    <img class="filters__search-icon"
                         src="${pageContext.request.contextPath}/icons/compass.png"
                         alt="Search Icon" />
                    <input
                            type="text"
                            id="filter-customer"
                            class="filters__search-input"
                            placeholder="Search by customer username or email..."
                            autocomplete="off"
                    />
                </div>
                <button id="btn-search" class="filters__search-btn" type="button">Search</button>
            </div>

            <div class="filters__controls">
                <div class="filters__group">
                    <label for="filter-status" class="filters__label">Status</label>
                    <select id="filter-status" class="filters__select">
                        <option value="">All</option>
                        <!-- options will be populated by JavaScript -->
                    </select>
                </div>

                <div class="filters__group filters__group--date">
                    <label for="filter-date-from" class="filters__label">From</label>
                    <input type="date" id="filter-date-from" class="filters__date-input" />
                </div>
                <div class="filters__group filters__group--date">
                    <label for="filter-date-to" class="filters__label">To</label>
                    <input type="date" id="filter-date-to" class="filters__date-input" />
                </div>

                <div class="filters__group">
                    <label for="filter-sort" class="filters__label">Sort by</label>
                    <select id="filter-sort" class="filters__select">
                        <option value="newest">Newest</option>
                        <option value="oldest">Oldest</option>
                        <option value="total-asc">Total (Low‑High)</option>
                        <option value="total-desc">Total (High‑Low)</option>
                        <option value="status-asc">Status (A‑Z)</option>
                        <option value="status-desc">Status (Z‑A)</option>
                    </select>
                </div>
            </div>
        </div>

        <!-- Table -->
        <div class="admin-orders__table-wrapper">
            <table class="data-table" id="orders-table">
                <thead class="data-table__head">
                <tr class="data-table__row data-table__row--header">
                    <th class="data-table__cell data-table__cell--header data-table__cell--id">Order ID</th>
                    <th class="data-table__cell data-table__cell--header">Customer</th>
                    <th class="data-table__cell data-table__cell--header">Date</th>
                    <th class="data-table__cell data-table__cell--header">Total</th>
                    <th class="data-table__cell data-table__cell--header">Status</th>
                </tr>
                </thead>
                <tbody class="data-table__body" id="orders-tbody">
                <!-- Rows rendered by JS -->
                </tbody>
            </table>

            <!-- States -->
            <div class="admin-orders__state admin-orders__state--loading" id="state-loading">
                <div class="admin-orders__spinner"></div>
                <p class="admin-orders__state-text">Loading orders...</p>
            </div>
            <div class="admin-orders__state admin-orders__state--empty" id="state-empty">
                <i data-lucide="package-open" class="admin-orders__state-icon"></i>
                <p class="admin-orders__state-text">No orders found</p>
            </div>
            <div class="admin-orders__state admin-orders__state--error" id="state-error">
                <i data-lucide="alert-circle" class="admin-orders__state-icon"></i>
                <p class="admin-orders__state-text" id="error-message">Something went wrong</p>
                <button id="btn-retry" class="admin-orders__retry-btn" type="button">
                    <i data-lucide="refresh-cw" class="admin-orders__retry-icon"></i>
                    <span>Retry</span>
                </button>
            </div>
        </div>

        <!-- Pagination -->
        <div class="pagination" id="pagination">
            <div class="pagination__info">
                <span id="pagination-info">Showing 0-0 of 0 orders</span>
            </div>
            <div class="pagination__controls" id="pagination-controls">
                <!-- Buttons rendered by JS -->
            </div>
        </div>

        <!-- Confirmation modal -->
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
    const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/admin/orders.js"></script>
</body>
</html>
