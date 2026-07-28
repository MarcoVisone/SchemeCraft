// Generic confirmation modal, reused across pages (product-detail, cart) for any
// "are you sure?" flow. Injects its own markup once, then can be opened repeatedly
// with different messages and confirm callbacks.

(function () {
    let modalEl = null;

    function ensureModal() {
        if (modalEl) return modalEl;

        modalEl = document.createElement('div');
        modalEl.className = 'confirm-modal-overlay';
        modalEl.innerHTML = `
            <div class="confirm-modal">
                <h3 class="confirm-modal__title" id="confirmModalTitle"></h3>
                <p class="confirm-modal__message" id="confirmModalMessage"></p>
                <div class="confirm-modal__actions">
                    <button type="button" class="confirm-modal__btn confirm-modal__btn--cancel" id="confirmModalCancel">Cancel</button>
                    <button type="button" class="confirm-modal__btn confirm-modal__btn--confirm" id="confirmModalConfirm">Confirm</button>
                </div>
            </div>
        `;
        document.body.appendChild(modalEl);

        modalEl.addEventListener('click', (e) => {
            if (e.target === modalEl) closeConfirmModal();
        });

        return modalEl;
    }

    window.openConfirmModal = function ({ title, message, confirmLabel, onConfirm }) {
        const modal = ensureModal();

        modal.querySelector('#confirmModalTitle').textContent = title || 'Are you sure?';
        modal.querySelector('#confirmModalMessage').textContent = message || '';

        const confirmBtn = modal.querySelector('#confirmModalConfirm');
        confirmBtn.textContent = confirmLabel || 'Confirm';

        // Replace the button to strip any previously attached listener
        const freshConfirmBtn = confirmBtn.cloneNode(true);
        confirmBtn.parentNode.replaceChild(freshConfirmBtn, confirmBtn);

        freshConfirmBtn.addEventListener('click', () => {
            closeConfirmModal();
            if (typeof onConfirm === 'function') onConfirm();
        });

        modal.querySelector('#confirmModalCancel').addEventListener('click', closeConfirmModal, { once: true });

        modal.classList.add('show');
    };

    window.closeConfirmModal = function () {
        if (modalEl) modalEl.classList.remove('show');
    };
})();