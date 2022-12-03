package com.etester.security.login.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.etester.security.login.models.ERole;
import com.etester.security.login.models.LoginUser;
import com.etester.security.login.models.Role;
import com.etester.security.login.payload.request.LoginRequest;
import com.etester.security.login.payload.request.SignupRequest;
import com.etester.security.login.payload.response.LoginUserInfoResponse;
import com.etester.security.login.payload.response.MessageResponse;
import com.etester.security.login.repository.JdbcUserRepository;
import com.etester.security.login.repository.RoleRepository;
import com.etester.security.login.security.jwt.JwtUtils;
import com.etester.security.login.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JdbcUserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	/**
	 * This method is a post request that expects a JSON pay-load of a format that
	 * can de-serialize to a "LoginRequest" class. That will typically look like
	 * this... { "username": "sesi", "password": "12345678" }
	 * 
	 * @param loginRequest
	 * @return
	 */
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		// Create the Jwt Auth Token
		String jwtToken = jwtUtils.generateJwtToken(userDetails);

		// Create a Cookie version of the Auth Token
		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(jwtToken);

		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		// Note that I sending the JWT Auth token in multiple spots. This will allow the
		// client to read as it sees fit.
		// 1.) HttpHeaders.SET_COOKIE header
		// 2.) HttpHeaders.AUTHORIZATION as a Bearer Toke ("Bearer " + Token)
		// 3.) In the Response payload as part of the UserInfoResponse
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
				// ***************************************************************************************************************************************************************/
				// MAKE SURE TO ADD THE NEXT HEADER.
				// MY GOD. This next line is important if the Browser is expected to be able to
				// read the Authorization Token.
				// By default, you can set the headers and not-javascript-browser apps like
				// Postman and enen chrome-debug network tab will show it.
				// However, if we want a Javascript program to be able to read the Header
				// containing the Authorization token, WE NEED TO EXPLICITELY ALLOW THAT.
				.header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.AUTHORIZATION)
				// this line would allow reads if ALL headers in Javascript
				// response.addHeader("Access-Control-Expose-Headers","*");
				// ***************************************************************************************************************************************************************/
				.body(new LoginUserInfoResponse(jwtToken, userDetails.getIdUser(), userDetails.getUsername(),
						userDetails.getEmailAddress(), roles));

	}

	/**
	 * This method is a post request that expects a JSON pay-load of a format that
	 * can de-serialize to a "SignupRequest" class. That will typically look like
	 * this... { "username": "mary", "firstName": "Mary", "lastName": "Turnmaire",
	 * "middleName": "", "emailAddress": "mary@etester.com", "password": "12345678",
	 * "role": ["provider", "admin"] }
	 * 
	 * @param signUpRequest
	 * @return
	 */
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmailAddress(signUpRequest.getEmailAddress())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		LoginUser user = new LoginUser(signUpRequest.getUsername(), signUpRequest.getFirstName(), signUpRequest.getLastName(),
				signUpRequest.getMiddleName(), signUpRequest.getEmailAddress(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByAuthority(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByAuthority(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "provider":
					Role modRole = roleRepository.findByAuthority(ERole.ROLE_PROVIDER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByAuthority(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@PostMapping("/signout")
	public ResponseEntity<?> logoutUser() {
		ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(new MessageResponse("You've been signed out!"));
	}
	
	
//	@PostMapping("/resetpassword")
//	ResponseEntity<String> resetpassword( 
//			HttpServletResponse httpServletResponse, 
//			@RequestParam("username") final String username, 
//			@RequestParam("newpassword") final String newpassword) {
//		try {
//			
//			if (username == null || username.trim().length() == 0 || newpassword == null || newpassword.trim().length() == 0) {
//				throw new Exception("Bad Request");
//			}
//			
//			boolean userExists = userRepository.existsByUsername(username);
//			if (userExists) {
//				Integer response = userDao.updateEncodedPassword(username, newpassword);
//				if (response == 0) {
//					return new ResponseEntity<String>("SUCCESS", HttpStatus.OK);
//				} else {
//					log.error("Error Updating User Password: {}", response);
//					return new ResponseEntity<String>("FAIL", HttpStatus.BAD_REQUEST);
//				}
//			} else {
//				return new ResponseEntity<String>("UPDATE_FAIL_NO_USER", HttpStatus.BAD_REQUEST);
//			}
//		} catch (Exception e) {
//			return new ResponseEntity<String>("FAIL", HttpStatus.BAD_REQUEST);
//		}
//
//	}

	
	
	
}
