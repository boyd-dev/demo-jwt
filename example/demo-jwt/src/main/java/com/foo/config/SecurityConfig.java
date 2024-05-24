package com.foo.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;


@Configuration
@EnableWebSecurity(debug = true)
@PropertySource(value = {"classpath:oauth2.properties"})
public class SecurityConfig {
	
	@Autowired
    private Environment env;
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		http.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers(new String[]{"/guest/**", "/resources/**"}).permitAll()
				.anyRequest().authenticated())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
		    .csrf(csrf -> csrf.disable())
		    .oauth2Login(oauth2 -> oauth2.clientRegistrationRepository(clientRegistrationRepository())
		    	.authorizedClientService(authorizedClientService())
		    	.redirectionEndpoint(redirect -> redirect.baseUri("/oauth2/callback"))		    	
		    	.successHandler(jwtIssuerHandler())
		     )
		    .logout(logout -> logout.logoutUrl("/signout").logoutSuccessHandler(jwtLogoutSuccessHandler()).deleteCookies("JSESSIONID", "jwt.foo.com"))
		    .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		    .addFilterBefore(jwtAuthenticationFilter(), OAuth2AuthorizationRequestRedirectFilter.class);
		
		return http.build();
	}
	
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		
		ClientRegistration google = ClientRegistration.withRegistrationId("google")
		    .clientId(env.getProperty("google.clientId"))
		    .clientSecret(env.getProperty("google.clientSecret"))
		    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
		    .authorizationUri(env.getProperty("google.authorizationUri"))		    
		    .tokenUri(env.getProperty("google.accessTokenUri"))
		    .redirectUri(env.getProperty("google.redirectUri"))
		    .scope(env.getProperty("google.scope").split(","))
		    .userInfoUri(env.getProperty("google.userInfoUri"))
		    .userNameAttributeName("email")		    
		    .build();
		
		InMemoryClientRegistrationRepository clientRegisterationRepository = new InMemoryClientRegistrationRepository(new ClientRegistration[] {google});
		return clientRegisterationRepository;
	}
	
	
	@Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
		InMemoryOAuth2AuthorizedClientService authorizedClientService = new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
		return authorizedClientService;
    }
	
	
	@Bean
	public AuthenticationSuccessHandler jwtIssuerHandler() {		 
		 return new JwtIssuerHandler();
	}
	
	@Value("${jwt.public.key}")
	private RSAPublicKey pubKey;

	@Value("${jwt.private.key}")
	private RSAPrivateKey priKey;
	
	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withPublicKey(this.pubKey).build();
	}	

	@Bean
	public JwtEncoder jwtEncoder() {
		JWK jwk = new RSAKey.Builder(this.pubKey).privateKey(this.priKey).build();
		JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
		return new NimbusJwtEncoder(jwks);
	}
	
	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		RequestMatcher requestMatcher = new AntPathRequestMatcher(env.getProperty("jwt.api.url"));
		return new JwtAuthenticationFilter(requestMatcher);
	}
	
	private CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration configuration = new CorsConfiguration();
	    configuration.setAllowCredentials(true);
	    configuration.setAllowedOrigins(Arrays.asList(env.getProperty("credential.allowedOrigin")));
	    configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
	    configuration.setAllowedHeaders(Arrays.asList("Content-Type"));
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration(env.getProperty("jwt.api.url"), configuration);
	    return source;
	}
	
	@Bean
	public LogoutSuccessHandler jwtLogoutSuccessHandler() {		 
		 return new JwtLogoutSuccessHandler();
	}
	
}
