package com.xyra.schemecraft.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyra.schemecraft.exception.InvalidTokenException;
import com.xyra.schemecraft.exception.ServiceException;
import com.xyra.schemecraft.model.UserSession;
import com.xyra.schemecraft.service.RememberTokenService;
import com.xyra.schemecraft.util.CookieUtils;

public class RememberMeFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RememberMeFilter.class);

    private RememberTokenService rememberTokenService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.rememberTokenService = new RememberTokenService();
        logger.info("RememberMeFilter successfully initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean alreadyLoggedIn = session != null && session.getAttribute("userSession") != null;

        if (!alreadyLoggedIn) {
            String rawToken = CookieUtils.getRememberMeCookieValue(req);

            if (rawToken != null) {
                try {
                    UserSession userSession = rememberTokenService.validateRememberToken(rawToken);

                    HttpSession newSession = req.getSession(true);
                    newSession.setAttribute("userSession", userSession);
                    newSession.setAttribute("account", userSession.getAccount());

                    logger.debug("Session restored from remember-me cookie for account: {}",
                            userSession.getAccount().getAccountId());

                } catch (InvalidTokenException e) {
                    logger.debug("Remember-me cookie invalid or expired, clearing it: {}", e.getMessage());
                    CookieUtils.clearRememberMeCookie(resp, req.getContextPath());

                } catch (ServiceException e) {
                    logger.error("Failed to validate remember-me cookie due to a system error", e);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
