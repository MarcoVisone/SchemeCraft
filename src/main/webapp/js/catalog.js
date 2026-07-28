document.addEventListener('DOMContentLoaded', () => {
    const catalogContainer = document.querySelector('.catalog-container');
    const contextPath = catalogContainer ? catalogContainer.dataset.contextPath : '';

    const filterForm = document.getElementById('catalogFilterForm');
    const productsGrid = document.getElementById('productsGrid');
    const resultsCount = document.getElementById('resultsCount');
    const paginationContainer = document.getElementById('catalogPagination');
    const btnResetFilters = document.getElementById('btnResetFilters');

    let currentPage = 1;
    let pageSize = 10;
    let debounceTimer = null;
    let hasMorePages = false;

    initAccordions();

    loadProducts();

    /* =========================================================================
       ACCORDION COLLAPSIBLE LOGIC
       ========================================================================= */
    function initAccordions() {
        document.querySelectorAll('.filter-accordion').forEach(accordion => {
            const header = accordion.querySelector('.accordion-header');
            if (header) {
                header.addEventListener('click', () => {
                    accordion.classList.toggle('is-closed');
                });
            }
        });
    }

    /* =========================================================================
       EVENT LISTENERS
       ========================================================================= */

    document.querySelectorAll('#keywords, #catalogFilterForm input[type="text"], #catalogFilterForm input[type="number"]').forEach(input => {
        input.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentPage = 1;
                loadProducts();
            }, 350);
        });
    });

    document.querySelectorAll('#orderByColumn, #ascending, #catalogFilterForm select, #catalogFilterForm input[type="checkbox"]').forEach(element => {
        element.addEventListener('change', () => {
            currentPage = 1;
            loadProducts();
        });
    });

    if (btnResetFilters) {
        btnResetFilters.addEventListener('click', () => {
            filterForm.reset();
            const mainSearch = document.getElementById('keywords');
            if (mainSearch) mainSearch.value = '';
            currentPage = 1;
            loadProducts();
        });
    }

    /* =========================================================================
       AJAX FETCH
       ========================================================================= */

    function loadProducts() {
        const params = new URLSearchParams();

        const keywords = document.getElementById('keywords') ? document.getElementById('keywords').value.trim() : '';
        const categoryId = document.getElementById('categoryId').value;
        const minecraftVersion = document.getElementById('minecraftVersion').value.trim();
        const minPrice = document.getElementById('minPrice').value;
        const maxPrice = document.getElementById('maxPrice').value;
        const minRating = document.getElementById('minRating').value;
        const maxRating = document.getElementById('maxRating').value;
        const onlyWithDiscount = document.getElementById('onlyWithDiscount').checked;
        const orderByColumn = document.getElementById('orderByColumn').value;
        const ascending = document.getElementById('ascending').value;

        if (keywords) params.append('keywords', keywords);
        if (categoryId && categoryId !== "") params.append('categoryId', categoryId);
        if (minecraftVersion) params.append('minecraftVersion', minecraftVersion);
        if (minPrice) params.append('minPrice', minPrice);
        if (maxPrice) params.append('maxPrice', maxPrice);
        if (minRating) params.append('minRating', minRating);
        if (maxRating) params.append('maxRating', maxRating);
        if (onlyWithDiscount) params.append('onlyWithDiscount', 'true');
        if (orderByColumn) params.append('orderByColumn', orderByColumn);
        if (ascending) params.append('ascending', ascending);

        params.append('pageNumber', currentPage);
        params.append('pageSize', pageSize);

        productsGrid.style.opacity = '0.4';

        fetch(`${contextPath}/product/search?${params.toString()}`)
            .then(response => {
                if (!response.ok) throw new Error("Search request failed");
                return response.json();
            })
            .then(data => {
                productsGrid.style.opacity = '1';
                const products = Array.isArray(data) ? data : (data.products || []);

                const totalCount = (data && typeof data !== 'undefined' && !Array.isArray(data))
                    ? (data.totalCount ?? data.totalProducts ?? data.total ?? null)
                    : null;

                hasMorePages = totalCount !== null
                    ? (currentPage * pageSize < totalCount)
                    : (products.length === pageSize);

                renderProducts(products, totalCount);
                renderCatalogPagination(products.length, totalCount);
            })
            .catch(err => {
                productsGrid.style.opacity = '1';
                console.error("Error loading products:", err);
                productsGrid.innerHTML = `<div class="error-state"><p>Unable to load schematics at this time. Please try again later.</p></div>`;
                resultsCount.textContent = '';
                if (paginationContainer) paginationContainer.innerHTML = '';
            });
    }

    /* =========================================================================
       RENDERING & PAGINATION
       ========================================================================= */

    function renderProducts(products, totalCount) {
        productsGrid.innerHTML = '';

        if (!products || products.length === 0) {
            resultsCount.textContent = '0 schematics found';
            productsGrid.innerHTML = `
            <div class="empty-state">
                <h3>No Schematics Found</h3>
                <p>Try adjusting your search terms or sidebar filters.</p>
            </div>`;
            return;
        }

        const startItem = (currentPage - 1) * pageSize + 1;
        const endItem = startItem + products.length - 1;

        if (totalCount !== null && totalCount !== undefined) {
            resultsCount.textContent = `Showing ${startItem}–${endItem} of ${totalCount} schematics`;
        } else {
            resultsCount.textContent = `Showing ${startItem}–${endItem} schematics`;
        }

        const avatarFallback = `${contextPath}/uploads/avatars/default-avatar.png`;

        products.forEach(product => {
            const card = document.createElement('article');
            card.className = 'catalog-card';

            const price = parseFloat(product.price || 0);
            const discountPercent = parseFloat(product.discount || 0);

            let finalPrice = price;
            if (discountPercent > 0) {
                finalPrice = price * (1 - (discountPercent / 100));
            }

            const rawRating = product.averageRating ?? product.average_rating ?? product.rating ?? 0;
            const rating = parseFloat(rawRating).toFixed(1);
            const downloads = formatCompactNumber(product.totalDownloads || 0);

            const bannerMarkup = product.coverImagePath
                ? `<div class="card-banner">
                   <img src="${contextPath}/${product.coverImagePath}" 
                        alt="${escapeHtml(product.productName)}" 
                        loading="lazy">
                   ${discountPercent > 0 ? `<span class="badge-sale">-${Math.round(discountPercent)}%</span>` : ''}
               </div>`
                : `<div class="card-banner dirt-bg">
                   ${discountPercent > 0 ? `<span class="badge-sale">-${Math.round(discountPercent)}%</span>` : ''}
               </div>`;

            const creatorAvatar = (product.creatorAvatar || product.creatorAvatarPath)
                ? `${contextPath}/${product.creatorAvatar || product.creatorAvatarPath}`
                : avatarFallback;

            const creatorName = product.creatorName || product.authorName || 'SchemeCraft Creator';

            let categoriesList = [];
            if (Array.isArray(product.categories)) {
                categoriesList = product.categories.map(c => typeof c === 'object' ? (c.categoryName || c.name || '') : c);
            } else if (Array.isArray(product.categoryNames)) {
                categoriesList = product.categoryNames;
            } else if (typeof product.categories === 'string' && product.categories.trim() !== '') {
                categoriesList = product.categories.split(',').map(s => s.trim());
            } else if (product.categoryName) {
                categoriesList = [product.categoryName];
            }

            let categoriesHtml = categoriesList.length > 0
                ? categoriesList.map(cat => `<span class="tag-pill">${escapeHtml(cat)}</span>`).join('')
                : `<span class="tag-pill">Schematic</span>`;

            card.innerHTML = `
            ${bannerMarkup}
            <div class="card-body">
                <div class="card-title-row">
                    <img class="creator-avatar" 
                         src="${creatorAvatar}" 
                         alt="${escapeHtml(creatorName)}" 
                         onerror="this.onerror=null; this.src='${avatarFallback}';">
                    <div class="title-meta">
                        <h2 class="card-title">${escapeHtml(product.productName)}</h2>
                        <span class="card-author">by ${escapeHtml(creatorName)}</span>
                    </div>
                </div>

                <p class="card-description">${escapeHtml(product.description || 'No description provided for this schematic.')}</p>

                <div class="card-tags">
                    ${categoriesHtml}
                    ${price === 0 ? '<span class="tag-pill tag-free">Free</span>' : ''}
                </div>

                <div class="card-footer">
                    <div class="card-stats">
                        <span class="stat-item" title="Downloads">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
                            ${downloads}
                        </span>
                        <span class="stat-item stat-rating" title="Rating">
                            ★ ${rating}
                        </span>
                    </div>

                    <div class="card-price-action">
                        <div class="card-price">
                            ${discountPercent > 0 ? `<span class="old-price">$${price.toFixed(2)}</span>` : ''}
                            <span class="current-price">${finalPrice === 0 ? 'FREE' : '$' + finalPrice.toFixed(2)}</span>
                        </div>
                        <a href="${contextPath}/product/detail?id=${product.productId}" class="btn-view">View</a>
                        </div>
                </div>
            </div>
        `;

            productsGrid.appendChild(card);
        });
    }

    function renderCatalogPagination(currentResultsCount, totalCount) {
        if (!paginationContainer) return;
        paginationContainer.innerHTML = '';

        if (currentResultsCount === 0 && currentPage === 1) return;

        const btnPrev = document.createElement('button');
        btnPrev.className = 'page-nav-btn';
        btnPrev.innerHTML = '&#10094;';
        btnPrev.disabled = (currentPage === 1);
        btnPrev.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                loadProducts();
                scrollToTop();
            }
        });
        paginationContainer.appendChild(btnPrev);

        const totalPages = totalCount ? Math.ceil(totalCount / pageSize) : null;

        const pagesToShow = [];
        if (currentPage > 1) pagesToShow.push(currentPage - 1);
        pagesToShow.push(currentPage);
        if (totalPages ? currentPage < totalPages : hasMorePages) pagesToShow.push(currentPage + 1);

        pagesToShow.forEach(page => {
            const pageBtn = document.createElement('button');
            pageBtn.className = `page-number ${page === currentPage ? 'active' : ''}`;
            pageBtn.textContent = page;

            if (page !== currentPage) {
                pageBtn.addEventListener('click', () => {
                    currentPage = page;
                    loadProducts();
                    scrollToTop();
                });
            }
            paginationContainer.appendChild(pageBtn);
        });

        const btnNext = document.createElement('button');
        btnNext.className = 'page-nav-btn';
        btnNext.innerHTML = '&#10095;';
        btnNext.disabled = totalPages ? (currentPage >= totalPages) : !hasMorePages;
        btnNext.addEventListener('click', () => {
            if (totalPages ? currentPage < totalPages : hasMorePages) {
                currentPage++;
                loadProducts();
                scrollToTop();
            }
        });
        paginationContainer.appendChild(btnNext);
    }

    function formatCompactNumber(num) {
        if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M';
        if (num >= 1000) return (num / 1000).toFixed(1) + 'k';
        return num.toString();
    }

    function scrollToTop() {
        catalogContainer.scrollIntoView({ behavior: 'smooth' });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, match => {
            const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
            return map[match];
        });
    }
});
