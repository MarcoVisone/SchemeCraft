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

import com.xyra.schemecraft.dto.UserSession;

public class AdminFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AdminFilter.class);

    private static final String SESSION_ATTRIBUTE = "userSession";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AdminFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) req;
        HttpServletResponse httpResp = (HttpServletResponse) resp;

        HttpSession session = httpReq.getSession(false);
        UserSession userSession = (session != null) ? (UserSession) session.getAttribute(SESSION_ATTRIBUTE) : null;

        if (userSession == null || !userSession.isAdmin()) {
            logger.warn("Non-admin or unauthenticated access attempt to admin resource: {}", httpReq.getRequestURI());
            httpResp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin privileges required.");
            return;
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
        logger.info("AdminFilter destroyed.");
    }
}
