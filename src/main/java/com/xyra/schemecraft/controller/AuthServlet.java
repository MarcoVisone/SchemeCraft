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

import com.xyra.schemecraft.exception.BadCredentialsException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.InactiveEntityException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.dto.AccountRegistrationRequest;
import com.xyra.schemecraft.dto.AccountRegistrationResponse;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.AccountService;

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
        configureEncoding(req, resp);
        String action = getActionPath(req);

        switch (action) {
            case "/logout" -> handleLogout(req, resp);
            case "/check-username" -> handleCheckUsername(req, resp);
            case "/check-email" -> handleCheckEmail(req, resp);
            case "/login" -> req.getRequestDispatcher("/login.jsp").forward(req, resp);
            case "/register" -> req.getRequestDispatcher("/register.jsp").forward(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);
        String action = getActionPath(req);

        switch (action) {
            case "/login" -> handleLogin(req, resp);
            case "/register" -> handleRegister(req, resp);
            case "/logout" -> handleLogout(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                    "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // ACTION HANDLERS
    // =========================================================================

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usernameOrEmail = req.getParameter("usernameOrEmail");
        String password = req.getParameter("password");

        if (isNullOrBlank(usernameOrEmail) || isNullOrBlank(password)) {
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
        AccountRegistrationRequest registrationRequest = buildRegistrationRequest(req);

        try {
            AccountRegistrationResponse response = accountService.registerAccount(registrationRequest);
            logger.info("New account successfully registered. Account ID: {}", response.accountId());

            resp.sendRedirect(req.getContextPath() + "/login.jsp?registered=true");

        } catch (DuplicateEntityException e) {
            logger.warn("Registration failed - Duplicate entity constraint: {}", e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);

        } catch (IllegalArgumentException | EntityNotFoundException | InactiveEntityException e) {
            logger.warn("Registration failed - Invalid input parameter: {}", e.getMessage());
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);

        } catch (ServiceException e) {
            logger.error("System failure during account registration for user: {}", registrationRequest.username(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error occurred during registration.");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
            logger.debug("User session successfully invalidated.");
        }
        resp.sendRedirect(req.getContextPath() + "/login.jsp?logout=true");
    }

    private void handleCheckUsername(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = req.getParameter("username");
        resp.setContentType("application/json");

        if (isNullOrBlank(username)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Username parameter is missing or blank\"}");
            return;
        }

        try {
            boolean exists = accountService.checkUsernameExists(username);
            resp.getWriter().write(String.format("{\"exists\": %b}", exists));
        } catch (ServiceException e) {
            logger.error("Failed to execute AJAX username availability check", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Unable to process username validation check\"}");
        }
    }

    private void handleCheckEmail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = req.getParameter("email");
        resp.setContentType("application/json");

        if (isNullOrBlank(email)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Email parameter is missing or blank\"}");
            return;
        }

        try {
            boolean exists = accountService.checkEmailExists(email);
            resp.getWriter().write(String.format("{\"exists\": %b}", exists));
        } catch (ServiceException e) {
            logger.error("Failed to execute AJAX email availability check", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Unable to process email validation check\"}");
        }
    }

    // =========================================================================
    // UTILITY / HELPER METHODS
    // =========================================================================

    private void configureEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    private String getActionPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return (pathInfo == null) ? "" : pathInfo;
    }

    private boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

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
