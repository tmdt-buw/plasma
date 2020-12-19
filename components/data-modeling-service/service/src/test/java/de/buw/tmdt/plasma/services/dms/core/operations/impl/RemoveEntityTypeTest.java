package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoveEntityTypeTest {

	@Mock
	private OperationLookUp lookUpMock;
	@Mock
	private EntityType entityTypeMock;
	@Mock
	private DataSourceSchema dataSourceSchemaMock;

	private RemoveEntityType testee;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		testee = new RemoveEntityType(lookUpMock);
	}

	@Test
	void testInvoke() throws ParameterParsingException {
		UUID uuid = UUID.randomUUID();
		PrimitiveNode node = new PrimitiveNode(
				entityTypeMock,
				DataType.STRING,
				"",
				Collections.emptyList(),
				null,
				Collections.emptyList(),
				uuid
		);

		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(node);
		when(dataSourceSchemaMock.find(node.getIdentity())).thenReturn(node);

		testee.invoke(
				dataSourceSchemaMock,
				new ParameterDefinition<>(
						Type.SYNTAX_NODE_ID,
						"NodeId",
						"",
						"",
						1,
						1,
						true,
						uuid
				)
		);

		assertNull(node.getEntityType());
	}

	@Test
	void wrongNodeId() {
		Assertions.assertThrows(ParameterParsingException.class, () -> {
			Node root = new PrimitiveNode(null, DataType.STRING);

			when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);

			testee.parseParameterDefinition(dataSourceSchemaMock, new ParameterDefinition<>(
					Type.SYNTAX_NODE_ID,
					"NodeId",
					"",
					"",
					1,
					1,
					true
			));
		});
	}

	@Test
	void wrongNodeType() {
		Assertions.assertThrows(ParameterParsingException.class, () -> {
			Node root = new ObjectNode(new HashMap<>(), null);
			UUID uuid = UUID.randomUUID();
			root.setUuid(uuid);

			when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);

			testee.parseParameterDefinition(dataSourceSchemaMock, new ParameterDefinition<>(
					Type.SYNTAX_NODE_ID,
					"NodeId",
					"",
					"",
					1,
					1,
					true,
					uuid
			));
		});
	}

	@Test
	void testRegistry() {
		verify(lookUpMock, times(1)).registerOperation(testee);
	}
}