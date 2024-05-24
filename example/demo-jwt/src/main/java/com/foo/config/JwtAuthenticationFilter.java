package com.foo.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;


@PropertySource(value = {"classpath:oauth2.properties"})
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	@Autowired
	private JwtDecoder jwtDecoder;
	
	@Value("${jwt.name}")
	private String jwtName;
	
	private RequestMatcher requestMatcher;
	
	public JwtAuthenticationFilter(RequestMatcher matcher) {
		this.requestMatcher = matcher; 
	}	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		if (this.requestMatcher.matches(request)) {			
			
			if (Objects.nonNull(request.getCookies())) {
				
				Stream<Cookie> s = Stream.of(request.getCookies());
				List<Cookie> cookies = s.filter(c -> jwtName.equals(c.getName())).toList();
				
				if (cookies.size() == 1) {
					
					String jwt = cookies.get(0).getValue();
					
					logger.info("JwtAuthenticationFilter::JWT={}", jwt);
					
					Jwt decodedJwt = jwtDecoder.decode(jwt);
					
					// TODO
					// possible to grant each user ROLE?
					List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
					authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
					
					// just set decoded JWT for user attributes
					DefaultOAuth2User userDetails = new DefaultOAuth2User(authorities, decodedJwt.getClaims(), "userId");				
					OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(userDetails, authorities, "userId");
	                authentication.setDetails(userDetails);
					
	                SecurityContextHolder.getContext().setAuthentication(authentication);
				
				} else {
					logger.info("JwtAuthenticationFilter:: NO COOKIE");
					// TODO anonymous user
										
				}
				
			 }
		}
		
		filterChain.doFilter(request, response);
		
	}

}
