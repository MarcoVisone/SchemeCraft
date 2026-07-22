package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.service.AccountService;
import com.xyra.schemecraft.exception.BadCredentialsException;
import com.xyra.schemecraft.exception.InactiveEntityException;
import com.xyra.schemecraft.exception.DuplicateEntityException;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(AuthServlet.class);

    private AccountService accountService;

    @FunctionalInterface
    private interface ExistenceChecker {
        boolean check(String value) throws Exception;
    }

    @Override
    public void init() throws ServletException {
        this.accountService = new AccountService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        switch (pathInfo) {
            case "/logout":
                handleLogout(request, response);
                break;

            case "/check-email":
                handleCheckEmail(request, response);
                break;

            case "/check-username":
                handleCheckUsername(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (pathInfo) {
            case "/login":
                handleLogin(request, response);
                break;

            case "/register":
                handleRegister(request, response);
                break;

            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usernameOrEmail = request.getParameter("usernameOrEmail");
        String password = request.getParameter("password");

        if (usernameOrEmail == null || password == null || usernameOrEmail.trim().isEmpty() || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "All fields are required.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            AccountBean account = accountService.login(usernameOrEmail, password);

            HttpSession session = request.getSession(true);
            session.setAttribute("currentAccount", account);

            logger.info("User '{}' successfully logged in.", usernameOrEmail);
            response.sendRedirect(request.getContextPath() + "/index.jsp");

        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for user '{}': invalid credentials.", usernameOrEmail);
            request.setAttribute("errorMessage", "Invalid credentials. Please try again.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (InactiveEntityException e) {
            logger.warn("Failed login attempt for user '{}': account is deactivated.", usernameOrEmail);
            request.setAttribute("errorMessage", "This account has been deactivated.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Unexpected error during login for user '{}'", usernameOrEmail, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String countryId = request.getParameter("countryId");

        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            request.setAttribute("errorMessage", "Invalid username (3-20 characters, alphanumeric and underscores only).");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            request.setAttribute("errorMessage", "Invalid email format.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        try {
            AccountBean newAccount = new AccountBean();
            newAccount.setUsername(username);
            newAccount.setEmail(email);
            newAccount.setCountryId(countryId);

            accountService.registerAccount(newAccount, password);

            logger.info("New account registered successfully: '{}' ({})", username, email);
            request.setAttribute("successMessage", "Registration completed successfully! You can now log in.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);

        } catch (DuplicateEntityException e) {
            logger.warn("Registration failed: username '{}' or email '{}' already exists.", username, email);
            request.setAttribute("errorMessage", "Username or email already registered in the system.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error during registration of user '{}'", username, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            AccountBean current = (AccountBean) session.getAttribute("currentAccount");
            if (current != null) {
                logger.info("User '{}' logged out.", current.getUsername());
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/index.jsp?logout=success");
    }

    private void handleCheckEmail(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String email = request.getParameter("email");
        executeAvailabilityCheck(response, email, accountService::checkEmailExists, "AJAX check-email error");
    }

    private void handleCheckUsername(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        executeAvailabilityCheck(response, username, accountService::checkUsernameExists, "AJAX check-username error");
    }

    private void executeAvailabilityCheck(HttpServletResponse response, String value,
                                          ExistenceChecker checker, String logMessage) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            if (value == null || value.trim().isEmpty()) {
                out.print("{\"exists\": false}");
                return;
            }

            try {
                boolean exists = checker.check(value);
                out.print("{\"exists\": " + exists + "}");
            } catch (Exception e) {
                logger.error("{}, value: '{}'", logMessage, value, e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Server error\"}");
            }
        }
    }
}
