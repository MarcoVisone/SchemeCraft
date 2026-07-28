/**
 * Admin Orders Dashboard — Client-side logic
 * Handles fetching, filtering, sorting, and pagination.
 */

(function () {
    'use strict';

    /* ----------------------------------------------------------------------
       Constants & Config
       ---------------------------------------------------------------------- */
    const API_BASE = (typeof CONTEXT_PATH !== 'undefined' ? CONTEXT_PATH : '') + '/admin/orders';
    const CTX = typeof CONTEXT_PATH !== 'undefined' ? CONTEXT_PATH : '';
    const PAGE_SIZE = 20;

    /* ----------------------------------------------------------------------
       State
       ---------------------------------------------------------------------- */
    let allOrders = [];
    let filteredOrders = [];
    let currentPage = 1;
    let isLoading = false;
    let statusLabelMap = {}; // statusId -> description

    /* ----------------------------------------------------------------------
       DOM References
       ---------------------------------------------------------------------- */
    const els = {
        tbody: document.getElementById('orders-tbody'),
        table: document.getElementById('orders-table'),
        stateLoading: document.getElementById('state-loading'),
        stateEmpty: document.getElementById('state-empty'),
        stateError: document.getElementById('state-error'),
        errorMessage: document.getElementById('error-message'),
        pagination: document.getElementById('pagination'),
        paginationInfo: document.getElementById('pagination-info'),
        paginationControls: document.getElementById('pagination-controls'),
        filterCustomer: document.getElementById('filter-customer'),
        btnSearch: document.getElementById('btn-search'),
        filterStatus: document.getElementById('filter-status'),
        filterDateFrom: document.getElementById('filter-date-from'),
        filterDateTo: document.getElementById('filter-date-to'),
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
    function formatCompactNumber(num) {
        if (num === null || num === undefined) return '0';
        const n = Number(num);
        if (isNaN(n)) return '0';
        if (n >= 1000000) return (n / 1000000).toFixed(1).replace(/\.0$/, '') + 'M';
        if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k';
        return n.toLocaleString();
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return '—';
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        return `${day}/${month}/${year}`;
    }

    function formatPrice(price) {
        if (price === null || price === undefined) return '€0.00';
        const n = Number(price);
        if (isNaN(n)) return '€0.00';
        return '€' + n.toFixed(2);
    }

    function truncate(str, maxLen) {
        if (!str) return '';
        return str.length > maxLen ? str.substring(0, maxLen) + '…' : str;
    }

    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const div = document.createElement('div');
        div.textContent = String(text);
        return div.innerHTML;
    }

    function debounce(fn, delay) {
        let timer = null;
        return function (...args) {
            clearTimeout(timer);
            timer = setTimeout(() => fn.apply(this, args), delay);
        };
    }

    /* ----------------------------------------------------------------------
       Confirmation Modal Helper
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

            function onConfirm() { cleanup(true); }
            function onCancel() { cleanup(false); }
            function onKeydown(e) {
                if (e.key === 'Escape') cleanup(false);
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
        els.stateLoading.classList.remove('admin-orders__state--visible');
        els.stateEmpty.classList.remove('admin-orders__state--visible');
        els.stateError.classList.remove('admin-orders__state--visible');
        els.table.style.display = 'none';
        els.pagination.style.display = 'none';

        if (state === 'loading') {
            els.stateLoading.classList.add('admin-orders__state--visible');
        } else if (state === 'empty') {
            els.stateEmpty.classList.add('admin-orders__state--visible');
            refreshIcons();
        } else if (state === 'error') {
            els.stateError.classList.add('admin-orders__state--visible');
            refreshIcons();
        } else if (state === 'data') {
            els.table.style.display = 'table';
            els.pagination.style.display = 'flex';
        }
    }

    /* ----------------------------------------------------------------------
       Fetch Order Statuses (populate filter dropdown & mapping)
       ---------------------------------------------------------------------- */
    async function fetchOrderStatuses() {
        try {
            const url = API_BASE + '/statuses';
            const response = await fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } });
            if (!response.ok) throw new Error('Failed to fetch statuses');
            const json = await response.json();
            if (json.success) {
                const statuses = json.statuses || [];
                const select = els.filterStatus;
                // Clear existing options except the first "All"
                while (select.options.length > 1) select.remove(1);

                statuses.forEach(status => {
                    const option = document.createElement('option');
                    option.value = status.statusId;
                    option.textContent = status.statusName || status.description || status.statusName;
                    select.appendChild(option);

                    // Store mapping
                    const id = status.statusId ?? status.id;
                    const label = status.statusName || status.description || status.name;
                    if (id && label) {
                        statusLabelMap[id] = label;
                    }
                });
            }
        } catch (e) {
            console.warn('Could not load order statuses, using fallback.');
            // Fallback hardcoded statuses (if API fails)
            const fallback = {
                1: 'PENDING',
                2: 'PAID',
                3: 'SHIPPED',
                4: 'CANCELLED',
                5: 'PENDING_VERIFICATION'
            };
            statusLabelMap = fallback;
            const select = els.filterStatus;
            while (select.options.length > 1) select.remove(1);
            Object.entries(fallback).forEach(([id, label]) => {
                const option = document.createElement('option');
                option.value = id;
                option.textContent = label;
                select.appendChild(option);
            });
        }
    }

    /* ----------------------------------------------------------------------
       Fetch Orders
       ---------------------------------------------------------------------- */
    async function fetchOrders() {
        if (isLoading) return;
        isLoading = true;
        showState('loading');

        try {
            const customer = els.filterCustomer.value.trim();
            const status = els.filterStatus.value;
            const dateFrom = els.filterDateFrom.value;
            const dateTo = els.filterDateTo.value;

            const url = new URL(API_BASE + '/list', window.location.origin);
            if (customer) url.searchParams.set('customerUsername', customer);
            if (status) url.searchParams.set('status', status);
            if (dateFrom) url.searchParams.set('dateFrom', dateFrom);
            if (dateTo) url.searchParams.set('dateTo', dateTo);

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

            allOrders = json.orders || [];
            currentPage = 1;
            applyFiltersAndSort();
            isLoading = false;
        } catch (err) {
            isLoading = false;
            console.error('Failed to load orders:', err);
            els.errorMessage.textContent = err.message || 'Failed to load orders. Please try again.';
            showState('error');
        }
    }

    /* ----------------------------------------------------------------------
       Filtering & Sorting (Client-side)
       ---------------------------------------------------------------------- */
    function applyFiltersAndSort() {
        let result = [...allOrders];

        const sortValue = els.filterSort.value;
        result.sort((a, b) => {
            switch (sortValue) {
                case 'newest': {
                    const da = new Date(a.orderDate || a.createdAt || 0);
                    const db = new Date(b.orderDate || b.createdAt || 0);
                    return db - da;
                }
                case 'oldest': {
                    const da = new Date(a.orderDate || a.createdAt || 0);
                    const db = new Date(b.orderDate || b.createdAt || 0);
                    return da - db;
                }
                case 'total-asc':
                    return (Number(a.totalAmount) || 0) - (Number(b.totalAmount) || 0);
                case 'total-desc':
                    return (Number(b.totalAmount) || 0) - (Number(a.totalAmount) || 0);
                case 'status-asc': {
                    const sa = (statusLabelMap[a.status] || '').toLowerCase();
                    const sb = (statusLabelMap[b.status] || '').toLowerCase();
                    return sa.localeCompare(sb);
                }
                case 'status-desc': {
                    const sa = (statusLabelMap[a.status] || '').toLowerCase();
                    const sb = (statusLabelMap[b.status] || '').toLowerCase();
                    return sb.localeCompare(sa);
                }
                default:
                    return 0;
            }
        });

        filteredOrders = result;
        currentPage = 1;
        renderPage();
    }

    /* ----------------------------------------------------------------------
       Rendering
       ---------------------------------------------------------------------- */
    function renderPage() {
        const total = filteredOrders.length;
        const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
        currentPage = Math.min(currentPage, totalPages);

        const startIdx = (currentPage - 1) * PAGE_SIZE;
        const endIdx = Math.min(startIdx + PAGE_SIZE, total);
        const pageItems = filteredOrders.slice(startIdx, endIdx);

        if (total === 0) {
            showState('empty');
            return;
        }

        showState('data');
        els.tbody.innerHTML = pageItems.map(order => buildRowHtml(order)).join('');
        refreshIcons();
        renderPagination(startIdx + 1, endIdx, total, totalPages);
    }

    function buildRowHtml(order) {
        const id = escapeHtml(order.orderId || order.id || '');
        const customer = escapeHtml(order.customerUsername || order.customerEmail || order.email || 'Guest');
        const orderDate = formatDate(order.orderDate || order.createdAt);
        const total = formatPrice(order.totalAmount);
        const statusLabel = statusLabelMap[order.status] || 'Unknown';

        const statusBadge = `<span class="status-badge"><span class="status-badge__dot"></span>${escapeHtml(statusLabel)}</span>`;

        return `
        <tr class="data-table__row" data-order-id="${id}">
            <td class="data-table__cell data-table__cell--id" data-label="Order ID" title="${id}">${truncate(id, 14)}</td>
            <td class="data-table__cell" data-label="Customer">${customer}</td>
            <td class="data-table__cell" data-label="Date">${orderDate}</td>
            <td class="data-table__cell" data-label="Total">${total}</td>
            <td class="data-table__cell" data-label="Status">${statusBadge}</td>
        </tr>
    `;
    }

    /* ----------------------------------------------------------------------
       Pagination Rendering
       ---------------------------------------------------------------------- */
    function renderPagination(start, end, total, totalPages) {
        els.paginationInfo.textContent = `Showing ${start}-${end} of ${total} orders`;

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
       Event Listeners
       ---------------------------------------------------------------------- */
    function initEventListeners() {
        // Search input (Enter key)
        els.filterCustomer.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                fetchOrders();
            }
        });

        // Search button
        els.btnSearch.addEventListener('click', function () {
            fetchOrders();
        });

        // Live search with debounce
        els.filterCustomer.addEventListener('input', debounce(function () {
            fetchOrders();
        }, 300));

        // Status filter
        els.filterStatus.addEventListener('change', function () {
            fetchOrders();
        });

        // Date filters
        els.filterDateFrom.addEventListener('change', function () {
            fetchOrders();
        });
        els.filterDateTo.addEventListener('change', function () {
            fetchOrders();
        });

        // Sort filter
        els.filterSort.addEventListener('change', function () {
            applyFiltersAndSort();
        });

        // Retry button
        els.btnRetry.addEventListener('click', function () {
            fetchOrders();
        });

        // Pagination delegation
        els.paginationControls.addEventListener('click', function (e) {
            const btn = e.target.closest('[data-page]');
            if (!btn || btn.disabled) return;

            const page = btn.dataset.page;
            const totalPages = Math.ceil(filteredOrders.length / PAGE_SIZE);

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
    }

    /* ----------------------------------------------------------------------
       Initialization
       ---------------------------------------------------------------------- */
    async function init() {
        if (!els.tbody) {
            console.error('Admin orders: required DOM elements not found.');
            return;
        }
        initEventListeners();
        await fetchOrderStatuses();
        fetchOrders();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();