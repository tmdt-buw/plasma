package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.shared.model.Element;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.Relation;
import de.buw.tmdt.plasma.services.kgs.TestApplication;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.NoElementForIDException;
import de.buw.tmdt.plasma.services.kgs.shared.model.Position;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SemanticModelITCase extends Neo4jEnvironment {

    private static final String DATABASE_SM_EXAMPLE = "semmodel.dump";
    private static final String DATABASE_SM_QUERY_EXAMPLE = "semmodel-queries.dump";
    private static final String DATABASE_SM_HYPERHYPO_EXAMPLE = "semmodel-hyperhypo.dump";

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
    void testCreateSemanticModelWithoutEntityTypes() {
        assertThrows(IllegalArgumentException.class, () -> new SemanticModel("42", "test", null, new HashSet<>(), null, ""));
        assertThrows(IllegalArgumentException.class, () -> new SemanticModel("42", "", null, new HashSet<>(), new HashSet<>(), ""));
    }

    @Test
    void testRetrieveNonExistentSemanticModel() {
        assertThrows(NoElementForIDException.class, () -> testee.retrieveSemanticModel("nonExistentID"));
    }

    @Test
    void testCreateSemanticModelWithoutRelations() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_EXAMPLE);
        // create EntityConcept
        EntityConcept entityConcept = testee.retrieveEntityConceptsByPrefix("")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No EntityConcept present."));

        // List of entityTypes pointing to the created entityConcept
        HashSet<EntityType> entityTypeList = new HashSet<>();
        entityTypeList.add(new EntityType("ET1", "entityT1", "entityT1", entityConcept, new Position(0, 0), null, true));
        entityTypeList.add(new EntityType("ET2", "entityT2", "entityT2", entityConcept, new Position(1, 1), null, true));
        entityTypeList.add(new EntityType("ET3", "entityT3", "entityT3", entityConcept, new Position(2, 2), null, true));

        //logger.trace(entityTypeList.toString());

        SemanticModel semanticModel = new SemanticModel(
                "42",
                "test",
                null,
                entityTypeList,
                null,
                ""
        );

        // Testee call.
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        assertNotNull(mapping);

        //Get the semantic model from the mapping
        SemanticModel smCreated = (SemanticModel) mapping.get("42");
        assertEquals(3, smCreated.getEntityTypes().size());
        assertEquals(0, smCreated.getRelations().size());

        //Load the semantic model from the database
        SemanticModel smPeristed = testee.retrieveSemanticModel(smCreated.getId());
        assertEquals(smCreated, smPeristed);
    }

    @Test
    void testCreateSemanticModelWithValidDataSourceId() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_EXAMPLE);
        // create EntityConcept
        EntityConcept entityConcept = testee.retrieveEntityConceptsByPrefix("")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No EntityConcept present."));

        // List of entityTypes pointing to the created entityConcept
        HashSet<EntityType> entityTypeList = new HashSet<>();
        entityTypeList.add(new EntityType("ET1", "entityT1", "entityT1", entityConcept, new Position(0, 0), null, true));

        SemanticModel semanticModel = new SemanticModel(
                "42",
                "test",
                null,
                entityTypeList,
                null,
                "VALID_ID"
        );

        // Testee call.
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        assertNotNull(mapping);

        //Get the semantic model from the mapping
        SemanticModel smCreated = (SemanticModel) mapping.get("42");
        assertEquals(1, smCreated.getEntityTypes().size());
        assertEquals(0, smCreated.getRelations().size());
        assertEquals("VALID_ID", smCreated.getDataSourceId());

        //Load the semantic model from the database
        SemanticModel smPeristed = testee.retrieveSemanticModel(smCreated.getId());
        assertEquals(smCreated, smPeristed);
    }

    @Test
    void testCreateSemanticModeWithRelations() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_EXAMPLE);

        // create EntityConcept
        EntityConcept entityConcept = testee.retrieveEntityConceptsByPrefix("")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No EntityConcept present."));

        // List of entityTypes pointing to the created entityConcept
        HashSet<EntityType> entityTypeList = new HashSet<>();
        entityTypeList.add(new EntityType("ET1", "entityT1", "entityT1", entityConcept, new Position(0, 0), null, true));
        entityTypeList.add(new EntityType("ET2", "entityT2", "entityT2", entityConcept, new Position(1, 1), null, true));
        entityTypeList.add(new EntityType("ET3", "entityT3", "entityT3", entityConcept, new Position(2, 2), null, true));

        RelationConcept arbitraryRelationConcept = testee.retrieveRelationConceptsByPrefix("")
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No RelationConcept present."));

        HashSet<Relation> relations = new HashSet<>();
        Iterator<EntityType> iterator = entityTypeList.iterator();
        relations.add(new Relation(
                "RC1",
                iterator.next(),
                iterator.next(),
                arbitraryRelationConcept
        ));

        SemanticModel semanticModel = new SemanticModel(
                "42",
                "test",
                null,
                entityTypeList,
                relations,
                ""
        );

        // Testee call.
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        assertNotNull(mapping);

        //Get the semantic model from the mapping
        SemanticModel smCreated = (SemanticModel) mapping.get("42");
        assertEquals(3, smCreated.getEntityTypes().size());
        assertEquals(1, smCreated.getRelations().size());

        //Load the semantic model from the database
        SemanticModel smPeristed = testee.retrieveSemanticModel(smCreated.getId());
        assertEquals(smCreated, smPeristed);
    }

    @Test
    void testCreateSemanticModelWithoutExistingEntityConcept() {
        HashSet<EntityType> entityTypeList = new HashSet<>();
        EntityConcept nonExistent = new EntityConcept("falseId", "falseDescription", "falseName");

        entityTypeList.add(new EntityType(
                "randomId",
                "randomLabel",
                "randomLabel",
                nonExistent,
                new Position(1, 2),
                null,
                false
        ));

        SemanticModel semanticModel = new SemanticModel(
                "42",
                "test",
                null,
                entityTypeList,
                null,
                ""
        );

        // Testee call.
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        assertNotNull(mapping);

        //Get the semantic model from the mapping
        SemanticModel smCreated = (SemanticModel) mapping.get("42");
        assertEquals(1, smCreated.getEntityTypes().size());
        assertEquals(0, smCreated.getRelations().size());

        //Load the semantic model from the database
        SemanticModel smPeristed = testee.retrieveSemanticModel(smCreated.getId());
        assertEquals(smCreated, smPeristed);
    }

    //test SemanticModel ETs without ECs and Relations without RC's
    @Test
    void testCreateSemanticModelWithoutExistingEntityConceptAndRelation() {
        HashSet<EntityType> entityTypeList = new HashSet<>();
        EntityConcept nonExistent = new EntityConcept("falseId", "falseDescription", "falseName");

        entityTypeList.add(new EntityType(
                "randomId1",
                "randomLabel1",
                "randomLabel1",
                nonExistent,
                new Position(1, 2),
                null,
                false
        ));
        entityTypeList.add(new EntityType(
                "randomId2",
                "randomLabel2",
                "randomLabel2",
                nonExistent,
                new Position(1, 2),
                null,
                false
        ));

        HashSet<Relation> relationList = new HashSet<>();
        RelationConcept nonExistentRC = new RelationConcept("noID", "noName", "noDesc", null);

        Iterator<EntityType> iterator = entityTypeList.iterator();
        relationList.add(new Relation("someID", iterator.next(), iterator.next(), nonExistentRC));

        SemanticModel semanticModel = new SemanticModel(
                "42",
                "test",
                "I am a test",
                entityTypeList,
                relationList,
                ""
        );

        // Testee call.
        Map<String, Element> mapping = testee.createSemanticModelExposeMapping(semanticModel);
        assertNotNull(mapping);

        //Get the semantic model from the mapping
        SemanticModel smCreated = (SemanticModel) mapping.get("42");
        assertEquals(2, smCreated.getEntityTypes().size());
        assertEquals(1, smCreated.getRelations().size());

        //Load the semantic model from the database
        SemanticModel smPeristed = testee.retrieveSemanticModel(smCreated.getId());
        assertEquals(smCreated, smPeristed);
    }

    @Test
    void testRemoveSemanticModel() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);
        assertTrue(testee.removeSemanticModel("sm_29d23f08-0965-4120-b3f1-c769e9b74732"));
    }

    @Test
    void testRemoveNonExistentSemanticModel() {
        assertFalse(testee.removeSemanticModel("nonExistentID"));
    }

    @Test
    void testRetrieveExistentSemanticModel() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        EntityConcept ec1 = new EntityConcept("ec_3a2d3b85-0ec9-4718-b9fc-96e615c79fab", "ec1", null);
        EntityConcept ec2 = new EntityConcept("ec_72ee41c4-3e3d-450b-9e3a-286be3c8fe0f", "ec2", null);
        EntityConcept ec3 = new EntityConcept("ec_9cfdb877-c229-4b4a-a584-0963ddeee8d9", "ec3", null);

        SemanticModel expected = new SemanticModel(
                "sm_29d23f08-0965-4120-b3f1-c769e9b74732",
                "test",
                "I am a test semantic model",
                new HashSet<>(Arrays.asList(
                        new EntityType("et_eb47feea-7c0b-46bb-87e7-0ee7d1f32ea8", "et1", "et1_original", ec1, new Position(0, 1), null, true),
                        new EntityType("et_ac0b190f-809a-4571-85c9-992eaa366070", "et2", "et2_original", ec2, new Position(0, 1), null, true),
                        new EntityType("et_d9d75b34-7b66-44db-bb7e-969695c34137", "et3", "et3_original", ec3, new Position(0, 1), null, true)
                )),
                null,
                "123"
        );
        SemanticModel actual = testee.retrieveSemanticModel("sm_29d23f08-0965-4120-b3f1-c769e9b74732");
        assertEquals(actual, expected);
    }

    @Test
    void testEntityTypeLabelQuery() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityType("et1");
        assertEquals(1, res.size());
        assertEquals("sm_29d23f08-0965-4120-b3f1-c769e9b74732", res.iterator().next().getId());
    }

    @Test
    void testEntityTypeLabelQuery2() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityType("et4");
        assertEquals(2, res.size());
        assertEquals(res.stream().map(Element::getId).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(
                "sm_755a7ad5-acd2-4cc6-ab5e-52e6cc41621c", "sm_592574a6-b12d-449b-a18a-01e2e470055f")));
    }

    @Test
    void testEntityTypeLabelQuery3() {
        Set<SemanticModel> res = testee.searchSemanticModelsForEntityType("doesntExistAtAll");
        assertEquals(0, res.size());
    }

    @Test
    void testEntityTypeOriginalLabelQuery() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityType("et1_original");
        assertEquals(1, res.size());
        assertEquals("sm_29d23f08-0965-4120-b3f1-c769e9b74732", res.iterator().next().getId());
    }

    @Test
    void testEntityTypeOriginalLabelQuery2() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityType("et4_original");
        assertEquals(2, res.size());
        assertEquals(res.stream().map(Element::getId).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(
                "sm_755a7ad5-acd2-4cc6-ab5e-52e6cc41621c", "sm_592574a6-b12d-449b-a18a-01e2e470055f")));
    }

    @Test
    void testEntityConceptNameQuery() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityConceptMainLabel("ec1");
        assertEquals(3, res.size());
        Set<String> expected = new HashSet<>(Arrays.asList(
                "sm_40160c7e-46fc-4bb9-90e4-b9b0627e61b0",
                "sm_d4ea0b47-0ac3-4717-83a0-fffb1988ff56",
                "sm_29d23f08-0965-4120-b3f1-c769e9b74732"
        ));
        assertEquals(res.stream().map(Element::getId).collect(Collectors.toSet()), expected);
    }

    @Test
    void testSynonyms() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityConceptSynonymLabel("ecsyn");
        assertEquals(1, res.size());
        Set<String> expected = new HashSet<>(Collections.singletonList("sm_592574a6-b12d-449b-a18a-01e2e470055f"));
        assertEquals(res.stream().map(Element::getId).collect(Collectors.toSet()), expected);
    }

    @Test
    void testSynonymsIgnoreCase() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelsForEntityConceptSynonymLabel("ecSYN");
        assertEquals(1, res.size());
        Set<String> expected = new HashSet<>(Collections.singletonList("sm_592574a6-b12d-449b-a18a-01e2e470055f"));
        assertEquals(res.stream().map(Element::getId).collect(Collectors.toSet()), expected);
    }

    @Test
    void testContaining() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_HYPERHYPO_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelByEntityTypeOrEntityConceptContainingLabel("Lbus");
        assertEquals(1, res.size());
        assertEquals("sm_ab7004eb-a939-42e3-948a-0e9c9251aa01", res.iterator().next().getId());
    }

    @Test
    void testEntityTypeContainingQuery() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelByEntityTypeOrEntityConceptContainingLabel("t1_orig");
        assertEquals(1, res.size());
        assertEquals("sm_29d23f08-0965-4120-b3f1-c769e9b74732", res.iterator().next().getId());
    }

    @Test
    void testEntityTypeContainingQuery2() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelByEntityTypeOrEntityConceptContainingLabel("4_orig");
        assertEquals(2, res.size());
        assertEquals(
                new HashSet<>(Arrays.asList("sm_755a7ad5-acd2-4cc6-ab5e-52e6cc41621c", "sm_592574a6-b12d-449b-a18a-01e2e470055f")),
                res.stream().map(Element::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void testEntityTypeContainingLabelQuery() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelByEntityTypeOrEntityConceptContainingLabel("t1"); // finds et1,et10,et11,et12
        assertEquals(2, res.size());
        assertEquals(
                new HashSet<>(Arrays.asList("sm_40160c7e-46fc-4bb9-90e4-b9b0627e61b0", "sm_29d23f08-0965-4120-b3f1-c769e9b74732")),
                res.stream().map(Element::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void testEntityTypeContainingLabelQuery2() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelByEntityTypeOrEntityConceptContainingLabel("4");
        assertEquals(2, res.size());
        assertEquals(
                new HashSet<>(Arrays.asList("sm_755a7ad5-acd2-4cc6-ab5e-52e6cc41621c", "sm_592574a6-b12d-449b-a18a-01e2e470055f")),
                res.stream().map(Element::getId).collect(Collectors.toSet())
        );
    }

    @Test
    void testEntityConceptContainingQuery() {
        graphDBTestDriverFactoryImpl.getGraphDBTestDriver().loadDatabaseDump(DATABASE_SM_QUERY_EXAMPLE);

        Set<SemanticModel> res = testee.searchSemanticModelByEntityTypeOrEntityConceptContainingLabel("c1"); // finds ec1,ec10,ec11
        assertEquals(4, res.size());
        Set<String> expected = new HashSet<>(Arrays.asList(
                "sm_40160c7e-46fc-4bb9-90e4-b9b0627e61b0",
                "sm_d4ea0b47-0ac3-4717-83a0-fffb1988ff56",
                "sm_29d23f08-0965-4120-b3f1-c769e9b74732",
                "sm_592574a6-b12d-449b-a18a-01e2e470055f"
        ));
        assertEquals(expected, res.stream().map(Element::getId).collect(Collectors.toSet()));
    }
}