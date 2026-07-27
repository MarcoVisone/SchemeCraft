const CONTEXT_PATH = window.location.pathname.substring(0, window.location.pathname.indexOf('/admin'));

document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
});

function loadProducts() {
    const keyword = document.getElementById('searchKeyword').value;
    let url = `${CONTEXT_PATH}/admin/products/list`;
    if (keyword) {
        url += `?keywords=${encodeURIComponent(keyword)}`;
    }

    fetch(url)
        .then(res => res.json())
        .then(response => {
            if (response.status === 'success') {
                renderProductsTable(response.data.products);
            } else {
                alert('Error loading products: ' + response.message);
            }
        })
        .catch(err => console.error('Error:', err));
}

function renderProductsTable(products) {
    const tbody = document.getElementById('productsTableBody');
    tbody.innerHTML = '';

    if (!products || products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-secondary);">No products found.</td></tr>';
        return;
    }

    products.forEach(p => {
        const isUnlimited = p.unlimitedStock;
        const stockDisplay = isUnlimited ? 'Unlimited' : p.stockQuantity;
        const isActive = p.active;

        let badgeClass = isActive ? 'badge-active' : 'badge-danger';
        let statusText = isActive ? 'Active' : 'Disabled';

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><code>${p.productId}</code></td>
            <td><strong>${p.productName}</strong></td>
            <td>€ ${p.price ? p.price.toFixed(2) : '0.00'}</td>
            <td>${stockDisplay}</td>
            <td><span class="badge ${badgeClass}">${statusText}</span></td>
            <td>
                ${isActive
            ? `<button class="btn btn-danger btn-sm" onclick="toggleProductState('${p.productId}', false)">Deactivate</button>`
            : `<button class="btn btn-green btn-sm" onclick="toggleProductState('${p.productId}', true)">Activate</button>`
        }
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function toggleProductState(productId, activate) {
    const endpoint = activate ? '/admin/products/activate' : '/admin/products/delete';
    const params = new URLSearchParams();
    params.append('productId', productId);

    fetch(`${CONTEXT_PATH}${endpoint}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                loadProducts();
            } else {
                alert('Operation failed: ' + data.message);
            }
        });
}

function saveProduct(event) {
    event.preventDefault();
    const form = document.getElementById('productForm');
    const params = new URLSearchParams(new FormData(form));

    fetch(`${CONTEXT_PATH}/admin/products/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                closeModal('productModal');
                form.reset();
                loadProducts();
            } else {
                alert('Error saving product: ' + data.message);
            }
        });
}

function saveCategory(event) {
    event.preventDefault();
    const form = document.getElementById('categoryForm');
    const params = new URLSearchParams(new FormData(form));

    fetch(`${CONTEXT_PATH}/admin/categories/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === 'success') {
                closeModal('categoryModal');
                form.reset();
                alert('Category created successfully!');
            } else {
                alert('Error creating category: ' + data.message);
            }
        });
}

function openProductModal() { document.getElementById('productModal').classList.add('active'); }
function openCategoryModal() { document.getElementById('categoryModal').classList.add('active'); }
function closeModal(id) { document.getElementById(id).classList.remove('active'); }