import React, { useContext } from 'react';
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Stack from "react-bootstrap/Stack";
import Button from "react-bootstrap/Button";
import "../css/bootstrap-5.2.3-dist/css/bootstrap.min.css";
import axios from 'axios';
import { AuthContext } from '../App';

/**
 * Main은 로그인을 해야만 사용할 수 있는 화면
 */
function Main() {

    const userInfo = useContext(AuthContext);

    const handleClick = async () => {

        const formData = new FormData();
        formData.append('data', 'Bart');

        try {
            const response = await axios.post(`${process.env.REACT_APP_SERVER_URI}/api/test`, {name: 'Bart'}, {withCredentials: true, timeout: 10000 });
            //const response = await axios.post(`${process.env.REACT_APP_SERVER_URI}/api/test`, formData, {withCredentials: true, timeout: 10000 });
            alert(response.data.name);
        } catch (err) {            
            throw new Error("API call failed");
        }
    }

    return (
        <>
            <Container fluid style={{marginTop:'10px'}}>           
                <Row>
                    <Col>
                        <Stack gap={3} className="col-md-3">
                            Welcome! {userInfo.id}.
                        </Stack>
                        <Stack gap={3} className="col-md-3">
                            <Button variant="primary" onClick={handleClick} >API Call</Button>
                        </Stack>
                    </Col>
                </Row>          
            </Container>
            {/* <Routes>               
                <Route path="sub" element={<Sub/>} />
            </Routes> */}
        </>        
    );
}


export default Main;