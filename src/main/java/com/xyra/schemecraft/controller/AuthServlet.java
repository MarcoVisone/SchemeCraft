package com.xyra.schemecraft.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.xyra.schemecraft.util.JsonUtils;
import com.xyra.schemecraft.util.ServletUtils;
import com.xyra.schemecraft.util.Utils;

@WebServlet(name = "AuthServlet", urlPatterns = {"/auth/*"})
public class AuthServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);

    private AccountService accountService;

    public AuthServlet() {
        super();
    }

    public AuthServlet(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.accountService == null) {
            this.accountService = new AccountService();
        }
        logger.info("AuthServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ServletUtils.getActionPath(req);

        switch (action) {
            case "/login" -> showLoginForm(req, resp);
            case "/register" -> req.getRequestDispatcher("/register.jsp").forward(req, resp);
            case "/logout" -> handleLogout(req, resp);
            case "/check-username" -> handleCheckUsername(req, resp);
            case "/check-email" -> handleCheckEmail(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = ServletUtils.getActionPath(req);

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

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    // =========================================================================
    // ACTION HANDLERS
    // =========================================================================

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usernameOrEmail = req.getParameter("usernameOrEmail");
        String password = req.getParameter("password");

        if (Utils.isNullOrBlank(usernameOrEmail) || Utils.isNullOrBlank(password)) {
            req.setAttribute("errorMessage", "Username/Email and Password are required.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
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

            logger.info("User successfully logged in. Account ID: {}", userSession.getAccount().getAccountId());

            resp.sendRedirect(req.getContextPath() + "/index.jsp");

        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user input: {}", usernameOrEmail);
            req.setAttribute("errorMessage", "Invalid credentials.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);

        } catch (ServiceException e) {
            logger.error("Internal service error during login process for user: {}", usernameOrEmail, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An internal server error occurred. Please try again later.");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AccountRegistrationRequest registrationRequest = null;

        try {
            registrationRequest = buildRegistrationRequest(req);
            AccountRegistrationResponse response = accountService.registerAccount(registrationRequest);

            logger.info("New account successfully registered. Account ID: {}", response.accountId());

            resp.sendRedirect(req.getContextPath() + "/auth/login?registered=true");

        } catch (IllegalArgumentException e) {
            logger.warn("Registration failed - Invalid input: {}", e.getMessage());
            req.setAttribute("errorMessage", "Internal error occurred. Please try again later.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);

        } catch (DuplicateEntityException e) {
            logger.warn("Registration failed - Duplicate entity constraint: {}", e.getMessage());
            req.setAttribute("errorMessage", "An account with this username or email already exists.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);

        } catch (EntityNotFoundException e) {
            logger.warn("Registration failed - Missing entity: {} (type: {})", e.getMessage(), e.getEntityType());

            String userFriendlyMessage = switch (e.getEntityType()) {
                case COUNTRY -> "The selected country is invalid or does not exist.";
                case CURRENCY -> "The selected currency is invalid or does not exist.";
                case LANGUAGE -> "The selected language is invalid or does not exist.";
                default -> "One of the selected options is invalid or no longer available.";
            };

            req.setAttribute("errorMessage", userFriendlyMessage);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);

        } catch (InactiveEntityException e) {
            logger.warn("Registration failed - Inactive entity: {}", e.getMessage());
            req.setAttribute("errorMessage", "The selected country or currency is currently inactive.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);

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
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
            logger.debug("User session successfully invalidated.");
        }

        resp.sendRedirect(req.getContextPath() + "/auth/login?logout=true");
    }

    private void handleCheckUsername(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");

        if (Utils.isNullOrBlank(username)) {
            JsonUtils.sendError(resp, "Username parameter is missing or blank", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean exists = accountService.checkUsernameExists(username);
            JsonUtils.sendSuccess(resp, null, "exists", exists);
        } catch (ServiceException e) {
            logger.error("Failed to execute AJAX username availability check", e);
            JsonUtils.sendError(resp, "Unable to process username validation check", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleCheckEmail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");

        if (Utils.isNullOrBlank(email)) {
            JsonUtils.sendError(resp, "Email parameter is missing or blank", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean exists = accountService.checkEmailExists(email);
            JsonUtils.sendSuccess(resp, null, "exists", exists);
        } catch (ServiceException e) {
            logger.error("Failed to execute AJAX email availability check", e);
            JsonUtils.sendError(resp, "Unable to process email validation check", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // UTILITY / HELPER METHODS
    // =========================================================================

    private AccountRegistrationRequest buildRegistrationRequest(HttpServletRequest req) {
        return new AccountRegistrationRequest(
                req.getParameter("username"),
                req.getParameter("email"),
                req.getParameter("password"),
                req.getParameter("countryId"),
                req.getParameter("languageId"),
                req.getParameter("currencyId"),
                req.getParameter("bio"),
                req.getParameter("bannerPath"),
                req.getParameter("profileImagePath")
        );
    }
}
