package com.xyra.schemecraft.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.xyra.schemecraft.dto.PaymentMethodRequest;
import com.xyra.schemecraft.dto.ProfileUpdateRequest;
import com.xyra.schemecraft.exception.BadCredentialsException;
import com.xyra.schemecraft.exception.DuplicateEntityException;
import com.xyra.schemecraft.exception.EntityNotFoundException;
import com.xyra.schemecraft.exception.PaymentTokenizationException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.AccountBean;
import com.xyra.schemecraft.model.AddressBean;
import com.xyra.schemecraft.model.PaymentMethodBean;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.AccountService;
import com.xyra.schemecraft.util.FileUploadUtils;
import com.xyra.schemecraft.util.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "AccountServlet", urlPatterns = {"/account/*"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 25
)
public class AccountServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(AccountServlet.class);

    private AccountService accountService;

    public AccountServlet() {
        super();
    }

    public AccountServlet(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (this.accountService == null) {
            this.accountService = new AccountService();
        }
        logger.info("AccountServlet successfully initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "", "/", "/profile" -> showProfilePage(req, resp, currentAccount);
            case "/addresses" -> handleListAddresses(req, resp, currentAccount.getAccountId());
            case "/payment-methods" -> handleListPaymentMethods(req, resp, currentAccount.getAccountId());
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested resource was not found.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        configureEncoding(req, resp);

        AccountBean currentAccount = getAuthenticatedAccount(req);
        if (currentAccount == null) {
            handleUnauthorized(req, resp);
            return;
        }

        String action = getActionPath(req);

        switch (action) {
            case "/update-profile" -> handleUpdateProfile(req, resp, currentAccount);
            case "/change-password" -> handleChangePassword(req, resp, currentAccount.getAccountId());
            case "/change-username" -> handleChangeUsername(req, resp, req.getSession(false), currentAccount);
            case "/change-email" -> handleChangeEmail(req, resp, req.getSession(false), currentAccount);
            case "/deactivate" -> handleDeactivateAccount(req, resp, req.getSession(false), currentAccount.getAccountId());

            case "/add-address" -> handleAddAddress(req, resp, currentAccount.getAccountId());
            case "/remove-address" -> handleRemoveAddress(req, resp);
            case "/set-default-address" -> handleSetDefaultAddress(req, resp, currentAccount.getAccountId());

            case "/add-payment-method" -> handleAddPaymentMethod(req, resp, currentAccount.getAccountId());
            case "/remove-payment-method" -> handleRemovePaymentMethod(req, resp);
            case "/set-default-payment-method" -> handleSetDefaultPaymentMethod(req, resp, currentAccount.getAccountId());

            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint.");
        }
    }

    // =========================================================================
    // GET HANDLERS
    // =========================================================================

    private void showProfilePage(HttpServletRequest req, HttpServletResponse resp, AccountBean account)
            throws ServletException, IOException {
        try {
            AccountBean updatedAccount = accountService.getAccountById(account.getAccountId());
            req.setAttribute("account", updatedAccount);
            req.getRequestDispatcher("/account.jsp").forward(req, resp);
        } catch (EntityNotFoundException | ServiceException e) {
            logger.error("Error loading profile for account: {}", account.getAccountId(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load profile.");
        }
    }

    private void handleListAddresses(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        try {
            List<AddressBean> addresses = accountService.listAddresses(accountId);
            JsonUtils.sendSuccess(resp, "addresses", addresses);
        } catch (ServiceException e) {
            logger.error("Failed to list addresses for account: {}", accountId, e);
            JsonUtils.sendError(resp, "Unable to retrieve addresses.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleListPaymentMethods(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        try {
            List<PaymentMethodBean> methods = accountService.listPaymentMethods(accountId);
            JsonUtils.sendSuccess(resp, "paymentMethods", methods);
        } catch (ServiceException e) {
            logger.error("Failed to list payment methods for account: {}", accountId, e);
            JsonUtils.sendError(resp, "Unable to retrieve payment methods.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // =========================================================================
    // POST HANDLERS
    // =========================================================================

    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp, AccountBean account)
            throws ServletException, IOException {

        try {
            String profileImagePath = FileUploadUtils.saveUploadedFile(req, "profileImageFile", "avatars");
            if (profileImagePath == null || profileImagePath.isBlank()) {
                profileImagePath = req.getParameter("profileImagePath");
                if (profileImagePath == null || profileImagePath.isBlank()) {
                    profileImagePath = account.getProfileImagePath();
                }
            }

            String bannerPath = FileUploadUtils.saveUploadedFile(req, "bannerFile", "banners");
            if (bannerPath == null || bannerPath.isBlank()) {
                bannerPath = req.getParameter("bannerPath");
                if (bannerPath == null || bannerPath.isBlank()) {
                    bannerPath = account.getBannerPath();
                }
            }

            ProfileUpdateRequest updateRequest = new ProfileUpdateRequest(
                    account.getAccountId(),
                    req.getParameter("countryId"),
                    req.getParameter("languageId"),
                    req.getParameter("currencyId"),
                    req.getParameter("bio"),
                    bannerPath,
                    profileImagePath
            );

            accountService.updateProfile(updateRequest);

            AccountBean updatedAccount = accountService.getAccountById(account.getAccountId());
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.setAttribute("account", updatedAccount);
                UserSession userSession = (UserSession) session.getAttribute("userSession");
                if (userSession != null) {
                    userSession.setAccount(updatedAccount);
                }
            }

            JsonUtils.sendSuccess(resp, "Profile updated successfully.",
                    "profileImagePath", updatedAccount.getProfileImagePath(),
                    "bannerPath", updatedAccount.getBannerPath());

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            logger.warn("Profile update failed for account: {}", account.getAccountId(), e);
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);

        } catch (ServletException e) {
            logger.warn("Multipart file upload error for account: {}", account.getAccountId(), e);
            JsonUtils.sendError(resp, "File upload error: " + e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");

        try {
            accountService.changePassword(accountId, oldPassword, newPassword);
            JsonUtils.sendSuccess(resp, "Password changed successfully.");

        } catch (BadCredentialsException e) {
            JsonUtils.sendError(resp, "Current password is incorrect.", HttpServletResponse.SC_UNAUTHORIZED);

        } catch (EntityNotFoundException | ServiceException e) {
            logger.error("Failed to change password for account: {}", accountId, e);
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleChangeUsername(HttpServletRequest req, HttpServletResponse resp, HttpSession session, AccountBean account) throws IOException {
        String newUsername = req.getParameter("newUsername");

        try {
            accountService.changeUsername(account.getAccountId(), newUsername);

            account.setUsername(newUsername);
            session.setAttribute("account", account);

            JsonUtils.sendSuccess(resp, "Username updated successfully.");

        } catch (DuplicateEntityException e) {
            JsonUtils.sendError(resp, "Username is already taken.", HttpServletResponse.SC_CONFLICT);

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleChangeEmail(HttpServletRequest req, HttpServletResponse resp, HttpSession session, AccountBean account) throws IOException {
        String newEmail = req.getParameter("newEmail");

        try {
            accountService.changeEmail(account.getAccountId(), newEmail);

            account.setEmail(newEmail);
            session.setAttribute("account", account);

            JsonUtils.sendSuccess(resp, "Email updated successfully.");

        } catch (DuplicateEntityException e) {
            JsonUtils.sendError(resp, "Email is already in use.", HttpServletResponse.SC_CONFLICT);

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleDeactivateAccount(HttpServletRequest req, HttpServletResponse resp, HttpSession session, String accountId) throws IOException {
        try {
            accountService.deactivateAccount(accountId);
            if (session != null) {
                session.invalidate();
            }

            JsonUtils.sendSuccess(resp, "Account successfully deactivated.");

        } catch (EntityNotFoundException | ServiceException e) {
            logger.error("Failed to deactivate account: {}", accountId, e);
            JsonUtils.sendError(resp, "Unable to deactivate account.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleAddAddress(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        AddressBean address = new AddressBean();
        address.setAccountId(accountId);
        address.setCountryId(req.getParameter("countryId"));
        address.setStreetAddress(req.getParameter("streetAddress"));
        address.setCity(req.getParameter("city"));
        address.setStateProvince(req.getParameter("stateProvince"));
        address.setPostalCode(req.getParameter("postalCode"));
        address.setDefault("true".equalsIgnoreCase(req.getParameter("isDefault")));

        try {
            accountService.addAddress(address);
            JsonUtils.sendSuccess(resp, "Address added successfully.");

        } catch (IllegalArgumentException | ServiceException e) {
            logger.warn("Failed to add address for account: {}", accountId, e);
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleRemoveAddress(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String addressId = req.getParameter("addressId");

        try {
            accountService.removeAddress(addressId);
            JsonUtils.sendSuccess(resp, "Address removed successfully.");

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleSetDefaultAddress(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        String addressId = req.getParameter("addressId");

        try {
            accountService.setDefaultAddress(accountId, addressId);
            JsonUtils.sendSuccess(resp, "Default address updated.");

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleAddPaymentMethod(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        int methodType = 0;
        String methodTypeParam = req.getParameter("methodType");
        if (methodTypeParam != null && !methodTypeParam.isBlank()) {
            try {
                methodType = Integer.parseInt(methodTypeParam);
            } catch (NumberFormatException e) {
                JsonUtils.sendError(resp, "Invalid methodType format.", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        PaymentMethodRequest request = new PaymentMethodRequest(
                methodType,
                "true".equalsIgnoreCase(req.getParameter("isDefault")),
                req.getParameter("cardNumber"),
                req.getParameter("cardExpiration"),
                req.getParameter("cvv"),
                req.getParameter("cardBrand"),
                req.getParameter("paypalEmail"),
                accountId
        );

        try {
            accountService.addPaymentMethod(request);
            JsonUtils.sendSuccess(resp, "Payment method added successfully.");

        } catch (PaymentTokenizationException e) {
            JsonUtils.sendError(resp, "Payment tokenization failed: " + e.getErrorCode(), 422);

        } catch (IllegalArgumentException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleRemovePaymentMethod(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String paymentMethodId = req.getParameter("paymentMethodId");

        try {
            accountService.removePaymentMethod(paymentMethodId);
            JsonUtils.sendSuccess(resp, "Payment method removed successfully.");

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handleSetDefaultPaymentMethod(HttpServletRequest req, HttpServletResponse resp, String accountId) throws IOException {
        String paymentMethodId = req.getParameter("paymentMethodId");

        try {
            accountService.setDefaultPaymentMethod(accountId, paymentMethodId);
            JsonUtils.sendSuccess(resp, "Default payment method updated.");

        } catch (IllegalArgumentException | EntityNotFoundException | ServiceException e) {
            JsonUtils.sendError(resp, e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    // =========================================================================
    // HELPER & UTILITY METHODS
    // =========================================================================

    private AccountBean getAuthenticatedAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        UserSession userSession = (UserSession) session.getAttribute("userSession");
        return (userSession != null) ? userSession.getAccount() : null;
    }

    private void handleUnauthorized(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acceptHeader = req.getHeader("Accept");
        String requestedWith = req.getHeader("X-Requested-With");

        if ((acceptHeader != null && acceptHeader.contains("application/json")) ||
                "XMLHttpRequest".equals(requestedWith)) {
            JsonUtils.sendError(resp, "User is not authenticated", HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }

    private void configureEncoding(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
    }

    private String getActionPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        return (pathInfo == null) ? "" : pathInfo;
    }
}
