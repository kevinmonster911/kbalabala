package com.kbalabala.tools;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import static com.kbalabala.tools.Constants.*;

/**
 *
 * Cookie工具类
 * @since 2015-4-23
 */
public class JmbCookieUtils {

    /**
     * 设置 Cookie（生成时间为1天）
     * @param name
     * @param value
     */
    public static void setCookie(HttpServletResponse response, String name, String value) {
        setCookie(response, name, value, ONE_DAY);
    }

    /**
     * 设置 Cookie
     * @param response
     * @param name
     * @param value
     * @param path
     */
    public static void setCookie(HttpServletResponse response, String name, String value, String path) {
        setCookie(response, name, value, path, ONE_DAY);
    }

    /**
     * 设置 Cookie
     * @param response
     * @param name
     * @param value
     * @param maxAge
     */
    public static void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        setCookie(response, name, value, "/", maxAge);
    }

    /**
     * 设置 Cookie
     * @param name
     * @param value
     * @param path
     * @param maxAge
     */
    public static void setCookie(HttpServletResponse response, String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        try {
            cookie.setValue(URLEncoder.encode(value, CHARSET_UTF8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        response.addCookie(cookie);
    }

    /**
     * 获得指定Cookie的值
     * @param name 名称
     * @return 值
     */
    public static String getCookie(HttpServletRequest request, String name) {
        return getCookie(request, null, name, false);
    }
    /**
     * 获得指定Cookie的值，并删除。
     * @param name 名称
     * @return 值
     */
    public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        return getCookie(request, response, name, true);
    }
    /**
     * 获得指定Cookie的值
     * @param request 请求对象
     * @param response 响应对象
     * @param name 名字
     * @param isRemove 是否移除
     * @return 值
     */
    public static String getCookie(HttpServletRequest request, HttpServletResponse response, String name, boolean isRemove) {
        String value = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    try {
                        value = URLDecoder.decode(cookie.getValue(), CHARSET_UTF8);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (isRemove) {
                        cookie.setValue(null);
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        response.addCookie(cookie);
                    }
                }
            }
        }
        return value;
    }

    public static void removeCookie(HttpServletResponse response, String name) {
        removeCookie(response, null, name);
    }

    public static void removeCookie(HttpServletResponse response, String domain, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        if(!JmbStringUtils.isBlank(domain)) cookie.setDomain(domain);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
