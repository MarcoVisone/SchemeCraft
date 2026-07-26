const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,50}$/;
const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const PASSWORD_REGEX = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,20}$/;

let isUsernameAvailable = true;
let isEmailAvailable = true;
let isPasswordValid = false;
let contextPath = "";

function validatePasswordInput() {
    const passwordInput = document.getElementById('password');
    const passwordFeedback = document.getElementById('password-feedback');
    if (!passwordInput || !passwordFeedback) return false;

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

    if (form) {
        contextPath = form.dataset.contextPath || "";
    }

    const usernameInput = document.getElementById('username');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const bioInput = document.getElementById('bio');
    const bioCharCount = document.getElementById('bio-char-count');

    const usernameFeedback = document.getElementById('username-feedback');
    const emailFeedback = document.getElementById('email-feedback');

    document.querySelectorAll('[data-step]').forEach(button => {
        button.addEventListener('click', (e) => {
            const stepTarget = parseInt(e.currentTarget.getAttribute('data-step'), 10);
            goToStep(stepTarget);
        });
    });

    if (bioInput && bioCharCount) {
        bioInput.addEventListener('input', () => {
            bioCharCount.textContent = bioInput.value.length;
        });
        bioCharCount.textContent = bioInput.value.length;
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', validatePasswordInput);
        passwordInput.addEventListener('blur', validatePasswordInput);
    }

    if (form) {
        form.addEventListener('submit', (e) => {
            const isPassOk = validatePasswordInput();

            if (!isUsernameAvailable || !isEmailAvailable || !isPassOk) {
                e.preventDefault();
                goToStep(1);
            }
        });
    }

    if (usernameInput) {
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
                const url = contextPath + '/auth/check-username?username=' + encodeURIComponent(username);
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
    }

    if (emailInput) {
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
                const url = contextPath + '/auth/check-email?email=' + encodeURIComponent(email);
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
    }
});
