<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Manage Categories | Admin</title>

  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_header.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-categories.css">
</head>
<body>

<jsp:include page="/WEB-INF/fragments/header_admin.jsp" />

<div class="admin-container">
  <div class="categories-card">
    <div class="card-header">
      <h1 class="card-title">Manage Categories</h1>
      <p class="card-subtitle">Organize product categories in a tree structure</p>
    </div>

    <div class="card-toolbar">
      <button class="btn btn-add-category" id="btn-add-root-category">
        <span>New Root Category</span>
      </button>
    </div>

    <div class="categories-tree" id="categories-tree">
      <div class="tree-loading">Loading categories...</div>
    </div>
  </div>
</div>

<!-- Modal: Create / Edit Category -->
<div class="modal-overlay" id="modal-category" style="display: none;">
  <div class="modal-container">
    <div class="modal-header">
      <h2 class="modal-title" id="modal-title">New Category</h2>
      <button class="modal-close" id="modal-close" aria-label="Close modal">&times;</button>
    </div>

    <form class="modal-body" id="form-category">
      <input type="hidden" id="input-category-id" value="">
      <input type="hidden" id="input-parent-id" value="">

      <!-- Fixed parent (for new subcategory) -->
      <div class="form-field" id="parent-fixed-field" style="display: none;">
        <label class="form-label">Parent Category</label>
        <div class="parent-fixed-value" id="parent-fixed-name"></div>
      </div>

      <div class="form-field">
        <label for="input-category-name" class="form-label">Category Name *</label>
        <input type="text" id="input-category-name" class="form-input"
               placeholder="Category name" maxlength="100" required>
      </div>

      <div class="form-field">

        <label
                class="form-label"
                for="input-category-description">
          Description
        </label>

        <textarea
                id="input-category-description"
                class="form-textarea"
                maxlength="255"
                rows="4"
                placeholder="Short description (max 255 characters)">
    </textarea>

      </div>

      <div class="modal-actions">
        <button type="button" class="btn btn-cancel" id="btn-cancel">Cancel</button>
        <button type="submit" class="btn btn-save" id="btn-save">Save Category</button>
      </div>
    </form>
  </div>
</div>

<!-- Modal: Delete Confirmation -->
<div class="modal-overlay" id="modal-delete-confirm" style="display: none;">
  <div class="modal-container modal-sm">
    <div class="modal-header">
      <h2 class="modal-title">Confirm Delete</h2>
      <button class="modal-close" id="modal-delete-close" aria-label="Close modal">&times;</button>
    </div>
    <div class="modal-body">
      <p class="delete-warning" id="delete-warning-text">Are you sure you want to delete this category?</p>
      <p class="delete-subtext" id="delete-subtext">This action cannot be undone.</p>
      <div class="modal-actions">
        <button type="button" class="btn btn-cancel" id="btn-delete-cancel">Cancel</button>
        <button type="button" class="btn btn-danger" id="btn-delete-confirm">Delete</button>
      </div>
    </div>
  </div>
</div>

<script>
  window.CONTEXT_PATH = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/js/admin/products-categories.js"></script>
</body>
</html>