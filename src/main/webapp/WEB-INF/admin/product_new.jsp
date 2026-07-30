<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin - Create Product</title>

  <!-- External Web Fonts -->
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin_header.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-new-product.css">
</head>
<body>
<%@ include file="/WEB-INF/fragments/header_admin.jsp" %>
<main class="admin-container">
  <div class="wizard-wrapper">
    <!-- Intro -->
    <div class="wizard-intro">
      <h1>Craft New Product</h1>
      <p>Fill in the item details, assign categories, and upload files to publish to the store.</p>
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
        <span class="stepper-label">Schematic</span>
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
          <label for="description">Description *</label>
          <textarea id="description" placeholder="Write a description..." required></textarea>
        </div>
      </div>

      <!-- STEP 2: Categories -->
      <div class="wizard-step" id="step-2">
        <div class="step-header">
          <h2>Categories</h2>
          <p>Select one or more store categories to organize this product.</p>
        </div>

        <div class="category-list" id="category-list">
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
        </div>
      </div>

      <!-- STEP 4: Schematic File & Version -->
      <div class="wizard-step" id="step-4">
        <div class="step-header">
          <h2>File & Versioning</h2>
          <p>Attach the primary schematic file and specify version release notes.</p>
        </div>

        <div class="form-group">
          <label>Schematic File (.schematic, .schem, .litematic) *</label>

          <div class="schematic-upload-zone" id="schematic-upload-zone">
            <div class="upload-zone-empty" id="schematic-empty-state">
              <div class="icon-container">
                <img src="${pageContext.request.contextPath}/icons/hopper.webp" alt="Schematic">
              </div>
              <div>
                <strong>Drop your schematic file here</strong>
                <span>.schematic, .schem or .litematic up to 50MB</span>
              </div>
              <label for="schematicFile" class="btn btn-secondary browse-btn">
                Browse Files
              </label>
            </div>

            <div class="upload-zone-selected" id="schematic-selected-state" style="display: none;">
              <div class="selected-file-card">
                <div class="file-info">
                  <img src="${pageContext.request.contextPath}/icons/lapis_lazuli.png" alt="File">
                  <span class="file-name" id="schematic-file-name"></span>
                  <span class="file-size" id="schematic-file-size"></span>
                </div>
                <button type="button" class="remove-file-btn" id="remove-schematic" title="Remove file">
                  <img src="${pageContext.request.contextPath}/icons/barrier.png" alt="Remove">
                </button>
              </div>
            </div>

            <input type="file" id="schematicFile" accept=".schematic,.schem,.litematic" hidden>
          </div>
        </div>

        <div class="form-grid">
          <div class="form-group">
            <label for="version">Version String *</label>
            <input type="text" id="version" placeholder="e.g. 1.0.0" required>
          </div>

          <div class="form-group">
            <label for="minecraftVersion">Minecraft Compatibility</label>
            <input type="text" id="minecraftVersion" placeholder="e.g. 1.20.4">
          </div>
        </div>

        <div class="form-group">
          <label for="changelog">Release Changelog</label>
          <textarea id="changelog" placeholder="Enter release details..."></textarea>
        </div>
      </div>

    </div>

    <!-- Footer Actions -->
    <div class="actions-footer">
      <button type="button" class="btn" id="btn-back">
        Cancel
      </button>
      <button type="button" class="btn btn-primary" id="btn-next">
        Next Step
      </button>
    </div>

  </div>
</main>

<script>
  const CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/js/admin/product_new.js"></script>
</body>
</html>
