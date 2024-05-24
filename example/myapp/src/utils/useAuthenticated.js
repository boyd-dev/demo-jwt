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
                // withCredentials이 true이므로 쿠키에 저장된 JWT를 같이 전송한다.
                // JWT가 없거나 인증에 실패하면 사용자 정보가 생성될 수 없다.
                response = await axios.get(`${process.env.REACT_APP_SERVER_URI}/api/user`, { withCredentials: true });
            } catch (err) {
                console.log("Fetch failed. Need to sign-in...");
            }
            return response;
        }
        
        getData().then(res => {
            if (!_.isEmpty(res.data)) { 
                // 사용자 데이터를 받았다면 인증을 모두 거친 것으로 판단한다.               
                setUserInfo({id: res.data.userid, isAuthenticated: true}); 
            }                
        });        

    }, []);

    return userInfo;
}

export default useAuthenticated;

