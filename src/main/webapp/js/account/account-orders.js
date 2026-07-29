document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';
    let cachedOrders = [];

    loadOrders();

    async function loadOrders() {
        const tbody = document.getElementById('ordersTableBody');
        if (!tbody) return;

        try {
            const response = await fetch(`${contextPath}/orders/list?format=json`, {
                headers: {
                    'Accept': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (!response.ok) {
                renderOrdersTable([]);
                return;
            }

            const data = await response.json();

            if (data && data.success && Array.isArray(data.orders)) {
                cachedOrders = data.orders;
            } else if (Array.isArray(data)) {
                cachedOrders = data;
            } else {
                cachedOrders = [];
            }

            renderOrdersTable(cachedOrders);
        } catch (err) {
            console.error("Error loading orders:", err);
            tbody.innerHTML = `<tr><td colspan="5" class="text-center">Error loading orders. Please try again.</td></tr>`;
        }
    }

    function parseOrderDate(dateStr) {
        if (!dateStr) return null;
        const d = new Date(dateStr);
        return isNaN(d.getTime()) ? null : d;
    }

    function getSymbol(currencyObj) {
        if (typeof currencyObj === 'object' && currencyObj !== null) {
            return currencyObj.symbol || currencyObj.currencyId || '$';
        }
        return currencyObj || '$';
    }

    function getStatusText(order) {
        if (order.statusName) return order.statusName;
        if (order.statusInfo && order.statusInfo.statusName) return order.statusInfo.statusName;

        if (order.status && isNaN(order.status)) {
            return order.status;
        }

        const statusMap = {
            1: 'PENDING',
            2: 'COMPLETED',
            3: 'CANCELLED',
            4: 'REFUNDED'
        };

        const statusId = order.statusId || order.status;
        return statusMap[statusId] || 'COMPLETED';
    }

    function getPaymentMethodText(order) {
        if (!order) return 'N/A';

        if (order.methodType !== undefined && order.methodType !== null) {
            const typeId = String(order.methodType);

            return `Method ID: ${typeId}`;
        }

        if (typeof order.paymentMethod === 'string') return order.paymentMethod;
        if (order.paymentMethodName) return order.paymentMethodName;
        if (order.paymentType) return order.paymentType;

        return 'Credit Card / PayPal';
    }

    function renderOrdersTable(orders) {
        const tbody = document.getElementById('ordersTableBody');
        if (!tbody) return;

        if (!orders || orders.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center">No orders found.</td></tr>`;
            return;
        }

        tbody.innerHTML = orders.map(order => {
            const orderId = order.orderId || order.transactionId || 'N/A';
            const parsedDate = parseOrderDate(order.createdAt);
            const dateDisplay = parsedDate ? parsedDate.toLocaleDateString() : 'N/A';

            const statusText = getStatusText(order);
            const statusClass = String(statusText).toLowerCase();

            const symbol = getSymbol(order.currency);
            const total = parseFloat(order.totalAmount || 0).toFixed(2);

            return `
            <tr>
                <td><strong>#${escapeHtml(String(orderId))}</strong></td>
                <td>${dateDisplay}</td>
                <td><strong>${symbol}${total}</strong></td>
                <td><span class="status-badge status-${statusClass}">${escapeHtml(String(statusText))}</span></td>
                <td class="text-right">
                    <button class="btn-action btn-sm btn-secondary-action btn-view-order" data-id="${escapeHtml(String(orderId))}">
                        Details
                    </button>
                    <button class="btn-action btn-sm btn-primary-action btn-print-inv" data-id="${escapeHtml(String(orderId))}">
                        Invoice
                    </button>
                </td>
            </tr>
        `;
        }).join('');

        tbody.querySelectorAll('.btn-view-order').forEach(btn => {
            btn.addEventListener('click', () => openOrderDetailsModal(btn.dataset.id));
        });

        tbody.querySelectorAll('.btn-print-inv').forEach(btn => {
            btn.addEventListener('click', () => fetchAndPrintInvoice(btn.dataset.id));
        });
    }

    const orderFilterForm = document.getElementById('orderFilterForm');
    if (orderFilterForm) {
        orderFilterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const startDateVal = document.getElementById('startDate').value;
            const endDateVal = document.getElementById('endDate').value;

            if (!startDateVal && !endDateVal) {
                renderOrdersTable(cachedOrders);
                return;
            }

            const filtered = cachedOrders.filter(order => {
                const oDate = parseOrderDate(order.createdAt);
                if (!oDate) return true;

                if (startDateVal && oDate < new Date(startDateVal)) return false;
                if (endDateVal && oDate > new Date(endDateVal + 'T23:59:59')) return false;
                return true;
            });

            renderOrdersTable(filtered);
        });

        document.getElementById('btnResetOrderFilter')?.addEventListener('click', () => {
            orderFilterForm.reset();
            renderOrdersTable(cachedOrders);
        });
    }

    async function openOrderDetailsModal(orderId) {
        let order = cachedOrders.find(o => String(o.orderId || o.transactionId) === String(orderId));

        try {
            const resp = await fetch(`${contextPath}/orders/detail?id=${encodeURIComponent(orderId)}&format=json`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (resp.ok) {
                const resData = await resp.json();
                if (resData.success && resData.order) {
                    order = resData.order;
                }
            }
        } catch(e) {
            console.warn("Could not fetch detailed order DTO, using cached item.", e);
        }

        if (!order) return;

        const body = document.getElementById('orderDetailsBody');
        const displayId = order.orderId || order.transactionId || 'N/A';
        document.getElementById('orderDetailsTitle').textContent = `Order Details #${displayId}`;

        const parsedDate = parseOrderDate(order.createdAt);
        const dateDisplay = parsedDate ? parsedDate.toLocaleString() : 'N/A';
        const symbol = getSymbol(order.currency);
        const total = parseFloat(order.totalAmount || 0).toFixed(2);
        const statusText = getStatusText(order);
        const paymentMethodText = getPaymentMethodText(order);

        let addressHtml = `<p class="text-muted">No address specified</p>`;
        if (order.address) {
            const addr = order.address;
            addressHtml = `
                <div class="address-box">
                    <strong>${escapeHtml(addr.fullName || 'Customer')}</strong><br>
                    ${escapeHtml(addr.streetAddress || '')}<br>
                    ${escapeHtml(addr.city || '')} ${escapeHtml(addr.postalCode || '')}<br>
                    ${escapeHtml(addr.country || '')}
                </div>
            `;
        }

        let subtotal = 0;
        let totalDiscount = 0;
        let totalTax = 0;

        const itemsHtml = (order.items || []).map(itemDTO => {
            const product = itemDTO.product || {};
            const productName = product.productName || itemDTO.productName || 'Digital Item';
            const imgPath = product.imagePath ? `${contextPath}/${product.imagePath}` : null;

            const price = parseFloat(itemDTO.pricePaid || itemDTO.price || 0);
            const discount = parseFloat(itemDTO.discountApplied || 0);
            const tax = parseFloat(itemDTO.taxPaid || 0);
            const lineTotal = itemDTO.lineTotal !== undefined ? parseFloat(itemDTO.lineTotal) : (price - discount + tax);

            subtotal += price;
            totalDiscount += discount;
            totalTax += tax;

            return `
                <tr class="order-item-row">
                    <td>
                        <div class="product-item-cell">
                            ${imgPath ? `<img src="${imgPath}" alt="${escapeHtml(productName)}" class="modal-product-thumb">` : ''}
                            <div>
                                <div class="product-item-title">${escapeHtml(productName)}</div>
                                <div class="product-item-sub">ID: ${escapeHtml(itemDTO.productId || 'N/A')}</div>
                            </div>
                        </div>
                    </td>
                    <td class="text-right">${symbol}${price.toFixed(2)}</td>
                    <td class="text-right">${discount > 0 ? `-${symbol}${discount.toFixed(2)}` : '—'}</td>
                    <td class="text-right">${tax > 0 ? `${symbol}${tax.toFixed(2)}` : '—'}</td>
                    <td class="text-right"><strong>${symbol}${lineTotal.toFixed(2)}</strong></td>
                </tr>
            `;
        }).join('') || `<tr><td colspan="5" class="text-center">No item details available</td></tr>`;

        body.innerHTML = `
            <div class="order-modal-wrapper">
                <div class="order-info-grid">
                    <div class="info-block">
                        <span class="info-label">Transaction ID</span>
                        <span class="info-value">${escapeHtml(order.transactionId || displayId)}</span>
                    </div>
                    <div class="info-block">
                        <span class="info-label">Date & Time</span>
                        <span class="info-value">${dateDisplay}</span>
                    </div>
                    <div class="info-block">
                        <span class="info-label">Payment Method</span>
                        <span class="info-value">${escapeHtml(paymentMethodText)}</span>
                    </div>
                    <div class="info-block">
                        <span class="info-label">Status</span>
                        <span class="status-badge status-${statusText.toLowerCase()}">${escapeHtml(statusText)}</span>
                    </div>
                </div>

                <hr class="panel-divider">

                <div class="order-shipping-section">
                    <h4 class="sub-panel-title">Billing & Delivery Details</h4>
                    ${addressHtml}
                </div>

                <hr class="panel-divider">

                <h4 class="sub-panel-title">Purchased Items</h4>
                <div class="table-responsive">
                    <table class="data-table modal-items-table">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th class="text-right">Price</th>
                                <th class="text-right">Discount</th>
                                <th class="text-right">Tax</th>
                                <th class="text-right">Total</th>
                            </tr>
                        </thead>
                        <tbody>${itemsHtml}</tbody>
                    </table>
                </div>

                <div class="order-summary-box">
                    <div class="summary-line"><span>Subtotal:</span> <span>${symbol}${subtotal.toFixed(2)}</span></div>
                    ${totalDiscount > 0 ? `<div class="summary-line text-discount"><span>Discounts:</span> <span>-${symbol}${totalDiscount.toFixed(2)}</span></div>` : ''}
                    ${totalTax > 0 ? `<div class="summary-line"><span>Taxes:</span> <span>${symbol}${totalTax.toFixed(2)}</span></div>` : ''}
                    <div class="summary-line summary-total"><span>Total Paid:</span> <span>${symbol}${total}</span></div>
                </div>
            </div>
        `;

        const printBtn = document.getElementById('btnPrintInvoiceFromModal');
        if (printBtn) {
            printBtn.onclick = () => generateAndPrintInvoice(order);
        }

        if (typeof openModal === 'function') {
            openModal('modalOrderDetails');
        } else {
            document.getElementById('modalOrderDetails')?.classList.add('show');
        }
    }

    async function fetchAndPrintInvoice(orderId) {
        let order = cachedOrders.find(o => String(o.orderId || o.transactionId) === String(orderId));
        try {
            const resp = await fetch(`${contextPath}/orders/detail?id=${encodeURIComponent(orderId)}&format=json`, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            if (resp.ok) {
                const resData = await resp.json();
                if (resData.success && resData.order) order = resData.order;
            }
        } catch(e) {
            console.warn("Invoice fetch fallback", e);
        }
        if (order) generateAndPrintInvoice(order);
    }

    function generateAndPrintInvoice(order) {
        const orderId = order.orderId || order.transactionId || 'INV-001';
        const parsedDate = parseOrderDate(order.createdAt);
        const dateStr = parsedDate ? parsedDate.toLocaleDateString() : new Date().toLocaleDateString();
        const symbol = getSymbol(order.currency);
        const total = parseFloat(order.totalAmount || 0).toFixed(2);
        const paymentMethodText = getPaymentMethodText(order);

        const username = document.querySelector('.account-username')?.textContent?.trim() || 'Customer';
        const email = document.querySelector('.account-email')?.textContent?.trim() || '';

        let addressBlock = `${escapeHtml(username)}<br>${escapeHtml(email)}`;
        if (order.address) {
            const a = order.address;
            addressBlock = `
                <strong>${escapeHtml(a.fullName || username)}</strong><br>
                ${escapeHtml(a.streetAddress || '')}<br>
                ${escapeHtml(a.city || '')} ${escapeHtml(a.postalCode || '')}<br>
                ${escapeHtml(a.country || '')}<br>
                ${escapeHtml(email)}
            `;
        }

        const itemsRows = (order.items || []).map(itemDTO => {
            const product = itemDTO.product || {};
            const name = product.productName || itemDTO.productName || 'Minecraft Schematic';
            const price = parseFloat(itemDTO.pricePaid || itemDTO.price || 0);
            const discount = parseFloat(itemDTO.discountApplied || 0);
            const lineTotal = itemDTO.lineTotal !== undefined ? parseFloat(itemDTO.lineTotal) : (price - discount);

            return `
                <tr>
                    <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">
                        <strong>${escapeHtml(name)}</strong>
                        <div style="font-size: 11px; color: #64748b;">Digital Download Product</div>
                    </td>
                    <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right;">${symbol}${price.toFixed(2)}</td>
                    <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right;">${discount > 0 ? `-${symbol}${discount.toFixed(2)}` : '—'}</td>
                    <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold;">${symbol}${lineTotal.toFixed(2)}</td>
                </tr>
            `;
        }).join('') || `
            <tr>
                <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">Minecraft Digital Content</td>
                <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right;">${symbol}${total}</td>
                <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right;">—</td>
                <td style="padding: 12px; border-bottom: 1px solid #e2e8f0; text-align: right; font-weight: bold;">${symbol}${total}</td>
            </tr>
        `;

        const invoiceHtml = `
            <!DOCTYPE html>
            <html>
            <head>
                <title>Invoice #${orderId} - SchemeCraft</title>
                <style>
                    body { font-family: 'Inter', system-ui, -apple-system, sans-serif; color: #1e293b; padding: 40px; max-width: 850px; margin: 0 auto; background: #fff; }
                    .header { display: flex; justify-content: space-between; align-items: flex-start; border-bottom: 3px solid #3C8527; padding-bottom: 20px; }
                    .brand { font-size: 26px; font-weight: 800; color: #3C8527; letter-spacing: -0.5px; }
                    .invoice-badge { background: #f1f5f9; padding: 6px 12px; border-radius: 6px; font-size: 13px; font-weight: 700; color: #475569; text-transform: uppercase; }
                    .meta-grid { display: flex; justify-content: space-between; margin-top: 30px; line-height: 1.6; font-size: 14px; }
                    .meta-box h4 { margin: 0 0 8px 0; color: #64748b; font-size: 12px; text-transform: uppercase; letter-spacing: 0.5px; }
                    table { width: 100%; border-collapse: collapse; margin-top: 35px; font-size: 14px; }
                    th { background-color: #f8fafc; text-align: left; padding: 12px; border-bottom: 2px solid #cbd5e1; color: #475569; font-size: 12px; text-transform: uppercase; }
                    .summary-container { margin-top: 30px; display: flex; justify-content: flex-end; }
                    .summary-table { width: 280px; }
                    .summary-table div { display: flex; justify-content: space-between; padding: 6px 0; font-size: 14px; }
                    .summary-table .grand-total { font-size: 18px; font-weight: 800; color: #3C8527; border-top: 2px solid #e2e8f0; padding-top: 10px; margin-top: 6px; }
                    .footer { margin-top: 60px; font-size: 12px; color: #94a3b8; text-align: center; border-top: 1px solid #f1f5f9; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <div>
                        <div class="brand">SCHEMECRAFT</div>
                        <div style="font-size: 12px; color: #64748b; margin-top: 4px;">Digital Minecraft Schematics Platform</div>
                    </div>
                    <div style="text-align: right;">
                        <span class="invoice-badge">Official Receipt</span>
                        <div style="margin-top: 8px; font-weight: bold; font-size: 15px;">#${escapeHtml(String(orderId))}</div>
                    </div>
                </div>

                <div class="meta-grid">
                    <div class="meta-box">
                        <h4>Billed To</h4>
                        ${addressBlock}
                    </div>
                    <div class="meta-box" style="text-align: right;">
                        <h4>Order Details</h4>
                        <strong>Date:</strong> ${dateStr}<br>
                        <strong>Payment Method:</strong> ${escapeHtml(paymentMethodText)}<br>
                        <strong>Payment Status:</strong> <span style="color: #16a34a;">PAID</span><br>
                        <strong>Transaction:</strong> ${escapeHtml(String(order.transactionId || 'N/A'))}
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Item Description</th>
                            <th style="text-align: right;">Price</th>
                            <th style="text-align: right;">Discount</th>
                            <th style="text-align: right;">Amount</th>
                        </tr>
                    </thead>
                    <tbody>${itemsRows}</tbody>
                </table>

                <div class="summary-container">
                    <div class="summary-table">
                        <div class="grand-total">
                            <span>Total Paid:</span>
                            <span>${symbol}${total}</span>
                        </div>
                    </div>
                </div>

                <div class="footer">
                    Thank you for your purchase on SchemeCraft!<br>
                    This is an official digital receipt for your order.
                </div>
            </body>
            </html>
        `;

        const win = window.open('', '_blank');
        if (win) {
            win.document.write(invoiceHtml);
            win.document.close();
            win.focus();
            setTimeout(() => win.print(), 500);
        }
    }
});
