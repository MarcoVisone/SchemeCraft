// =========================================================================
// CONTEXT PATH FALLBACK
// =========================================================================
const basePath = (typeof CONTEXT_PATH !== 'undefined')
    ? CONTEXT_PATH
    : window.location.pathname.substring(0, window.location.pathname.indexOf('/admin'));

// =========================================================================
// STATE & DOM REFERENCES
// =========================================================================
let allCategories = [];
let currentDeleteId = null;

const treeContainer = document.getElementById('categories-tree');
const modal = document.getElementById('modal-category');
const modalTitle = document.getElementById('modal-title');
const form = document.getElementById('form-category');
const inputId = document.getElementById('input-category-id');
const inputParentId = document.getElementById('input-parent-id');
const inputName = document.getElementById('input-category-name');
const selectParent = document.getElementById('select-parent-category');
const inputDesc = document.getElementById('input-category-description');
const parentFixedField = document.getElementById('parent-fixed-field');
const parentFixedName = document.getElementById('parent-fixed-name');
const parentSelectField = document.getElementById('parent-select-field');
const parentSearchInput = document.getElementById('parent-search-input');
const modalDelete = document.getElementById('modal-delete-confirm');
const deleteWarningText = document.getElementById('delete-warning-text');
const deleteSubtext = document.getElementById('delete-subtext');
const btnDeleteConfirm = document.getElementById('btn-delete-confirm');

// =========================================================================
// INIT
// =========================================================================
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();

    const modalCloseBtn = document.getElementById('modal-close');
    const btnCancel = document.getElementById('btn-cancel');
    const modalDeleteCloseBtn = document.getElementById('modal-delete-close');
    const btnDeleteCancel = document.getElementById('btn-delete-cancel');

    if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);
    if (btnCancel) btnCancel.addEventListener('click', closeModal);
    if (modalDeleteCloseBtn) modalDeleteCloseBtn.addEventListener('click', closeDeleteModal);
    if (btnDeleteCancel) btnDeleteCancel.addEventListener('click', closeDeleteModal);

    const btnAddRoot = document.getElementById('btn-add-root-category');
    if (btnAddRoot) {
        btnAddRoot.addEventListener('click', () => openCreateModal(null));
    }

    if (form) form.addEventListener('submit', handleFormSubmit);
    if (btnDeleteConfirm) btnDeleteConfirm.addEventListener('click', confirmDelete);
    if (parentSearchInput) parentSearchInput.addEventListener('input', filterParentOptions);
});

// =========================================================================
// DATA LOADING
// =========================================================================
async function loadCategories() {
    if (!treeContainer) return;

    try {
        treeContainer.innerHTML = '<div class="tree-loading">Loading categories...</div>';

        const resp = await fetch(basePath + '/admin/categories/list');

        if (resp.status === 401 || resp.status === 403) {
            throw new Error('Unauthorized access. Please log in again with Administrator credentials.');
        }

        if (!resp.ok) {
            throw new Error(`Server error (${resp.status}): Unable to load categories.`);
        }

        const data = await resp.json();
        if (!data.success) throw new Error(data.message || 'Failed to load categories');

        allCategories = data.categories || [];
        renderTree();
    } catch (err) {
        console.error('Error loading categories:', err);
        treeContainer.innerHTML =
            '<div class="tree-loading" style="color: #e74c3c;">Error: ' + err.message + '</div>';
    }
}

// =========================================================================
// TREE BUILDING & RENDERING
// =========================================================================
function buildTree(parentId = null) {
    return allCategories
        .filter(cat => (cat.parentCategoryId || null) === parentId)
        .map(cat => ({
            ...cat,
            children: buildTree(cat.categoryId)
        }));
}

function renderTree() {
    const tree = buildTree(null);
    treeContainer.innerHTML = '';
    if (tree.length === 0) {
        treeContainer.innerHTML =
            '<div class="tree-loading">No categories yet. Create the first one.</div>';
        return;
    }
    renderNodes(tree, treeContainer, true);
}

function renderNodes(nodes, parentElement, isRoot = false) {
    nodes.forEach(node => {
        const nodeDiv = document.createElement('div');
        nodeDiv.className = 'tree-node';
        nodeDiv.dataset.categoryId = node.categoryId;

        const mainDiv = document.createElement('div');
        mainDiv.className = 'node-main';

        const toggleBtn = document.createElement('button');
        toggleBtn.type = 'button';
        toggleBtn.className = 'node-toggle';
        toggleBtn.setAttribute('aria-label', 'Toggle children');
        if (node.children.length === 0) {
            toggleBtn.classList.add('no-children');
            toggleBtn.disabled = true;
        }
        toggleBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            toggleChildren(nodeDiv);
        });

        const contentDiv = document.createElement('div');
        contentDiv.className = 'node-content';

        const nameSpan = document.createElement('span');
        nameSpan.className = 'node-name';
        nameSpan.textContent = node.categoryName;
        contentDiv.appendChild(nameSpan);

        if (node.description) {
            const descSpan = document.createElement('span');
            descSpan.className = 'node-description';
            descSpan.textContent = node.description;
            contentDiv.appendChild(descSpan);
        }

        const actionsDiv = document.createElement('div');
        actionsDiv.className = 'node-actions';

        const editBtn = document.createElement('button');
        editBtn.type = 'button';
        editBtn.className = 'btn-icon-only btn-edit';
        editBtn.title = 'Edit category';
        editBtn.textContent = '✏️';
        editBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            openEditModal(node);
        });

        const addChildBtn = document.createElement('button');
        addChildBtn.type = 'button';
        addChildBtn.className = 'btn-icon-only btn-add-child';
        addChildBtn.title = 'Add subcategory';
        addChildBtn.textContent = '＋';
        addChildBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            openCreateModal(node.categoryId, node.categoryName);
        });

        const deleteBtn = document.createElement('button');
        deleteBtn.type = 'button';
        deleteBtn.className = 'btn-icon-only btn-delete';
        deleteBtn.title = 'Delete category';
        deleteBtn.textContent = '🗑️';
        deleteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            openDeleteModal(node.categoryId, node.children.length > 0);
        });

        actionsDiv.appendChild(editBtn);
        actionsDiv.appendChild(addChildBtn);
        actionsDiv.appendChild(deleteBtn);

        mainDiv.appendChild(toggleBtn);
        mainDiv.appendChild(contentDiv);
        mainDiv.appendChild(actionsDiv);
        nodeDiv.appendChild(mainDiv);

        const childrenDiv = document.createElement('div');
        childrenDiv.className = 'node-children';
        if (node.children.length > 0) {
            renderNodes(node.children, childrenDiv, false);
        }
        nodeDiv.appendChild(childrenDiv);

        if (isRoot && node.children.length > 0) {
            childrenDiv.classList.add('expanded');
            toggleBtn.classList.add('expanded');
        }

        parentElement.appendChild(nodeDiv);
    });
}

function toggleChildren(nodeDiv) {
    const childrenDiv = nodeDiv.querySelector('.node-children');
    const toggleBtn = nodeDiv.querySelector('.node-toggle');
    if (childrenDiv) {
        childrenDiv.classList.toggle('expanded');
        toggleBtn.classList.toggle('expanded');
    }
}

// =========================================================================
// PARENT SELECT MANAGEMENT
// =========================================================================
function populateParentSelect(selectedParentId = null, excludeId = null) {
    if (!selectParent) return;
    selectParent.innerHTML = '<option value="">-- None (root) --</option>';

    function addOptions(parentId = null, depth = 0) {
        const children = allCategories.filter(c => (c.parentCategoryId || null) === parentId);
        children.forEach(cat => {
            if (excludeId && cat.categoryId === excludeId) return;

            const prefix = '\u00A0\u00A0'.repeat(depth) + (depth > 0 ? '- ' : '');
            const option = document.createElement('option');
            option.value = cat.categoryId;
            option.textContent = prefix + cat.categoryName;
            if (cat.categoryId === selectedParentId) option.selected = true;
            selectParent.appendChild(option);
            addOptions(cat.categoryId, depth + 1);
        });
    }

    addOptions(null, 0);
    filterParentOptions();
}

function filterParentOptions() {
    if (!parentSearchInput || !selectParent) return;
    const filter = parentSearchInput.value.trim().toLowerCase();
    const options = selectParent.querySelectorAll('option');
    options.forEach(opt => {
        if (opt.value === '') {
            opt.style.display = '';
            return;
        }
        const text = opt.textContent.toLowerCase();
        opt.style.display = text.includes(filter) ? '' : 'none';
    });
}

// =========================================================================
// MODAL MANAGEMENT
// =========================================================================
function openCreateModal(parentId, parentName = null) {
    if (!modal) return;

    inputId.value = '';
    inputParentId.value = parentId || '';
    inputName.value = '';
    inputDesc.value = '';

    if (parentId) {
        // Subcategory
        if (modalTitle) modalTitle.textContent = 'New Subcategory';

        if (parentFixedField)
            parentFixedField.style.display = 'block';

        if (parentFixedName)
            parentFixedName.textContent = parentName || 'Unknown';

        if (parentSelectField)
            parentSelectField.style.display = 'none';

    } else {
        // Root category
        if (modalTitle)
            modalTitle.textContent = 'New Root Category';

        if (parentFixedField)
            parentFixedField.style.display = 'none';

        if (parentSelectField)
            parentSelectField.style.display = 'none';
    }

    modal.style.display = 'flex';
    inputName.focus();
}

function openEditModal(category) {
    if (!modal) return;
    inputId.value = category.categoryId;
    // Keep the original parentId in hidden field (will be sent on update)
    inputParentId.value = category.parentCategoryId || '';
    inputName.value = category.categoryName || '';
    inputDesc.value = category.description || '';

    if (modalTitle) modalTitle.textContent = 'Edit Category';

    // In edit mode, we don't show any parent field at all
    if (parentFixedField) parentFixedField.style.display = 'none';
    if (parentSelectField) parentSelectField.style.display = 'none';

    modal.style.display = 'flex';
    inputName.focus();
}

function closeModal() {
    if (!modal) return;
    modal.style.display = 'none';
    if (form) form.reset();
    inputId.value = '';
    inputParentId.value = '';
    // Restore default visibility for parent fields
    if (parentFixedField) parentFixedField.style.display = 'none';
    if (parentSelectField) parentSelectField.style.display = 'block';
    if (parentSearchInput) parentSearchInput.value = '';
}

// =========================================================================
// FORM SUBMIT (CREATE / UPDATE)
// =========================================================================
async function handleFormSubmit(e) {

    e.preventDefault();

    const id = inputId.value.trim();
    const name = inputName.value.trim();
    const desc = inputDesc.value.trim();

    let parentId = inputParentId.value.trim();

    if (parentId === "")
        parentId = null;

    if (!name) {
        alert("Category name is required.");
        return;
    }

    const isEdit = !!id;

    const endpoint = isEdit
        ? basePath + "/admin/categories/update"
        : basePath + "/admin/categories/create";

    const params = new URLSearchParams();

    if (isEdit)
        params.append("categoryId", id);

    params.append("categoryName", name);

    if (parentId)
        params.append("parentCategoryId", parentId);

    if (desc)
        params.append("description", desc);

    try {

        const resp = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params.toString()
        });

        if (!resp.ok) {
            const errorData = await resp.json().catch(() => ({}));
            throw new Error(errorData.message || ("HTTP Error " + resp.status));
        }

        const data = await resp.json();

        if (!data.success)
            throw new Error(data.message || "Operation failed");

        closeModal();
        await loadCategories();

    } catch (err) {
        alert("Error: " + err.message);
    }
}

// =========================================================================
// DELETE CONFIRMATION
// =========================================================================
function openDeleteModal(categoryId, hasChildren) {
    if (!modalDelete) return;
    currentDeleteId = categoryId;
    const category = allCategories.find(c => c.categoryId === categoryId);
    const name = category ? category.categoryName : 'this category';

    if (hasChildren) {
        if (deleteWarningText) deleteWarningText.textContent = 'Cannot delete "' + name + '" because it has subcategories.';
        if (deleteSubtext) deleteSubtext.textContent = 'Please remove or reassign its subcategories first.';
        if (btnDeleteConfirm) {
            btnDeleteConfirm.disabled = true;
            btnDeleteConfirm.style.opacity = '0.5';
        }
    } else {
        if (deleteWarningText) deleteWarningText.textContent = 'Are you sure you want to delete "' + name + '"?';
        if (deleteSubtext) deleteSubtext.textContent = 'This action cannot be undone. Products in this category will become uncategorized.';
        if (btnDeleteConfirm) {
            btnDeleteConfirm.disabled = false;
            btnDeleteConfirm.style.opacity = '1';
        }
    }
    modalDelete.style.display = 'flex';
}

function closeDeleteModal() {
    if (!modalDelete) return;
    modalDelete.style.display = 'none';
    currentDeleteId = null;
    if (btnDeleteConfirm) {
        btnDeleteConfirm.disabled = false;
        btnDeleteConfirm.style.opacity = '1';
    }
}

async function confirmDelete() {
    if (!currentDeleteId || (btnDeleteConfirm && btnDeleteConfirm.disabled)) return;

    try {
        const resp = await fetch(basePath + '/admin/categories/delete', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'categoryId=' + encodeURIComponent(currentDeleteId)
        });

        if (!resp.ok) {
            const errorData = await resp.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP Error ${resp.status}`);
        }

        const data = await resp.json();
        if (!data.success) throw new Error(data.message || 'Delete failed');
        closeDeleteModal();
        await loadCategories();
    } catch (err) {
        alert('Error: ' + err.message);
    }
}
