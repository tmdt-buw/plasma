package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityConcept;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.ObjectNode;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetEntityTypeTest {

	private static final long ENTITY_TYPE_ID = 1L;

	@Mock
	private OperationLookUp lookUpMock;
	@Mock
	private DataSourceSchema dataSourceSchemaMock;

	private EntityType entityType;
	private SetEntityType testee;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);

		entityType = new EntityType(
				null,
				"label",
				"originalLabel",
				"description",
				new EntityConcept(UUID.randomUUID().toString(), "ECName", "ECDescription", "ECSourceURI", null),
				new Position(0, 0),
				ENTITY_TYPE_ID
		);

		testee = new SetEntityType(lookUpMock);
	}

	@Test
	void testInvoke() throws ParameterParsingException {
		PrimitiveNode root = new PrimitiveNode(
				null,
				DataType.UNKNOWN,
				"",
				Collections.emptyList(),
				null,
				Collections.emptyList(),
				UUID.randomUUID()
		);
		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);
		when(dataSourceSchemaMock.find(root.getIdentity())).thenReturn(root);
		when(dataSourceSchemaMock.find(entityType.getIdentity())).thenReturn(entityType);

		assertNull(root.getEntityType());

		testee.invoke(dataSourceSchemaMock, setParameters(root.getUuid(), ENTITY_TYPE_ID));
		assertEquals(entityType, root.getEntityType());
		assertEquals(DataType.UNKNOWN, root.getDataType());
	}

	@Test
	void testInvokeStringDataType() throws ParameterParsingException {
		PrimitiveNode root = new PrimitiveNode(
				null,
				DataType.UNKNOWN,
				"",
				Collections.emptyList(),
				null,
				Arrays.asList("0", "myExampleString"),
				UUID.randomUUID()
		);
		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);
		when(dataSourceSchemaMock.find(root.getIdentity())).thenReturn(root);
		when(dataSourceSchemaMock.find(entityType.getIdentity())).thenReturn(entityType);

		assertNull(root.getEntityType());

		testee.invoke(dataSourceSchemaMock, setParameters(root.getUuid(), ENTITY_TYPE_ID));
		assertEquals(entityType, root.getEntityType());
		assertEquals(DataType.STRING, root.getDataType());
	}

	@Test
	void testInvokeNumberDataType() throws ParameterParsingException {
		PrimitiveNode root = new PrimitiveNode(
				null,
				DataType.UNKNOWN,
				"",
				Collections.emptyList(),
				null,
				Arrays.asList("0", "42", "1", "0.0"),
				UUID.randomUUID()
		);
		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);
		when(dataSourceSchemaMock.find(root.getIdentity())).thenReturn(root);
		when(dataSourceSchemaMock.find(entityType.getIdentity())).thenReturn(entityType);

		assertNull(root.getEntityType());

		testee.invoke(dataSourceSchemaMock, setParameters(root.getUuid(), ENTITY_TYPE_ID));
		assertEquals(entityType, root.getEntityType());
		assertEquals(DataType.NUMBER, root.getDataType());
	}

	@Test
	void testInvokeBooleanDataType() throws ParameterParsingException {
		PrimitiveNode root = new PrimitiveNode(
				null,
				DataType.UNKNOWN,
				"",
				Collections.emptyList(),
				null,
				Arrays.asList("0", "1", "t", "f", "true", "false"),
				UUID.randomUUID()
		);
		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);
		when(dataSourceSchemaMock.find(root.getIdentity())).thenReturn(root);
		when(dataSourceSchemaMock.find(entityType.getIdentity())).thenReturn(entityType);

		assertNull(root.getEntityType());

		testee.invoke(dataSourceSchemaMock, setParameters(root.getUuid(), ENTITY_TYPE_ID));
		assertEquals(entityType, root.getEntityType());
		assertEquals(DataType.BOOLEAN, root.getDataType());
	}

	@Test
	void wrongNodeId() {
		Assertions.assertThrows(ParameterParsingException.class, () -> {
			Node root = new PrimitiveNode(null, DataType.STRING);
			ParameterDefinition parameterDefinition = setParameters(null, ENTITY_TYPE_ID);

			when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);

			testee.parseParameterDefinition(dataSourceSchemaMock, parameterDefinition);
		});
	}

	@Test
	void wrongNodeType() {
		Assertions.assertThrows(ParameterParsingException.class, () -> {
			Node root = new ObjectNode(new HashMap<>(), null, UUID.randomUUID());
			ParameterDefinition parameterDefinition = setParameters(root.getUuid(), ENTITY_TYPE_ID);

			when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);

			testee.parseParameterDefinition(dataSourceSchemaMock, parameterDefinition);
		});
	}

	@Test
	void testRegistry() {
		verify(lookUpMock, times(1)).registerOperation(testee);
	}

	private ParameterDefinition setParameters(UUID uuid, Long entityTypeId) {
		return new ParameterDefinition<>(
				Type.COMPLEX,
				"",
				"",
				"",
				1,
				1,
				new ParameterDefinition<>(
						Type.SYNTAX_NODE_ID,
						SetEntityType.NODE_ID_PARAMETER_NAME,
						"",
						"",
						1,
						1,
						true,
						uuid
				),
				new ParameterDefinition<>(
						Type.ENTITY_TYPE_ID,
						SetEntityType.ENTITY_TYPE_ID_PARAMETER_NAME,
						"",
						"",
						1,
						1,
						entityTypeId
				)
		);
	}
}