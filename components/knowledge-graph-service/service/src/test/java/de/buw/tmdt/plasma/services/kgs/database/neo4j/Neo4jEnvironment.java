package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Neo4jContainer;

@ContextConfiguration(initializers = {Neo4jEnvironment.Initializer.class})
public abstract class Neo4jEnvironment {
	private static final String PASSWORD = "plasma";

	private static final Neo4jContainer neo4jContainer = new Neo4jContainer("neo4j:4.1.3")
			.withAdminPassword(PASSWORD);

	static {
		neo4jContainer.start();
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			neo4jContainer.start();
			TestPropertyValues.of(
					"neo4j.host=" + neo4jContainer.getContainerIpAddress(),
					"neo4j.bolt-port=" + neo4jContainer.getMappedPort(7687),
					"neo4j.username=neo4j",
					"neo4j.password=" + neo4jContainer.getAdminPassword()
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}
}
