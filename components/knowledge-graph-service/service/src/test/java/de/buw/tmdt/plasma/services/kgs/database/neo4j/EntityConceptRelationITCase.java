package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.shared.model.Element;
import de.buw.tmdt.plasma.services.kgs.TestApplication;
import de.buw.tmdt.plasma.services.kgs.shared.model.Position;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.Relation;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = TestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityConceptRelationITCase extends Neo4jEnvironment {

    private static final String DATABASE_ECR_EXAMPLE = "entityconceptrelations.dump";

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
    void testCreateEntityConceptRelationWithEntityConcepts() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_ECR_EXAMPLE);

        //Get the relation concept from the database
        RelationConcept rc = testee.retrieveRelationConcept("rc_300e2fe4-4f36-4f8f-b0a3-327bd5afd4fa");

        //Define two new entity concepts
        EntityConcept ec1 = testee.retrieveEntityConcept("ec_fd27d9f7-bb37-43a0-a578-53d576b6cdec");
        EntityConcept ec2 = testee.retrieveEntityConcept("ec_1d33d23d-3379-40b8-8322-e5f4384a659d");

        //Define two entity types
        EntityType et1 = new EntityType("et1", "et1Label", "", ec1, new Position(0.0, 0.0), "", false);
        EntityType et2 = new EntityType("et2", "et2Label", "", ec2, new Position(0.0, 0.0), "", false);

        //Define relation
        Relation relation = new Relation("", et1, et2, rc);

        //Define semantic model
        SemanticModel semanticModel = new SemanticModel(
                "",
                "sm1",
                "",
                Stream.of(et1, et2).collect(Collectors.toList()),
                Collections.singleton(relation),
                ""
        );

        testee.createSemanticModelExposeMapping(semanticModel);

        Set<EntityConceptRelation> relationSet = testee.queryAllRelatedEntityConcepts(ec1);
        assertFalse(relationSet.isEmpty());
        assertEquals(relationSet.size(), 1);

        EntityConceptRelation actualRelation = relationSet.stream().findFirst().get();

        //evaluate further properties
        assertEquals(rc, actualRelation.getRelationConcept());
        assertEquals(ec1.getMainLabel(), actualRelation.getTailNode().getMainLabel());
        assertEquals(ec1.getDescription(), actualRelation.getTailNode().getDescription());
        assertEquals(ec2.getMainLabel(), actualRelation.getHeadNode().getMainLabel());
        assertEquals(ec2.getDescription(), actualRelation.getHeadNode().getDescription());
    }

    @Test
    void testCreateEntityConceptRelationCounterIncrease() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_ECR_EXAMPLE);

        //Get the relation concept from the database
        RelationConcept rc1 = testee.retrieveRelationConcept("rc_300e2fe4-4f36-4f8f-b0a3-327bd5afd4fa");
        RelationConcept rc2 = testee.retrieveRelationConcept("rc_120e2fg4-12e2-23fv-b0a3-adsfgggag325");

        //Define two new entity concepts
        EntityConcept ec1 = testee.retrieveEntityConcept("ec_fd27d9f7-bb37-43a0-a578-53d576b6cdec");
        EntityConcept ec2 = testee.retrieveEntityConcept("ec_1d33d23d-3379-40b8-8322-e5f4384a659d");

        //Define two entity types
        EntityType et1 = new EntityType("et1", "et1Label", "", ec1, new Position(0.0, 0.0), "", false);
        EntityType et2 = new EntityType("et2", "et2Label", "", ec2, new Position(0.0, 0.0), "", false);

        //Define relations
        Relation relation = new Relation("r1", et1, et2, rc1);
        Relation relation2 = new Relation("r2", et1, et2, rc2);

        //Define first with semantic model
        SemanticModel semanticModel = new SemanticModel(
                "41",
                "sm1",
                "",
                Stream.of(et1, et2).collect(Collectors.toList()),
                Collections.singleton(relation),
                ""
        );

        //Define second semantic model
        SemanticModel semanticModel2 = new SemanticModel(
                "42",
                "sm2",
                "",
                Stream.of(et1, et2).collect(Collectors.toList()),
                Collections.singleton(relation2),
                ""
        );

        //Create two semantic models with two different relations between the same ECs
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        testee.createSemanticModelExposeMapping(semanticModel2);

        //Test for new size of queried ECR sets
        Set<EntityConceptRelation> relationSet2 = testee.queryAllRelatedEntityConcepts(ec1);
        assertFalse(relationSet2.isEmpty());
        assertEquals(relationSet2.size(), 2);

        //Evaluate updated relative usage property
        for (EntityConceptRelation ecr : relationSet2) {
            assertEquals(0.5, ecr.getRelativeUsage());
        }

        //Remove first semantic model
        testee.removeSemanticModel(mapping.get("41").getId());

        //Test for removal of Relation and implying a reduction of relative usage
        Set<EntityConceptRelation> relationSet3 = testee.queryAllRelatedEntityConcepts(ec1);
        assertFalse(relationSet3.isEmpty());
        assertEquals(relationSet3.size(), 1);

        EntityConceptRelation actualRelation3 = relationSet3.stream().findFirst().get();

        //Evaluate updated relative usage property after delete)
        assertEquals(actualRelation3.getRelativeUsage(), 1.0);
    }

    @Test
    void testRemoveEntityConceptRelationCounterDecrease() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_ECR_EXAMPLE);

        //Get the relation concept from the database
        RelationConcept rc1 = testee.retrieveRelationConcept("rc_300e2fe4-4f36-4f8f-b0a3-327bd5afd4fa");
        RelationConcept rc2 = testee.retrieveRelationConcept("rc_120e2fg4-12e2-23fv-b0a3-adsfgggag325");

        //Define two new entity concepts
        EntityConcept ec1 = testee.retrieveEntityConcept("ec_fd27d9f7-bb37-43a0-a578-53d576b6cdec");
        EntityConcept ec2 = testee.retrieveEntityConcept("ec_1d33d23d-3379-40b8-8322-e5f4384a659d");

        //Define two entity types
        EntityType et1 = new EntityType("et1", "et1Label", "", ec1, new Position(0.0, 0.0), "", false);
        EntityType et2 = new EntityType("et2", "et2Label", "", ec2, new Position(0.0, 0.0), "", false);

        //Define relations
        Relation relation = new Relation("r1", et1, et2, rc1);
        Relation relation2 = new Relation("r2", et1, et2, rc2);

        //Define first with semantic model
        SemanticModel semanticModel = new SemanticModel(
                "41",
                "sm1",
                "",
                Stream.of(et1, et2).collect(Collectors.toList()),
                Collections.singleton(relation),
                ""
        );

        //Define second semantic model
        SemanticModel semanticModel2 = new SemanticModel(
                "42",
                "sm2",
                "",
                Stream.of(et1, et2).collect(Collectors.toList()),
                Collections.singleton(relation2),
                ""
        );

        //Create two semantic models with two different relations between the same ECs
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        Map<String, Element> mapping2 = testee.createSemanticModelExposeMapping(semanticModel2);

        //Test for new size of queried ECR sets
        Set<EntityConceptRelation> relationSet = testee.queryAllRelatedEntityConcepts(ec1);
        assertFalse(relationSet.isEmpty());
        assertEquals(2, relationSet.size());

        //Remove first semantic model
        testee.removeSemanticModel(mapping.get("41").getId());

        //Test for removal of Relation and implying a reduction of relative usage
        Set<EntityConceptRelation> relationSet3 = testee.queryAllRelatedEntityConcepts(ec1);
        assertFalse(relationSet3.isEmpty());
        assertEquals(1, relationSet3.size());

        EntityConceptRelation actualRelation3 = relationSet3.stream().findFirst().get();

        //Evaluate updated relative usage property after delete)
        assertEquals(1.0, actualRelation3.getRelativeUsage());
    }
}