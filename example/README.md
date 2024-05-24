## 백엔드 서버 
- demo-jwt  
STS 플러그인에서 demo-jwt 프로젝트를 deploy  
구글의 OAuth 2.0 서비스의 설정을 `oauth2.properties`에 추가해야 함(클라이언트 ID, 콜백URL 등)  
콜백 URL은 `http://localhost:8080/demo-mvc/oauth2/callback`으로 되어 있음  
JWT 유효시간은 `jwt.expiry.time=900` 으로 15분으로 되어 있음


## 프론트엔드 서버
- myapp  
`env.development`에 백엔드 서버 URL을 추가해야 함
`yarn install`  
`yarn start`  
사용된 `yarn` 버전은 1.22.19

## RSA 키페어
`resources/jwt.private.key`와 `resources/jwt.public.key`는 테스트용으로만 사용할 것
