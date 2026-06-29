package com.terraplanistas.clinic.http.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class CookieService {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final JwtProperties jwtProperties;

    public CookieService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, ACCESS_TOKEN_COOKIE, token,
                (int) (jwtProperties.getAccessTokenExpirationMs() / 1000));
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        setCookie(response, REFRESH_TOKEN_COOKIE, token,
                (int) (jwtProperties.getRefreshTokenExpirationMs() / 1000));
    }

    public void clearAccessTokenCookie(HttpServletResponse response) {
        clearCookie(response, ACCESS_TOKEN_COOKIE);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        clearCookie(response, REFRESH_TOKEN_COOKIE);
    }

    public void clearAllTokenCookies(HttpServletResponse response) {
        clearAccessTokenCookie(response);
        clearRefreshTokenCookie(response);
    }

    public String getAccessTokenFromRequest(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath(jwtProperties.getCookiePath());
        cookie.setDomain(jwtProperties.getCookieDomain());
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setHttpOnly(jwtProperties.isCookieHttpOnly());
        cookie.setAttribute("SameSite", jwtProperties.getSameSitePolicy());
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(jwtProperties.getCookiePath());
        cookie.setDomain(jwtProperties.getCookieDomain());
        cookie.setSecure(jwtProperties.isCookieSecure());
        cookie.setHttpOnly(jwtProperties.isCookieHttpOnly());
        cookie.setAttribute("SameSite", jwtProperties.getSameSitePolicy());
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
