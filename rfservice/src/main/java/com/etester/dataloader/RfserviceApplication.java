package com.etester.dataloader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ComponentScan({ "com.etester.data", "com.etester.dataloader" })

@SpringBootApplication
public class RfserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RfserviceApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// Remember that this shuts off not only authentication but also any security
	// protections like XSS.
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/**");
	}

//	@Bean
//	public JavaMailSender mailSender() {
//		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//		mailSender.setHost("smtp.gmail.com");
//		mailSender.setPort(587);
//		mailSender.setUsername("your_email");
//		mailSender.setPassword("your_password");
//		 
//		Properties properties = new Properties();
//		properties.setProperty("mail.smtp.auth", "true");
//		properties.setProperty("mail.smtp.starttls.enable", "true");
//		 
//		mailSender.setJavaMailProperties(properties);
//		return mailSender;
//	}

}
