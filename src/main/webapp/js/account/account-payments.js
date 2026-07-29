document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';

    loadPaymentMethods();

    const btnOpenAddPayment = document.getElementById('btnOpenAddPaymentModal');
    if (btnOpenAddPayment) {
        btnOpenAddPayment.onclick = () => openModal('modalAddPayment');
    }

    async function loadPaymentMethods() {
        const pmContainer = document.getElementById('paymentMethodsContainer');
        if (!pmContainer) return;

        try {
            const response = await fetch(`${contextPath}/account/payment-methods`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            const data = await response.json();
            const list = Array.isArray(data) ? data : (data.paymentMethods || []);

            if (response.ok && list.length > 0) {
                renderPaymentMethods(list);
            } else {
                pmContainer.innerHTML = `<p class="text-secondary">No payment methods saved.</p>`;
            }
        } catch (err) {
            pmContainer.innerHTML = `<p class="text-secondary">Error loading payment methods.</p>`;
        }
    }

    function renderPaymentMethods(methods) {
        const pmContainer = document.getElementById('paymentMethodsContainer');
        if (!pmContainer) return;

        pmContainer.innerHTML = methods.map(pm => {
            const isCard = pm.methodType === 1 || pm.cardNumber;
            const title = isCard ? `${pm.cardBrand || 'Card'} ending in •••• ${pm.cardLast4 || '8890'}` : `PayPal (${pm.paypalEmail || 'Connected'})`;

            return `
                <div class="info-card ${pm.default ? 'is-default' : ''}">
                    ${pm.default ? '<span class="default-badge">DEFAULT</span>' : ''}
                    <strong>${escapeHtml(title)}</strong>
                    ${isCard ? `<span>Expires: ${escapeHtml(pm.cardExpiration || 'N/A')}</span>` : ''}
                    <div class="info-card-actions">
                        ${!pm.default ? `<button class="btn-action btn-sm btn-secondary-action btn-set-default-pm" data-id="${pm.paymentMethodId}">Set Default</button>` : ''}
                        <button class="btn-action btn-sm btn-danger-action btn-remove-pm" data-id="${pm.paymentMethodId}">Remove</button>
                    </div>
                </div>
            `;
        }).join('');

        pmContainer.querySelectorAll('.btn-set-default-pm').forEach(btn => {
            btn.onclick = () => setDefaultPaymentMethod(btn.dataset.id);
        });

        pmContainer.querySelectorAll('.btn-remove-pm').forEach(btn => {
            btn.onclick = () => removePaymentMethod(btn.dataset.id);
        });
    }

    const methodTypeSelect = document.getElementById('methodType');
    if (methodTypeSelect) {
        methodTypeSelect.addEventListener('change', (e) => {
            const ccFields = document.getElementById('creditCardFields');
            const ppFields = document.getElementById('paypalFields');
            if (e.target.value === '2') {
                ccFields.style.display = 'none';
                ppFields.style.display = 'block';
            } else {
                ccFields.style.display = 'block';
                ppFields.style.display = 'none';
            }
        });
    }

    const formAddPayment = document.getElementById('formAddPayment');
    if (formAddPayment) {
        formAddPayment.addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                const response = await fetch(`${contextPath}/account/add-payment-method`, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    body: new URLSearchParams(new FormData(formAddPayment))
                });
                const data = await response.json();
                if (response.ok && data.success) {
                    showToast(data.message || 'Payment method added!', 'success');
                    closeModal('modalAddPayment');
                    formAddPayment.reset();
                    loadPaymentMethods();
                } else {
                    showToast(data.error || 'Failed to add payment method.', 'error');
                }
            } catch (err) {
                showToast('Connection error adding payment method.', 'error');
            }
        });
    }

    async function setDefaultPaymentMethod(paymentMethodId) {
        try {
            const response = await fetch(`${contextPath}/account/set-default-payment-method`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams({ paymentMethodId })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                showToast('Default payment method updated!', 'success');
                loadPaymentMethods();
            } else {
                showToast(data.error || 'Failed to update payment method.', 'error');
            }
        } catch (err) {
            showToast('Connection error updating payment method.', 'error');
        }
    }

    async function removePaymentMethod(paymentMethodId) {
        if (!confirm('Are you sure you want to remove this payment method?')) return;
        try {
            const response = await fetch(`${contextPath}/account/remove-payment-method`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams({ paymentMethodId })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                showToast('Payment method removed!', 'success');
                loadPaymentMethods();
            } else {
                showToast(data.error || 'Failed to remove payment method.', 'error');
            }
        } catch (err) {
            showToast('Connection error removing payment method.', 'error');
        }
    }
});
