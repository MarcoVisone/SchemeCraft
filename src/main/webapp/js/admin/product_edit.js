/**
 * Admin Edit Product Wizard — Client-side logic
 * Handles loading existing product data, step navigation, category tree,
 * image gallery management, and version management (add/edit/delete).
 * Submits full update to /admin/products/update-full.
 */

(function () {
    'use strict';

    /* ----------------------------------------------------------------------
       Constants & Config
       ---------------------------------------------------------------------- */
    const CTX = typeof CONTEXT_PATH !== 'undefined' ? CONTEXT_PATH : '';
    const API_ADMIN = CTX + '/admin';
    const TOTAL_STEPS = 4;

    // Get product ID from DOM
    const productIdEl = document.getElementById('edit-product-id');
    const PRODUCT_ID = productIdEl ? productIdEl.value : null;

    if (!PRODUCT_ID) {
        console.error('Product ID is required for edit mode.');
        return;
    }

    /* ----------------------------------------------------------------------
       State
       ---------------------------------------------------------------------- */
    let currentStep = 1;
    let allCategories = [];
    let selectedCategoryIds = new Set();

    // Images: existing + new
    let imageItems = []; // { file: File|null, previewUrl: string, isExisting: boolean, path: string|null }

    // Versions: existing + new
    let versions = []; // { versionId: string|null, version: string, minecraftVersion: string, filePath: string, changelog: string, isNew: boolean }

    // New version form state
    let newVersionFile = null; // File object for new version schematic
    let editingVersionIndex = null; // Index of version being edited

    /* ----------------------------------------------------------------------
       DOM References
       ---------------------------------------------------------------------- */
    const els = {
        // Stepper
        trackProgress: document.getElementById('track-progress'),
        stepNodes: [1, 2, 3, 4].map(n => document.getElementById(`step-node-${n}`)),
        stepPanels: [1, 2, 3, 4].map(n => document.getElementById(`step-${n}`)),

        // Step 1 — Details
        productName: document.getElementById('productName'),
        price: document.getElementById('price'),
        discount: document.getElementById('discount'),
        currencyId: document.getElementById('currencyId'),
        stockQuantity: document.getElementById('stockQuantity'),
        unlimitedStock: document.getElementById('unlimitedStock'),
        description: document.getElementById('description'),

        // Step 2 — Categories
        categoryList: document.getElementById('category-list'),

        // Step 3 — Gallery
        uploadZone: document.getElementById('upload-zone'),
        imageInput: document.getElementById('image-input'),
        galleryGrid: document.getElementById('gallery-grid'),

        // Step 4 — Versions
        versionsList: document.getElementById('versions-list'),
        versionCountBadge: document.getElementById('version-count-badge'),
        addVersionBtn: document.getElementById('add-version-btn'),
        newVersionForm: document.getElementById('new-version-form'),
        newVersionString: document.getElementById('new-version-string'),
        newMinecraftVersion: document.getElementById('new-minecraft-version'),
        newChangelog: document.getElementById('new-changelog'),
        newSchematicZone: document.getElementById('new-schematic-zone'),
        newSchematicInput: document.getElementById('new-schematic-input'),
        newSchematicFileInfo: document.getElementById('new-schematic-file-info'),
        newSchematicFileName: document.getElementById('new-schematic-file-name'),
        newSchematicFileSize: document.getElementById('new-schematic-file-size'),
        newVersionSubmit: document.getElementById('new-version-submit'),
        newVersionCancel: document.getElementById('new-version-cancel'),
        newVersionFormTitle: document.querySelector('#new-version-form h4'),

        // Custom confirmation modal
        confirmModal: document.getElementById('confirm-modal'),
        confirmModalTitle: document.getElementById('confirm-modal-title'),
        confirmModalConfirm: document.getElementById('confirm-modal-confirm'),
        confirmModalCancel: document.getElementById('confirm-modal-cancel'),
        confirmModalBackdrop: document.getElementById('confirm-modal-backdrop'),

        // Footer
        btnBack: document.getElementById('btn-back'),
        btnUpdate: document.getElementById('btn-update'),
        btnNext: document.getElementById('btn-next'),
    };

    // Re-parent the modal directly under <body>. If any ancestor element elsewhere
    // on the page has a CSS transform/filter/perspective set, it becomes the containing
    // block for descendant "position: fixed" elements, which breaks full-viewport centering.
    // Moving the modal to <body> guarantees it always centers against the viewport.
    if (els.confirmModal && els.confirmModal.parentElement !== document.body) {
        document.body.appendChild(els.confirmModal);
    }

    /* ----------------------------------------------------------------------
       Utility Functions
       ---------------------------------------------------------------------- */
    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const div = document.createElement('div');
        div.textContent = String(text);
        return div.innerHTML;
    }

    function formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function truncate(str, maxLen) {
        if (!str) return '';
        return str.length > maxLen ? str.substring(0, maxLen) + '…' : str;
    }

    /* ----------------------------------------------------------------------
       Toast Notifications (replaces native browser alert())
       ---------------------------------------------------------------------- */
    const toastContainer = document.getElementById('toast-container');

    function showToast(message, type) {
        if (!toastContainer) {
            // Fallback only if the container is missing from the page.
            alert(message);
            return;
        }

        const toast = document.createElement('div');
        toast.className = 'toast toast--' + (type || 'error');
        toast.textContent = message;
        toastContainer.appendChild(toast);

        // Auto-dismiss after a few seconds.
        setTimeout(function () {
            toast.classList.add('toast--hide');
            setTimeout(function () {
                toast.remove();
            }, 300);
        }, 4000);
    }

    /* ----------------------------------------------------------------------
       Custom Confirmation Modal
       ---------------------------------------------------------------------- */
    function showConfirm(title) {
        return new Promise((resolve) => {
            if (!els.confirmModal) {
                resolve(confirm(title));
                return;
            }

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
       Stepper Navigation
       ---------------------------------------------------------------------- */
    function goToStep(stepNumber) {
        if (stepNumber < 1 || stepNumber > TOTAL_STEPS) return;

        currentStep = stepNumber;

        els.stepPanels.forEach((panel, idx) => {
            panel.classList.toggle('active', idx + 1 === stepNumber);
        });

        els.stepNodes.forEach((node, idx) => {
            const nodeStepNumber = idx + 1;
            node.classList.toggle('active', nodeStepNumber === stepNumber);
            node.classList.toggle('completed', nodeStepNumber < stepNumber);
        });

        const progressPercent = ((stepNumber - 1) / (TOTAL_STEPS - 1)) * 100;
        els.trackProgress.style.width = progressPercent + '%';

        els.btnBack.textContent = stepNumber === 1 ? 'Cancel' : 'Back';
        updateNextButtonLabel();
    }

    function updateNextButtonLabel() {
        // btnNext only navigates forward through the wizard steps; it never submits.
        // It's disabled on the last step since there's nowhere further to go
        // (Update Product, a separate always-visible button, handles submission).
        els.btnNext.textContent = 'Next Step';
        els.btnNext.disabled = currentStep >= TOTAL_STEPS;
    }

    // Event listeners for stepper
    els.btnBack.addEventListener('click', function () {
        if (currentStep === 1) {
            window.location.href = API_ADMIN + '/products';
        } else {
            goToStep(currentStep - 1);
        }
    });

    els.btnNext.addEventListener('click', function () {
        if (currentStep < TOTAL_STEPS) {
            goToStep(currentStep + 1);
        }
    });

    // Update Product is always visible and clickable regardless of the current step,
    // so admins can save changes without having to walk through every step first.
    els.btnUpdate.addEventListener('click', function () {
        submitProduct();
    });

    els.stepNodes.forEach((node, idx) => {
        node.addEventListener('click', function () {
            goToStep(idx + 1);
        });
        node.style.cursor = 'pointer';
    });

    /* ----------------------------------------------------------------------
       Step 1 — Details: Unlimited Stock Toggle
       ---------------------------------------------------------------------- */
    els.unlimitedStock.addEventListener('change', function () {
        els.stockQuantity.disabled = els.unlimitedStock.checked;
        if (els.unlimitedStock.checked) {
            els.stockQuantity.value = '';
        }
        updateNextButtonLabel();
    });

    // Input listeners for form completeness
    [els.productName, els.price, els.discount, els.currencyId, els.stockQuantity,
        els.description].forEach(input => {
        input.addEventListener('input', updateNextButtonLabel);
        input.addEventListener('change', updateNextButtonLabel);
    });

    /* ----------------------------------------------------------------------
       Step 2 — Categories: Tree Loading & Rendering
       ---------------------------------------------------------------------- */
    async function loadCategories() {
        els.categoryList.innerHTML = '<p class="category-list__loading">Loading categories...</p>';

        try {
            const response = await fetch(API_ADMIN + '/categories/list', {
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

            allCategories = json.categories || [];
            renderCategoryTree();
        } catch (err) {
            console.error('Failed to load categories:', err);
            els.categoryList.innerHTML = '<p class="category-list__error">Failed to load categories. Please retry.</p>';
        }
    }

    function renderCategoryTree() {
        const roots = allCategories.filter(c => !c.parentCategoryId);
        if (roots.length === 0) {
            els.categoryList.innerHTML = '<p class="category-list__empty">No categories available.</p>';
            return;
        }
        els.categoryList.innerHTML = roots.map(cat => buildCategoryNodeHtml(cat)).join('');

        // After rendering, apply selected state
        document.querySelectorAll('.category-checkbox').forEach(cb => {
            const catId = cb.dataset.categoryId;
            if (selectedCategoryIds.has(catId)) {
                cb.checked = true;
            }
        });
    }

    function buildCategoryNodeHtml(category) {
        const children = allCategories.filter(c => c.parentCategoryId === category.categoryId);
        const checked = selectedCategoryIds.has(category.categoryId) ? 'checked' : '';

        const childrenHtml = children.length > 0
            ? `<div class="category-children">${children.map(c => buildCategoryNodeHtml(c)).join('')}</div>`
            : '';

        return `
            <div class="category-card">
                <label class="category-info">
                    <input type="checkbox" class="category-checkbox" data-category-id="${escapeHtml(category.categoryId)}" ${checked} />
                    <span>${escapeHtml(category.categoryName)}</span>
                </label>
            </div>
            ${childrenHtml}
        `;
    }

    els.categoryList.addEventListener('change', function (e) {
        const checkbox = e.target.closest('.category-checkbox');
        if (!checkbox) return;

        const categoryId = checkbox.dataset.categoryId;
        if (checkbox.checked) {
            selectedCategoryIds.add(categoryId);
        } else {
            selectedCategoryIds.delete(categoryId);
        }
        updateNextButtonLabel();
    });

    /* ----------------------------------------------------------------------
       Step 3 — Gallery: Image Selection & Preview
       ---------------------------------------------------------------------- */
    els.uploadZone.addEventListener('click', function () {
        els.imageInput.click();
    });

    els.uploadZone.addEventListener('dragover', function (e) {
        e.preventDefault();
        els.uploadZone.classList.add('dragover');
    });

    els.uploadZone.addEventListener('dragleave', function () {
        els.uploadZone.classList.remove('dragover');
    });

    els.uploadZone.addEventListener('drop', function (e) {
        e.preventDefault();
        els.uploadZone.classList.remove('dragover');
        addImageFiles(e.dataTransfer.files);
    });

    els.imageInput.addEventListener('change', function () {
        addImageFiles(els.imageInput.files);
        els.imageInput.value = '';
    });

    function addImageFiles(fileList) {
        for (const file of fileList) {
            if (!file.type.startsWith('image/')) continue;
            const isDuplicate = imageItems.some(
                item => item.file && item.file.name === file.name && item.file.size === file.size
            );
            if (isDuplicate) continue;
            imageItems.push({
                file: file,
                previewUrl: URL.createObjectURL(file),
                isExisting: false,
                path: null,
            });
        }
        renderGallery();
        updateNextButtonLabel();
    }

    function removeImageItem(index) {
        const removed = imageItems.splice(index, 1);
        if (removed[0] && removed[0].previewUrl && removed[0].previewUrl.startsWith('blob:')) {
            URL.revokeObjectURL(removed[0].previewUrl);
        }
        renderGallery();
        updateNextButtonLabel();
    }

    function renderGallery() {
        if (imageItems.length === 0) {
            els.galleryGrid.innerHTML = '';
            return;
        }

        els.galleryGrid.innerHTML = imageItems.map((item, idx) => `
            <div class="gallery-item">
                <img src="${item.previewUrl}" alt="Preview ${idx + 1}" />
                ${idx === 0 ? '<span class="gallery-badge">Cover</span>' : ''}
                <button type="button" class="gallery-delete" data-index="${idx}" title="Remove image">
                    <img src="${CTX}/icons/barrier.png" alt="Remove" width="16" height="16">
                </button>
            </div>
        `).join('');
    }

    els.galleryGrid.addEventListener('click', function (e) {
        const btn = e.target.closest('.gallery-delete');
        if (!btn) return;
        removeImageItem(parseInt(btn.dataset.index, 10));
    });

    /* ----------------------------------------------------------------------
       Step 4 — Version Management
       ---------------------------------------------------------------------- */

    // Render versions list
    function renderVersionsList() {
        if (!els.versionsList) return;

        if (versions.length === 0) {
            els.versionsList.innerHTML = '<p class="versions-empty">No versions yet. Add one below.</p>';
            els.versionCountBadge.textContent = '0';
            return;
        }

        els.versionsList.innerHTML = versions.map((v, index) => `
            <div class="version-card" data-index="${index}">
                <div class="version-card__header">
                    <span class="version-card__version">${escapeHtml(v.version)}</span>
                    ${v.minecraftVersion ? `<span class="version-card__mc">MC: ${escapeHtml(v.minecraftVersion)}</span>` : ''}
                    ${v.isNew ? '<span class="version-card__badge">New</span>' : `<span class="version-card__badge">ID: ${escapeHtml(truncate(v.versionId, 10))}</span>`}
                </div>
                ${v.changelog ? `<div class="version-card__changelog">${escapeHtml(truncate(v.changelog, 80))}</div>` : ''}
                <div class="version-card__actions">
                    <button class="btn btn-secondary" data-action="edit-version" data-index="${index}" style="padding: 0.25rem 0.75rem; font-size: 0.8rem;">Edit</button>
                    <button class="btn btn--danger" data-action="delete-version" data-index="${index}" style="padding: 0.25rem 0.75rem; font-size: 0.8rem;">Remove</button>
                </div>
            </div>
        `).join('');
    }

    // Version action delegation (no inline onclick)
    els.versionsList.addEventListener('click', function (e) {
        const editBtn = e.target.closest('[data-action="edit-version"]');
        if (editBtn) {
            const index = parseInt(editBtn.dataset.index, 10);
            openEditVersionForm(index);
            return;
        }

        const deleteBtn = e.target.closest('[data-action="delete-version"]');
        if (deleteBtn) {
            const index = parseInt(deleteBtn.dataset.index, 10);
            handleDeleteVersion(index);
            return;
        }
    });

    // Open edit version form
    function openEditVersionForm(index) {
        const v = versions[index];
        if (!v) return;

        editingVersionIndex = index;

        // Pre-populate the form with version data
        els.newVersionString.value = v.version;
        els.newMinecraftVersion.value = v.minecraftVersion || '';
        els.newChangelog.value = v.changelog || '';

        // Show the form with "Edit Version" title
        if (els.newVersionFormTitle) {
            els.newVersionFormTitle.textContent = 'Edit Version';
        }

        // Clear any existing file
        newVersionFile = null;
        els.newSchematicFileInfo.classList.remove('visible');
        els.newSchematicFileName.textContent = '';
        els.newSchematicFileSize.textContent = '';

        // Change submit button text
        els.newVersionSubmit.textContent = 'Update Version';

        els.newVersionForm.classList.add('visible');
        els.newVersionString.focus();
    }

    // Handle delete version with custom confirmation
    async function handleDeleteVersion(index) {
        const v = versions[index];
        if (!v) return;

        const confirmed = await showConfirm(`Are you sure you want to delete version "${v.version}"?`);
        if (!confirmed) return;

        versions.splice(index, 1);
        renderVersionsList();
        updateNextButtonLabel();

        // Reset editing state if we deleted the version being edited
        if (editingVersionIndex === index) {
            editingVersionIndex = null;
            resetNewVersionForm();
            els.newVersionForm.classList.remove('visible');
            if (els.newVersionFormTitle) {
                els.newVersionFormTitle.textContent = 'New Version';
            }
            els.newVersionSubmit.textContent = 'Add Version';
        }
    }

    // Add new version - show form
    els.addVersionBtn.addEventListener('click', function () {
        // Reset editing state
        editingVersionIndex = null;
        resetNewVersionForm();
        if (els.newVersionFormTitle) {
            els.newVersionFormTitle.textContent = 'New Version';
        }
        els.newVersionSubmit.textContent = 'Add Version';

        els.newVersionForm.classList.toggle('visible');
        if (els.newVersionForm.classList.contains('visible')) {
            els.newVersionString.focus();
        }
    });

    // Cancel new version / edit
    els.newVersionCancel.addEventListener('click', function () {
        editingVersionIndex = null;
        resetNewVersionForm();
        els.newVersionForm.classList.remove('visible');
        if (els.newVersionFormTitle) {
            els.newVersionFormTitle.textContent = 'New Version';
        }
        els.newVersionSubmit.textContent = 'Add Version';
    });

    // New version schematic upload zone
    els.newSchematicZone.addEventListener('click', function (e) {
        if (e.target.closest('.btn')) return;
        els.newSchematicInput.click();
    });

    els.newSchematicZone.addEventListener('dragover', function (e) {
        e.preventDefault();
        els.newSchematicZone.classList.add('dragover');
    });

    els.newSchematicZone.addEventListener('dragleave', function () {
        els.newSchematicZone.classList.remove('dragover');
    });

    els.newSchematicZone.addEventListener('drop', function (e) {
        e.preventDefault();
        els.newSchematicZone.classList.remove('dragover');
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            handleNewSchematicFile(files[0]);
        }
    });

    // Browse button inside zone
    const browseBtn = els.newSchematicZone.querySelector('.btn');
    if (browseBtn) {
        browseBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            els.newSchematicInput.click();
        });
    }

    els.newSchematicInput.addEventListener('change', function () {
        const file = els.newSchematicInput.files[0];
        if (file) {
            handleNewSchematicFile(file);
        }
        els.newSchematicInput.value = '';
    });

    function handleNewSchematicFile(file) {
        const validExtensions = ['.schematic', '.schem', '.litematic'];
        const fileName = file.name.toLowerCase();
        const valid = validExtensions.some(ext => fileName.endsWith(ext));
        if (!valid) {
            showToast('Invalid file type. Allowed: .schematic, .schem, .litematic', 'error');
            return;
        }

        newVersionFile = file;
        els.newSchematicFileName.textContent = file.name;
        els.newSchematicFileSize.textContent = formatFileSize(file.size);
        els.newSchematicFileInfo.classList.add('visible');
    }

    // Submit new version or update existing
    els.newVersionSubmit.addEventListener('click', async function () {
        const version = els.newVersionString.value.trim();
        if (!version) {
            showToast('Version string is required.', 'error');
            return;
        }

        let filePath = null;

        // If a new file was selected, upload it first
        if (newVersionFile) {
            try {
                filePath = await uploadFile('/upload/schematic', newVersionFile);
            } catch (err) {
                showToast('Failed to upload schematic file: ' + err.message, 'error');
                return;
            }
        } else if (editingVersionIndex !== null) {
            // Editing existing version, keep existing file path if no new file uploaded
            filePath = versions[editingVersionIndex].filePath;
        } else {
            showToast('Please select a schematic file.', 'error');
            return;
        }

        const mcVersion = els.newMinecraftVersion.value.trim() || null;
        const changelog = els.newChangelog.value.trim() || null;

        if (editingVersionIndex !== null) {
            // Update existing version
            const v = versions[editingVersionIndex];
            v.version = version;
            v.minecraftVersion = mcVersion;
            v.changelog = changelog;
            if (newVersionFile) {
                v.filePath = filePath;
            }
        } else {
            // Add new version
            versions.push({
                versionId: null,
                version: version,
                minecraftVersion: mcVersion,
                filePath: filePath,
                changelog: changelog,
                isNew: true,
            });
        }

        renderVersionsList();
        resetNewVersionForm();
        els.newVersionForm.classList.remove('visible');
        editingVersionIndex = null;
        if (els.newVersionFormTitle) {
            els.newVersionFormTitle.textContent = 'New Version';
        }
        els.newVersionSubmit.textContent = 'Add Version';
        updateNextButtonLabel();
    });

    function resetNewVersionForm() {
        els.newVersionString.value = '';
        els.newMinecraftVersion.value = '';
        els.newChangelog.value = '';
        newVersionFile = null;
        els.newSchematicFileInfo.classList.remove('visible');
        els.newSchematicFileName.textContent = '';
        els.newSchematicFileSize.textContent = '';
        els.newSchematicInput.value = '';
    }

    /* ----------------------------------------------------------------------
       Load Product Data (for edit mode)
       ---------------------------------------------------------------------- */
    async function loadProductData() {
        try {
            const response = await fetch(`${API_ADMIN}/products/get?productId=${PRODUCT_ID}`, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const json = await response.json();
            if (json.success === false) {
                throw new Error(json.message || 'Failed to load product data.');
            }

            const data = json.product;
            const product = data.product;

            // Step 1 – Populate fields
            els.productName.value = product.productName || '';
            els.price.value = product.price || '';
            els.discount.value = product.discount || 0;
            els.currencyId.value = product.currencyId || '';
            els.description.value = product.description || '';

            // Unlimited stock is represented as stockQuantity = null/undefined in the DB and API response;
            // the backend never sends a separate 'unlimitedStock' flag on read.
            const isUnlimited = product.stockQuantity === null || product.stockQuantity === undefined;
            if (isUnlimited) {
                els.unlimitedStock.checked = true;
                els.stockQuantity.disabled = true;
                els.stockQuantity.value = '';
            } else {
                els.unlimitedStock.checked = false;
                els.stockQuantity.disabled = false;
                els.stockQuantity.value = product.stockQuantity || 0;
            }

            // Step 2 – Categories
            if (data.categories && data.categories.length > 0) {
                selectedCategoryIds = new Set(data.categories.map(c => c.categoryId));
                // Re-render categories after they load
                renderCategoryTree();
            }

            // Step 3 – Images - FIX: Ensure proper URL construction
            if (data.imagePaths && data.imagePaths.length > 0) {
                imageItems = data.imagePaths.map((path) => {
                    // Remove duplicate slash if present
                    let imagePath = path;
                    if (imagePath.startsWith('/')) {
                        imagePath = imagePath.substring(1);
                    }
                    // Build URL with context path
                    const fullUrl = CTX ? CTX + '/' + imagePath : '/' + imagePath;
                    return {
                        file: null,
                        previewUrl: fullUrl,
                        isExisting: true,
                        path: imagePath,
                    };
                });
                renderGallery();
            }

            // Step 4 – Versions
            if (data.versions && data.versions.length > 0) {
                versions = data.versions.map(v => ({
                    versionId: v.versionId,
                    version: v.version,
                    minecraftVersion: v.minecraftVersion || null,
                    filePath: v.filePath,
                    changelog: v.changelog || null,
                    isNew: false,
                }));
                renderVersionsList();
            }

            updateNextButtonLabel();

        } catch (err) {
            console.error('Error loading product data:', err);
            showToast('Failed to load product data for editing.', 'error');
        }
    }

    /* ----------------------------------------------------------------------
       Form Completeness Check
       ---------------------------------------------------------------------- */
    function isFormComplete() {
        return getMissingFieldsMessage() === null;
    }

    // Returns a human-readable message listing missing required fields,
    // or null if the form is complete. Used to give clear feedback when
    // Update is pressed from a step where the missing field isn't visible.
    function getMissingFieldsMessage() {
        const missing = [];

        if (els.productName.value.trim() === '') missing.push('Product Name');
        if (els.price.value.trim() === '' || Number(els.price.value) < 0) missing.push('Price');
        if (els.currencyId.value.trim() === '') missing.push('Currency');
        if (!els.unlimitedStock.checked && els.stockQuantity.value.trim() === '') missing.push('Stock Quantity');
        if (selectedCategoryIds.size === 0) missing.push('at least one Category');
        if (versions.length === 0) missing.push('at least one Version');

        if (missing.length === 0) return null;
        return 'Please complete the following before updating: ' + missing.join(', ') + '.';
    }

    /* ----------------------------------------------------------------------
       File Upload Helper
       ---------------------------------------------------------------------- */
    async function uploadFile(endpoint, file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(API_ADMIN + endpoint, {
            method: 'POST',
            body: formData,
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const json = await response.json();
        if (json.success === false) {
            throw new Error(json.message || 'File upload failed.');
        }

        return json.path;
    }

    /* ----------------------------------------------------------------------
       Final Submission: Update Full Product
       ---------------------------------------------------------------------- */
    async function submitProduct() {
        const missingMessage = getMissingFieldsMessage();
        if (missingMessage) {
            showToast(missingMessage, 'error');
            return;
        }

        els.btnUpdate.disabled = true;
        els.btnUpdate.textContent = 'Updating...';

        try {
            // Upload new images (if any)
            const imagePaths = [];
            for (const item of imageItems) {
                if (item.isExisting) {
                    imagePaths.push(item.path);
                } else {
                    const path = await uploadFile('/upload/image', item.file);
                    imagePaths.push(path);
                }
            }

            const unlimitedStock = els.unlimitedStock.checked;
            const payload = {
                productId: PRODUCT_ID,
                product: {
                    productName: els.productName.value.trim(),
                    description: els.description.value.trim() || null,
                    price: Number(els.price.value),
                    discount: els.discount.value.trim() === '' ? 0 : Number(els.discount.value),
                    currencyId: els.currencyId.value,
                    unlimitedStock: unlimitedStock,
                    stockQuantity: unlimitedStock ? null : Number(els.stockQuantity.value),
                },
                categoryIds: Array.from(selectedCategoryIds),
                imagePaths: imagePaths,
                versions: versions.map(v => ({
                    versionId: v.versionId,
                    version: v.version,
                    minecraftVersion: v.minecraftVersion || null,
                    filePath: v.filePath,
                    changelog: v.changelog || null,
                })),
            };

            const response = await fetch(API_ADMIN + '/products/update-full', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const json = await response.json();
            if (json.success === false) {
                throw new Error(json.message || 'Failed to update product.');
            }

            // Success → redirect to products list
            window.location.href = API_ADMIN + '/products';

        } catch (err) {
            console.error('Update failed:', err);
            showToast('Update failed: ' + err.message, 'error');
            els.btnUpdate.disabled = false;
            els.btnUpdate.textContent = 'Update Product';
        }
    }

    /* ----------------------------------------------------------------------
       Currency Dropdown Loading
       ---------------------------------------------------------------------- */
    async function loadCurrencies() {
        try {
            const response = await fetch(API_ADMIN + '/currencies/list', {
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

            const currencies = json.currencies || [];
            currencies.forEach(currency => {
                const option = document.createElement('option');
                option.value = currency.currencyId;
                option.textContent = `${currency.currencyName} (${currency.symbol})`;
                els.currencyId.appendChild(option);
            });
        } catch (err) {
            console.error('Failed to load currencies:', err);
        }
    }

    /* ----------------------------------------------------------------------
       Initialization
       ---------------------------------------------------------------------- */
    function init() {
        if (!els.categoryList || !els.galleryGrid) {
            console.error('Admin edit product wizard: required DOM elements not found.');
            return;
        }

        // Load dependencies
        loadCategories();
        loadCurrencies();

        // Load product data (edit mode) - this will populate all fields
        loadProductData();

        // Start at step 1
        goToStep(1);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();
