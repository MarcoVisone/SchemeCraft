document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';

    initAvatarPreview();
    initBioCounter();
    initDeactivateAccount();

    function initBioCounter() {
        const bioInput = document.getElementById('bio');
        const bioCount = document.getElementById('bio-char-count');
        if (bioInput && bioCount) {
            const updateCount = () => { bioCount.textContent = bioInput.value.length; };
            bioInput.addEventListener('input', updateCount);
            updateCount();
        }
    }

    function initAvatarPreview() {
        const fileInput = document.getElementById('profileImageFile');
        const previewImg = document.getElementById('avatarPreview');
        const placeholder = document.getElementById('avatarPlaceholder');

        if (fileInput && previewImg) {
            fileInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = (evt) => {
                        previewImg.src = evt.target.result;
                        previewImg.style.display = 'block';
                        if (placeholder) placeholder.style.display = 'none';
                    };
                    reader.readAsDataURL(file);
                }
            });
        }
    }

    // Form Update Profile
    const formUpdateProfile = document.getElementById('formUpdateProfile');
    if (formUpdateProfile) {
        formUpdateProfile.addEventListener('submit', async (e) => {
            e.preventDefault();
            try {
                const response = await fetch(`${contextPath}/account/update-profile`, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    body: new FormData(formUpdateProfile)
                });
                const data = await response.json();
                if (response.ok && data.success) {
                    showToast(data.message || 'Profile updated successfully!', 'success');
                    if (data.profileImagePath) {
                        const hAvatar = document.getElementById('headerAvatarImg');
                        if (hAvatar) hAvatar.src = `${contextPath}/${data.profileImagePath}`;
                    }
                } else {
                    showToast(data.error || 'Failed to update profile.', 'error');
                }
            } catch (err) {
                showToast('Connection error during profile update.', 'error');
            }
        });
    }

    // Form Change Username
    const formChangeUsername = document.getElementById('formChangeUsername');
    if (formChangeUsername) {
        formChangeUsername.addEventListener('submit', async (e) => {
            e.preventDefault();
            const newUsername = document.getElementById('newUsername').value;
            try {
                const response = await fetch(`${contextPath}/account/change-username`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams({ newUsername })
                });
                const data = await response.json();
                if (response.ok && data.success) {
                    showToast(data.message || 'Username updated!', 'success');
                    const headerUsername = document.querySelector('.account-username');
                    if (headerUsername) headerUsername.textContent = newUsername;
                } else {
                    showToast(data.error || 'Username change failed.', 'error');
                }
            } catch (err) {
                showToast('Connection error while changing username.', 'error');
            }
        });
    }

    // Form Change Password
    const formChangePassword = document.getElementById('formChangePassword');
    if (formChangePassword) {
        formChangePassword.addEventListener('submit', async (e) => {
            e.preventDefault();
            const oldPassword = document.getElementById('oldPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            try {
                const response = await fetch(`${contextPath}/account/change-password`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams({ oldPassword, newPassword })
                });
                const data = await response.json();
                if (response.ok && data.success) {
                    showToast(data.message || 'Password updated!', 'success');
                    formChangePassword.reset();
                } else {
                    showToast(data.error || 'Password update failed.', 'error');
                }
            } catch (err) {
                showToast('Connection error while changing password.', 'error');
            }
        });
    }

    function initDeactivateAccount() {
        const btnDeactivateTrigger = document.getElementById('btnDeactivateAccount');
        const deactivateInput = document.getElementById('deactivateConfirmInput');
        const btnConfirmDeactivate = document.getElementById('btnConfirmDeactivate');

        if (btnDeactivateTrigger) {
            btnDeactivateTrigger.addEventListener('click', (e) => {
                e.preventDefault();
                if (deactivateInput) deactivateInput.value = '';
                if (btnConfirmDeactivate) btnConfirmDeactivate.disabled = true;
                openModal('modalDeactivateAccount');
            });
        }

        if (deactivateInput && btnConfirmDeactivate) {
            deactivateInput.addEventListener('input', () => {
                btnConfirmDeactivate.disabled = (deactivateInput.value.trim() !== 'EXTERMINATE');
            });

            btnConfirmDeactivate.addEventListener('click', async () => {
                if (deactivateInput.value.trim() !== 'EXTERMINATE') return;
                try {
                    btnConfirmDeactivate.disabled = true;
                    const response = await fetch(`${contextPath}/account/deactivate`, {
                        method: 'POST',
                        headers: { 'X-Requested-With': 'XMLHttpRequest' }
                    });
                    const result = await response.json();
                    if (response.ok && result.success) {
                        showToast('Account successfully deactivated.', 'success');
                        setTimeout(() => { window.location.href = `${contextPath}/auth/login`; }, 1500);
                    } else {
                        showToast(result.error || 'Failed to deactivate account.', 'error');
                        btnConfirmDeactivate.disabled = false;
                    }
                } catch (err) {
                    showToast('Connection error. Please try again.', 'error');
                    btnConfirmDeactivate.disabled = false;
                }
            });
        }
    }
});
