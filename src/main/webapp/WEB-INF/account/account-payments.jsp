<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="acc" value="${not empty account ? account : sessionScope.userSession.account}" />

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payment Methods - SchemeCraft</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/account.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">

    <script src="${pageContext.request.contextPath}/js/account/account-common.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/account/account-orders.js" defer></script>
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
                    <a href="${pageContext.request.contextPath}/account/profile" class="tab-link">
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
                    <a href="${pageContext.request.contextPath}/account/payment-methods" class="tab-link active">
                        <svg class="tab-icon" viewBox="0 0 24 24"><path fill="currentColor" d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
                        Payment Methods
                    </a>
                </li>
            </ul>
        </nav>

        <section class="account-content">
            <div id="tab-payments" class="tab-content active">
                <div class="card-panel">
                    <div class="panel-header-flex">
                        <h2 class="panel-title">Payment Methods</h2>
                        <button type="button" id="btnOpenAddPaymentModal" class="btn-action btn-primary-action btn-sm">+ Add Payment Method</button>
                    </div>

                    <div id="paymentMethodsContainer" class="payment-grid margin-top-md">
                        <div class="loading-spinner">Loading payment methods...</div>
                    </div>
                </div>
            </div>
        </section>
    </div>
</main>

<div id="modalAddPayment" class="modal-overlay">
    <div class="modal-card">
        <div class="modal-header">
            <h3 class="modal-title">Add Payment Method</h3>
            <button type="button" class="modal-close" data-close-modal="modalAddPayment">&times;</button>
        </div>
        <form id="formAddPayment" class="account-form">
            <div class="form-group">
                <label for="methodType" class="form-label">Payment Type</label>
                <select id="methodType" name="methodType" class="form-control auth-select">
                    <option value="1">Credit / Debit Card</option>
                    <option value="2">PayPal Account</option>
                </select>
            </div>

            <div id="creditCardFields" class="payment-type-fields">
                <div class="form-group">
                    <label for="cardNumber" class="form-label">Card Number</label>
                    <input type="text" id="cardNumber" name="cardNumber" class="form-control" placeholder="4532 •••• •••• 8890" maxlength="19">
                </div>
                <div class="form-row">
                    <div class="form-group col-md-4">
                        <label for="cardExpiration" class="form-label">Expiry (MM/YY)</label>
                        <input type="text" id="cardExpiration" name="cardExpiration" class="form-control" placeholder="12/28" maxlength="5">
                    </div>
                    <div class="form-group col-md-4">
                        <label for="cvv" class="form-label">CVV</label>
                        <input type="password" id="cvv" name="cvv" class="form-control" placeholder="123" maxlength="4">
                    </div>
                    <div class="form-group col-md-4">
                        <label for="cardBrand" class="form-label">Brand</label>
                        <input type="text" id="cardBrand" name="cardBrand" class="form-control" placeholder="Visa, Mastercard">
                    </div>
                </div>
            </div>

            <div id="paypalFields" class="payment-type-fields" style="display: none;">
                <div class="form-group">
                    <label for="paypalEmail" class="form-label">PayPal Email Address</label>
                    <input type="email" id="paypalEmail" name="paypalEmail" class="form-control" placeholder="user@example.com">
                </div>
            </div>

            <div class="form-group checkbox-group">
                <label class="checkbox-label">
                    <input type="checkbox" name="isDefault" value="true">
                    Set as default payment method
                </label>
            </div>

            <div class="modal-actions">
                <button type="button" class="btn-action btn-secondary-action" data-close-modal="modalAddPayment">Cancel</button>
                <button type="submit" class="btn-action btn-primary-action">Add Payment Method</button>
            </div>
        </form>
    </div>
</div>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
