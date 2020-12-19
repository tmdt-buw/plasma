package de.buw.tmdt.plasma.services.sas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "de.buw.tmdt.plasma")
public class SchemaAnalysisApplication {
	private static final Logger logger = LoggerFactory.getLogger(SchemaAnalysisApplication.class);

	public static void main(String[] args) {
		final ConfigurableApplicationContext context = SpringApplication.run(SchemaAnalysisApplication.class, args);
		final String applicationName = context.getEnvironment().getProperty("spring.application.name");
		logger.info("{}: Booting successful", applicationName);
	}
}
