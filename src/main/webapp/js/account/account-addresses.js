document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';

    loadAddresses();

    const btnOpenAddAddress = document.getElementById('btnOpenAddAddressModal');
    if (btnOpenAddAddress) {
        btnOpenAddAddress.onclick = () => openModal('modalAddAddress');
    }

    async function loadAddresses() {
        const addrContainer = document.getElementById('addressesContainer');
        if (!addrContainer) return;

        try {
            const response = await fetch(`${contextPath}/account/addresses`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            const data = await response.json();
            const list = Array.isArray(data) ? data : (data.addresses || []);

            if (response.ok && list.length > 0) {
                renderAddresses(list);
            } else {
                addrContainer.innerHTML = `<p class="text-secondary">No saved addresses found.</p>`;
            }
        } catch (err) {
            addrContainer.innerHTML = `<p class="text-secondary">Error loading addresses.</p>`;
        }
    }

    function renderAddresses(addresses) {
        const addrContainer = document.getElementById('addressesContainer');
        if (!addrContainer) return;

        addrContainer.innerHTML = addresses.map(addr => `
            <div class="info-card ${addr.default ? 'is-default' : ''}">
                ${addr.default ? '<span class="default-badge">DEFAULT</span>' : ''}
                <strong>${escapeHtml(addr.streetAddress)}</strong>
                <span>${escapeHtml(addr.city)}, ${escapeHtml(addr.stateProvince || '')} ${escapeHtml(addr.postalCode)}</span>
                <span>Country: ${escapeHtml(addr.countryId)}</span>
                <div class="info-card-actions">
                    ${!addr.default ? `<button class="btn-action btn-sm btn-secondary-action btn-set-default-addr" data-id="${addr.addressId}">Set Default</button>` : ''}
                    <button class="btn-action btn-sm btn-danger-action btn-remove-addr" data-id="${addr.addressId}">Remove</button>
                </div>
            </div>
        `).join('');

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
                const response = await fetch(`${contextPath}/account/add-address`, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    body: new URLSearchParams(new FormData(formAddAddress))
                });
                const data = await response.json();
                if (response.ok && data.success) {
                    showToast(data.message || 'Address added!', 'success');
                    closeModal('modalAddAddress');
                    formAddAddress.reset();
                    loadAddresses();
                } else {
                    showToast(data.error || 'Could not add address.', 'error');
                }
            } catch (err) {
                showToast('Connection error adding address.', 'error');
            }
        });
    }

    async function setDefaultAddress(addressId) {
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
            if (response.ok && data.success) {
                showToast('Default address updated!', 'success');
                loadAddresses();
            } else {
                showToast(data.error || 'Failed to update default address.', 'error');
            }
        } catch (err) {
            showToast('Connection error updating address.', 'error');
        }
    }

    async function removeAddress(addressId) {
        if (!confirm('Are you sure you want to remove this address?')) return;
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
            if (response.ok && data.success) {
                showToast('Address removed!', 'success');
                loadAddresses();
            } else {
                showToast(data.error || 'Failed to remove address.', 'error');
            }
        } catch (err) {
            showToast('Connection error removing address.', 'error');
        }
    }
});
