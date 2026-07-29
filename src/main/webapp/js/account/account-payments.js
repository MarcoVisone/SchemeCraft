document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';

    loadPaymentMethods();

    const btnOpenAddPayment = document.getElementById('btnOpenAddPaymentModal');
    if (btnOpenAddPayment) {
        btnOpenAddPayment.onclick = () => openModal('modalAddPayment');
    }

    const methodTypeSelect = document.getElementById('methodType');
    if (methodTypeSelect) {
        methodTypeSelect.addEventListener('change', () => {
            const ccFields = document.getElementById('creditCardFields');
            const ppFields = document.getElementById('paypalFields');

            const ccInputs = ccFields.querySelectorAll('input');
            const ppInputs = ppFields.querySelectorAll('input');

            const selectedOpt = methodTypeSelect.options[methodTypeSelect.selectedIndex];
            const text = (selectedOpt ? selectedOpt.text : '').toLowerCase();
            const isPayPal = methodTypeSelect.value === '2' || text.includes('paypal');

            if (isPayPal) {
                ccFields.style.display = 'none';
                ppFields.style.display = 'block';

                ccInputs.forEach(input => input.disabled = true);
                ppInputs.forEach(input => input.disabled = false);
            } else {
                ccFields.style.display = 'block';
                ppFields.style.display = 'none';

                ccInputs.forEach(input => input.disabled = false);
                ppInputs.forEach(input => input.disabled = true);
            }
        });
    }

    const formAddPayment = document.getElementById('formAddPayment');
    if (formAddPayment) {
        formAddPayment.addEventListener('submit', async (e) => {
            e.preventDefault();

            try {
                const formData = new FormData(formAddPayment);

                const response = await fetch(`${contextPath}/account/add-payment-method`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest',
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    body: new URLSearchParams(formData)
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    showToast(data.message || 'Payment method added successfully!', 'success');
                    closeModal('modalAddPayment');
                    formAddPayment.reset();

                    document.getElementById('creditCardFields').style.display = 'block';
                    document.getElementById('paypalFields').style.display = 'none';

                    loadPaymentMethods();
                } else {
                    showToast(data.message || data.error || 'Failed to add payment method.', 'error');
                }
            } catch (err) {
                console.error('Error adding payment method:', err);
                showToast('Connection error adding payment method.', 'error');
            }
        });
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
            const isDefault = pm.isDefault === true || pm.isDefault === 1 || pm.isDefault === 'true' || pm.isDefault === '1' ||
                pm.default === true || pm.default === 1 || pm.default === 'true' || pm.default === '1';

            const cardLast4 = pm.cardLastFour || pm.cardLast4;
            const email = pm.paymentEmail || pm.paypalEmail;

            const isCard = pm.methodType === 1 || cardLast4;
            const title = isCard
                ? `${pm.cardBrand || 'Card'} ending in •••• ${cardLast4 || '8890'}`
                : `PayPal (${email || 'Connected'})`;

            return `
                <div class="info-card ${isDefault ? 'is-default' : ''}">
                    <div class="info-card-header">
                        <strong>${escapeHtml(title)}</strong>
                        ${isDefault ? '<span class="default-badge">DEFAULT</span>' : ''}
                    </div>
                    ${isCard ? `<div class="info-card-body">Expires: ${escapeHtml(pm.cardExpiration || 'N/A')}</div>` : ''}
                    <div class="info-card-actions">
                        ${!isDefault ? `<button class="btn-action btn-sm btn-secondary-action btn-set-default-pm" data-id="${pm.paymentMethodId}">Set Default</button>` : ''}
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

    async function setDefaultPaymentMethod(paymentMethodId) {
        try {
            const response = await fetch(`${contextPath}/account/set-default-payment-method`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams({ paymentMethodId })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                showToast('Default payment method updated!', 'success');
                loadPaymentMethods();
            } else {
                showToast(data.message || data.error || 'Failed to update payment method.', 'error');
            }
        } catch (err) {
            showToast('Connection error updating payment method.', 'error');
        }
    }

    async function removePaymentMethod(paymentMethodId) {
        try {
            const response = await fetch(`${contextPath}/account/remove-payment-method`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: new URLSearchParams({ paymentMethodId })
            });
            const data = await response.json();
            if (response.ok && data.success) {
                showToast('Payment method removed!', 'success');
                loadPaymentMethods();
            } else {
                showToast(data.message || data.error || 'Failed to remove payment method.', 'error');
            }
        } catch (err) {
            showToast('Connection error removing payment method.', 'error');
        }
    }
});
