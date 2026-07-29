<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="acc" value="${not empty account ? account : sessionScope.userSession.account}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile Settings - SchemeCraft</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/account.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">

    <script src="${pageContext.request.contextPath}/js/header.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/account/account-common.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/account/account-profile.js" defer></script>
</head>
<body class="account-body">

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="account-container"
      data-context-path="${pageContext.request.contextPath}"
      data-account-id="${acc.accountId}">

    <header class="account-header-summary">
        <div class="profile-avatar-wrapper">
            <c:choose>
                <c:when test="${not empty acc.profileImagePath}">
                    <img id="headerAvatarImg" src="${pageContext.request.contextPath}/${acc.profileImagePath}" alt="${acc.username}" class="profile-avatar-img">
                </c:when>
                <c:otherwise>
                    <div id="headerAvatarFallback" class="profile-avatar-placeholder">
                            ${acc.username.substring(0, 1).toUpperCase()}
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
        <div class="profile-summary-details">
            <h1 class="account-username">${acc.username}</h1>
            <p class="account-email">${acc.email}</p>
            <span class="account-role-badge">Member</span>
        </div>
    </header>

    <div class="account-layout">

        <nav class="account-sidebar" aria-label="Account Navigation">
            <ul class="account-tabs">
                <li>
                    <a href="${pageContext.request.contextPath}/account/profile" class="tab-link active">
                        <svg class="tab-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
                        Profile Settings
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/account/library" class="tab-link">
                        <svg class="tab-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H8V4h12v12z"/></svg>
                        My Library
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/account/orders" class="tab-link">
                        <svg class="tab-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M19 3h-4.18C14.4 1.84 13.3 1 12 1s-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm7 16H5V5h2v2h10V5h2v14z"/></svg>
                        Orders & Invoices
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/account/addresses" class="tab-link">
                        <svg class="tab-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/></svg>
                        Addresses
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/account/payment-methods" class="tab-link">
                        <svg class="tab-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
                        Payment Methods
                    </a>
                </li>
                <li class="tab-logout-item">
                    <a href="${pageContext.request.contextPath}/auth/logout" class="tab-link tab-logout">
                        <svg class="tab-icon" viewBox="0 0 24 24">
                            <path fill="currentColor" d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
                        </svg>
                        Logout
                    </a>
                </li>
            </ul>
        </nav>

        <section class="account-content">
            <div id="tab-profile" class="tab-content active">
                <div class="card-panel">
                    <h2 class="panel-title">Profile Preferences</h2>
                    <form id="formUpdateProfile" class="account-form" enctype="multipart/form-data">

                        <div class="form-group avatar-upload-group">
                            <label class="form-label">Avatar Image</label>
                            <div class="avatar-preview-container">
                                <c:choose>
                                    <c:when test="${not empty acc.profileImagePath}">
                                        <img id="avatarPreview" src="${pageContext.request.contextPath}/${acc.profileImagePath}"
                                             alt="Avatar Preview" class="avatar-preview"
                                             onerror="this.style.display='none'; document.getElementById('avatarPlaceholder').style.display='flex';">
                                        <div id="avatarPlaceholder" class="profile-avatar-placeholder" style="display:none;">
                                                ${acc.username.substring(0, 1).toUpperCase()}
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <img id="avatarPreview" src="" alt="Avatar Preview" class="avatar-preview" style="display:none;">
                                        <div id="avatarPlaceholder" class="profile-avatar-placeholder">
                                                ${acc.username.substring(0, 1).toUpperCase()}
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <div class="avatar-controls">
                                    <input type="file" id="profileImageFile" name="profileImageFile" accept="image/*" class="file-input">
                                    <label for="profileImageFile" class="btn-action btn-secondary-action">Upload New Photo</label>
                                    <small class="form-hint">Max file size: 10MB (JPG, PNG, WEBP)</small>
                                </div>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="bio" class="form-label">Biography <span class="optional">(Optional)</span></label>
                            <textarea id="bio" name="bio" class="auth-textarea form-control" rows="4" maxlength="255" style="resize: none;"
                                      placeholder="Tell us a little about your building style...">${acc.bio}</textarea>
                            <div style="text-align: right; font-size: 0.8rem; color: #888; margin-top: 4px;">
                                <span id="bio-char-count">0</span>/255 characters
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="countryId" class="form-label">Country <span class="required">*</span></label>
                            <select id="countryId" name="countryId" class="auth-select form-control" required>
                                <option value="" disabled ${empty acc.countryId ? 'selected' : ''}>Select your country</option>
                                <c:forEach var="country" items="${countries}">
                                    <option value="${country.countryId}" ${acc.countryId == country.countryId ? 'selected' : ''}>
                                            ${country.countryName}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="currencyId" class="form-label">Preferred Currency <span class="required">*</span></label>
                            <select id="currencyId" name="currencyId" class="auth-select form-control" required>
                                <option value="" disabled ${empty acc.currencyId ? 'selected' : ''}>Select currency</option>
                                <c:forEach var="currency" items="${currencies}">
                                    <option value="${currency.currencyId}" ${acc.currencyId == currency.currencyId ? 'selected' : ''}>
                                            ${currency.currencyName} (${currency.symbol})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="languageId" class="form-label">Interface Language <span class="required">*</span></label>
                            <select id="languageId" name="languageId" class="auth-select form-control" required>
                                <option value="" disabled ${empty acc.languageId ? 'selected' : ''}>Select language</option>
                                <c:forEach var="language" items="${languages}">
                                    <option value="${language.languageId}" ${acc.languageId == language.languageId ? 'selected' : ''}>
                                            ${language.languageName}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-actions margin-top-md">
                            <button type="submit" class="btn-action btn-primary-action">Save Profile Changes</button>
                        </div>
                    </form>
                </div>

                <div class="card-panel margin-top-lg">
                    <h2 class="panel-title">Account Security</h2>

                    <form id="formChangeUsername" class="account-form inline-form">
                        <div class="form-group flex-1">
                            <label for="newUsername" class="form-label">Username</label>
                            <input type="text" id="newUsername" name="newUsername" class="form-control" value="${acc.username}" required>
                        </div>
                        <button type="submit" class="btn-action btn-secondary-action">Update Username</button>
                    </form>

                    <hr class="panel-divider">

                    <form id="formChangeEmail" class="account-form inline-form">
                        <div class="form-group flex-1">
                            <label for="newEmail" class="form-label">Email Address</label>
                            <input type="email" id="newEmail" name="newEmail" class="form-control" value="${acc.email}" required>
                        </div>
                        <button type="submit" class="btn-action btn-secondary-action">Update Email</button>
                    </form>

                    <hr class="panel-divider">

                    <form id="formChangePassword" class="account-form">
                        <h3 class="sub-panel-title">Change Password</h3>
                        <div class="form-row">
                            <div class="form-group col-md-6">
                                <label for="oldPassword" class="form-label">Current Password</label>
                                <input type="password" id="oldPassword" name="oldPassword" class="form-control" required>
                            </div>
                            <div class="form-group col-md-6">
                                <label for="newPassword" class="form-label">New Password</label>
                                <input type="password" id="newPassword" name="newPassword" class="form-control" required>
                            </div>
                        </div>
                        <div class="form-actions">
                            <button type="submit" class="btn-action btn-secondary-action">Change Password</button>
                        </div>
                    </form>

                    <hr class="panel-divider">

                    <div class="danger-zone-box">
                        <div class="danger-zone-info">
                            <h3 class="danger-title">Deactivate Account</h3>
                            <p class="danger-desc">Once deactivated, you will lose access to your account and saved preferences.</p>
                        </div>
                        <button type="button" id="btnDeactivateAccount" class="btn-action btn-danger-action">
                            Deactivate Account
                        </button>
                    </div>
                </div>
            </div>
        </section>
    </div>
</main>

<div id="modalDeactivateAccount" class="modal-overlay">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="modal-title" style="color: #ff6b6b;">Deactivate Account</h3>
            <button type="button" class="modal-close" data-close-modal="modalDeactivateAccount">&times;</button>
        </div>
        <div class="modal-body">
            <p style="color: var(--text-secondary); margin-bottom: 1rem;">
                Warning: This action is permanent. Type <strong style="color: #ff6b6b;">EXTERMINATE</strong> below to confirm.
            </p>
            <div class="form-group">
                <input type="text" id="deactivateConfirmInput" class="auth-input form-control" placeholder="Type EXTERMINATE here" autocomplete="off">
            </div>
        </div>
        <div class="modal-actions">
            <button type="button" class="btn-action btn-secondary-action" data-close-modal="modalDeactivateAccount">Cancel</button>
            <button type="button" id="btnConfirmDeactivate" class="btn-action btn-danger-action" disabled>Deactivate Account</button>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
