package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
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

import static de.buw.tmdt.plasma.services.dms.core.operations.impl.SetDataType.DATA_TYPE_PARAMETER_NAME;
import static de.buw.tmdt.plasma.services.dms.core.operations.impl.SetDataType.NODE_ID_PARAMETER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetDataTypeTest {

	@Mock
	private OperationLookUp lookUp;
	@Mock
	private DataSourceSchema dataSourceSchemaMock;

	private SetDataType testee;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);

		testee = new SetDataType(lookUp);
	}

	@Test
	void testInvoke() throws ParameterParsingException {
		PrimitiveNode node = new PrimitiveNode(
				null,
				DataType.STRING,
				"",
				Collections.emptyList(),
				null,
				Collections.emptyList(),
				UUID.randomUUID()
		);

		assertEquals(DataType.STRING, node.getDataType());

		when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(node);
		when(dataSourceSchemaMock.find(node.getIdentity())).thenReturn(node);

		testee.invoke(dataSourceSchemaMock, setParameters(node.getUuid(), DataType.BOOLEAN));

		assertNull(node.getEntityType());
		assertEquals(DataType.BOOLEAN, node.getDataType());
	}

	@Test
	void wrongNodeId() {
		Assertions.assertThrows(ParameterParsingException.class, () -> {
			Node root = new PrimitiveNode(null, DataType.STRING);

			when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);

			testee.parseParameterDefinition(dataSourceSchemaMock, setParameters(null, null));
		});
	}

	@Test
	void wrongNodeType() {
		Assertions.assertThrows(ParameterParsingException.class, () -> {
			Node root = new ObjectNode(new HashMap<>(), null);
			UUID uuid = UUID.randomUUID();
			root.setUuid(uuid);

			when(dataSourceSchemaMock.getSyntaxModel()).thenReturn(root);

			testee.parseParameterDefinition(dataSourceSchemaMock, setParameters(null, null));
		});
	}

	@Test
	void testRegistry() {
		verify(lookUp, times(1)).registerOperation(testee);
	}

	private ParameterDefinition setParameters(UUID uuid, DataType dataType) {
		return new ParameterDefinition<>(
				Type.COMPLEX,
				"",
				"",
				"",
				1,
				1,
				new ParameterDefinition<>(
						Type.SYNTAX_NODE_ID,
						NODE_ID_PARAMETER_NAME,
						"",
						"",
						1,
						1,
						true,
						uuid
				),
				new ParameterDefinition<>(
						Type.DATA_TYPE,
						DATA_TYPE_PARAMETER_NAME,
						"",
						"",
						1,
						1,
						dataType
				)
		);
	}
}