<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SchemeCraft - Sign Up</title>

    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Press+Start+2P&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">

    <script src="${pageContext.request.contextPath}/js/header.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/register.js" defer></script>
</head>
<body class="auth-body">

<%@ include file="/WEB-INF/fragments/header.jsp" %>

<main class="auth-container">
    <div class="auth-card">

        <div class="auth-header">
            <h1 class="auth-title">Create Your Account</h1>
            <p class="auth-subtitle">Join SchemeCraft and start building today</p>

            <div class="step-wizard">
                <div class="step-item active" id="step-indicator-1">
                    <span class="step-number">1</span>
                    <span class="step-label">Account</span>
                </div>
                <div class="step-line"></div>
                <div class="step-item" id="step-indicator-2">
                    <span class="step-number">2</span>
                    <span class="step-label">Preferences</span>
                </div>
                <div class="step-line"></div>
                <div class="step-item" id="step-indicator-3">
                    <span class="step-number">3</span>
                    <span class="step-label">Profile</span>
                </div>
            </div>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="auth-error-alert">
                <span>${errorMessage}</span>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth/register"
              method="POST"
              id="registrationForm"
              class="auth-form"
              enctype="multipart/form-data"
              data-context-path="${pageContext.request.contextPath}">

            <div class="form-step" id="step-1">
                <div class="form-group">
                    <label for="username">Username <span class="required">*</span></label>
                    <input type="text" id="username" name="username" class="auth-input"
                           value="${param.username}" placeholder="e.g. SteveCraft" required autocomplete="username">
                    <span class="field-feedback" id="username-feedback"></span>
                </div>

                <div class="form-group">
                    <label for="email">Email Address <span class="required">*</span></label>
                    <input type="email" id="email" name="email" class="auth-input"
                           value="${param.email}" placeholder="steve@schemecraft.com" required autocomplete="email">
                    <span class="field-feedback" id="email-feedback"></span>
                </div>

                <div class="form-group">
                    <label for="password">Password <span class="required">*</span></label>
                    <input type="password" id="password" name="password" class="auth-input"
                           placeholder="Choose a strong password" required autocomplete="new-password">
                    <span class="field-feedback" id="password-feedback"></span>
                </div>

                <button type="button" class="btn-primary btn-block" data-step="2">
                    Next Step &rarr;
                </button>
            </div>

            <div class="form-step hidden" id="step-2">

                <div class="form-group">
                    <label for="countryId">Country <span class="required">*</span></label>
                    <select id="countryId" name="countryId" class="auth-select" required>
                        <option value="" disabled ${empty param.countryId ? 'selected' : ''}>Select your country</option>
                        <c:forEach var="country" items="${countries}">
                            <option value="${country.countryId}" ${param.countryId == country.countryId ? 'selected' : ''}>
                                    ${country.countryName}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label for="currencyId">Preferred Currency <span class="required">*</span></label>
                    <select id="currencyId" name="currencyId" class="auth-select" required>
                        <option value="" disabled ${empty param.currencyId ? 'selected' : ''}>Select currency</option>
                        <c:forEach var="currency" items="${currencies}">
                            <option value="${currency.currencyId}" ${param.currencyId == currency.currencyId ? 'selected' : ''}>
                                    ${currency.currencyName} (${currency.symbol})
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label for="languageId">Interface Language <span class="required">*</span></label>
                    <select id="languageId" name="languageId" class="auth-select" required>
                        <option value="" disabled ${empty param.languageId ? 'selected' : ''}>Select language</option>
                        <c:forEach var="language" items="${languages}">
                            <option value="${language.languageId}" ${param.languageId == language.languageId ? 'selected' : ''}>
                                    ${language.languageName}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="button-group">
                    <button type="button" class="btn-secondary" data-step="1">
                        &larr; Back
                    </button>
                    <button type="button" class="btn-primary" data-step="3">
                        Next Step &rarr;
                    </button>
                </div>
            </div>

            <div class="form-step hidden" id="step-3">

                <div class="form-group">
                    <label for="profileImage">Profile Picture <span class="optional">(Optional)</span></label>
                    <input type="file" id="profileImage" name="profileImage" class="auth-input" accept="image/png, image/jpeg, image/webp">
                </div>

                <div class="form-group">
                    <label for="bio">Biography <span class="optional">(Optional)</span></label>
                    <textarea id="bio" name="bio" class="auth-textarea" rows="4" maxlength="250" style="resize: none;"
                              placeholder="Tell us a little about your building style...">${param.bio}</textarea>
                    <div style="text-align: right; font-size: 0.8rem; color: #888; margin-top: 4px;">
                        <span id="bio-char-count">0</span>/250 characters
                    </div>
                </div>

                <div class="button-group">
                    <button type="button" class="btn-secondary" data-step="2">
                        &larr; Back
                    </button>
                    <button type="submit" class="btn-primary">
                        Complete Registration
                    </button>
                </div>
            </div>

        </form>

        <div class="auth-footer">
            <p>Already have an account? <a href="${pageContext.request.contextPath}/auth/login">Log in here</a></p>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/fragments/footer.jsp" %>

</body>
</html>
