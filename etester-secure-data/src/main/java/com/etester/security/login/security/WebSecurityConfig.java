package com.etester.security.login.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.etester.security.login.repository.JdbcUserRepository;
import com.etester.security.login.repository.UserRepository;
import com.etester.security.login.security.jwt.AuthEntryPointJwt;
import com.etester.security.login.security.jwt.AuthTokenFilter;
import com.etester.security.login.security.services.UserDetailsServiceImpl;

@Configuration
//@EnableWebSecurity
@EnableGlobalMethodSecurity(
		// securedEnabled = true,
		// jsr250Enabled = true,
		prePostEnabled = true)
public class WebSecurityConfig { // extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Autowired
	private DataSource dataSource;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserRepository userRepository() {
		return new JdbcUserRepository(dataSource, passwordEncoder());
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}


/*
 * ************************************************************************************************************************	
 */
//	Retained from old EtesterDB code	
//	private static final String[] LOGIN_URL_WHITELIST = { "/logincontroller/login", "/register", "/resetpassword" };
////	private static final String[] LOGIN_URL_REDIRECT = { "/login_redirect" };
//	private static final String[] AUTH_WHITELIST = { "/", "/data/*", "/data/*/*", "/data/**", };
//
//	@Override
//	protected void configure(HttpSecurity httpSecurity) throws Exception {
//		httpSecurity.cors().and().csrf().disable().authorizeRequests()
//			.antMatchers(AUTH_WHITELIST).permitAll()
//			.antMatchers(HttpMethod.POST, LOGIN_URL_WHITELIST).permitAll()
////			.antMatchers(LOGIN_URL_REDIRECT).permitAll()
//			.anyRequest().authenticated().and()
//			.addFilter(new EtesterAuthenticationFilter(authenticationManager()))
//			.addFilter(new EtesterAuthorizationFilter(authenticationManager())).sessionManagement()
//			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//	}
//
/*
 * ************************************************************************************************************************	
 */

//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http.cors().and().csrf().disable()
//      .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
//      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//      .authorizeRequests().antMatchers("/api/auth/**").permitAll()
//      .antMatchers("/api/test/**").permitAll()
//      .antMatchers(h2ConsolePath + "/**").permitAll()
//      .anyRequest().authenticated();
//    
//    // fix H2 database console: Refused to display ' in a frame because it set 'X-Frame-Options' to 'deny'
//    http.headers().frameOptions().sameOrigin();
//
//    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//  }

	private static final String[] LOGIN_URL_WHITELIST = { "/api/auth/**" };
	private static final String[] H2_CONSOLE_PATH = { "h2-console/**", "/h2-ui/**" };
	private static final String[] AUTHENTICATED_WHITELIST = { "/api/data/**", };

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// https://docs.spring.io/spring-security/reference/whats-new.html
	// gh-11939 - Remove deprecated antMatchers, mvcMatchers, regexMatchers helper methods from Java Configuration. 
	// Instead, use requestMatchers or HttpSecurity#securityMatchers.
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.requestMatchers(LOGIN_URL_WHITELIST).permitAll().requestMatchers(AUTHENTICATED_WHITELIST).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
//        .antMatchers(H2_CONSOLE_PATH).permitAll()
				.anyRequest().authenticated()
				;

		// fix H2 database console: Refused to display ' in a frame because it set
		// 'X-Frame-Options' to 'deny'
		http.headers().frameOptions().sameOrigin();

		http.authenticationProvider(authenticationProvider());

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}



//	@Bean
//	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//		http.cors().and().csrf().disable().exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
//				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
//				.antMatchers(LOGIN_URL_WHITELIST).permitAll().antMatchers(AUTHENTICATED_WHITELIST).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
////        .antMatchers(H2_CONSOLE_PATH).permitAll()
//				.anyRequest().authenticated()
//				;
//
//		// fix H2 database console: Refused to display ' in a frame because it set
//		// 'X-Frame-Options' to 'deny'
//		http.headers().frameOptions().sameOrigin();
//
//		http.authenticationProvider(authenticationProvider());
//
//		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//
//		return http.build();
//	}
	


}
