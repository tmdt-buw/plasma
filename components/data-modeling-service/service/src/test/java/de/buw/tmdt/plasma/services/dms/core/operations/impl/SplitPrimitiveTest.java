package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import de.buw.tmdt.plasma.datamodel.modification.operation.ParameterDefinition;
import de.buw.tmdt.plasma.datamodel.modification.operation.Type;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.*;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SplitPrimitiveTest {

	private PrimitiveNode primitiveNode;

	@Mock
	private OperationLookUp operationLookUpMock;

	private CombinedModel combinedModel;

	private SplitPrimitive testee;
	private String[] patterns;
	private List<Splitting> splitters;
	private List<PrimitiveNode> expectedPrimitiveNodes;
	private ParameterDefinition<?, ?> parameterDefinition;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);

		primitiveNode = new PrimitiveNode("primitive", DataType.String);
		primitiveNode.setExamples(Arrays.asList("Some;Funny:Value", "SomeOther;NotSoFunny:Stuff"));

		SyntaxModel syntaxModel = new SyntaxModel(primitiveNode.getUuid(), new ArrayList<>(Collections.singletonList(primitiveNode)), new ArrayList<>());

		combinedModel = new CombinedModel("ignored", syntaxModel, null);

		testee = new SplitPrimitive(operationLookUpMock);

		patterns = new String[]{";", ":"};
		splitters = Arrays.stream(patterns)
				.map(Splitting::new)
				.collect(Collectors.toList());

		expectedPrimitiveNodes = Arrays.asList(
				new PrimitiveNode(
						"node1",
						true,
						DataType.Unknown,
						Arrays.asList("Some", "SomeOther"),
						null),
				new PrimitiveNode(
						"node2",
						true,
						DataType.Unknown,
						Arrays.asList("Funny", "NotSoFunny"),
						null),

				new PrimitiveNode(
						"node3",
						true,
						DataType.Unknown,
						Arrays.asList("Value", "Stuff"),
						null)
		);

		parameterDefinition = new ParameterDefinition<>(
				Type.COMPLEX,
				"",
				"",
				"",
				1,
				1,
				new ParameterDefinition<>(
						Type.SYNTAX_NODE_ID,
						SplitPrimitive.NODE_ID_PARAMETER_NAME,
						"",
						"",
						1,
						1,
						primitiveNode.getUuid()
				),
				new ParameterDefinition<>(
						Type.PATTERN,
						SplitPrimitive.SPLITTER_PARAMETER_NAME,
						"",
						"",
						1,
						Integer.MAX_VALUE,
						patterns
				)
		);
	}

	@Test
	void parseRaw() throws ParameterParsingException {
		Pair<PrimitiveNode, List<Splitting>> parsedInput = testee.parseParameterDefinition(combinedModel, parameterDefinition);

		String[] readPatterns = parsedInput.getRight().stream()
				.map(Splitting::getPattern)
				.toArray(String[]::new);

		assertArrayEquals(patterns, readPatterns);
		assertEquals(primitiveNode, parsedInput.getLeft());
	}

	@Test
	void apply() {

		combinedModel = testee.execute(
				this.combinedModel,
				new Pair<>(primitiveNode, new ArrayList<>(splitters))
		);

        assertEquals(4, combinedModel.getSyntaxModel().getNodes().size());
        assertEquals(3, combinedModel.getSyntaxModel().getEdges().size());

        SchemaNode replacementCompositeNode = combinedModel.getSyntaxModel().getNodes().stream().filter(schemaNode -> schemaNode.getUuid().equals(primitiveNode.getUuid())).findFirst().orElseThrow();

        assertTrue(replacementCompositeNode instanceof CompositeNode, "New node has wrong type");
        assertEquals(2, ((CompositeNode) replacementCompositeNode).getSplitter().size(), "Splitting is missing in new node");
        assertEquals(primitiveNode.getCleansingPattern(), ((CompositeNode) replacementCompositeNode).getCleansingPattern(), "Cleansing patterns do not match");

        // get outgoing edges
        List<Edge> newEdges = combinedModel.getSyntaxModel().getEdges().stream().filter(edge -> edge.getFromId().equals(replacementCompositeNode.getUuid())).collect(Collectors.toList());

        // get the generated nodes
        List<String> generatedNodesUuids = newEdges.stream().map(Edge::getToId).collect(Collectors.toList());
        List<PrimitiveNode> generatedNodes = combinedModel.getSyntaxModel().getNodes().stream()
                .filter(schemaNode -> generatedNodesUuids.contains(schemaNode.getUuid()))
                .map(schemaNode -> (PrimitiveNode) schemaNode)
                .sorted(Comparator.comparing(CombinedModelElement::getLabel)).collect(Collectors.toList());

        assertEquals(3, generatedNodes.size());

        // validate example values of generated nodes
        assertIterableEquals(expectedPrimitiveNodes.get(0).getExamples(), generatedNodes.get(0).getExamples(), "First generated nodes example values do not match");
        assertIterableEquals(expectedPrimitiveNodes.get(1).getExamples(), generatedNodes.get(1).getExamples(), "Second generated nodes example values do not match");
		assertIterableEquals(expectedPrimitiveNodes.get(2).getExamples(), generatedNodes.get(2).getExamples(), "Third generated nodes example values do not match");
	}
}