import React from 'react';
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Stack from "react-bootstrap/Stack";
import "../css/bootstrap-5.2.3-dist/css/bootstrap.min.css";


function Help() {

    return (
        <>
        <Container fluid style={{marginTop:'10px'}}>           
        <Row>
            <Col>
                <Stack gap={3} className="col-md-3">
                    Help - 일반적인 라우트 화면
                </Stack>               
            </Col>
        </Row>          
        </Container>       
        </>
        
    )
}


export default Help;
