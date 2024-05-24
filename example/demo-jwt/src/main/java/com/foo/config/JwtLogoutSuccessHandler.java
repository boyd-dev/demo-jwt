package com.foo.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

@PropertySource(value = {"classpath:oauth2.properties"})
public class JwtLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {
	
	@Value("${userAgent.redirectUri}")
	private String redirectUri;
	
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		getRedirectStrategy().sendRedirect(request, response, redirectUri);
		
	}

}
