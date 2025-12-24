package com.docwhisperer.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * The main entry point for the Doc-Whisperer Backend application.
 * <p>
 * This class triggers the Spring Boot auto-configuration and component scanning.
 * It initializes the web server (Tomcat), sets up the application context,
 * and loads all defined beans (controllers, services, repositories).
 * </p>
 */
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
