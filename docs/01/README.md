## 프론트엔드와 백엔드 구분
이 예제에서는 리액트 SPA가 `http://localhost:3000`에 있고 스프링 MVC 백엔드가 `http://localhost:8080`에 있습니다. 그래서 리액트에서 `axios` 같은 것으로 백엔드의 API를 호출하면 다음과 같은 CORS 위반 메시지를 보게 됩니다.

```
Access to XMLHttpRequest at 'http://localhost:8080/demo-mvc/guest/test' from origin 'http://localhost:3000' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```
Postman과 같은 API 호출 테스트 도구에서는 CORS 문제가 나타나지 않습니다. 왜냐하면 Postman은 기본적으로 브라우저가 아니기 때문입니다.

아무튼 이 문제를 어떻게 해결할 수 있는지 살펴보겠습니다.

## 백엔드 서버 설정
먼저 구글의 OAuth 2.0 인증 서비스를 이용하기 위한 설정을 합니다. 스프링 시큐리티의 `oauth2Login`을 사용하면 수월하게 구현할 수 있습니다. 스프링 MVC 기본 구성에 스프링 시큐리티를 적용하는 경우 구성 클래스에 아래와 같은 `SecurityFilterChain` 설정을 합니다.

```
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
		    	.successHandler(jwtIssuerHandler()))
		.logout(logout -> logout.logoutUrl("/signout")
                .logoutSuccessHandler(jwtLogoutSuccessHandler()).deleteCookies("JSESSIONID", "jwt.foo.com"))
		.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.addFilterBefore(jwtAuthenticationFilter(), OAuth2AuthorizationRequestRedirectFilter.class);
	return http.build();
}
```
사실 이 설정에 대부분의 내용들이 함축되어 있습니다. 하나씩 알아보도록 하겠습니다.  

우선 요청 패턴을 구분합니다. 보통 모든 요청에 대해 인증을 받아야 하는 것은 아니기 때문에 `"/guest/**"` 패턴으로 들어오는 것은 허용하고 그 외에는 인증이 필요하도록 합니다. 이를 위해 `HttpSecurity`의 `authorizeHttpRequests`를 설정합니다.  

프론트엔드와 백엔드 서버의 URL이 다르므로 인증된 요청이든 아니든 CORS 설정을 해야 합니다. 공식문서에는 이와 관련하여 중요한 [내용](https://docs.spring.io/spring-security/reference/5.8/servlet/integrations/cors.html)이 있습니다. 그대로 옮겨보겠습니다. 

>CORS must be processed before Spring Security because the pre-flight request will not contain any cookies (i.e. the JSESSIONID). If the request does not contain any cookies and Spring Security is first, the request will determine the user is not authenticated (since there are no cookies in the request) and reject it. The easiest way to ensure that CORS is handled first is to use the CorsFilter. Users can integrate the CorsFilter with Spring Security by providing a CorsConfigurationSource...

[CORS 프로토콜](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)에 의하면 `XMLHttpRequest`를 사용하여 원래 출처와 다른 URL로 요청을 보낼 때 내부적으로 두 개의 요청을 보냅니다. 즉 "pre-flight"라고 표현한 HTTP 메소드 `OPTIONS`로 해당 서버가 과연 "Resource Sharing"을 하고 있는지 여부를 먼저 검사합니다. 만약 여기서 `Access-Control-Allow-Origin` 헤더가 "Resource Sharing"에 해당하지 않는다면 브라우저는 요청을 차단하게 됩니다. 반대로 허용한다면 진짜 요청(이를테면 `POST` 요청)을 보내게 됩니다. 

그런데 "pre-flight"는 어떠한 인증 정보도 가지고 있지 않기 때문에 만약 시큐리티 필터가 먼저 적용된다면 설령 서버가 리소스 공유를 허용해도 시큐리티는 "pre-flight"을 막을 것입니다. 따라서 어떤 것을 허용해주려면 CORS 관련 설정이 시큐리티 필터보다 앞에 있어야 하고 이를 위해서는 `CorsFilter`를 설정하면 된다는 말입니다. 그리고 그러한 CORS 관련 설정은 `CorsConfigurationSource` 인터페이스를 이용하면 되겠습니다.  

"pre-flight"가 거부된 경우는 CORS 위반 메시지와 약간 다르게 나옵니다.

```
Access to XMLHttpRequest at 'http://localhost:8080/demo-mvc/api/test' from origin 'http://localhost:3000' has been blocked by CORS policy: Response to preflight request doesn't pass access control check: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

여기서 또 유념할 것이 있는데, 스프링 MVC의 CORS와 시큐리티의 CORS가 다르다는 것입니다. 공식문서에 의하면 스프링 MVC의 CORS를 이용하려면 `CorsConfigurationSource` 빈을 설정하지 말고 아래와 같이 하면 되겠습니다.

```
// if Spring MVC is on classpath and no CorsConfigurationSource is provided,
// Spring Security will use CORS configuration provided to Spring MVC
.cors(withDefaults())
```
그리고 스프링 MVC의 CORS 설정은 시큐리티 설정이 아닌 `@EnableWebMvc` 구성 클래스에서 합니다.

```
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.foo.myapp"})
public class WebConfig implements WebMvcConfigurer {
	...
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/guest/**").allowedOrigins("*");
	}
}
```
이것은 `/guest/**` 패턴의 요청은 모두 CORS를 허용하겠다는 의미가 되겠습니다. 하지만 이것으로 충분하지 않습니다. 요청 패턴이 `/api/**`일 때는 인증한 사용자만이 가능하도록 할 생각이므로 이 역시 CORS를 허용해야 합니다. 그래서 <b>일단</b> `CorsConfigurationSource`를 다음과 같이 설정해주기로 합니다. 

```
.cors(cors -> cors.configurationSource(corsConfigurationSource()))

private CorsConfigurationSource corsConfigurationSource() {
	CorsConfiguration configuration = new CorsConfiguration();
	configuration.setAllowCredentials(true);
	configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
	configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
	configuration.setAllowedHeaders(Arrays.asList("Content-Type"));
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	source.registerCorsConfiguration("/api/**", configuration);
	return source;
}
```
여기서 중요한 것은 `setAllowCredentials(true)` 설정입니다. 이것은 `"/api/**"` 패턴의 경우에는 요청에 "credentials"이 있어야 한다는 것을 의미하는데 받은 요청의 헤더가 `Access-Control-Allow-Credentials=true`임을 서버가 확인한다는 말이 되겠습니다. MDN의 CORS [설명](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS#requests_with_credentials)을 인용해보겠습니다. 

>The most interesting capability exposed by both fetch() or XMLHttpRequest and CORS is the ability to make "credentialed" requests that are aware of HTTP cookies and HTTP Authentication information. By default, in cross-origin fetch() or XMLHttpRequest calls, browsers will not send credentials.

설명처럼 "credentials"은 쿠키가 될 수 있는데, 바로 이 예제에서는 백엔드 서버가 인증된 사용자에게 JWT를 발급하여 쿠키로 주게 되고 이후 요청부터는 JWT를 포함시켜 인증되어 있음을 증명해야 되므로 이러한 설정이 필요합니다. 그렇다면, 예를 들어 `axios`에서 `XMLHttpRequest`를 보낼 때 다른 도메인 즉 백엔드 서버(localhost:8080)가 심은 쿠키를 보내는 방법이 존재해야 합니다(리액트 애플리케이션은 localhost:3000에서 제공되므로 일반적으로는 localhost:8080이 심은 쿠키를 이용할 수 없습니다). 당연하게도 `XMLHttpRequest.withCredentials`이라는 옵션이 제공됩니다. MDN에 다음과 같이 잘 [설명](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest/withCredentials)되어 있습니다. 

>The XMLHttpRequest.withCredentials property is a boolean value that indicates whether or not cross-site Access-Control requests should be made using credentials such as cookies, authentication headers or...

예를 들어 `axios`에서는 다음과 같이 `withCredentials` 옵션을 주게 됩니다.

```
const response = await axios.post(`${process.env.REACT_APP_SERVER_URI}/api/test`, {withCredentials: true});
```

`setAllowCredentials`을 true로 설정한 경우 `setAllowedOrigins`, `setAllowedMethods`, `setAllowedHeaders` 등을 반드시 설정해야 하는데 기억할 것은 전부 허용을 의미하는 <b>와일드카드를 `*` 사용할 수 없다는 점입니다.</b> MDN CORS의 설명을 다시 인용해보겠습니다.

>If a request includes a credential (most commonly a Cookie header) and the response includes an Access-Control-Allow-Origin: * header (that is, with the wildcard), the browser will block access to the response, and report a CORS error in the devtools console.

다시 말해서 와일드카드 설정을 하면 브라우저는 이런 응답 역시 차단한다는 말이 되겠습니다. 그래서 위와 같이 `setAllowedOrigins`, `setAllowedMethods`, `setAllowedHeaders` 등을 목적에 맞게 설정해 주었습니다. 

이렇게 시큐리티 설정을 통해 프론트엔드와 백엔드가 분리되어도 CORS 문제 없이 요청과 응답을 주고받을 수 있습니다. 또 요청 패턴에 따라 credential을 포함할 수도, 또는 모두 허용할 수도 있겠습니다.

다음은 OAuth2 인증 설정으로 시큐리티의 `oauth2Login` 기능을 이용합니다. 최소한의 설정으로 구글 로그인 서비스와 연결할 수 있습니다. 스프링 프레임워크에서 시큐리티를 그대로 사용했으므로 다소 복잡한 설정이 들어간 것처럼 보이지만 기본적으로는 스프링 부트와 동일합니다. `oauth2Login`의 설정 부분은 [여기](https://github.com/boyd-dev/demo-security/blob/main/docs/05/README.md)를 참조하세요.

여기서 주목할 것은 세션 설정입니다. JWT를 사용하므로 세션을 "stateless"로 설정했습니다.  

```
.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```
구글 인증이 성공한 후 핸들러인 `successHandler`에서 JWT를 발급합니다. 그리고 로그아웃을 하면 쿠키를 삭제하게 됩니다. 다음에는 JWT를 어떻게 만들고 요청에서 넘어온 JWT를 확인하는지 살펴보겠습니다.


[처음](../README.md) | [다음](../02/README.md)