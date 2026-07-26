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

    <script src="${pageContext.request.contextPath}/js/header.js"></script>
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

        <form action="${pageContext.request.contextPath}/auth/register" method="POST" id="registrationForm" class="auth-form" enctype="multipart/form-data">

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

                <button type="button" class="btn-primary btn-block" onclick="goToStep(2)">
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
                                    ${country.countryName} <!-- UPDATE QUI -->
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
                    <button type="button" class="btn-secondary" onclick="goToStep(1)">
                        &larr; Back
                    </button>
                    <button type="button" class="btn-primary" onclick="goToStep(3)">
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
                    <button type="button" class="btn-secondary" onclick="goToStep(2)">
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

<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";

    const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,50}$/;
    const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const PASSWORD_REGEX = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$/;

    let isUsernameAvailable = true;
    let isEmailAvailable = true;
    let isPasswordValid = false;

    function validatePasswordInput() {
        const passwordInput = document.getElementById('password');
        const passwordFeedback = document.getElementById('password-feedback');
        const password = passwordInput.value;

        if (!password) {
            passwordFeedback.textContent = "Password is required.";
            passwordFeedback.className = "field-feedback error";
            isPasswordValid = false;
            return false;
        }

        if (!PASSWORD_REGEX.test(password)) {
            isPasswordValid = false;
            passwordFeedback.textContent = "Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, and one number.";
            passwordFeedback.className = "field-feedback error";
            return false;
        } else {
            isPasswordValid = true;
            passwordFeedback.textContent = "Strong password!";
            passwordFeedback.className = "field-feedback success";
            return true;
        }
    }

    function goToStep(step) {
        const step1 = document.getElementById('step-1');
        const step2 = document.getElementById('step-2');
        const step3 = document.getElementById('step-3');

        const ind1 = document.getElementById('step-indicator-1');
        const ind2 = document.getElementById('step-indicator-2');
        const ind3 = document.getElementById('step-indicator-3');

        if (step === 2) {
            const isPassOk = validatePasswordInput();
            const usernameInput = document.getElementById('username');
            const emailInput = document.getElementById('email');

            if (!usernameInput.value.trim()) {
                document.getElementById('username-feedback').textContent = "Username is required.";
                document.getElementById('username-feedback').className = "field-feedback error";
            }

            if (!emailInput.value.trim()) {
                document.getElementById('email-feedback').textContent = "Email is required.";
                document.getElementById('email-feedback').className = "field-feedback error";
            }

            if (!isPassOk || !isUsernameAvailable || !isEmailAvailable || !usernameInput.value.trim() || !emailInput.value.trim()) {
                return;
            }

            step1.classList.add('hidden');
            step2.classList.remove('hidden');
            step3.classList.add('hidden');

            ind1.classList.remove('active');
            ind2.classList.add('active');
            ind3.classList.remove('active');

        } else if (step === 3) {
            const country = document.getElementById('countryId');
            const currency = document.getElementById('currencyId');
            const language = document.getElementById('languageId');

            let isStep2Valid = true;

            if (!country.value) {
                country.reportValidity();
                isStep2Valid = false;
            } else if (!currency.value) {
                currency.reportValidity();
                isStep2Valid = false;
            } else if (!language.value) {
                language.reportValidity();
                isStep2Valid = false;
            }

            if (!isStep2Valid) return;

            step1.classList.add('hidden');
            step2.classList.add('hidden');
            step3.classList.remove('hidden');

            ind1.classList.remove('active');
            ind2.classList.remove('active');
            ind3.classList.add('active');

        } else {
            step1.classList.remove('hidden');
            step2.classList.add('hidden');
            step3.classList.add('hidden');

            ind1.classList.add('active');
            ind2.classList.remove('active');
            ind3.classList.remove('active');
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const form = document.getElementById('registrationForm');
        const usernameInput = document.getElementById('username');
        const emailInput = document.getElementById('email');
        const passwordInput = document.getElementById('password');
        const bioInput = document.getElementById('bio');
        const bioCharCount = document.getElementById('bio-char-count');

        const usernameFeedback = document.getElementById('username-feedback');
        const emailFeedback = document.getElementById('email-feedback');

        if (bioInput && bioCharCount) {
            bioInput.addEventListener('input', () => {
                bioCharCount.textContent = bioInput.value.length;
            });
            bioCharCount.textContent = bioInput.value.length;
        }

        passwordInput.addEventListener('input', validatePasswordInput);
        passwordInput.addEventListener('blur', validatePasswordInput);

        form.addEventListener('submit', (e) => {
            const isPassOk = validatePasswordInput();

            if (!isUsernameAvailable || !isEmailAvailable || !isPassOk) {
                e.preventDefault();
                goToStep(1);
            }
        });

        usernameInput.addEventListener('blur', async () => {
            const username = usernameInput.value.trim();
            if (!username) {
                usernameFeedback.textContent = "Username is required.";
                usernameFeedback.className = "field-feedback error";
                isUsernameAvailable = false;
                return;
            }

            if (!USERNAME_REGEX.test(username)) {
                isUsernameAvailable = false;
                usernameFeedback.textContent = "Username must be 3-50 alphanumeric characters or underscores.";
                usernameFeedback.className = "field-feedback error";
                return;
            }

            try {
                const url = CONTEXT_PATH + '/auth/check-username?username=' + encodeURIComponent(username);
                const response = await fetch(url);
                const result = await response.json();

                if (!response.ok) {
                    isUsernameAvailable = false;
                    usernameFeedback.textContent = result.message || "Invalid username format.";
                    usernameFeedback.className = "field-feedback error";
                    return;
                }

                const exists = result.exists ?? result.data?.exists ?? false;

                if (exists) {
                    isUsernameAvailable = false;
                    usernameFeedback.textContent = "Username is already taken.";
                    usernameFeedback.className = "field-feedback error";
                } else {
                    isUsernameAvailable = true;
                    usernameFeedback.textContent = "Username is available!";
                    usernameFeedback.className = "field-feedback success";
                }
            } catch (error) {
                console.error("Error during username check:", error);
            }
        });

        emailInput.addEventListener('blur', async () => {
            const email = emailInput.value.trim();
            if (!email) {
                emailFeedback.textContent = "Email is required.";
                emailFeedback.className = "field-feedback error";
                isEmailAvailable = false;
                return;
            }

            if (!EMAIL_REGEX.test(email)) {
                isEmailAvailable = false;
                emailFeedback.textContent = "Please enter a valid email address.";
                emailFeedback.className = "field-feedback error";
                return;
            }

            try {
                const url = CONTEXT_PATH + '/auth/check-email?email=' + encodeURIComponent(email);
                const response = await fetch(url);
                const result = await response.json();

                if (!response.ok) {
                    isEmailAvailable = false;
                    emailFeedback.textContent = result.message || "Invalid email format.";
                    emailFeedback.className = "field-feedback error";
                    return;
                }

                const exists = result.exists ?? result.data?.exists ?? false;

                if (exists) {
                    isEmailAvailable = false;
                    emailFeedback.textContent = "Email is already registered.";
                    emailFeedback.className = "field-feedback error";
                } else {
                    isEmailAvailable = true;
                    emailFeedback.textContent = "Email is valid and available!";
                    emailFeedback.className = "field-feedback success";
                }
            } catch (error) {
                console.error("Error during email check:", error);
            }
        });
    });
</script>

</body>
</html>
