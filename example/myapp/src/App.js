import React, { createContext } from 'react';
import _ from 'lodash';
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Stack from "react-bootstrap/Stack";
import Navbar from 'react-bootstrap/Navbar';
import { NavLink, Outlet, Route, Routes, useLoaderData } from "react-router-dom";
import Help from './pages/Help';
import Login from './pages/Login';
import Main from './pages/Main';
import useAuthenticated from './utils/useAuthenticated';
import "./css/bootstrap-5.2.3-dist/css/bootstrap.min.css";
import "./css/style.css";

// Context API
const AuthContext = createContext();
const Provider = AuthContext.Provider;

function App() {

    // App이 로드될 때 호출되는 hook 함수
    // 사용자 정보를 가져오는 API를 호출한다. 
    const userInfo = useAuthenticated();

    // guest API로 받아온 데이터
    // 로그인 하지 않아도 가져올 수 있다.
    const data = useLoaderData();    
    
    const handleLogout = () => {
        window.location.href = `${process.env.REACT_APP_SERVER_URI}/signout`; 
    }

    return (        
           <>
           <Container fluid style={{marginTop:'10px'}}>
            <Row>
                <Col>
                    <Stack gap={3}>
                        <Navbar expand="lg" className="sidebar">                                                   
                            <NavLink to="/" className="nav-link">Home</NavLink>                                
                            <NavLink to="/help" className="nav-link">Help</NavLink>                                
                            {(!_.isEmpty(userInfo) && userInfo.isAuthenticated)?
                                <>
                                    <NavLink to="/main" className="nav-link">Main</NavLink>                                       
                                    <a href="#" onClick={handleLogout} className="nav-link">Logout</a>
                                </>
                                :  
                                <>                                       
                                   <NavLink to="/login" className="nav-link">Login</NavLink>
                                </>                                   
                            }
                        </Navbar>
                    </Stack>                    
                </Col>
            </Row>
            <Row>
                <Col>
                    <Stack gap={3}>{data} 서버로부터 온 데이터</Stack>
                </Col>
            </Row>
            <Row>
                <Col>
                    <Stack gap={3}><Outlet /></Stack>
                </Col>
            </Row>
            </Container>
            <Routes>
                <Route path="/" element={""} />
                <Route path="/help" element={<Help/>} />
                <Route path="/login" element={<Login/>} /> 
                <Route path="/main/*" element={
                        (!_.isEmpty(userInfo) && userInfo.isAuthenticated)?                             
                            <Provider value={userInfo}>                                    
                                <Main/>                                    
                            </Provider>
                        :
                        ""}/>                                          
            </Routes>        
            </>                             
    );

}

export default App;

export {
    AuthContext
}
