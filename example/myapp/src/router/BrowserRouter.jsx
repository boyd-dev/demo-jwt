import React from "react";
import {createBrowserRouter} from "react-router-dom";
import App from "../App";
import Sub from "../pages/Sub";
import axios from "axios";
import ErrorMsg from "../pages/ErrorMsg";

const router = createBrowserRouter([
  {
    path: "*",
    element: <App/>,
    loader: async () => {
      // 렌더링 전에 백엔드 API를 호출하여 데이터를 가져온다.
      // guest 경로는 모든 요청에 대해 응답하도록 되어 있다.
      const response = await axios.get(`${process.env.REACT_APP_SERVER_URI}/guest/test`, {timeout: 10000 });
      return response.data;
    },
    errorElement: <ErrorMsg/>,
    children: [
      {
        path: "sub",
        element: <Sub />
      }
    ]
  },
]);

export default router;