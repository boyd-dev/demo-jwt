## 프론트엔드

프론트엔드는 리액트를 사용하여 간단한 SPA 형태로 만들었습니다. 단순하게 로그인과 인증을 받아야 열리는 페이지, 인증없이 열리는 페이지 등으로 구성되었습니다. `react-router-dom`의 [`createBrowserRouter`](https://reactrouter.com/en/main/routers/create-browser-router)를 사용하여 브라우저 내에서 모든 뷰 전환이 이루어집니다. 따라서 백엔드 서버와는 데이터만을 주고 받게 됩니다.  

세션을 쓰지 않고 JWT를 사용하므로 로그인이 성공해서 JWT가 쿠키에 저장되면 이후 인증이 필요한 페이지로 라우트할 때 페이지 로드 전에 JWT를 서버에서 항상 확인할 필요가 있습니다. 

하지만 그렇게 하면 비효율적인 측면이 있기 때문에 리액트의 Context API를 사용하여 상위 컴포넌트인 `<App/>`에서 fetch된 사용자 정보는 `Provider`를 통해 하위 컴포넌트로 전달되고 하위 컴포넌트들은 `useContext`로 참조하도록 만들었습니다. 

`<App/>`이 렌더링되면 항상 사용자 정보를 조회하는 `useAuthenticated`를 호출합니다. `<App/>`은 루트 컴포넌트이므로 처음 마운트되거나 F5로 새로고침하는 경우에만 렌더링됩니다. `useAuthenticated`은 `axios`를 통해 `GET` 메소드로 사용자 정보를 조회합니다. 당연히 미리 구글 인증을 거쳐 JWT를 발급받아야 사용자 정보 조회 API를 호출할 수 있습니다.

```
import axios from 'axios';
import { useEffect, useState } from 'react';
import _ from 'lodash';

function useAuthenticated() {

    const [userInfo, setUserInfo] = useState({});

    useEffect(()=> {

        console.log("Fetch user info from server...");

        //get user info with cookie credentials(jwt)
        const getData = async () => {    
            let response = {};
            try {
                response = await axios.get(`${process.env.REACT_APP_SERVER_URI}/api/user`, { withCredentials: true });
            } catch (err) {
                console.log("Fetch failed. Need to sign-in...");
            }
            return response;
        }
        
        getData().then(res => {
            if (!_.isEmpty(res.data)) {                
                setUserInfo({id: res.data.userid, isAuthenticated: true}); 
            }                
        });        

    }, []);

    return userInfo;
}

export default useAuthenticated;
```

사용자 정보가 전달되는 컴포넌트들은 인증을 받아야 라우트 될 수 있으므로 다음과 같이 조건부 렌더링이 되도록 라우터를 구성합니다. 예를 들어 `/main/*`으로 라우트되는 `<Main/>` 하위로 인증이 필요한 컴포넌트들을 배치한다고 하면 아래와 같이 라우트 정보를 설정합니다.

```
<Route path="/main/*" element={
    (!_.isEmpty(userInfo) && userInfo.isAuthenticated)?                             
    <Provider value={userInfo}>                                    
        <Main/>
    </Provider>
    :
    ""}/>
```

예제의 전체 소스파일은 [여기](https://github.com/boyd-dev/demo-jwt/tree/main/example)를 보면 되겠습니다. 


[처음](../README.md)