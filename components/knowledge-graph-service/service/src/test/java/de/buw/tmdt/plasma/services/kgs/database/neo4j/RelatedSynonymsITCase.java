package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.TestApplication;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RelatedSynonymsITCase extends Neo4jEnvironment {

	private static final String DATABASE_RC_DUMP = "related-entityconcepts.dump";

	@Autowired
	private GraphDBDriverFactoryImpl graphDBDriverFactoryImpl;

	@Autowired
	private GraphDBTestDriverFactoryImpl graphDBTestDriverFactoryImpl;

	private GraphDBDriver testee;

	@BeforeAll
	void beforeAll() {
		graphDBTestDriverFactoryImpl.clearDatabase(); //ensure that all tests start with a clean database
		testee = graphDBDriverFactoryImpl.getGraphDBDriver(); //initialize the testee
	}

	@AfterEach
	void afterEach() {
		graphDBTestDriverFactoryImpl.clearDatabase(); //cleanup after all tests
	}

	@AfterAll
	void afterAll() {
		graphDBTestDriverFactoryImpl.clearDatabase(); //cleanup after all tests
		graphDBDriverFactoryImpl.closeDriver();
	}

	@Test
	void retrieveRelatedConceptsTest() {
		graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_RC_DUMP);
		Set<EntityConcept> ecConceptsInGraph = testee.retrieveEntityConceptsByPrefix("Dog");
		EntityConcept mammal = testee.retrieveEntityConcept("#14:52");
		EntityConcept trap = testee.retrieveEntityConcept("#14:53");

		Set<EntityConcept> neighbours =
				ecConceptsInGraph.stream().map(concept -> testee.retrieveEntityConceptNeighbours(concept)).flatMap(Set::stream).collect(Collectors.toSet());

		assertTrue(neighbours.contains(mammal));
		assertTrue(neighbours.contains(trap));
	}
}
