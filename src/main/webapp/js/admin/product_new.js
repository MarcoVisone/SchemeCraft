/**
 * Admin New Product Wizard — Client-side logic
 * Handles step navigation, category tree loading, image/schematic selection
 * with local preview, and the final sequential upload + create-full submission.
 */

(function () {
    'use strict';

    /* ----------------------------------------------------------------------
       Constants & Config
       ---------------------------------------------------------------------- */
    const CTX = typeof CONTEXT_PATH !== 'undefined' ? CONTEXT_PATH : '';
    const API_ADMIN = CTX + '/admin';
    const TOTAL_STEPS = 4;

    /* ----------------------------------------------------------------------
       State
       ---------------------------------------------------------------------- */
    let currentStep = 1;
    let allCategories = [];          // Flat list fetched from the server
    let selectedCategoryIds = new Set();

    // Files kept in memory until the final submit; nothing is uploaded
    // to the server until the user clicks "Create Product".
    let selectedImageFiles = [];     // Array of { file: File, previewUrl: string }
    let selectedSchematicFile = null;

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

        // Step 4 — Schematic & Version
        schematicFile: document.getElementById('schematicFile'),
        version: document.getElementById('version'),
        minecraftVersion: document.getElementById('minecraftVersion'),
        changelog: document.getElementById('changelog'),

        // Footer
        btnBack: document.getElementById('btn-back'),
        btnNext: document.getElementById('btn-next'),
    };

    /* ----------------------------------------------------------------------
       Utility Functions
       ---------------------------------------------------------------------- */
    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const div = document.createElement('div');
        div.textContent = String(text);
        return div.innerHTML;
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
        if (currentStep < TOTAL_STEPS) {
            els.btnNext.textContent = 'Next Step';
            els.btnNext.disabled = false;
        } else {
            // Final step: label and enabled state depend on whether every
            // required field across all steps has been filled in.
            const complete = isFormComplete();
            els.btnNext.textContent = complete ? 'Create Product' : 'Complete all steps';
            els.btnNext.disabled = !complete;
        }
    }

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
        } else {
            submitProduct();
        }
    });

    els.stepNodes.forEach((node, idx) => {
        node.addEventListener('click', function () {
            // Free navigation between steps, as confirmed: no per-step validation gate.
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

    // Re-check completeness on every relevant input change so the final
    // button state stays accurate without requiring a click to discover it.
    [els.productName, els.price, els.discount, els.currencyId, els.stockQuantity,
        els.description, els.version, els.minecraftVersion, els.changelog].forEach(input => {
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
       Step 3 — Gallery: Image Selection & Local Preview
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
        els.imageInput.value = ''; // allow re-selecting the same file later
    });

    function addImageFiles(fileList) {
        for (const file of fileList) {
            if (!file.type.startsWith('image/')) continue;
            selectedImageFiles.push({
                file: file,
                previewUrl: URL.createObjectURL(file),
            });
        }
        renderGallery();
        updateNextButtonLabel();
    }

    function removeImageFile(index) {
        const removed = selectedImageFiles.splice(index, 1);
        if (removed[0]) {
            URL.revokeObjectURL(removed[0].previewUrl);
        }
        renderGallery();
        updateNextButtonLabel();
    }

    function renderGallery() {
        els.galleryGrid.innerHTML = selectedImageFiles.map((item, idx) => `
            <div class="gallery-item">
                <img src="${item.previewUrl}" alt="Preview ${idx + 1}" />
                ${idx === 0 ? '<span class="gallery-badge">Cover</span>' : ''}
                <button type="button" class="gallery-delete" data-index="${idx}" title="Remove image">
                    <img src="${CTX}/icons/deactive.png" alt="Remove" width="16" height="16">
                </button>
            </div>
        `).join('');
    }

    els.galleryGrid.addEventListener('click', function (e) {
        const btn = e.target.closest('.gallery-delete');
        if (!btn) return;
        removeImageFile(parseInt(btn.dataset.index, 10));
    });

    /* ----------------------------------------------------------------------
       Step 4 — Schematic File Selection
       ---------------------------------------------------------------------- */
    els.schematicFile.addEventListener('change', function () {
        selectedSchematicFile = els.schematicFile.files[0] || null;
        updateNextButtonLabel();
    });

    /* ----------------------------------------------------------------------
       Form Completeness Check
       ---------------------------------------------------------------------- */
    function isFormComplete() {
        const hasName = els.productName.value.trim() !== '';
        const hasPrice = els.price.value.trim() !== '' && Number(els.price.value) >= 0;
        const hasCurrency = els.currencyId.value.trim() !== '';
        const hasStock = els.unlimitedStock.checked || els.stockQuantity.value.trim() !== '';
        const hasCategory = selectedCategoryIds.size > 0;
        const hasSchematic = selectedSchematicFile !== null;
        const hasVersion = els.version.value.trim() !== '';

        return hasName && hasPrice && hasCurrency && hasStock && hasCategory && hasSchematic && hasVersion;
    }

    /* ----------------------------------------------------------------------
       Final Submission: Sequential Upload + Create-Full Request
       ---------------------------------------------------------------------- */

    /**
     * Uploads a single file to the given admin upload endpoint and returns
     * the server-assigned path. Throws on any failure so the caller can stop
     * the whole submission immediately (no partial/orphaned product state).
     */
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

    async function submitProduct() {
        if (!isFormComplete()) return;

        els.btnNext.disabled = true;
        els.btnNext.textContent = 'Uploading...';

        try {
            // Step 1: sequentially upload all gallery images.
            const imagePaths = [];
            for (const item of selectedImageFiles) {
                const path = await uploadFile('/upload/image', item.file);
                imagePaths.push(path);
            }

            // Step 2: upload the schematic file.
            const schematicPath = await uploadFile('/upload/schematic', selectedSchematicFile);

            // Step 3: build and send the final create-full payload.
            const unlimitedStock = els.unlimitedStock.checked;
            const payload = {
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
                version: {
                    filePath: schematicPath,
                    version: els.version.value.trim(),
                    minecraftVersion: els.minecraftVersion.value.trim() || null,
                    changelog: els.changelog.value.trim() || null,
                },
            };

            const response = await fetch(API_ADMIN + '/products/create-full', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const json = await response.json();
            if (json.success === false) {
                throw new Error(json.message || 'Failed to create product.');
            }

            // Success: return to the product list.
            window.location.href = API_ADMIN + '/products';

        } catch (err) {
            // Stop entirely on any failure, as confirmed: no partial submission.
            // Non-blocking: logged only, no alert(). Re-enable the button so
            // the user can retry once the underlying issue is fixed.
            console.error('Product creation failed:', err);
            els.btnNext.disabled = false;
            updateNextButtonLabel();
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
            console.error('Admin new product wizard: required DOM elements not found.');
            return;
        }

        // Initialize unlimited stock toggle state
        if (els.unlimitedStock.checked) {
            els.stockQuantity.disabled = true;
            els.stockQuantity.value = '';
        }

        loadCategories();
        loadCurrencies();
        goToStep(1);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();