package com.etester.security.login.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.etester.security.login.security.services.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = parseJwt(request);
			if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
				String username = jwtUtils.getUserNameFromJwtToken(jwt);

				UserDetails userDetails = userDetailsService.loadUserByUsername(username);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				log.debug("JWT Auth Token is either Null, Not Valid or Expired.  Token: {}", jwt);
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * This method retrieves the Jwt Auth Token.  In do so it gives a preference to Authorization Beader Header and then to the Cookie.
	 * It does not really matter whih way we go.  However, I am choosing to go with auth Bearer Token header first.   
	 * @param request
	 * @return
	 */
	private String parseJwt(HttpServletRequest request) {
		String jwt = getJwtFromAuthHeader(request); 
		if (jwt != null) {
			log.debug("Lord Shiva sent a Auth Token: {}", jwt);
		} else {
			jwt = getJwtFromCookie(request);
			log.debug("Lord Shiva sent a Cookie: {}", jwt);
		}
		return jwt;
	}

	private String getJwtFromCookie(HttpServletRequest request) {
		String jwt = jwtUtils.getJwtFromCookies(request);
		return jwt;
	}

	private String getJwtFromAuthHeader(HttpServletRequest request) {
		String headerAuth = request.getHeader("Authorization");

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			return headerAuth.substring(7, headerAuth.length());
		}

		return null;
	}
}
