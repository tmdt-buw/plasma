package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphDBTestDriverFactoryImpl {

	@Autowired
	private GraphDBDriverFactoryImpl graphDBDriverFactory;

	@NotNull
	public GraphDBTestDriver getGraphDBTestDriver() {
		return new GraphDBTestDriver(graphDBDriverFactory.openDriver());
	}

	public void clearDatabase() {
		graphDBDriverFactory.executeCypher("MATCH (n) DETACH DELETE n");
	}
}