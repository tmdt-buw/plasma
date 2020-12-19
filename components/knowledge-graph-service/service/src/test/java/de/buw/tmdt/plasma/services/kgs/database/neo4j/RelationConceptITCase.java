package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.services.kgs.TestApplication;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.ConceptNameAlreadyExistsException;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.NoElementForIDException;
import de.buw.tmdt.plasma.utilities.collections.ArrayUtilities;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RelationConceptITCase extends Neo4jEnvironment {

	private static final String DATABASE_RC_EXAMPLE = "relationconcepts.dump";

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
	void testRetrieveRelationConceptsWithEmptyPrefix() {
		graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_RC_EXAMPLE);
		Set<RelationConcept> actual = testee.retrieveRelationConceptsByPrefix("");
		assertEquals(5, actual.size());
		final Set<String> expectedNames = new HashSet<>(Arrays.asList("paints", "leads", "produces", "presents", "likes"));
		final Set<String> actualNames = actual.stream().map(RelationConcept::getLabel).collect(Collectors.toSet());
		assertEquals(expectedNames, actualNames);
	}

	@Test
	void testRetrieveCreatedRelationConcept() {
		Set<RelationConcept.Property> properties = ArrayUtilities.toCollection(
				HashSet::new, RelationConcept.Property.REFLEXIVE, RelationConcept.Property.SYMMETRIC
		);
		RelationConcept rc = new RelationConcept("relationConceptID", "newRelationConcept", "relationConceptDescription", properties);
		RelationConcept createdRelationConcept = testee.createRelationConcept(rc);

		//Validate that label, description and properties are valid and that it starts with an RC ID
		assertEquals(createdRelationConcept.getLabel(), rc.getLabel());
		assertEquals(createdRelationConcept.getDescription(), rc.getDescription());
		assertEquals(createdRelationConcept.getProperties(), rc.getProperties());
		assertTrue(createdRelationConcept.getId().startsWith(Neo4JModel.RelationConcept.ID_PREFIX));

		//try to retrieve RelationConcept with created ID and compare it to the created one
		RelationConcept retrievedNewRelationConcept = testee.retrieveRelationConcept(createdRelationConcept.getId());
		assertNotNull(retrievedNewRelationConcept);
		assertEquals(retrievedNewRelationConcept, createdRelationConcept);
	}

	@Test
	void testRetrieveNonExistentRelationConcept() {
		assertThrows(NoElementForIDException.class, () -> testee.retrieveRelationConcept("nonExistentID"));
	}

	@Test
	void testFindRelationConceptByName() {
		graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_RC_EXAMPLE);
		RelationConcept expectedRelationConcept = new RelationConcept("1", "presents", "Give an exhibition of to an interested audience", new HashSet<>());
		Set<RelationConcept> expected = new HashSet<>();
		expected.add(expectedRelationConcept);
		Set<RelationConcept> actual = testee.retrieveRelationConceptsContainingString("presents");
		assertEquals(expected, actual);
	}

	@Test
	void testRelationConceptCreationWithDescriptions() {
		RelationConcept rc = new RelationConcept("relationConceptID", "testConcept", "", "kg:rc1", null);
		RelationConcept rcWith = new RelationConcept("relationConceptID", "testConcept", "myTestDescription is fancy", "kg:rc2", null);
		RelationConcept createdRelationConcept = testee.createRelationConcept(rc);

		//Validate that main label, description are valid and that it starts with an EC ID
		assertEquals(rc.getLabel(), createdRelationConcept.getLabel());
		assertEquals(rc.getDescription(), createdRelationConcept.getDescription());

		RelationConcept createdRelationConceptWith = testee.createRelationConcept(rcWith);
		assertEquals(rcWith.getLabel(), createdRelationConceptWith.getLabel());
		assertEquals(rcWith.getDescription(), createdRelationConceptWith.getDescription());
	}

	@Test
	void testRelationConceptCreationTwice() {
		RelationConcept rc = new RelationConcept("relationConceptID", "testConcept", "desc", null);
		RelationConcept createdRelationConcept = testee.createRelationConcept(rc);
		assertEquals(rc.getLabel(), createdRelationConcept.getLabel());
		assertEquals(rc.getDescription(), createdRelationConcept.getDescription());
		assertThrows(ConceptNameAlreadyExistsException.class, () -> testee.createRelationConcept(new RelationConcept("", "testConcept", "desc", null)));
	}
}