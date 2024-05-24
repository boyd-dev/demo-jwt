import React from 'react';
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Button from "react-bootstrap/Button"
import Stack from "react-bootstrap/Stack";
import "../css/bootstrap-5.2.3-dist/css/bootstrap.min.css";

function Login() {

    const handleClick = (e) => {
        const id = e.target.id;
        window.location.href = `${process.env.REACT_APP_SERVER_URI}/oauth2/authorization/${id}`;         
    } 
  
    return (
        
        <Container fluid style={{marginTop:'10px'}}>
            <Row>
                <Col>
                    <Stack gap={3} className="col-md-3">
                        <Button variant="primary" id="google" onClick={handleClick} >Login with Google</Button>
                        <Button variant="primary" id="github">Login with Github</Button>
                    </Stack>
                </Col>
            </Row>
        </Container>               
    );

}

export default Login;
