<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String productId = (String) request.getAttribute("productId");
  if (productId == null || productId.isEmpty()) {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing product ID.");
    return;
  }
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin - Edit Product</title>

  <!-- External Web Fonts -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

  <!-- Core Styles -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_header.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-new-product.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-product-edit.css">
</head>
<body>
<!-- Toast container for notifications -->
<div id="toast-container" style="position: fixed; top: 80px; right: 20px; z-index: 9999; display: flex; flex-direction: column; gap: 10px;"></div>
<%@ include file="/WEB-INF/fragments/header_admin.jsp" %>
<main class="admin-container">
  <div class="wizard-wrapper">
    <!-- Intro -->
    <div class="wizard-intro">
      <h1>Edit Product</h1>
      <p>Modify product details, manage categories, update gallery, and maintain versions.</p>
    </div>

    <!-- Stepper Indicator -->
    <div class="stepper">
      <div class="stepper-track">
        <div class="stepper-track-progress" id="track-progress"></div>
      </div>

      <div class="stepper-item active" id="step-node-1">
        <div class="stepper-icon">
          <img src="${pageContext.request.contextPath}/icons/book_and_quill.webp" alt="Details">
        </div>
        <span class="stepper-label">Details</span>
      </div>

      <div class="stepper-item" id="step-node-2">
        <div class="stepper-icon">
          <img src="${pageContext.request.contextPath}/icons/chest.png" alt="Categories">
        </div>
        <span class="stepper-label">Categories</span>
      </div>

      <div class="stepper-item" id="step-node-3">
        <div class="stepper-icon">
          <img src="${pageContext.request.contextPath}/icons/painting.webp" alt="Gallery">
        </div>
        <span class="stepper-label">Gallery</span>
      </div>

      <div class="stepper-item" id="step-node-4">
        <div class="stepper-icon">
          <img src="${pageContext.request.contextPath}/icons/lapis_lazuli.png" alt="Schematic">
        </div>
        <span class="stepper-label">Versions</span>
      </div>
    </div>

    <!-- Main Card Container -->
    <div class="card-container">

      <!-- STEP 1: Basic Details -->
      <div class="wizard-step active" id="step-1">
        <div class="step-header">
          <h2>Basic Information</h2>
          <p>Enter general product properties and pricing options.</p>
        </div>

        <div class="form-group">
          <label for="productName">Product Name *</label>
          <input type="text" id="productName" placeholder="Product name..." required>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="price">Price *</label>
            <input type="number" id="price" step="0.01" min="0" placeholder="0.00" required>
          </div>

          <div class="form-group">
            <label for="discount">Discount (%)</label>
            <input type="number" id="discount" step="1" min="0" max="100" placeholder="0">
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="currencyId">Currency *</label>
            <select id="currencyId" required>
              <option value="" disabled selected>Select currency...</option>
              <!-- Dynamically loaded from DB -->
            </select>
          </div>

          <div class="form-group">
            <label for="stockQuantity">Stock Quantity</label>
            <input type="number" id="stockQuantity" min="0" disabled>
            <label class="toggle-wrapper">
                            <span class="switch">
                                <input type="checkbox" id="unlimitedStock" checked>
                                <span class="slider"></span>
                            </span>
              <span>Unlimited Digital Downloads</span>
            </label>
          </div>
        </div>

        <div class="form-group">
          <label for="description">Description</label>
          <textarea id="description" placeholder="Write a description..."></textarea>
        </div>
      </div>

      <!-- STEP 2: Categories -->
      <div class="wizard-step" id="step-2">
        <div class="step-header">
          <h2>Categories</h2>
          <p>Select one or more store categories to organize this product.</p>
        </div>

        <div class="category-list" id="category-list">
          <!-- Dynamically loaded from DB -->
        </div>
      </div>

      <!-- STEP 3: Images -->
      <div class="wizard-step" id="step-3">
        <div class="step-header">
          <h2>Media Gallery</h2>
          <p>Upload screenshots and previews. The first image will serve as primary cover.</p>
        </div>

        <div class="upload-zone" id="upload-zone">
          <div class="icon-container">
            <img src="${pageContext.request.contextPath}/icons/hopper.webp" alt="Upload">
          </div>
          <div>
            <strong>Drop your images here</strong>
            <span>PNG, JPG or WEBP up to 50MB</span>
          </div>
          <input type="file" id="image-input" multiple accept="image/png, image/jpeg, image/webp" hidden>
        </div>

        <div class="gallery-grid" id="gallery-grid">
          <!-- Image previews rendered by JavaScript -->
        </div>
      </div>

      <!-- STEP 4: Versions Management -->
      <div class="wizard-step" id="step-4">
        <div class="step-header">
          <h2>Versions & Files</h2>
          <p>Manage all versions of this product. Add, edit, or remove existing versions.</p>
        </div>

        <div class="versions-section">
          <div class="versions-section__title">
            <span>Product Versions</span>
            <span class="badge-count" id="version-count-badge">0</span>
          </div>

          <!-- Container for version list -->
          <div id="versions-list">
            <!-- Rendered by JavaScript -->
          </div>

          <!-- Form for new version (shown when clicking add button) -->
          <div class="new-version-form" id="new-version-form">
            <h4 style="margin: 0 0 0.75rem 0; font-weight: var(--weight-medium);">New Version</h4>
            <div class="form-grid">
              <div class="form-group">
                <label for="new-version-string">Version *</label>
                <input type="text" id="new-version-string" placeholder="e.g. 1.0.0">
              </div>
              <div class="form-group">
                <label for="new-minecraft-version">Minecraft Version</label>
                <input type="text" id="new-minecraft-version" placeholder="e.g. 1.20.4">
              </div>
              <div class="form-group full-width">
                <label for="new-changelog">Changelog</label>
                <textarea id="new-changelog" placeholder="Release notes..." rows="2"></textarea>
              </div>
              <div class="form-group full-width">
                <label for="new-schematic-file">Schematic File *</label>
                <div class="schematic-upload-zone" id="new-schematic-zone">
                  <span style="color: var(--text-secondary);">Drop file or</span>
                  <button type="button" class="btn btn-secondary" style="padding: 0.3rem 1rem; font-size: 0.8rem;">Browse</button>
                  <input type="file" id="new-schematic-input" accept=".schematic,.schem,.litematic" hidden>
                </div>
                <div class="schematic-file-info" id="new-schematic-file-info">
                  <span class="file-name" id="new-schematic-file-name"></span>
                  <span class="file-size" id="new-schematic-file-size"></span>
                </div>
              </div>
            </div>
            <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
              <button type="button" class="btn btn-primary" id="new-version-submit">Add Version</button>
              <button type="button" class="btn" id="new-version-cancel">Cancel</button>
            </div>
          </div>

          <!-- Button to add new version -->
          <button type="button" class="btn btn--success" id="add-version-btn" style="margin-top: 0.75rem;">
            + Add New Version
          </button>
        </div>
      </div>
    </div>

    <!-- Footer Actions -->
    <div class="actions-footer">
      <button type="button" class="btn" id="btn-back">Cancel</button>
      <button type="button" class="btn btn--success" id="btn-update">Update Product</button>
      <button type="button" class="btn btn-primary" id="btn-next">Next Step</button>
    </div>

    <!-- Hidden field for product ID -->
    <input type="hidden" id="edit-product-id" value="<%= productId %>">
  </div>
</main>

<!-- Custom confirmation modal (replaces native browser confirm()) -->
<div id="confirm-modal" class="confirm-modal" hidden>
  <div id="confirm-modal-backdrop" class="confirm-modal__backdrop"></div>
  <div class="confirm-modal__box" role="alertdialog" aria-modal="true" aria-labelledby="confirm-modal-title">
    <p id="confirm-modal-title" class="confirm-modal__title"></p>
    <div class="confirm-modal__actions">
      <button type="button" class="btn" id="confirm-modal-cancel">Cancel</button>
      <button type="button" class="btn btn--danger" id="confirm-modal-confirm">Confirm</button>
    </div>
  </div>
</div>

<script>
  // Global configuration for JavaScript
  // These variables are used by product_edit.js
  var CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/admin/product_edit.js"></script>
</body>
</html>