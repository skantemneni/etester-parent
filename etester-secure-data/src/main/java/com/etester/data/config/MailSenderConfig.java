package com.etester.data.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
//@Profile("cloud")
public class MailSenderConfig {

    @Bean
    public JavaMailSender mailSender() {
    	String host = "email-smtp.us-east-1.amazonaws.com";
    	String username = "AKIAJC75PYCNKTRMPRWA";
		String password ="AuDTa8mOpUsPXyKlzxGJrLxgUb0v3sSLeAtuxgs0lCyY";
		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtps.auth", true);
		javaMailProperties.put("mail.transport.protocol", "smtp");
		javaMailProperties.put("mail.smtp.port", 587);
		javaMailProperties.put("mail.smtp.starttls.enable", true);
		javaMailProperties.put("mail.smtp.starttls.required", true);
		javaMailProperties.put("mail.debug", true);
		javaMailProperties.put("mail.smtp.from", "contactus@etester.com");

    	JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    	mailSender.setHost(host);
    	mailSender.setUsername(username);
    	mailSender.setPassword(password);
    	mailSender.setJavaMailProperties(javaMailProperties);
    	return mailSender;
    }

}
