package com.foo.myapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RestHomeController {	
	
	private static final Logger logger = LoggerFactory.getLogger(RestHomeController.class);
	
	@RequestMapping(value="/api/user")
	public void user(HttpServletResponse response, @AuthenticationPrincipal OAuth2User user) throws IOException {
		
		//TODO
		// additional user data
		//
		logger.info("USER_ID={}", user.getName());
		
		//test
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("userid", user.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		String data = objectMapper.writeValueAsString(userInfo);
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		ServletOutputStream out = response.getOutputStream();
		out.print(data);		
	}
	
	@PostMapping(value="/api/test")
	public ResponseEntity<TestDto> test(@RequestBody TestDto data, @AuthenticationPrincipal OAuth2User user) throws IOException {
		
		data.setName("Bart");
		data.setAge(66);
		
		return ResponseEntity.ok(data);		
	}
	
	@GetMapping(value="/guest/test")
	public @ResponseBody String guest(HttpServletResponse response) throws IOException {		
		return "Hello!";
	}

}
