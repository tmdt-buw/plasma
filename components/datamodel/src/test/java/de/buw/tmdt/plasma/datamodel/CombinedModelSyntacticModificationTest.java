package de.buw.tmdt.plasma.datamodel;

import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.semanticmodel.MappableSemanticModelNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.*;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static de.buw.tmdt.plasma.datamodel.CombinedModelGenerator.getCombinedModel;
import static org.junit.jupiter.api.Assertions.*;

public class CombinedModelSyntacticModificationTest {

    @Test
    public void testSplitPrimitiveNode() {
        CombinedModel model = getCombinedModel();

        SchemaNode targetNode = model.getSyntaxModel().getNodes().stream().filter(schemaNode -> schemaNode.getLabel().equals("ident")).findFirst().orElseThrow();
        assertTrue(targetNode instanceof PrimitiveNode);
        PrimitiveNode identNode = (PrimitiveNode) targetNode;
        Splitting splitting1 = new Splitting("-");

        MappableSemanticModelNode mappedSemanticNode = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof MappableSemanticModelNode)
                .map(node -> (MappableSemanticModelNode) node)
                .filter(MappableSemanticModelNode::isMapped)
                .filter(node -> node.getMappedSyntaxNodeUuid().equals(identNode.getUuid()))
                .findFirst().orElseThrow();

        // by just defining the split, needed new nodes should be generated automatically
        CompositeNode compositeNode = new CompositeNode(identNode.getUuid(), "", null, identNode.getXCoordinate(), identNode.getYCoordinate(), true, new ArrayList<>(), identNode.getCleansingPattern(), Arrays.asList(splitting1));

        DeltaModification modification = new DeltaModification("mod_testSplitPrimitiveNode", null, null, Collections.singletonList(compositeNode), null, 0.0);
        model.apply(modification);

        // assert that the node has been properly replaced
        SchemaNode replacementCompositeNode = model.getSyntaxModel().getNodes().stream().filter(schemaNode -> schemaNode.getUuid().equals(targetNode.getUuid())).findFirst().orElseThrow();

        assertTrue(replacementCompositeNode instanceof CompositeNode, "New node has wrong type");
        assertEquals(1, ((CompositeNode) replacementCompositeNode).getSplitter().size(), "Splitting is missing in new node");
        assertEquals(((PrimitiveNode) targetNode).getCleansingPattern(), ((CompositeNode) replacementCompositeNode).getCleansingPattern(), "Cleansing patterns do not match");
        assertIterableEquals(((CompositeNode) replacementCompositeNode).getExamples(), ((PrimitiveNode) targetNode).getExamples(), "Example values do not match");
        assertEquals(replacementCompositeNode.getXCoordinate(), targetNode.getXCoordinate(), "X coordinate does not match");
        assertEquals(replacementCompositeNode.getYCoordinate(), targetNode.getYCoordinate(), "Y coordinate does not match");

        // assure that no entity types have been deleted
        assertEquals(getCombinedModel().getSemanticModel().getNodes().size(), model.getSemanticModel().getNodes().size(), "Number of expected entities does not match");

        int expectedNewEdgesCount = compositeNode.getSplitter().size() + 1;
        int expectedNewNodesCount = compositeNode.getSplitter().size() + 1;
        // assert that two more nodes and two more edges have been created in the syntactic model
        assertEquals(getCombinedModel().getSyntaxModel().getNodes().size() + expectedNewNodesCount, model.getSyntaxModel().getNodes().size(), "Number of expected nodes does not match");
        assertEquals(getCombinedModel().getSyntaxModel().getEdges().size() + expectedNewEdgesCount, model.getSyntaxModel().getEdges().size(), "Number of expected edges does not match");


        // assert that the originally assigned node is disconnected now, as the node is no longer a PrimitiveNode
        MappableSemanticModelNode identifierType = model.getSemanticModel().getNodes().stream()
                .filter(node -> node instanceof MappableSemanticModelNode)
                .map(node -> (MappableSemanticModelNode) node)
                .filter(node -> node.getUuid().equals(mappedSemanticNode.getUuid())).findFirst().orElseThrow();
        assertNull(identifierType.getMappedSyntaxNodeUuid());
        assertNull(identifierType.getMappedSyntaxNodeLabel());
        assertNull(identifierType.getMappedSyntaxNodePath());

        // also assure that the explicit mapping has been removed
        assertTrue(model.generateModelMappings().stream().noneMatch(primitiveEntityTypeMapping -> primitiveEntityTypeMapping.getPrimitiveUUID().equals(identNode.getUuid())), "Explicit mapping to old node has not been removed");

        // get outgoing edges
        List<Edge> newEdges = model.getSyntaxModel().getEdges().stream().filter(edge -> edge.getFromId().equals(compositeNode.getUuid())).collect(Collectors.toList());

        assertEquals(expectedNewEdgesCount, newEdges.size(), "Missing created edges or edges not properly instantiated");

        // check that annotations are added to edges
        List<Pair<String, List<String>>> tokenToStrings = new ArrayList<>();
        tokenToStrings.add(Pair.of(splitting1.getPattern(), Arrays.asList("10", "20", "test")));
        tokenToStrings.add(Pair.of(null, Arrays.asList("11;suffix1", "21;suffix2", "test;suffix3")));

        for (Edge newEdge : newEdges) {
            // find the matching pattern in the list
            Pair<String, List<String>> currentPair = tokenToStrings.stream().filter(pair -> Objects.equals(newEdge.getAnnotation(), pair.getLeft()))
                    .findFirst().orElseThrow();
            PrimitiveNode generatedNode = model.getSyntaxModel().getNodes().stream()
                    .filter(schemaNode -> newEdge.getToId().equals(schemaNode.getUuid()))
                    .map(schemaNode -> (PrimitiveNode) schemaNode)
                    .findFirst().orElseThrow();
            // validate example values of generated node
            assertIterableEquals(currentPair.getRight(), generatedNode.getExamples(), "Example values do not match");
        }
    }

    @Test
    public void testReSplitCompositeNode() {
        CombinedModel model = getCombinedModel();

        SchemaNode targetNode = model.getSyntaxModel().getNodes().stream().filter(schemaNode -> schemaNode.getLabel().equals("ident")).findFirst().orElseThrow();
        assertTrue(targetNode instanceof PrimitiveNode);

        PrimitiveNode identNode = (PrimitiveNode) targetNode;
        Splitting splitting1 = new Splitting("-");

        CompositeNode compositeNode = new CompositeNode(identNode.getUuid(), "", null, identNode.getXCoordinate(), identNode.getYCoordinate(), true, new ArrayList<>(), identNode.getCleansingPattern(), new ArrayList<>());
        compositeNode.getSplitter().add(splitting1);

        DeltaModification modification = new DeltaModification("mod_testSplitPrimitiveNode", null, null, Collections.singletonList(compositeNode), null, 0.0);
        model.apply(modification);

        // we assume the split went correctly, now re-split the node on a copy (use the old modified instance for reference)
        CombinedModel modifiedModel = model.copy();

        Splitting splitting2 = new Splitting(";");
        compositeNode.getSplitter().add(splitting2);

        modifiedModel.apply(modification);

        // assert that the node has been properly replaced
        SchemaNode replacementCompositeNode = modifiedModel.getSyntaxModel().getNodes().stream().filter(schemaNode -> schemaNode.getUuid().equals(targetNode.getUuid())).findFirst().orElseThrow();

        assertTrue(replacementCompositeNode instanceof CompositeNode, "New node has wrong type");
        assertEquals(2, ((CompositeNode) replacementCompositeNode).getSplitter().size(), "Splitting is missing in new node");
        assertEquals(((PrimitiveNode) targetNode).getCleansingPattern(), ((CompositeNode) replacementCompositeNode).getCleansingPattern(), "Cleansing patterns do not match");
        assertIterableEquals(((CompositeNode) replacementCompositeNode).getExamples(), ((PrimitiveNode) targetNode).getExamples(), "Example values do not match");
        assertEquals(replacementCompositeNode.getXCoordinate(), targetNode.getXCoordinate(), "X coordinate does not match");
        assertEquals(replacementCompositeNode.getYCoordinate(), targetNode.getYCoordinate(), "Y coordinate does not match");

        // assure that no entity types have been deleted
        assertEquals(model.getSemanticModel().getNodes().size(), modifiedModel.getSemanticModel().getNodes().size(), "Number of expected entities does not match");

        int expectedNewEdgesCount = 1;
        int expectedNewNodesCount = 1;

        // assert that two more nodes and two more edges have been created in the syntactic model
        assertEquals(model.getSyntaxModel().getNodes().size() + expectedNewNodesCount, modifiedModel.getSyntaxModel().getNodes().size(), "Number of expected nodes does not match");
        assertEquals(model.getSyntaxModel().getEdges().size() + expectedNewEdgesCount, modifiedModel.getSyntaxModel().getEdges().size(), "Number of expected edges does not match");

        // get outgoing edges
        List<Edge> newEdges = modifiedModel.getSyntaxModel().getEdges().stream().filter(edge -> edge.getFromId().equals(compositeNode.getUuid())).collect(Collectors.toList());
        List<Edge> oldEdges = model.getSyntaxModel().getEdges().stream().filter(edge -> edge.getFromId().equals(compositeNode.getUuid())).collect(Collectors.toList());

        assertEquals(oldEdges.size() + expectedNewEdgesCount, newEdges.size(), "Missing created edges or edges not properly instantiated");

        // check that annotations are added to edges
        List<Pair<String, List<String>>> tokenToStrings = new ArrayList<>();
        tokenToStrings.add(Pair.of(splitting1.getPattern(), Arrays.asList("10", "20", "test")));
        tokenToStrings.add(Pair.of(splitting2.getPattern(), Arrays.asList("11", "21", "test")));
        tokenToStrings.add(Pair.of(null, Arrays.asList("suffix1", "suffix2", "suffix3")));

        for (Edge newEdge : newEdges) {
            // find the matching pattern in the list
            Pair<String, List<String>> currentPair = tokenToStrings.stream().filter(pair -> Objects.equals(newEdge.getAnnotation(), pair.getLeft()))
                    .findFirst().orElseThrow();
            PrimitiveNode generatedNode = modifiedModel.getSyntaxModel().getNodes().stream()
                    .filter(schemaNode -> newEdge.getToId().equals(schemaNode.getUuid()))
                    .map(schemaNode -> (PrimitiveNode) schemaNode)
                    .findFirst().orElseThrow();
            // validate example values of generated node
            assertIterableEquals(currentPair.getRight(), generatedNode.getExamples(), "Example values do not match");
        }
    }
}