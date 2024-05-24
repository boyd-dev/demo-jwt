import { useEffect, useState } from 'react';
import _ from 'lodash';


function useTest() {

    const [userInfo, setUserInfo] = useState({});

    useEffect(()=> {
        console.log("useTest");
        setUserInfo({id: "Kate", isAuthenticated: true}); 
    }, []);

    return userInfo;
}

export default useTest;

