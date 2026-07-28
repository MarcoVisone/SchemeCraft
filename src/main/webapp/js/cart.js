// Handles guest/authenticated cart interactions on cart.jsp: item removal,
// cart clearing, and checkout, all via AJAX against CartServlet endpoints.
// Reuses the same toast pattern as product-detail.js for visual consistency.

function showToast(message, type = 'info') {
    let toast = document.getElementById('inline-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'inline-toast';
        toast.className = 'inline-toast';
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.className = `inline-toast ${type} show`;

    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

function removeFromCart(productId) {
    fetch(`${CONTEXT_PATH}/cart/remove`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: `productId=${encodeURIComponent(productId)}`
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                window.location.reload();
            } else {
                showToast(data.error || 'Unable to remove product from cart.', 'error');
            }
        })
        .catch(() => showToast('Network error while removing product from cart.', 'error'));
}

function clearCart() {
    openConfirmModal({
        title: 'Clear cart',
        message: 'Remove all items from your cart?',
        confirmLabel: 'Clear cart',
        onConfirm: () => {
            fetch(`${CONTEXT_PATH}/cart/clear`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        showToast(data.error || 'Unable to clear cart.', 'error');
                    }
                })
                .catch(() => showToast('Network error while clearing cart.', 'error'));
        }
    });
}

function checkout() {
    openConfirmModal({
        title: 'Confirm checkout',
        message: 'Place your order and charge your default payment method?',
        confirmLabel: 'Place order',
        onConfirm: () => {
            fetch(`${CONTEXT_PATH}/cart/checkout`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        showToast('Order placed successfully!', 'success');
                        setTimeout(() => window.location.reload(), 1500);
                    } else {
                        showToast(data.error || 'Unable to complete checkout.', 'error');
                    }
                })
                .catch(() => showToast('Network error during checkout.', 'error'));
        }
    });
}