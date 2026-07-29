/**
 * Admin Products Dashboard — Client-side logic
 * Handles fetching, filtering, sorting, pagination, and actions.
 */

(function () {
    'use strict';

    /* ----------------------------------------------------------------------
       Constants & Config
       ---------------------------------------------------------------------- */
    const API_BASE = (typeof CONTEXT_PATH !== 'undefined' ? CONTEXT_PATH : '') + '/admin/products';
    const CTX = typeof CONTEXT_PATH !== 'undefined' ? CONTEXT_PATH : '';
    const PAGE_SIZE = 20;

    /* ----------------------------------------------------------------------
       State
       ---------------------------------------------------------------------- */
    let allProducts = [];
    let filteredProducts = [];
    let currentPage = 1;
    let isLoading = false;

    /* ----------------------------------------------------------------------
       DOM References
       ---------------------------------------------------------------------- */
    const els = {
        tbody: document.getElementById('products-tbody'),
        table: document.getElementById('products-table'),
        stateLoading: document.getElementById('state-loading'),
        stateEmpty: document.getElementById('state-empty'),
        stateError: document.getElementById('state-error'),
        errorMessage: document.getElementById('error-message'),
        pagination: document.getElementById('pagination'),
        paginationInfo: document.getElementById('pagination-info'),
        paginationControls: document.getElementById('pagination-controls'),
        filterSearch: document.getElementById('filter-search'),
        btnSearch: document.getElementById('btn-search'),
        filterStatus: document.getElementById('filter-status'),
        filterSort: document.getElementById('filter-sort'),
        btnRetry: document.getElementById('btn-retry'),
        confirmModal: document.getElementById('confirm-modal'),
        confirmModalBackdrop: document.getElementById('confirm-modal-backdrop'),
        confirmModalTitle: document.getElementById('confirm-modal-title'),
        confirmModalCancel: document.getElementById('confirm-modal-cancel'),
        confirmModalConfirm: document.getElementById('confirm-modal-confirm'),
    };

    /* ----------------------------------------------------------------------
       Utility Functions
       ---------------------------------------------------------------------- */

    /**
     * Format a number as compact (e.g. 1.2k, 340).
     */
    function formatCompactNumber(num) {
        if (num === null || num === undefined) return '0';
        const n = Number(num);
        if (isNaN(n)) return '0';
        if (n >= 1000000) return (n / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
        if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
        return n.toLocaleString();
    }

    /**
     * Format a date string to dd/MM/yyyy.
     */
    function formatDate(dateStr) {
        if (!dateStr) return '—';
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return '—';
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        return `${day}/${month}/${year}`;
    }

    /**
     * Format price with currency symbol.
     */
    function formatPrice(price) {
        if (price === null || price === undefined) return '€0.00';
        const n = Number(price);
        if (isNaN(n)) return '€0.00';
        return '€' + n.toFixed(2);
    }

    /**
     * Truncate text with ellipsis.
     */
    function truncate(str, maxLen) {
        if (!str) return '';
        return str.length > maxLen ? str.substring(0, maxLen) + '…' : str;
    }

    /**
     * Escape HTML to prevent XSS.
     */
    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const div = document.createElement('div');
        div.textContent = String(text);
        return div.innerHTML;
    }

    /**
     * Debounce a function.
     */
    function debounce(fn, delay) {
        let timer = null;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), delay);
        };
    }

    /* ----------------------------------------------------------------------
       Confirmation Modal Helper
       Non-blocking replacement for the native window.confirm() dialog.
       Resolves to true if the user confirms, false if they cancel or dismiss.
       ---------------------------------------------------------------------- */
    function showConfirm(title) {
        return new Promise((resolve) => {
            els.confirmModalTitle.textContent = title;
            els.confirmModal.hidden = false;

            function cleanup(result) {
                els.confirmModal.hidden = true;
                els.confirmModalConfirm.removeEventListener('click', onConfirm);
                els.confirmModalCancel.removeEventListener('click', onCancel);
                els.confirmModalBackdrop.removeEventListener('click', onCancel);
                document.removeEventListener('keydown', onKeydown);
                resolve(result);
            }

            function onConfirm() {
                cleanup(true);
            }

            function onCancel() {
                cleanup(false);
            }

            function onKeydown(e) {
                if (e.key === 'Escape') {
                    cleanup(false);
                }
            }

            els.confirmModalConfirm.addEventListener('click', onConfirm);
            els.confirmModalCancel.addEventListener('click', onCancel);
            els.confirmModalBackdrop.addEventListener('click', onCancel);
            document.addEventListener('keydown', onKeydown);

            els.confirmModalConfirm.focus();
        });
    }

    /* ----------------------------------------------------------------------
       Lucide Icons Helper
       ---------------------------------------------------------------------- */
    function refreshIcons() {
        if (typeof lucide !== 'undefined' && lucide.createIcons) {
            try {
                lucide.createIcons();
            } catch (e) {
                console.warn('Lucide icons refresh failed:', e);
            }
        }
    }

    /* ----------------------------------------------------------------------
       UI State Helpers
       ---------------------------------------------------------------------- */
    function showState(state) {
        els.tbody.innerHTML = '';
        els.stateLoading.classList.remove('admin-products__state--visible');
        els.stateEmpty.classList.remove('admin-products__state--visible');
        els.stateError.classList.remove('admin-products__state--visible');
        els.table.style.display = 'none';
        els.pagination.style.display = 'none';

        if (state === 'loading') {
            els.stateLoading.classList.add('admin-products__state--visible');
        } else if (state === 'empty') {
            els.stateEmpty.classList.add('admin-products__state--visible');
            refreshIcons();
        } else if (state === 'error') {
            els.stateError.classList.add('admin-products__state--visible');
            refreshIcons();
        } else if (state === 'data') {
            els.table.style.display = 'table';
            els.pagination.style.display = 'flex';
        }
    }

    /* ----------------------------------------------------------------------
       Fetch Products
       ---------------------------------------------------------------------- */

    async function fetchProducts() {
        if (isLoading) return;
        isLoading = true;
        showState('loading');

        try {
            const keywords = els.filterSearch.value.trim();
            const status = els.filterStatus.value; // "all" | "active" | "inactive" — forwarded to the server
            const url = new URL(API_BASE + '/list', window.location.origin);
            if (keywords) url.searchParams.set('keywords', keywords);
            if (status && status !== 'all') url.searchParams.set('status', status);
            // Fetch a large batch; sort/paginate client-side, filtering is done server-side
            url.searchParams.set('page', '1');
            url.searchParams.set('pageSize', '9999');

            const response = await fetch(url.toString(), {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const json = await response.json();

            if (json.success === false) {
                throw new Error(json.message || 'Server returned an error.');
            }

            // Extract products array from various possible shapes
            allProducts = json.products || (json.data && json.data.products) || [];
            currentPage = 1;
            applyFiltersAndSort();
            isLoading = false;
        } catch (err) {
            isLoading = false;
            console.error('Failed to load products:', err);
            els.errorMessage.textContent = err.message || 'Failed to load products. Please try again.';
            showState('error');
        }
    }

    /* ----------------------------------------------------------------------
       Filtering & Sorting (Client-side)
       ---------------------------------------------------------------------- */

    function applyFiltersAndSort() {
        let result = [...allProducts];

        // Status filtering is now done server-side (see fetchProducts), since
        // ProductDAO.searchProductsForAdmin accepts an explicit active/inactive
        // filter. No client-side status filtering needed here anymore.

        // Sort
        const sortValue = els.filterSort.value;
        result.sort((a, b) => {
            switch (sortValue) {
                case 'newest': {
                    const da = new Date(a.createdAt || a.dateCreated || a.created || 0);
                    const db = new Date(b.createdAt || b.dateCreated || b.created || 0);
                    return db - da;
                }
                case 'oldest': {
                    const da = new Date(a.createdAt || a.dateCreated || a.created || 0);
                    const db = new Date(b.createdAt || b.dateCreated || b.created || 0);
                    return da - db;
                }
                case 'price-asc':
                    return (Number(a.price) || 0) - (Number(b.price) || 0);
                case 'price-desc':
                    return (Number(b.price) || 0) - (Number(a.price) || 0);
                case 'name-asc':
                    return String(a.productName || a.name || '').localeCompare(String(b.productName || b.name || ''));
                case 'name-desc':
                    return String(b.productName || b.name || '').localeCompare(String(a.productName || a.name || ''));
                case 'most-downloaded':
                    return (Number(b.totalDownloads) || 0) - (Number(a.totalDownloads) || 0);
                case 'highest-rated':
                    return (Number(b.averageRating) || 0) - (Number(a.averageRating) || 0);
                default:
                    return 0;
            }
        });

        filteredProducts = result;
        currentPage = 1;
        renderPage();
    }

    /* ----------------------------------------------------------------------
       Rendering
       ---------------------------------------------------------------------- */

    function renderPage() {
        const total = filteredProducts.length;
        const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages);

        const startIdx = (currentPage - 1) * PAGE_SIZE;
        const endIdx = Math.min(startIdx + PAGE_SIZE, total);
        const pageItems = filteredProducts.slice(startIdx, endIdx);

        if (total === 0) {
            showState('empty');
            return;
        }

        showState('data');
        els.tbody.innerHTML = pageItems.map(product => buildRowHtml(product)).join('');
        refreshIcons();
        renderPagination(startIdx + 1, endIdx, total, totalPages);
    }

    function buildRowHtml(product) {
        const id = escapeHtml(product.productId || product.id || '');
        const name = escapeHtml(product.productName || product.name || 'Untitled');
        const created = formatDate(product.createdAt || product.dateCreated || product.created);
        const price = formatPrice(product.price);

        const isUnlimited = product.stockQuantity === null || product.stockQuantity === undefined;
        const stock = isUnlimited
            ? '<span class="status-badge status-badge--inactive" style="text-transform:none;letter-spacing:normal;font-size:0.8125rem;padding:3px 10px;">Unlimited</span>'
            : escapeHtml(String(product.stockQuantity));

        const rating = product.averageRating !== null && product.averageRating !== undefined ? Number(product.averageRating) : 0;
        const downloads = formatCompactNumber(product.totalDownloads);
        const isActive = product.isActive === true;

        let categoriesHtml = '';
        const categories = product.categories || [];
        if (Array.isArray(categories) && categories.length > 0) {
            const visible = categories.slice(0, 3);
            const remaining = categories.length - visible.length;
            const chips = visible.map(c => {
                const label = typeof c === 'string' ? c : (c.categoryName || c.name || '');
                return `<span class="category-chips__item">${escapeHtml(label)}</span>`;
            }).join('');
            const more = remaining > 0 ? `<span class="category-chips__more">+${remaining} more</span>` : '';
            categoriesHtml = `<div class="category-chips">${chips}${more}</div>`;
        } else {
            categoriesHtml = '<span style="color:var(--text-secondary);font-size:0.8125rem;">—</span>';
        }

        const statusBadge = isActive
            ? `<span class="status-badge status-badge--active"><span class="status-badge__dot"></span>Active</span>`
            : `<span class="status-badge status-badge--inactive"><span class="status-badge__dot"></span>Inactive</span>`;

        const toggleIconPath = isActive
            ? `${CTX}/icons/deactive.png`
            : `${CTX}/icons/active.png`;
        const toggleClass = isActive ? 'actions__btn--toggle-active' : 'actions__btn--toggle-inactive';
        const toggleTitle = isActive ? 'Deactivate product' : 'Activate product';

        return `
        <tr class="data-table__row" data-product-id="${id}">
            <td class="data-table__cell data-table__cell--id" data-label="ID" title="${id}">${truncate(id, 14)}</td>
            <td class="data-table__cell" data-label="Name">${name}</td>
            <td class="data-table__cell data-table__cell--created" data-label="Created">${created}</td>
            <td class="data-table__cell" data-label="Price">${price}</td>
            <td class="data-table__cell" data-label="Stock">${stock}</td>
            <td class="data-table__cell data-table__cell--categories" data-label="Categories">${categoriesHtml}</td>
            <td class="data-table__cell data-table__cell--rating" data-label="Rating">
                <span class="rating"><span class="rating__star">★</span><span class="rating__value">${rating.toFixed(1)}</span></span>
            </td>
            <td class="data-table__cell data-table__cell--downloads" data-label="Downloads">${downloads}</td>
            <td class="data-table__cell" data-label="Status">${statusBadge}</td>
            <td class="data-table__cell data-table__cell--actions" data-label="Actions">
                <div class="actions">
                    <button class="actions__btn ${toggleClass}" data-action="toggle" data-id="${id}" data-active="${isActive}" title="${toggleTitle}" type="button">
                        <img src="${toggleIconPath}" alt="${toggleTitle}" class="actions__btn-icon" />
                    </button>
                    <a href="${CTX}/admin/products/edit?productId=${id}" 
                       class="actions__btn actions__btn--edit" 
                       title="Edit product">
                        <img src="${CTX}/icons/command_block_edit.gif" alt="Edit product" class="actions__btn-icon" />
                    </a>
                </div>
            </td>
        </tr>
    `;
    }

    /* ----------------------------------------------------------------------
       Pagination Rendering
       ---------------------------------------------------------------------- */

    function renderPagination(start, end, total, totalPages) {
        els.paginationInfo.textContent = `Showing ${start}-${end} of ${total} products`;

        if (totalPages <= 1) {
            els.paginationControls.innerHTML = '';
            return;
        }

        const buttons = [];

        // Previous
        buttons.push(`
            <button class="pagination__btn" data-page="prev" ${currentPage === 1 ? 'disabled' : ''} type="button">
                Previous
            </button>
        `);

        // Page numbers with ellipsis
        const maxVisible = 5;
        let startPage = Math.max(1, currentPage - Math.floor(maxVisible / 2));
        let endPage = Math.min(totalPages, startPage + maxVisible - 1);

        if (endPage - startPage + 1 < maxVisible) {
            startPage = Math.max(1, endPage - maxVisible + 1);
        }

        if (startPage > 1) {
            buttons.push(`<button class="pagination__btn" data-page="1" type="button">1</button>`);
            if (startPage > 2) {
                buttons.push(`<span class="pagination__ellipsis">…</span>`);
            }
        }

        for (let i = startPage; i <= endPage; i++) {
            const activeClass = i === currentPage ? 'pagination__btn--active' : '';
            buttons.push(`<button class="pagination__btn ${activeClass}" data-page="${i}" type="button">${i}</button>`);
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                buttons.push(`<span class="pagination__ellipsis">…</span>`);
            }
            buttons.push(`<button class="pagination__btn" data-page="${totalPages}" type="button">${totalPages}</button>`);
        }

        // Next
        buttons.push(`
            <button class="pagination__btn" data-page="next" ${currentPage === totalPages ? 'disabled' : ''} type="button">
                Next
            </button>
        `);

        els.paginationControls.innerHTML = buttons.join('');
    }

    /* ----------------------------------------------------------------------
       Actions: Toggle Active/Inactive
       ---------------------------------------------------------------------- */

    async function handleToggle(productId, currentlyActive) {
        const action = currentlyActive ? 'deactivate' : 'activate';
        const confirmMsg = currentlyActive
            ? 'Are you sure you want to deactivate this product?'
            : 'Are you sure you want to activate this product?';

        const confirmed = await showConfirm(confirmMsg);
        if (!confirmed) return;

        const url = API_BASE + (currentlyActive ? '/delete' : '/activate');
        const body = new URLSearchParams();
        body.append('productId', productId);

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: body.toString(),
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const json = await response.json();
            if (json.success === false) {
                throw new Error(json.message || `Failed to ${action} product.`);
            }

            await fetchProducts();
        } catch (err) {
            // Non-blocking: log only, no alert(). UI feedback for action failures
            // is intentionally deferred until a proper toast/notification component exists.
            console.error(`Failed to ${action} product:`, err);
        }
    }

    /* ----------------------------------------------------------------------
       Event Listeners
       ---------------------------------------------------------------------- */

    function initEventListeners() {
        // Search input (Enter key)
        els.filterSearch.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                fetchProducts();
            }
        });

        // Search button
        els.btnSearch.addEventListener('click', function () {
            fetchProducts();
        });

        // Live search with debounce
        els.filterSearch.addEventListener('input', debounce(function () {
            fetchProducts();
        }, 300));

        // Status filter — now applied server-side, so changing it requires a new fetch
        els.filterStatus.addEventListener('change', function () {
            fetchProducts();
        });

        // Sort filter
        els.filterSort.addEventListener('change', function () {
            applyFiltersAndSort();
        });

        // Retry button
        els.btnRetry.addEventListener('click', function () {
            fetchProducts();
        });

        // Pagination delegation
        els.paginationControls.addEventListener('click', function (e) {
            const btn = e.target.closest('[data-page]');
            if (!btn || btn.disabled) return;

            const page = btn.dataset.page;
            const totalPages = Math.ceil(filteredProducts.length / PAGE_SIZE);

            if (page === 'prev') {
                if (currentPage > 1) currentPage--;
            } else if (page === 'next') {
                if (currentPage < totalPages) currentPage++;
            } else {
                currentPage = parseInt(page, 10);
            }

            renderPage();
            els.table.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });

        // Table action delegation
        els.tbody.addEventListener('click', function (e) {
            const btn = e.target.closest('[data-action]');
            if (!btn) return;

            const action = btn.dataset.action;
            const productId = btn.dataset.id;

            if (action === 'toggle') {
                const currentlyActive = btn.dataset.active === 'true';
                handleToggle(productId, currentlyActive);
            }
        });
    }

    /* ----------------------------------------------------------------------
       Initialization
       ---------------------------------------------------------------------- */

    function init() {
        if (!els.tbody) {
            console.error('Admin products: required DOM elements not found.');
            return;
        }
        initEventListeners();
        fetchProducts();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
