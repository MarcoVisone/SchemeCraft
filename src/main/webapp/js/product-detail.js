document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.product-detail-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';
    const productId = container.dataset.productId || '';

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

    const tabLinks = document.querySelectorAll('.tab-link');
    const tabContents = document.querySelectorAll('.tab-content');

    tabLinks.forEach(link => {
        link.addEventListener('click', (evt) => {
            const tabId = link.getAttribute('data-tab');

            tabContents.forEach(content => content.classList.remove('active'));
            tabLinks.forEach(btn => btn.classList.remove('active'));

            const targetContent = document.getElementById(tabId);
            if (targetContent) targetContent.classList.add('active');
            evt.currentTarget.classList.add('active');
        });
    });

    const btnDownload = document.getElementById('btnDownload');
    if (btnDownload && btnDownload.tagName.toLowerCase() !== 'a') {
        btnDownload.addEventListener('click', () => {
            const versionsTabBtn = document.querySelector('.tab-link[data-tab="tab-versions"]');
            if (versionsTabBtn) {
                versionsTabBtn.click();
                versionsTabBtn.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }

    const btnAddToCart = document.getElementById('btnAddToCart');
    if (btnAddToCart) {
        btnAddToCart.addEventListener('click', async () => {
            try {
                const response = await fetch(`${contextPath}/cart/add`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams({ productId: productId })
                });

                let data = {};
                try {
                    data = await response.json();
                } catch (e) {
                    // Ignora parsing se non è JSON
                }

                if (response.ok && data.success) {
                    showToast(data.message || 'Product added to cart!', 'success');
                } else if (response.status === 401) {
                    showToast('Please login to add items to cart.', 'warning');
                    setTimeout(() => window.location.href = `${contextPath}/login`, 1500);
                } else {
                    showToast(data.error || 'Could not add product to cart.', 'warning');
                }
            } catch (err) {
                console.error('Cart error:', err);
                showToast('Network error. Try again.', 'error');
            }
        });
    }

    const btnBuyNow = document.getElementById('btnBuyNow');
    if (btnBuyNow) {
        btnBuyNow.addEventListener('click', () => {
            window.location.href = `${contextPath}/checkout?productId=${productId}`;
        });
    }

    const btnFavorite = document.getElementById('btnFavorite');
    if (btnFavorite) {
        btnFavorite.addEventListener('click', async () => {
            if (btnFavorite.disabled) return;
            btnFavorite.disabled = true;

            const isCurrentlyActive = btnFavorite.classList.contains('active');
            const actionPath = isCurrentlyActive ? '/favorites/remove' : '/favorites/add';
            const endpoint = `${contextPath}${actionPath}`;

            try {
                const response = await fetch(endpoint, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams({ productId: productId })
                });

                let data = {};
                try {
                    data = await response.json();
                } catch (e) {
                }

                if (response.ok && data.success) {
                    btnFavorite.classList.toggle('active');
                    const defaultMsg = !isCurrentlyActive ? 'Added to favorites!' : 'Removed from favorites.';
                    showToast(data.message || defaultMsg, !isCurrentlyActive ? 'success' : 'info');
                }
                else if (response.status === 401) {
                    showToast('Please login to manage favorites.', 'warning');
                    setTimeout(() => window.location.href = `${contextPath}/login`, 1500);
                }
                else {
                    const errorMsg = data.error || data.message || 'Could not update favorites.';
                    showToast(errorMsg, 'error');
                }
            } catch (err) {
                console.error('Error toggling favorite:', err);
                showToast('Connection error. Please try again.', 'error');
            } finally {
                btnFavorite.disabled = false;
            }
        });
    }

    const starPicker = document.getElementById('starPicker');
    const ratingInput = document.getElementById('ratingInput');
    const reviewForm = document.getElementById('reviewForm');

    if (starPicker && ratingInput) {
        const starBtns = starPicker.querySelectorAll('.star-btn');

        function setStarsState(val) {
            starBtns.forEach(btn => {
                const btnVal = parseInt(btn.dataset.value);
                if (btnVal <= val) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            });
        }

        setStarsState(parseInt(ratingInput.value) || 5);

        starBtns.forEach(btn => {
            btn.addEventListener('mouseenter', () => {
                setStarsState(parseInt(btn.dataset.value));
            });

            btn.addEventListener('mouseleave', () => {
                setStarsState(parseInt(ratingInput.value) || 5);
            });

            btn.addEventListener('click', () => {
                const selectedVal = btn.dataset.value;
                ratingInput.value = selectedVal;
                setStarsState(selectedVal);
            });
        });
    }

    if (reviewForm) {
        reviewForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const btnSubmit = document.getElementById('btnSubmitReview');
            const commentInput = document.getElementById('reviewComment');

            if (btnSubmit) btnSubmit.disabled = true;

            const params = new URLSearchParams({
                productId: productId,
                rating: ratingInput ? ratingInput.value : '5',
                comment: commentInput ? commentInput.value : ''
            });

            try {
                const response = await fetch(`${contextPath}/reviews/add`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: params
                });

                let data = {};
                try {
                    data = await response.json();
                } catch (e) {
                }

                if (response.ok && data.success) {
                    showToast(data.message || 'Review submitted successfully!', 'success');
                    setTimeout(() => window.location.reload(), 1200);
                } else if (response.status === 401) {
                    showToast('Please login to leave a review.', 'warning');
                    setTimeout(() => window.location.href = `${contextPath}/login`, 1500);
                } else {
                    const errorMsg = data.error || data.message || 'Could not save review.';
                    showToast(errorMsg, 'error');
                }
            } catch (err) {
                console.error('Error submitting review:', err);
                showToast('Connection error. Please try again.', 'error');
            } finally {
                if (btnSubmit) btnSubmit.disabled = false;
            }
        });
    }

    const btnDeleteReview = document.getElementById('btnDeleteReview');
    if (btnDeleteReview) {
        btnDeleteReview.addEventListener('click', async () => {
            btnDeleteReview.disabled = true;

            try {
                const response = await fetch(`${contextPath}/reviews/delete`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams({ productId: productId })
                });

                let data = {};
                try {
                    data = await response.json();
                } catch (e) {
                }

                if (response.ok && data.success) {
                    showToast(data.message || 'Review deleted successfully!', 'success');
                    setTimeout(() => window.location.reload(), 1200);
                } else {
                    const errorMsg = data.error || data.message || 'Could not delete review.';
                    showToast(errorMsg, 'error');
                    btnDeleteReview.disabled = false;
                }
            } catch (err) {
                console.error('Error deleting review:', err);
                showToast('Connection error. Please try again.', 'error');
                btnDeleteReview.disabled = false;
            }
        });
    }
});
