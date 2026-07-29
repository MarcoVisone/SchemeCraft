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
    }, 3200);
}

function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('show');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('show');
}

function initModals() {
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.onclick = () => {
            const targetModal = btn.getAttribute('data-close-modal');
            closeModal(targetModal);
        };
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, m => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    }[m]));
}

document.addEventListener('DOMContentLoaded', initModals);
