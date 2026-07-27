<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft Admin - Product & Category Management</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">

    <script src="${pageContext.request.contextPath}/js/header.js"></script>
</head>
<body>

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="admin-container">
    <header class="admin-header">
        <h1 class="admin-title">Admin Dashboard</h1>
        <nav class="admin-nav">
            <a href="${pageContext.request.contextPath}/admin/products" class="active">Products & Categories</a>
            <a href="${pageContext.request.contextPath}/admin/orders">Orders</a>
            <a href="${pageContext.request.contextPath}/admin/users">Users</a>
        </nav>
    </header>

    <section class="toolbar-card">
        <div class="search-group">
            <input type="text" id="searchKeyword" placeholder="Search product by name or ID...">
            <button class="btn btn-secondary" onclick="loadProducts()">Search</button>
        </div>
        <div>
            <button class="btn btn-green" onclick="openProductModal()">+ New Product</button>
            <button class="btn btn-secondary" onclick="openCategoryModal()">+ New Category</button>
        </div>
    </section>

    <section class="table-container">
        <table class="data-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Product Name</th>
                <th>Price</th>
                <th>Stock</th>
                <th>Status</th>
                <th>Actions</th>
            </tr>
            </thead>
            <tbody id="productsTableBody">
            <tr>
                <td colspan="6" style="text-align: center; color: var(--text-secondary);">Loading products...</td>
            </tr>
            </tbody>
        </table>
    </section>
</main>

<!-- Create/Edit Product Modal -->
<div class="modal-overlay" id="productModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3 id="productModalTitle">New Product</h3>
            <button class="btn btn-secondary btn-sm" onclick="closeModal('productModal')">✕</button>
        </div>
        <form id="productForm" onsubmit="saveProduct(event)">
            <input type="hidden" id="prodId" name="productId">
            <div class="form-group">
                <label for="prodName">Product Name</label>
                <input type="text" id="prodName" name="productName" required>
            </div>
            <div class="form-group">
                <label for="prodCurrency">Currency ID</label>
                <input type="text" id="prodCurrency" name="currencyId" value="EUR" required>
            </div>
            <div class="form-group">
                <label for="prodPrice">Base Price (€)</label>
                <input type="number" step="0.01" id="prodPrice" name="price" required>
            </div>
            <div class="form-group">
                <label for="prodDiscount">Discount (%)</label>
                <input type="number" step="0.01" id="prodDiscount" name="discount" value="0">
            </div>
            <div class="form-group">
                <label for="prodStock">Stock Quantity</label>
                <input type="number" id="prodStock" name="stockQuantity" value="0">
            </div>
            <div class="form-group">
                <label>
                    <input type="checkbox" id="prodUnlimited" name="unlimitedStock" value="true"> Unlimited Stock
                </label>
            </div>
            <div class="form-group">
                <label for="prodDesc">Description</label>
                <textarea id="prodDesc" name="description" rows="3"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('productModal')">Cancel</button>
                <button type="submit" class="btn btn-green">Save Product</button>
            </div>
        </form>
    </div>
</div>

<!-- Category Modal -->
<div class="modal-overlay" id="categoryModal">
    <div class="modal-content">
        <div class="modal-header">
            <h3>New Category</h3>
            <button class="btn btn-secondary btn-sm" onclick="closeModal('categoryModal')">✕</button>
        </div>
        <form id="categoryForm" onsubmit="saveCategory(event)">
            <div class="form-group">
                <label for="catName">Category Name</label>
                <input type="text" id="catName" name="categoryName" required>
            </div>
            <div class="form-group">
                <label for="catParent">Parent Category ID (Optional)</label>
                <input type="text" id="catParent" name="parentCategoryId">
            </div>
            <div class="form-group">
                <label for="catDesc">Description</label>
                <textarea id="catDesc" name="description" rows="2"></textarea>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal('categoryModal')">Cancel</button>
                <button type="submit" class="btn btn-green">Save Category</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

<script src="${pageContext.request.contextPath}/js/admin/products.js"></script>
</body>
</html>