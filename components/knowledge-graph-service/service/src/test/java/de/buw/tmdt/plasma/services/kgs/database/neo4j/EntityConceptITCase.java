package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.TestApplication;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityConceptITCase extends Neo4jEnvironment {

	private static final String DATABASE_EC_EXAMPLE = "entityconcepts.dump";
	private static final String DATABASE_RELATED_EXAMPLE = "related-entityconcepts.dump";

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
	void testRetrieveEntityConceptsWithEmptyPrefix() {
		graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_EC_EXAMPLE);
		Set<EntityConcept> actual = testee.retrieveEntityConceptsByPrefix("");
		final Set<String> actualMainLabels = actual.stream().map(EntityConcept::getMainLabel).collect(Collectors.toSet());
		final Set<String> expectedMainLabels = new HashSet<>(Arrays.asList("factory", "robot", "Floor", "car", "computer"));
		assertEquals(expectedMainLabels, actualMainLabels);
	}

	@Test
	void testRetrieveEntityConceptsByPrefix() {
		graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_EC_EXAMPLE);
		final Set<EntityConcept> actual = testee.retrieveEntityConceptsByPrefix("f");
		final Set<EntityConcept> expected = new HashSet<>();
		expected.add(new EntityConcept(
				"1",
				"factory",
				"A plant consisting of one or more buildings with facilities for manufacturing"
		));
		expected.add(new EntityConcept(
				"3",
				"Floor",
				"The inside lower horizontal surface (as of a room, hallway, tent, or other structure)"
		));
		assertEquals(expected, actual);
	}

	@Test
	void testRetrieveCreatedEntityConcept() {
		EntityConcept ec = new EntityConcept("entityConceptID", "some concept", "some desc");
		EntityConcept createdEntityConcept = testee.createEntityConcept(ec);

		//Validate that main label, description are valid and that it starts with an EC ID
		assertEquals(ec.getMainLabel(), createdEntityConcept.getMainLabel());
		assertEquals(ec.getDescription(), createdEntityConcept.getDescription());
		assertTrue(createdEntityConcept.getId().startsWith(Neo4JModel.EntityConcept.ID_PREFIX));

		//try to retrieve EntityConcept with created ID and compare it to the created one
		EntityConcept retrievedNewEntityConcept = testee.retrieveEntityConcept(createdEntityConcept.getId());
		assertNotNull(retrievedNewEntityConcept);
		assertEquals(createdEntityConcept, retrievedNewEntityConcept);
	}

	@Test
	void testQueryRelatedEntityConcepts() {
		graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_RELATED_EXAMPLE);

		EntityConcept rootEntityConcept = testee.retrieveEntityConceptsContainingString("Dog").iterator().next();
		RelationConcept relationConcept = testee.retrieveRelationConcept("#22:14"); //hardcoded against the imported schema
		Set<EntityConcept> actualEntityConcepts = testee.queryRelatedEntityConcepts(rootEntityConcept, relationConcept);

		assertEquals(3, actualEntityConcepts.size(), "Amount of found EntityConcepts didn't match.");
		Set<String> expectedEntityConcepts = new HashSet<>();
		expectedEntityConcepts.add("Dog");
		expectedEntityConcepts.add("Mammal");
		expectedEntityConcepts.add("Animal");
		Iterator<EntityConcept> entityConceptIterator = actualEntityConcepts.iterator();
		while (entityConceptIterator.hasNext()) {
			EntityConcept entityConcept = entityConceptIterator.next();
			if (expectedEntityConcepts.contains(entityConcept.getMainLabel())) {
				expectedEntityConcepts.remove(entityConcept.getMainLabel());
				entityConceptIterator.remove();
			}
		}
		assertEquals(0, expectedEntityConcepts.size(), "False negative(s) in result: " + expectedEntityConcepts.toString());
		assertEquals(0, actualEntityConcepts.size(), "False positive(s) in result: " + actualEntityConcepts.toString());
	}

	@Test
	void testEntityConceptCreationWithDescriptions() {
		EntityConcept ec = new EntityConcept("entityConceptID", "testConcept", "");
		EntityConcept ecWith = new EntityConcept("entityConceptID", "testConcept", "myTestDescription is fancy");
		EntityConcept createdEntityConcept = testee.createEntityConcept(ec);

		//Validate that main label, description are valid and that it starts with an EC ID
		assertEquals(ec.getMainLabel(), createdEntityConcept.getMainLabel());
		assertEquals(ec.getDescription(), createdEntityConcept.getDescription());

		EntityConcept createdEntityConceptWith = testee.createEntityConcept(ecWith);
		assertEquals(ecWith.getMainLabel(), createdEntityConceptWith.getMainLabel());
		assertEquals(ecWith.getDescription(), createdEntityConceptWith.getDescription());

		EntityConcept createdEntityConceptTwice = testee.createEntityConcept(ec);
		assertEquals(ec.getMainLabel(), createdEntityConceptTwice.getMainLabel());
		assertEquals(ec.getDescription(), createdEntityConceptTwice.getDescription());
		assertEquals(createdEntityConcept.getId(), createdEntityConceptTwice.getId());
	}
}