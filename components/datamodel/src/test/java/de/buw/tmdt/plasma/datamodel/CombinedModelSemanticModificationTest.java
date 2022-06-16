package de.buw.tmdt.plasma.datamodel;

import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.*;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static de.buw.tmdt.plasma.datamodel.CombinedModelGenerator.generateResourceURI;
import static de.buw.tmdt.plasma.datamodel.CombinedModelGenerator.getCombinedModel;
import static org.junit.jupiter.api.Assertions.*;

public class CombinedModelSemanticModificationTest {

    @Test
    public void testApplyFailsIfFinalized() {
        CombinedModel model = getCombinedModel();
        model.finalizeModel();

        Class companyClass = new Class(generateResourceURI("company"), "company", "A company");

        DeltaModification modification = new DeltaModification("mod_testApplyAddOneClass", Collections.singletonList(companyClass), null, null, null, 0.0);

        assertThrows(UnsupportedOperationException.class, () -> model.apply(modification));
    }

    @Test
    public void testApplyAddOneEntity() {
        CombinedModel model = getCombinedModel();

        Class companyClass = new Class(generateResourceURI("company"), "company", "A company");

        DeltaModification modification = new DeltaModification("mod_testApplyAddOneClass", Collections.singletonList(companyClass), null, null, null, 0.0);

        model.apply(modification);

        assertEquals(getCombinedModel().getSemanticModel().getNodes().size() + 1, model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");
        assertTrue(model.getSemanticModel().getNodes().contains(companyClass), "New entity type is not available");
        assertEquals(getCombinedModel().getSemanticModel().getEdges().size(), model.getSemanticModel().getEdges().size(), "Number of expected relations does not match");
    }

    @Test
    public void testApplyAddOneEntityAndOneRelation() {
        CombinedModel model = getCombinedModel();

        Class airlineClass = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(clazz -> clazz.getURI().equals(generateResourceURI("airline")))
                .findFirst()
                .orElseThrow();

        Class companyClass = new Class(generateResourceURI("company"), "company", "A company");

        Relation airlineCompanyRelation = new ObjectProperty(airlineClass.getUuid(), companyClass.getUuid(), generateResourceURI("isA"));

        DeltaModification modification = new DeltaModification("mod_testApplyAddOneEntityAndOneRelation", Collections.singletonList(companyClass), Collections.singletonList(airlineCompanyRelation), null, null, 0.0);

        model.apply(modification);

        assertEquals(getCombinedModel().getSemanticModel().getNodes().size() + 1, model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");
        assertTrue(model.getSemanticModel().getNodes().contains(companyClass), "New entity type is not available");
        assertEquals(getCombinedModel().getSemanticModel().getEdges().size() + 1, model.getSemanticModel().getEdges().size(), "Number of expected relations does not match");
        assertTrue(model.getSemanticModel().getEdges().contains(airlineCompanyRelation), "New relation is not available");
    }

    @Test
    public void testApplyReplaceOneNode() {
        CombinedModel model = getCombinedModel();

        Class iataCodeClass = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(clazz -> clazz.getURI().equals(generateResourceURI("iata_code")))
                .findFirst()
                .orElseThrow();

        Instance iataCodeInstance = iataCodeClass.getInstance();

        assertNotNull(iataCodeInstance);

        Class codeClass = new Class(iataCodeClass.getUuid(),
                generateResourceURI("code"),
                "code",
                "A sort of identifier or short version of text",
                iataCodeClass.getXCoordinate(),
                iataCodeClass.getYCoordinate(),
                iataCodeClass.getMappedSyntaxNodeUuid(),
                iataCodeClass.getMappedSyntaxNodeLabel(),
                iataCodeClass.getMappedSyntaxNodePath(),
                null,
                iataCodeClass.isProvisional()
        );
        Instance codeInstance = new Instance(
                null,
                null,
                "code",
                "The code of this airline"
        );
        codeClass.setInstance(codeInstance);

        DeltaModification modification = new DeltaModification("mod_testApplyReplaceOneEntity", Collections.singletonList(codeClass), null, null, null, 0.0);

        model.apply(modification);

        assertEquals(getCombinedModel().getSemanticModel().getNodes().size(), model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");

        Class replacedNode = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(clazz -> Objects.equals(clazz.getUuid(), codeClass.getUuid()))
                .findFirst()
                .orElseThrow();

        assertEquals(replacedNode.getUuid(), iataCodeClass.getUuid(), "Replacement entity does not have the same id as replaced entity");
        assertEquals(codeClass.getURI(), replacedNode.getURI(), "New class is not assigned");
        assertEquals(codeClass.getDescription(), replacedNode.getDescription(), "New description is not applied correctly");
        assertNotNull(replacedNode.getInstance());
        Instance replacedInstance = replacedNode.getInstance();
        assertEquals(replacedInstance.getDescription(), codeInstance.getDescription(), "New instance description is not applied correctly");
        assertEquals(replacedInstance.getLabel(), codeInstance.getLabel(), "New instance label is not applied correctly");

        assertEquals(getCombinedModel().getSemanticModel().getEdges().size(), model.getSemanticModel().getEdges().size(), "Number of expected relations does not match");
    }

    @Test
    public void testApplyMapOneEntityToPrimitiveNode() {
        CombinedModel model = getCombinedModel();

        // get the target node we want to map to
        SchemaNode timeNode = model.getSyntaxModel().getNodes().stream().filter(schemaNode -> schemaNode.getLabel().equals("time")).findFirst().orElseThrow();

        // check that the node is not already mapped for some reason
        assertTrue(timeNode instanceof PrimitiveNode);
        assertTrue(model.generateModelMappings().stream().noneMatch(mapping -> mapping.getPrimitiveUUID().equals(timeNode.getUuid())));
        assertTrue(model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof MappableSemanticModelNode)
                .map(node -> (MappableSemanticModelNode) node)
                .filter(MappableSemanticModelNode::isMapped)
                .noneMatch(node -> Objects.equals(node.getMappedSyntaxNodeUuid(), timeNode.getUuid())));

        // create the new class to assign
        Class timeClass = new Class(generateResourceURI("time"), "time", "A time");
        timeClass.setMappedSyntaxNodeUuid(timeNode.getUuid());
        timeClass.setMappedSyntaxNodeLabel(timeNode.getLabel());
        timeClass.setMappedSyntaxNodePath(String.join("->", timeNode.getPath()));

        DeltaModification modification = new DeltaModification("mod_testApplyMapOneClassToPrimitiveNode", Collections.singletonList(timeClass), null, null, null, 0.0);

        model.apply(modification);

        // check numbers
        assertEquals(getCombinedModel().getSemanticModel().getNodes().size() + 1, model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");
        assertEquals(getCombinedModel().getSemanticModel().getEdges().size(), model.getSemanticModel().getEdges().size(), "Number of expected relations does not match");

        // check if the class has been added
        MappableSemanticModelNode mappedNode = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof MappableSemanticModelNode)
                .map(node -> (MappableSemanticModelNode) node)
                .filter(MappableSemanticModelNode::isMapped)
                .filter(node -> Objects.equals(node.getUuid(), timeClass.getUuid()))
                .findFirst().orElseThrow();
        assertEquals(timeClass.getUuid(), mappedNode.getUuid(), "Added class does not have the same id as added class");
        assertEquals(timeNode.getLabel(), mappedNode.getMappedSyntaxNodeLabel(), "New node's original label is not set properly");
        assertEquals(timeNode.getUuid(), mappedNode.getMappedSyntaxNodeUuid(), "New node's mapping uuid is not set properly");

        // ensure that the explicit mapping from class and instance to syntax has been created
        assertTrue(model.generateModelMappings().stream().anyMatch(mapping -> mapping.getNodeUUID().equals(mappedNode.getUuid())));
    }

    @Test
    public void testApplyMapOneEntityToNonPrimitiveNode() {
        CombinedModel model = getCombinedModel();

        // get the target node we want to map to
        SchemaNode schemaNode = model.getSyntaxModel().getNodes().stream()
                .filter(node -> !(node instanceof PrimitiveNode))
                .findFirst()
                .orElseThrow();

        // check that the node is a valid candidate
        assertFalse(schemaNode instanceof PrimitiveNode);
        assertTrue(model.generateModelMappings().stream().noneMatch(mapping -> mapping.getPrimitiveUUID().equals(schemaNode.getUuid())));
        assertTrue(model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof MappableSemanticModelNode)
                .map(node -> (MappableSemanticModelNode) node)
                .filter(MappableSemanticModelNode::isMapped)
                .noneMatch(node -> Objects.equals(node.getMappedSyntaxNodeUuid(), schemaNode.getUuid())));

        // create the new type and concept
        Class timeClass = new Class(generateResourceURI("time"), "time", "A time");
        timeClass.setMappedSyntaxNodeUuid(schemaNode.getUuid());
        timeClass.setMappedSyntaxNodeLabel(schemaNode.getLabel());

        DeltaModification modification = new DeltaModification("mod_testApplyMapOneEntityToNonPrimitiveNode", Collections.singletonList(timeClass), null, null, null, 0.0);

        assertThrows(CombinedModelIntegrityException.class, () -> model.apply(modification), "Error message not thrown when mapping to non-primitive node " + schemaNode.getUuid());
    }

    @Test
    public void testApplyUnmapOneEntityFromSyntaxNode() {
        CombinedModel model = getCombinedModel();

        Class iataCodeClass = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(instance -> instance.getURI().equals(generateResourceURI("iata_code")))
                .findFirst().orElseThrow();

        Class replacementClass = iataCodeClass.copy();
        assertNotNull(replacementClass.getInstance());
        replacementClass.unmap();

        DeltaModification modification = new DeltaModification("mod_testApplyUnmapOneEntityFromSyntaxNode", Collections.singletonList(replacementClass), null, null, null, 0.0);

        model.apply(modification);

        assertEquals(getCombinedModel().getSemanticModel().getNodes().size(), model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");

        Class retrievedReplacementClass = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(clazz -> Objects.equals(clazz.getUuid(), replacementClass.getUuid()))
                .findFirst()
                .orElseThrow();
        assertEquals(retrievedReplacementClass.getUuid(), retrievedReplacementClass.getUuid(), "Replacement entity does not have the same id as replaced entity");

        assertNull(retrievedReplacementClass.getMappedSyntaxNodeLabel(), "New node's original label is not unset");
        assertNull(retrievedReplacementClass.getMappedSyntaxNodeUuid(), "New node's mapping uuid is not unset");

        assertEquals(getCombinedModel().getSemanticModel().getEdges().size(), model.getSemanticModel().getEdges().size(), "Number of expected relations does not match");

        // ensure that the explicit mapping from entity to syntax node does not get generated
        assertTrue(model.generateModelMappings().stream()
                .noneMatch(mapping -> mapping.getNodeUUID().equals(retrievedReplacementClass.getUuid())));
    }

    @Test
    public void testApplyRemoveEntityAndRelation() {
        CombinedModel model = getCombinedModel();

        Class airlineClass = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(clazz -> clazz.getURI().equals(generateResourceURI("airline")))
                .findFirst()
                .orElseThrow();

        Class nameClass = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof Class)
                .map(node -> (Class) node)
                .filter(instance -> instance.getURI().equals(generateResourceURI("name")))
                .findFirst()
                .orElseThrow();

        Relation airlineNameRelation = model.getSemanticModel().getEdges().stream()
                .filter(relation -> relation.getFrom().equals(airlineClass.getUuid())
                        && relation.getTo().equals(nameClass.getUuid()))
                .findFirst()
                .orElseThrow();

        DeltaModification modification = new DeltaModification("mod_testApplyRemoveOneEntityAndOneRelation",
                List.of(airlineClass, nameClass),
                Collections.singletonList(airlineNameRelation),
                null,
                null,
                0.0);
        modification.setDeletion(true);

        model.apply(modification);

        assertEquals(getCombinedModel().getSemanticModel().getNodes().size() - 2, model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");
        assertFalse(model.getSemanticModel().getNodes().contains(nameClass), "Deleted node is still available");
        assertFalse(model.getSemanticModel().getNodes().contains(airlineClass), "Deleted node is still available");
        assertFalse(model.getSemanticModel().getEdges().contains(airlineNameRelation), "Deleted relation is still available");
        // check that connecting edges are also deleted
        assertEquals(getCombinedModel().getSemanticModel().getEdges().size() - 3, model.getSemanticModel().getEdges().size(), "Number of expected relations does not match");

        // ensure that the mapping from name to syntax has been deleted
        assertTrue(model.generateModelMappings().stream().noneMatch(mapping -> mapping.getNodeUUID().equals(nameClass.getUuid())));
    }


}