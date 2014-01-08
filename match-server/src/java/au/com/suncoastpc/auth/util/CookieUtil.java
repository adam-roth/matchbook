package au.com.suncoastpc.auth.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility for getting/setting cookies.
 * 
 * @author Adam
 */
public class CookieUtil {
	
	public static void setCookie(String name, String value, long durationMillis, HttpServletResponse response) {
		Cookie cookie = new Cookie(name, value);
		cookie.setMaxAge((int)(durationMillis / 1000));
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
	public static void removeCookie(String name, HttpServletRequest request, HttpServletResponse response) {
		Cookie cookie = getCookie(name, request);
		if (cookie != null) {
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}
	
	public static Cookie getCookie(String name, HttpServletRequest request) {
		if (request.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().equals(name)) {
				return cookie;
			}
		}
		
		return null;
	}
	
	public static String getCookieValue(String name, HttpServletRequest request) {
		String result = null;
		Cookie cookie = getCookie(name, request);
		if (cookie != null) {
			result = cookie.getValue();
		}
		
		return result;
	}
}
