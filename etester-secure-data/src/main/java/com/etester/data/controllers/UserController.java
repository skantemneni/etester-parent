package com.etester.data.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.etester.data.domain.user.User;
import com.etester.data.payload.response.UserDetailsResponse;
import com.etester.data.repository.JdbcUserDetailsRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/data/user")
public class UserController {
	@Autowired
	JdbcUserDetailsRepository userDetailsRepository;

	@GetMapping("/userdetails")
	public ResponseEntity<UserDetailsResponse> findUserDetails (HttpServletResponse httpServletResponse, 
			@RequestParam("username") final String username) {
		log.info("User Not found for username: {}", username);
		boolean userExists = userDetailsRepository.existsByUsername(username);
		Optional<User> userDetails = null;
		if (userExists) {
			userDetails = userDetailsRepository.findUserDetailsByUsername(username);
			if (userDetails.isPresent()) {
				UserDetailsResponse userDetailsResponse = new UserDetailsResponse(userDetails.get());
				return ResponseEntity.ok().body(userDetailsResponse);
			}
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	@GetMapping("/currentuserdetails")
	ResponseEntity<UserDetailsResponse> findCurrentUserDetails (HttpServletResponse httpServletResponse) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Calling findCurrentUserDetails for Current User: {}", username);

		boolean userExists = userDetailsRepository.existsByUsername(username);
		Optional<User> userDetails = null;
		if (userExists) {
			userDetails = userDetailsRepository.findUserDetailsByUsername(username);
			if (userDetails.isPresent()) {
				UserDetailsResponse userDetailsResponse = new UserDetailsResponse(userDetails.get());
				return ResponseEntity.ok().body(userDetailsResponse);
			}
		}
		log.info("User Not found for username: {}", username);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	
	
}
