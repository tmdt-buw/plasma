package de.buw.tmdt.plasma.services.dps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "de.buw.tmdt.plasma")
public class DataProcessingApplication {
	private static final Logger logger = LoggerFactory.getLogger(DataProcessingApplication.class);

	public static void main(String[] args) {
		final ConfigurableApplicationContext context = SpringApplication.run(DataProcessingApplication.class, args);
		final String applicationName = context.getEnvironment().getProperty("spring.application.name");
		logger.info("{}: Booting successful", applicationName);
	}
}
