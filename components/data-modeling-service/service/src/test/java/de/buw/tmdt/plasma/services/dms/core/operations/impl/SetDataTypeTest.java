package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import de.buw.tmdt.plasma.datamodel.modification.operation.ParameterDefinition;
import de.buw.tmdt.plasma.datamodel.modification.operation.Type;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SetNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static de.buw.tmdt.plasma.services.dms.core.operations.impl.SetDataType.DATA_TYPE_PARAMETER_NAME;
import static de.buw.tmdt.plasma.services.dms.core.operations.impl.SetDataType.NODE_ID_PARAMETER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SetDataTypeTest {

	@Mock
	private OperationLookUp lookUp;

	private CombinedModel combinedModel;

	private SetDataType testee;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);

		testee = new SetDataType(lookUp);
	}

	@Test
	void testInvoke() throws ParameterParsingException {
		SchemaNode node = new PrimitiveNode(
				"label",
				true,
				DataType.String,
				new ArrayList<>(),
				null
		);

		assertEquals(DataType.String, ((PrimitiveNode) node).getDataType());
		SyntaxModel syntaxModel = new SyntaxModel(node.getUuid(), new ArrayList<>(Collections.singletonList(node)), new ArrayList<>());

		combinedModel = new CombinedModel("", syntaxModel, null);

		testee.invoke(combinedModel, setParameters(node.getUuid(), DataType.Boolean));

		node = combinedModel.getSyntaxModel().getNode(node.getUuid());

		assertEquals(DataType.Boolean, ((PrimitiveNode) node).getDataType());
	}

	@Test
	void wrongNodeId() {
		SchemaNode node = new PrimitiveNode("node1", true, DataType.String);

		SyntaxModel syntaxModel = new SyntaxModel(node.getUuid(), new ArrayList<>(Collections.singletonList(node)), new ArrayList<>());

		combinedModel = new CombinedModel("", syntaxModel, null);

		Assertions.assertThrows(ParameterParsingException.class, () -> {
			testee.parseParameterDefinition(combinedModel, setParameters(null, null));
		});

		Assertions.assertThrows(ParameterParsingException.class, () -> {
			testee.parseParameterDefinition(combinedModel, setParameters(UUID.randomUUID().toString(), null));
		});
	}

	@Test
	void wrongNodeType() {
		SchemaNode node = new SetNode(true);

		SyntaxModel syntaxModel = new SyntaxModel(node.getUuid(), new ArrayList<>(Collections.singletonList(node)), new ArrayList<>());
		combinedModel = new CombinedModel("", syntaxModel, null);

		Assertions.assertThrows(ParameterParsingException.class, () -> {
			testee.parseParameterDefinition(combinedModel, setParameters(node.getUuid(), null));
		});
	}

	@Test
	void testRegistry() {
		verify(lookUp, times(1)).registerOperation(testee);
	}

	private ParameterDefinition setParameters(String nodeUuid, DataType dataType) {
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
						nodeUuid
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