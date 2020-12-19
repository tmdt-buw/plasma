package de.buw.tmdt.plasma.services.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.ConfigurableApplicationContext;

@EnableEurekaServer
@SpringBootApplication(scanBasePackages = "de.buw.tmdt.plasma")
public class ServiceDiscoveryApplication {
	private static final Logger logger = LoggerFactory.getLogger(ServiceDiscoveryApplication.class);

	public static void main(String[] args) {
		final ConfigurableApplicationContext context = SpringApplication.run(ServiceDiscoveryApplication.class, args);
		final String applicationName = context.getEnvironment().getProperty("spring.application.name");
		logger.info("{}: Booting successful", applicationName);
	}
}
