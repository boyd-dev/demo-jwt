import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import Stack from "react-bootstrap/Stack";
import Alert from "react-bootstrap/Alert";
import { useRouteError } from "react-router-dom";
import { Container } from "react-bootstrap";

/**
 *  서버가 응답하지 않을 때 화면
 */
function ErrorMsg() {

    const error = useRouteError();

    return ( 
        <Container fluid style={{marginTop:'10px'}}>   
        <Row>
            <Col>
                <Stack gap={3} className="col-md-3">
                    <Alert variant="danger">Server is not responding. {error.message} </Alert>                           
                </Stack>
            </Col>
        </Row>
        </Container>
    )
}

export default ErrorMsg;

