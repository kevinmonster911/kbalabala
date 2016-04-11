package com.kbalabala.tools.security.web.csrf.spring;

import com.kbalabala.tools.JmbStringUtils;
import com.kbalabala.tools.security.web.csrf.CsrfTokenManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * pls input by self
 * </p>
 *
 * @author kevin
 * @since 2015-09-23 14:26
 */
public class CsrfInterceptor extends HandlerInterceptorAdapter {

    private boolean invalidateSession = true;
    private String redirectUrl = null;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String CsrfToken = CsrfTokenManager.getTokenFromRequest(request);
            if (CsrfToken == null
                    || !CsrfToken.equals(request.getSession().getAttribute(
                    CsrfTokenManager.CSRF_TOKEN_FOR_SESSION_ATTR_NAME))) {
                if (invalidateSession) request.getSession().invalidate();
                response.sendRedirect(JmbStringUtils.isBlank(redirectUrl) ? getCurrentUrl(request) : redirectUrl);
                return false;
            }
        }

        return true;
    }

    private String getCurrentUrl(HttpServletRequest request) {
        String currentUrl = request.getRequestURL().toString();
        if (!JmbStringUtils.isEmpty(request.getQueryString())) {
            currentUrl += "?" + request.getQueryString();
        }

        return currentUrl;
    }

    public boolean isInvalidateSession() {
        return invalidateSession;
    }

    public void setInvalidateSession(boolean invalidateSession) {
        this.invalidateSession = invalidateSession;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
