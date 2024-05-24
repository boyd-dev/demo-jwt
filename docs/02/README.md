## JWT 발급
먼저 시큐리티에서 JWT를 만들기 위해 사용할 수 있는 라이브러리를 디펜던시에 추가합니다.

```
implementation "org.springframework.security:spring-security-oauth2-jose:$springSecurityVersion"
```

`successHandler`에서 JWT를 발급할 것이므로 핸들러는 `SimpleUrlAuthenticationSuccessHandler`을 상속하여 `JwtIssuerHandler`를 만들겠습니다. 재정의할 메소드는 `onAuthenticationSuccess`입니다.

```
public class JwtIssuerHandler extends SimpleUrlAuthenticationSuccessHandler {
   ...
   @Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {	

    }
}
```

JWT를 만들기 위해서 아래와 같은 빈을 추가합니다. 이 예제에서는 RSA 공개키 방식으로 JWT를 서명하여 인코딩합니다. 

```
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
```
이렇게 설정한 빈은 `JwtIssuerHandler`에 주입하여 JWT 토큰을 생성하는데 사용됩니다.

JWT의 claim에 넣을 수 있는 항목은 표준으로 권장되는 것도 있지만 임의로 추가할 수 있으므로 애플리케이션에서 필요한 정보를 넣습니다. 여기서는 구글에서 받은 사용자 정보 중 email을 `userId`라는 이름으로 넣도록 하겠습니다. 

```
JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(this.issuer)
				.issuedAt(now)
				.expiresAt(now.plusSeconds(this.expiryTime))
				.claim("userId", authentication.getName())
				.build();
```

이렇게 만든 JWT를 쿠키에 넣어 리턴합니다. 이때 쿠키는 `HttpOnly` 쿠키로 만들어서 스크립트가 접근할 수 없도록 합니다. 쿠키 유효시간은 정하기 나름입니다. 그리고 리액트에게 정해진 redirection 페이지로 라우트하도록 합니다. `react-router-dom`을 사용하므로 리디렉션은 브라우저 내에서 이루어질 것입니다. 

```
response.addCookie(CookieUtils.generateJwtHttpOnlyCookie(this.jwtName, jwt.getTokenValue(), this.expiryTime));	
getRedirectStrategy().sendRedirect(request, response, this.redirectUri);
```

## JWT 필터

백엔드 서버가 준 JWT는 리액트 애플리케이션이 `withCredentials=true` 옵션으로 `/api/**`에 요청을 보낼때마다 포함되어 전송됩니다. JWT를 확인하는 것은 `JwtAuthenticationFilter`입니다. `JwtAuthenticationFilter`는 단순한 웹 필터로, `/api/**`에 대해서만 적용되고 요청에 대해 한번만 수행되는 필터인 `OncePerRequestFilter`를 상속해서 만듭니다. 오버라이드할 메소드는 `doFilterInternal`로 여기에서 JWT를 검사하면 되겠습니다. `request.getCookies()`에서 지정된 이름의 쿠키를 찾아 JWT를 검사하고 디코딩합니다. 

```
List<Cookie> cookies = s.filter(c -> jwtName.equals(c.getName())).toList();
if (cookies.size() == 1) {
    String jwt = cookies.get(0).getValue();
    Jwt decodedJwt = jwtDecoder.decode(jwt);
    ...
}
```
이렇게 디코딩된 정보를 바탕으로 인증 정보를 `SecurityContextHolder`에 넣습니다. 그렇게 해야 나머지 시큐리티 필터체인을 무사히 통과할 수 있습니다. 인증 정보가 없다면 시큐리티 필터는 그 전처럼 "Access Denied"로 차단할 것입니다.

```
SecurityContextHolder.getContext().setAuthentication(authentication);
```

`JwtAuthenticationFilter`는 시큐리티 설정에서 `OAuth2AuthorizationRequestRedirectFilter` 보다 앞에 배치해서 `JwtAuthenticationFilter`에서 인증 정보를 생성한 후에 다음 필터로 넘어갈 수 있도록 합니다. `addFilterBefore`를 사용하면 특정 필터 앞에 배치할 수 있습니다.

```
http.authorizeHttpRequests((authorize) -> authorize
				.requestMatchers(new String[]{"/guest/**", "/resources/**"}).permitAll()
				.anyRequest().authenticated())
     ...           
    .addFilterBefore(jwtAuthenticationFilter(), OAuth2AuthorizationRequestRedirectFilter.class);
```


[처음](../README.md) | [다음](../03/README.md)