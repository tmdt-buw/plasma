package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.CompositeNode;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.ObjectNode;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members.Splitter;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SplitPrimitiveTest {

	@Mock
	private PrimitiveNode primitiveNodeMock;

	@Mock
	private Traversable.Identity<?> primitiveNodeIdentityMock;

	@Mock
	private ObjectNode rootNodeMock;

	@Mock
	private OperationLookUp operationLookUpMock;

	@Mock
	private DataSourceSchema dataSourceSchemaMock;

	@Captor
	private ArgumentCaptor<CompositeNode> replacementCaptor;

	private SplitPrimitive testee;
	private String[] patterns;
	private List<Splitter> splitters;
	private CompositeNode expectation;
	private ParameterDefinition<?, ?> parameterDefinition;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		UUID uuid = UUID.randomUUID();

		when(primitiveNodeMock.getUuid()).thenReturn(uuid);
		when(primitiveNodeMock.getDataType()).thenReturn(DataType.STRING);
		when(primitiveNodeMock.getExamples()).thenReturn(Arrays.asList("Some;Funny:Value", "SomeOther;NotSoFunny:Shit"));
		Mockito.<Traversable.Identity<?>>when(primitiveNodeMock.getIdentity()).thenReturn(primitiveNodeIdentityMock);

		when(rootNodeMock.find(eq(new Traversable.Identity<>(uuid)))).thenReturn(primitiveNodeMock);
		when(rootNodeMock.replace(any(), any())).thenReturn(rootNodeMock);

		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(rootNodeMock);
		when(dataSourceSchemaMock.find(eq(new Traversable.Identity<>(uuid)))).thenReturn(primitiveNodeMock);
		when(dataSourceSchemaMock.replace(any(), any())).thenReturn(dataSourceSchemaMock);

		testee = new SplitPrimitive(operationLookUpMock);

		patterns = new String[]{";", ":"};
		splitters = Arrays.stream(patterns)
				.map(Splitter::new)
				.collect(Collectors.toList());

		expectation = new CompositeNode(
				Arrays.asList(
						new PrimitiveNode(
								null,
								DataType.UNKNOWN,
								Arrays.asList("Some", "SomeOther")
						), new PrimitiveNode(
								null,
								DataType.UNKNOWN,
								Arrays.asList("Funny", "NotSoFunny")
						), new PrimitiveNode(
								null,
								DataType.UNKNOWN,
								Arrays.asList("Value", "Shit")
						)
				),
				splitters,
				primitiveNodeMock.getExamples(),
				null,
				null
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
						"NodeId",
						"",
						"",
						1,
						1,
						primitiveNodeMock.getUuid()
				),
				new ParameterDefinition<>(
						Type.PATTERN,
						"Splitter",
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
		Pair<PrimitiveNode, List<Splitter>> parsedInput = testee.parseParameterDefinition(dataSourceSchemaMock, parameterDefinition);

		String[] readPatterns = parsedInput.getRight().stream()
				.peek(Splitter::getCompiledPattern)
				.map(Splitter::getPattern)
				.toArray(String[]::new);

		assertArrayEquals(patterns, readPatterns);
		assertEquals(primitiveNodeMock, parsedInput.getLeft());
	}

	@Test
	void apply() {
		DataSourceSchema result = testee.execute(
				dataSourceSchemaMock,
				new Pair<>(primitiveNodeMock, new ArrayList<>(splitters))
		);

		assertEquals(dataSourceSchemaMock, result);
		verify(dataSourceSchemaMock, atLeastOnce()).replace(eq(primitiveNodeIdentityMock), replacementCaptor.capture());
		assertEquals(expectation.toString(), replacementCaptor.getValue().toString());
	}

	@Test
	void concatenate() throws ParameterParsingException {
		DataSourceSchema result = testee.invoke(dataSourceSchemaMock, parameterDefinition);

		assertEquals(dataSourceSchemaMock, result);
		verify(dataSourceSchemaMock, atLeastOnce()).replace(eq(primitiveNodeIdentityMock), replacementCaptor.capture());

		final CompositeNode replacement = replacementCaptor.getValue();

		assertEquals(expectation.getExamples(), replacement.getExamples());
		assertEquals(expectation.getSplitter(), replacement.getSplitter());
		assertEquals(expectation.getCleansingPattern(), replacement.getCleansingPattern());
		assertEquals(expectation.getComponents(), replacement.getComponents());
		assertEquals(expectation.toString(), replacement.toString());
	}
}