package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xyra.schemecraft.constant.ValidationConstants;
import com.xyra.schemecraft.service.CartService;
import com.xyra.schemecraft.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.dto.AccountRegistrationRequest;
import com.xyra.schemecraft.dto.AccountRegistrationResponse;
import com.xyra.schemecraft.exception.BadCredentialsException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.InactiveEntityException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.AccountService;
import com.xyra.schemecraft.service.LookupService;
import com.xyra.schemecraft.service.RememberTokenService;

@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class AuthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);

    private AccountService accountService;
    private RememberTokenService rememberTokenService;
    private LookupService lookupService;
    private CartService cartService;

    public AuthServlet() {
        super();
    }

    public AuthServlet(AccountService accountService, RememberTokenService rememberTokenService, LookupService lookupService, CartService cartService) {
        this.accountService = accountService;
        this.rememberTokenService = rememberTokenService;
        this.lookupService = lookupService;
        this.cartService = cartService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.accountService == null) {
            this.accountService = new AccountService();
        }
        if (this.rememberTokenService == null) {
            this.rememberTokenService = new RememberTokenService();
        }
        if (this.lookupService == null) {
            this.lookupService = new LookupService();
        }
        if (this.cartService == null) {
            this.cartService = new CartService();
        }

        logger.info("AuthServlet successfully initialized with LookupService and RememberTokenService.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ServletUtils.getActionPath(req);
        if (action.isEmpty() || "/".equals(action)) {
            action = "/login";
        }

        switch (action) {
            case "/login" -> showLoginForm(req, resp);
            case "/register" -> showRegisterForm(req, resp);
            case "/logout" -> handleLogout(req, resp);
            case "/check-username" -> handleCheckUsername(req, resp);
            case "/check-email" -> handleCheckEmail(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ServletUtils.getActionPath(req);
        if (action.isEmpty() || "/".equals(action)) {
            action = "/login";
        }

        switch (action) {
            case "/login" -> handleLogin(req, resp);
            case "/register" -> handleRegister(req, resp);
            case "/logout" -> handleLogout(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // VIEW HANDLERS
    // =========================================================================

    private void showLoginForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("true".equals(req.getParameter("registered"))) {
            req.setAttribute("successMessage", "Account created successfully! You can now log in.");
        } else if ("true".equals(req.getParameter("logout"))) {
            req.setAttribute("infoMessage", "You have been logged out successfully.");
        }

        req.getRequestDispatcher("/WEB-INF/auth/login.jsp").forward(req, resp);
    }

    private void showRegisterForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        populateLookupAttributes(req);
        req.getRequestDispatcher("/WEB-INF/auth/register.jsp").forward(req, resp);
    }

    // =========================================================================
    // ACTION HANDLERS
    // =========================================================================

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usernameOrEmail = req.getParameter("usernameOrEmail");
        String password = req.getParameter("password");

        if (Utils.isNullOrBlank(usernameOrEmail) || Utils.isNullOrBlank(password)) {
            req.setAttribute("errorMessage", "Username/Email and Password are required.");
            req.getRequestDispatcher("/WEB-INF/auth/login.jsp").forward(req, resp);
            return;
        }

        try {
            UserSession userSession = accountService.login(usernameOrEmail, password);

            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("userSession", userSession);
            newSession.setAttribute("account", userSession.getAccount());

            List<String> guestCartProductIds = CookieUtils.getCartProductIds(req);
            if (!guestCartProductIds.isEmpty()) {
                cartService.mergeGuestCart(userSession.getAccount().getAccountId(), guestCartProductIds);
                CookieUtils.clearCartCookie(resp, req.getContextPath());
            }

            if ("true".equals(req.getParameter("rememberMe"))) {
                String rawToken = rememberTokenService.createRememberToken(userSession.getAccount().getAccountId());
                CookieUtils.setRememberMeCookie(resp, rawToken, req.getContextPath());
            }

            logger.info("User successfully logged in. Account ID: {}", userSession.getAccount().getAccountId());

            resp.sendRedirect(req.getContextPath() + "/index.jsp");

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user input: {}", usernameOrEmail);
            req.setAttribute("errorMessage", "Invalid credentials.");
            req.getRequestDispatcher("/WEB-INF/auth/login.jsp").forward(req, resp);

        } catch (ServiceException e) {
            logger.error("Internal service error during login process for user: {}", usernameOrEmail, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred. Please try again later.");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AccountRegistrationRequest registrationRequest = null;

        try {
            String uploadedProfileImagePath = FileUploadUtils.saveUploadedFile(req, "profileImage", "avatars");

            registrationRequest = buildRegistrationRequest(req, uploadedProfileImagePath);
            logger.info("Received registration request: {}", registrationRequest);

            AccountRegistrationResponse response = accountService.registerAccount(registrationRequest);

            logger.info("New account successfully registered. Account ID: {}", response.accountId());

            resp.sendRedirect(req.getContextPath() + "/auth/login?registered=true");

        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed - Invalid input: {}", e.getMessage());
            forwardToRegisterWithError(req, resp, "Invalid input data provided. Please try again.");

        } catch (DuplicateEntityException e) {
            logger.warn("Registration failed - Duplicate entity constraint: {}", e.getMessage());
            forwardToRegisterWithError(req, resp, "An account with this username or email already exists.");

        } catch (EntityNotFoundException e) {
            logger.warn("Registration failed - Missing entity: {} (type: {})", e.getMessage(), e.getEntityType());

            String userFriendlyMessage = switch (e.getEntityType()) {
                case COUNTRY -> "The selected country is invalid or does not exist.";
                case CURRENCY -> "The selected currency is invalid or does not exist.";
                case LANGUAGE -> "The selected language is invalid or does not exist.";
                default -> "One of the selected options is invalid or no longer available.";
            };

            forwardToRegisterWithError(req, resp, userFriendlyMessage);

        } catch (InactiveEntityException e) {
            logger.warn("Registration failed - Inactive entity: {}", e.getMessage());
            forwardToRegisterWithError(req, resp, "The selected country or currency is currently inactive.");

        } catch (ServiceException e) {
            String username = (registrationRequest != null) ? registrationRequest.username() : "unknown";
            logger.error("System failure during account registration for user: {}", username, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An unexpected system error occurred. Please try again later.");

        } catch (Exception e) {
            logger.error("Unexpected error handling registration", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An unexpected system error occurred. Please try again later.");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rememberToken = CookieUtils.getRememberMeCookieValue(req);
        if (rememberToken != null && !rememberToken.isBlank()) {
            try {
                RememberTokenService rememberTokenService = new RememberTokenService();
                rememberTokenService.invalidateRememberToken(rememberToken);
            } catch (Exception e) {
                logger.error("Error invalidating remember token during logout", e);
            }
            CookieUtils.clearRememberMeCookie(resp, req.getContextPath());
        }

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/auth/login?logout=true");
    }

    private void handleCheckUsername(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");

        if (Utils.isNullOrBlank(username)) {
            JsonUtils.sendError(resp, "Username parameter is missing or blank", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        username = username.trim();

        if (!ValidationConstants.USERNAME_PATTERN.matcher(username).matches()) {
            String errorMessage = String.format(
                    "Invalid username format (%d-%d characters, letters, numbers, and underscores allowed)",
                    ValidationConstants.USERNAME_MIN_LENGTH,
                    ValidationConstants.USERNAME_MAX_LENGTH
            );
            JsonUtils.sendError(resp, errorMessage, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean exists = accountService.checkUsernameExists(username);
            JsonUtils.sendSuccess(resp, null, "exists", exists);
        } catch (Exception e) {
            logger.error("Failed to execute AJAX username availability check for input: {}", username, e);
            JsonUtils.sendError(resp, "Unable to process username validation check", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleCheckEmail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");

        if (Utils.isNullOrBlank(email)) {
            JsonUtils.sendError(resp, "Email parameter is missing or blank", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        email = email.trim();

        if (!ValidationConstants.EMAIL_PATTERN.matcher(email).matches()) {
            JsonUtils.sendError(resp, "Invalid email format provided", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean exists = accountService.checkEmailExists(email);
            JsonUtils.sendSuccess(resp, null, "exists", exists);
        } catch (Exception e) {
            logger.error("Failed to execute AJAX email availability check for input: {}", email, e);
            JsonUtils.sendError(resp, "Unable to process email validation check", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // UTILITY / HELPER METHODS
    // =========================================================================

    private void populateLookupAttributes(HttpServletRequest req) {
        try {
            if (lookupService != null) {
                req.setAttribute("countries", lookupService.listActiveCountries());
                req.setAttribute("currencies", lookupService.listActiveCurrencies());
                req.setAttribute("languages", lookupService.listAllLanguages());
            }
        } catch (Exception e) {
            logger.error("Failed to populate lookup attributes for registration form", e);
            req.setAttribute("errorMessage", "Unable to load country and language choices from server.");
        }
    }

    private void forwardToRegisterWithError(HttpServletRequest req, HttpServletResponse resp, String errorMessage)
            throws ServletException, IOException {
        req.setAttribute("errorMessage", errorMessage);
        populateLookupAttributes(req);
        req.getRequestDispatcher("/WEB-INF/auth/register.jsp").forward(req, resp);
    }

    private AccountRegistrationRequest buildRegistrationRequest(HttpServletRequest req, String profileImagePath) {
        String rawPassword = req.getParameter("password");
        if (Utils.isNullOrBlank(rawPassword)) {
            rawPassword = req.getParameter("plainTextPassword");
        }

        logger.info("Building AccountRegistrationRequest with username: {}, email: {}, countryId: {}, languageId: {}, currencyId: {}, profileImagePath: {}",
                req.getParameter("username"),
                req.getParameter("email"),
                req.getParameter("countryId"),
                req.getParameter("languageId"),
                req.getParameter("currencyId"),
                profileImagePath
        );

        return new AccountRegistrationRequest(
                req.getParameter("username"),
                req.getParameter("email"),
                rawPassword,
                req.getParameter("countryId"),
                req.getParameter("currencyId"),
                req.getParameter("languageId"),
                req.getParameter("bio"),
                req.getParameter("bannerPath"),
                profileImagePath
        );
    }
}
