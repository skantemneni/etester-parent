package com.etester.security.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.etester.data", "com.etester.security"})

@SpringBootApplication
public class EtesterSecurityLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(EtesterSecurityLoginApplication.class, args);
	}

}
