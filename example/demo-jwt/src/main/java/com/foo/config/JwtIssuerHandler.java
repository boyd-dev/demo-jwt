package com.foo.config;

import java.io.IOException;
import java.time.Instant;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.foo.utils.CookieUtils;

@PropertySource(value = {"classpath:oauth2.properties"})
public class JwtIssuerHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(JwtIssuerHandler.class);
	
	@Autowired
	private JwtEncoder jwtEncoder;
	
//	@Autowired
//	private JwtDecoder jwtDecoder;
	
	@Value("${userAgent.redirectUri}")
	private String redirectUri;
	
	@Value("${jwt.issuer}")
	private String issuer;
	
	@Value("${jwt.name}")
	private String jwtName;
	
	@Value("${jwt.expiry.time}")
	private int expiryTime;
	
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {	
				
		Instant now = Instant.now();		
		JwtClaimsSet claims = JwtClaimsSet.builder()
								.issuer(this.issuer)
								.issuedAt(now)
								.expiresAt(now.plusSeconds(this.expiryTime))
								.claim("userId", authentication.getName())
								.build();
		
		Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(claims));
		
//		if (logger.isDebugEnabled()) {
//			logger.debug("JwtIssuerHandler::JWT={}", jwt.getTokenValue());		
//			Jwt decodedJwt = jwtDecoder.decode(jwt.getTokenValue());
//			decodedJwt.getClaims().forEach((k,v) -> {
//				logger.debug(k + "=" + v);
//			});
//		}
		
		response.addCookie(CookieUtils.generateJwtHttpOnlyCookie(this.jwtName, jwt.getTokenValue(), this.expiryTime));
		
		getRedirectStrategy().sendRedirect(request, response, this.redirectUri);
	}

}
