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

import com.xyra.schemecraft.model.UserSession;

/**
 * Ensures the user is logged in before accessing protected areas
 * (checkout, account, orders, favorites, reviews, admin).
 * URL patterns are declared in web.xml, not in the annotation,
 * so the filter order with AdminFilter can be controlled explicitly.
 */
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private static final String LOGIN_PAGE = "/login.jsp";
    private static final String SESSION_ATTRIBUTE = "userSession";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthenticationFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        HttpSession session = httpReq.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute(SESSION_ATTRIBUTE) : null;

        if (userSession == null || !userSession.isLoggedIn()) {
            logger.debug("Unauthenticated access attempt to protected resource: {}", httpReq.getRequestURI());
            httpResp.sendRedirect(httpReq.getContextPath() + LOGIN_PAGE);
            return;
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
        logger.info("AuthenticationFilter destroyed.");
    }
}
