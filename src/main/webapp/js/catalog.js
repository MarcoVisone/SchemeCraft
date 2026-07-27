document.addEventListener('DOMContentLoaded', () => {
    const catalogContainer = document.querySelector('.catalog-container');
    const contextPath = catalogContainer ? catalogContainer.dataset.contextPath : '';

    const filterForm = document.getElementById('catalogFilterForm');
    const productsGrid = document.getElementById('productsGrid');
    const resultsCount = document.getElementById('resultsCount');

    const btnPrevPage = document.getElementById('btnPrevPage');
    const btnNextPage = document.getElementById('btnNextPage');
    const currentPageDisplay = document.getElementById('currentPageDisplay');
    const paginationContainer = document.getElementById('pagination');
    const btnResetFilters = document.getElementById('btnResetFilters');

    let currentPage = 1;
    const pageSize = 20;
    let debounceTimer = null;

    loadProducts();

    /* =========================================================================
       EVENT LISTENERS
       ========================================================================= */

    filterForm.querySelectorAll('input[type="text"], input[type="number"]').forEach(input => {
        input.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentPage = 1;
                loadProducts();
            }, 350);
        });
    });

    filterForm.querySelectorAll('select, input[type="checkbox"]').forEach(element => {
        element.addEventListener('change', () => {
            currentPage = 1;
            loadProducts();
        });
    });

    if (btnResetFilters) {
        btnResetFilters.addEventListener('click', () => {
            filterForm.reset();
            currentPage = 1;
            loadProducts();
        });
    }

    if (btnPrevPage) {
        btnPrevPage.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                loadProducts();
                scrollToTop();
            }
        });
    }

    if (btnNextPage) {
        btnNextPage.addEventListener('click', () => {
            currentPage++;
            loadProducts();
            scrollToTop();
        });
    }

    /* =========================================================================
       AJAX FETCH
       ========================================================================= */

    function loadProducts() {
        const params = new URLSearchParams();

        const keywords = document.getElementById('keywords').value.trim();
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

        if (categoryId && categoryId !== "") {
            params.append('categoryId', categoryId);
        }

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

        productsGrid.style.opacity = '0.5';

        fetch(`${contextPath}/product/search?${params.toString()}`)
            .then(response => {
                if (!response.ok) throw new Error("Search request failed");
                return response.json();
            })
            .then(data => {
                productsGrid.style.opacity = '1';
                const products = Array.isArray(data) ? data : (data.products || []);
                renderProducts(products);
            })
            .catch(err => {
                productsGrid.style.opacity = '1';
                console.error("Error loading products:", err);
                productsGrid.innerHTML = `<div class="error-state"><p>Unable to load schematics at this time. Please try again later.</p></div>`;
                resultsCount.textContent = '';
                paginationContainer.classList.add('hidden');
            });
    }

    /* =========================================================================
       RENDERING CARD
       ========================================================================= */

    function renderProducts(products) {
        productsGrid.innerHTML = '';

        if (!products || products.length === 0) {
            resultsCount.textContent = '0 schematics found';
            productsGrid.innerHTML = `
                <div class="empty-state">
                    <h3>No Schematics Found</h3>
                    <p>Try adjusting your filters or search terms.</p>
                </div>`;
            paginationContainer.classList.add('hidden');
            return;
        }

        resultsCount.textContent = `Showing ${products.length} schematic(s)`;

        paginationContainer.classList.remove('hidden');
        currentPageDisplay.textContent = `Page ${currentPage}`;
        btnPrevPage.disabled = (currentPage === 1);
        btnNextPage.disabled = (products.length < pageSize);

        products.forEach(product => {
            const card = document.createElement('article');
            card.className = 'product-card';

            const price = parseFloat(product.price || 0);
            const discountPercent = parseFloat(product.discount || 0);

            let finalPrice = price;
            if (discountPercent > 0) {
                finalPrice = price * (1 - (discountPercent / 100));
            }

            const rating = parseFloat(product.averageRating || product.rating || 0).toFixed(1);

            const imagePath = product.coverImagePath
                ? `${contextPath}/${product.coverImagePath}`
                : `${contextPath}/images/default-schematic.png`;

            card.innerHTML = `
                <div class="product-image-wrap">
                    <img src="${imagePath}" alt="${escapeHtml(product.productName)}" class="product-image" loading="lazy">
                    ${discountPercent > 0 ? `<span class="discount-badge">-${Math.round(discountPercent)}%</span>` : ''}
                </div>
                <div class="product-info">
                    <div class="product-header-row">
                        <h2 class="product-name">${escapeHtml(product.productName)}</h2>
                        <div class="product-rating">★ <span>${rating}</span></div>
                    </div>
                    <p class="product-description">${escapeHtml(product.description || 'No description available.')}</p>
                    <div class="product-footer">
                        <div class="product-price-box">
                            ${discountPercent > 0 ? `<span class="original-price">$${price.toFixed(2)}</span>` : ''}
                            <span class="final-price">$${finalPrice.toFixed(2)}</span>
                        </div>
                        <a href="${contextPath}/product/detail?id=${product.productId}" class="btn-view-product">View Schematic</a>
                    </div>
                </div>
            `;

            productsGrid.appendChild(card);
        });
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
