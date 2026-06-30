package com.infotact.project1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/*
Entry point of the Hotel Reservation System
Spring Boot starts execution from this class

Responsibilities:
-> Bootstraps the Spring application
-> Initializes the Spring IoC container
-> Scans and registers beans
-> Configures embedded Tomcat server
-> Loads application.properties
-> Establishes database connections
-> Creates all required Spring-managed objects
 */
@SpringBootApplication
@EnableScheduling
public class Project1Application {

	/*
	Main method executed by the JVM
	This method starts the entire Spring Boot application
	 */

	public static void main(String[] args) {
		SpringApplication.run(Project1Application.class, args);
	}
	/*
	Boots the Spring application
	Internally performs:
	1. Creates ApplicationContext
	2. Scans packages starting from com.infotact.project1
	3. Regsiters Controllers, Services, Repositories and Configurations
	4. Loads application.properties
	5. Connects to PostgreSQL
	6. Initializes Redis and Redisson
	7. Starts embedded Tomcat server
	8. Makes APIs available on port 8080
	 */
}
