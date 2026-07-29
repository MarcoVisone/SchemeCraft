document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.account-container');
    if (!container) return;

    const contextPath = container.dataset.contextPath || '';

    const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,50}$/;
    const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const PASSWORD_REGEX = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$/;

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

    // Form Change Username (con Regex & Check Esistenza)
    const formChangeUsername = document.getElementById('formChangeUsername');
    if (formChangeUsername) {
        formChangeUsername.addEventListener('submit', async (e) => {
            e.preventDefault();
            const newUsername = document.getElementById('newUsername').value.trim();

            if (!USERNAME_REGEX.test(newUsername)) {
                showToast('Username must be 3-50 alphanumeric characters or underscores.', 'error');
                return;
            }

            try {
                const checkResp = await fetch(`${contextPath}/auth/check-username?username=${encodeURIComponent(newUsername)}`);
                const checkData = await checkResp.json();
                const exists = checkData.exists ?? checkData.data?.exists ?? false;

                if (exists) {
                    showToast('Username is already taken by another user.', 'error');
                    return;
                }

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

    const formChangeEmail = document.getElementById('formChangeEmail');
    if (formChangeEmail) {
        formChangeEmail.addEventListener('submit', async (e) => {
            e.preventDefault();
            const newEmail = document.getElementById('newEmail').value.trim();

            if (!EMAIL_REGEX.test(newEmail)) {
                showToast('Please enter a valid email address.', 'error');
                return;
            }

            try {
                const checkResp = await fetch(`${contextPath}/auth/check-email?email=${encodeURIComponent(newEmail)}`);
                const checkData = await checkResp.json();
                const exists = checkData.exists ?? checkData.data?.exists ?? false;

                if (exists) {
                    showToast('Email is already registered by another account.', 'error');
                    return;
                }

                const response = await fetch(`${contextPath}/account/change-email`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest'
                    },
                    body: new URLSearchParams({ newEmail })
                });
                const data = await response.json();
                if (response.ok && data.success) {
                    showToast(data.message || 'Email updated!', 'success');
                    const headerEmail = document.querySelector('.account-email');
                    if (headerEmail) headerEmail.textContent = newEmail;
                } else {
                    showToast(data.error || 'Email change failed.', 'error');
                }
            } catch (err) {
                showToast('Connection error while changing email.', 'error');
            }
        });
    }

    // Form Change Password (con Regex)
    const formChangePassword = document.getElementById('formChangePassword');
    if (formChangePassword) {
        formChangePassword.addEventListener('submit', async (e) => {
            e.preventDefault();
            const oldPassword = document.getElementById('oldPassword').value;
            const newPassword = document.getElementById('newPassword').value;

            if (!PASSWORD_REGEX.test(newPassword)) {
                showToast('Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, and one number.', 'error');
                return;
            }

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
