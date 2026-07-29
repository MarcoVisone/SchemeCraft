document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';

    const escapeHtml = (str) => {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    const showToast = (message, type = 'info') => {
        if (typeof window.showToast === 'function') {
            window.showToast(message, type);
        } else {
            console.log(`[Toast ${type}]: ${message}`);
        }
    };

    const openModal = (modalId) => {
        if (typeof window.openModal === 'function') {
            window.openModal(modalId);
        } else {
            const modal = document.getElementById(modalId);
            if (modal) modal.classList.add('active', 'show');
        }
    };

    const closeModal = (modalId) => {
        if (typeof window.closeModal === 'function') {
            window.closeModal(modalId);
        } else {
            const modal = document.getElementById(modalId);
            if (modal) modal.classList.remove('active', 'show');
        }
    };

    const btnOpenAddAddress = document.getElementById('btnOpenAddAddressModal');
    if (btnOpenAddAddress) {
        btnOpenAddAddress.onclick = () => openModal('modalAddAddress');
    }

    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.onclick = () => {
            const targetId = btn.getAttribute('data-close-modal');
            closeModal(targetId);
        };
    });

    const modalAddAddress = document.getElementById('modalAddAddress');
    if (modalAddAddress) {
        modalAddAddress.addEventListener('click', (e) => {
            if (e.target === modalAddAddress) {
                closeModal('modalAddAddress');
            }
        });
    }

    loadAddresses();

    async function loadAddresses() {
        const addrContainer = document.getElementById('addressesContainer');
        if (!addrContainer) return;

        try {
            const response = await fetch(`${contextPath}/account/addresses`, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'application/json'
                }
            });

            const contentType = response.headers.get('content-type');
            if (!contentType || !contentType.includes('application/json')) {
                throw new Error('Response is not JSON');
            }

            const data = await response.json();
            const list = Array.isArray(data) ? data : (data.addresses || []);

            const activeAddresses = list.filter(addr => addr.isActive !== false);

            if (response.ok && activeAddresses.length > 0) {
                renderAddresses(activeAddresses);
            } else {
                addrContainer.innerHTML = `<p class="text-secondary">No saved addresses found.</p>`;
            }
        } catch (err) {
            console.error('Error loading addresses:', err);
            addrContainer.innerHTML = `<p class="text-secondary">Error loading addresses.</p>`;
        }
    }

    function renderAddresses(addresses) {
        const addrContainer = document.getElementById('addressesContainer');
        if (!addrContainer) return;

        const activeList = addresses.filter(addr => addr.isActive !== false);

        if (activeList.length === 0) {
            addrContainer.innerHTML = `<p class="text-secondary">No saved addresses found.</p>`;
            return;
        }

        addrContainer.innerHTML = activeList.map(addr => {
            const id = addr.addressId || addr.id || addr.idAddress;
            const street = addr.streetAddress || addr.street || addr.address || '';
            const city = addr.city || '';
            const state = addr.stateProvince || addr.state || addr.province || '';
            const postal = addr.postalCode || addr.zipCode || addr.postal || '';
            const country = addr.countryId || addr.country || addr.countryCode || '';
            const isDefault = addr.isDefault !== undefined ? addr.isDefault : (addr.default !== undefined ? addr.default : false);

            return `
                <div class="info-card ${isDefault ? 'is-default' : ''}">
                    ${isDefault ? '<span class="default-badge">DEFAULT</span>' : ''}
                    <strong>${escapeHtml(street)}</strong>
                    <span>${escapeHtml(city)}${state ? ', ' + escapeHtml(state) : ''} ${escapeHtml(postal)}</span>
                    <span>Country: ${escapeHtml(country)}</span>
                    <div class="info-card-actions">
                        ${!isDefault ? `<button type="button" class="btn-action btn-sm btn-secondary-action btn-set-default-addr" data-id="${id}">Set Default</button>` : ''}
                        <button type="button" class="btn-action btn-sm btn-danger-action btn-remove-addr" data-id="${id}">Remove</button>
                    </div>
                </div>
            `;
        }).join('');

        addrContainer.querySelectorAll('.btn-set-default-addr').forEach(btn => {
            btn.onclick = () => setDefaultAddress(btn.dataset.id);
        });

        addrContainer.querySelectorAll('.btn-remove-addr').forEach(btn => {
            btn.onclick = () => removeAddress(btn.dataset.id);
        });
    }

    const formAddAddress = document.getElementById('formAddAddress');
    if (formAddAddress) {
        formAddAddress.addEventListener('submit', async (e) => {
            e.preventDefault();

            try {
                const formData = new FormData(formAddAddress);

                const response = await fetch(`${contextPath}/account/add-address`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    body: new URLSearchParams(formData)
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showToast(data.message || 'Address added successfully!', 'success');
                    closeModal('modalAddAddress');
                    formAddAddress.reset();
                    loadAddresses();
                } else {
                    showToast(data.message || 'Could not add address.', 'error');
                }
            } catch (err) {
                console.error('Error adding address:', err);
                showToast('Connection error adding address.', 'error');
            }
        });
    }

    async function setDefaultAddress(addressId) {
        if (!addressId) return;
        try {
            const response = await fetch(`${contextPath}/account/set-default-address`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams({ addressId })
            });

            const data = await response.json();
            if (response.ok && (data.success || data.status === 'success')) {
                showToast('Default address updated!', 'success');
                loadAddresses();
            } else {
                showToast(data.error || data.message || 'Failed to update default address.', 'error');
            }
        } catch (err) {
            console.error('Error setting default address:', err);
            showToast('Connection error updating address.', 'error');
        }
    }

    async function removeAddress(addressId) {
        if (!addressId) return;

        try {
            const response = await fetch(`${contextPath}/account/remove-address`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams({ addressId })
            });

            const data = await response.json();
            if (response.ok && (data.success || data.status === 'success')) {
                showToast('Address removed!', 'success');
                loadAddresses();
            } else {
                showToast(data.error || data.message || 'Failed to remove address.', 'error');
            }
        } catch (err) {
            console.error('Error removing address:', err);
            showToast('Connection error removing address.', 'error');
        }
    }
});
