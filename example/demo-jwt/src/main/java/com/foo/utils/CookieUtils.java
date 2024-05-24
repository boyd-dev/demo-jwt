package com.foo.utils;

import javax.servlet.http.Cookie;

public class CookieUtils {
	
	
    public static Cookie generateJwtHttpOnlyCookie(String name, String value, int maxAge) {
    	
    	Cookie cookie = new Cookie(name, value);
		cookie.setPath("/demo-mvc");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setMaxAge(maxAge);
		
		return cookie;		
	}
    
    public static Cookie generateCookie(String name, String value, int maxAge) {
    	
    	Cookie cookie = new Cookie(name, value);
		cookie.setPath("/demo-mvc");
		cookie.setMaxAge(maxAge);
		
		return cookie;		
	}

}
