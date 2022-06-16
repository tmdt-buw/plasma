package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import de.buw.tmdt.plasma.datamodel.modification.operation.ParameterDefinition;
import de.buw.tmdt.plasma.datamodel.modification.operation.Type;
import de.buw.tmdt.plasma.datamodel.modification.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static de.buw.tmdt.plasma.services.dms.core.operations.Operation.Strings.PRIMITIVE_NODE_IDENTIFIER;

/**
 * Operation to set the DataType of a PrimitiveNode. So this operation is attached to all PrimitiveNodes. As an input it
 * expects the NodeId of the node the DataType is to be added and of course the DataType itself.
 */
@Service
public class SetDataType extends Operation<Pair<PrimitiveNode, DataType>> {

	private static final String OPERATION_NAME = "SetDataType";
	static final String NODE_ID_PARAMETER_NAME = "NodeId";
	static final String DATA_TYPE_PARAMETER_NAME = "DataType";

	private static final ParameterDefinition<TypeDefinitionDTO, ParameterDefinition> PARAMETER_PROTOTYPE =
			new ParameterDefinition<>(
					Type.COMPLEX,
					"",
					"",
					"",
					1,
					1,
					new ParameterDefinition<>(
							Type.SYNTAX_NODE_ID,
							NODE_ID_PARAMETER_NAME,
							PRIMITIVE_NODE_IDENTIFIER,
							"The id of the primitive node of which the data type shall be defined.",
							1,
							1,
							true
					),
					new ParameterDefinition<>(
							Type.DATA_TYPE,
							DATA_TYPE_PARAMETER_NAME,
							"Data type",
							"The data type to set for the selected primitive node.",
							1,
							1
					)

			);

	@Autowired
	SetDataType(OperationLookUp operationLookUp) {
		super(
				OPERATION_NAME,
				PARAMETER_PROTOTYPE,
				"Set data type",
				"Sets the data type of the selected primitive node.",
				operationLookUp
		);
	}

	@Override
	public Pair<PrimitiveNode, DataType> parseParameterDefinition(
			CombinedModel model, ParameterDefinition<?, ?> parameterDefinition
	) throws ParameterParsingException {
		final String nodeId;
		final DataType dataType;

		validateParameterDefinition(parameterDefinition);

		ParameterDefinition<?, ParameterDefinition> parameter = Type.COMPLEX.castParameterDefinition(parameterDefinition);

		//validate node id child element
		ParameterDefinition<?, ?> uuidParameterDefinition = super.findChildParameterDefinition(parameter, NODE_ID_PARAMETER_NAME);
		if (uuidParameterDefinition == null) {
			throw new ParameterParsingException("uuidParameterDefinition is missing");
		}
		nodeId = getValueAs(uuidParameterDefinition, Type.SYNTAX_NODE_ID, 0, false);

		//validate data Type element
		ParameterDefinition<?, ?> dataTypeDefinition = super.findChildParameterDefinition(parameter, DATA_TYPE_PARAMETER_NAME);
		if (dataTypeDefinition == null) {
			throw new ParameterParsingException("dataTypeDefinition is missing");
		}
		dataType = getValueAs(dataTypeDefinition, Type.DATA_TYPE, 0, false);

		SchemaNode node = model.getSyntaxModel().getNode(nodeId);
		if (node == null) {
			throw new ParameterParsingException("Did not find node with id `" + nodeId + "` in model.");
		}
		if (!(node instanceof PrimitiveNode)) {
			throw new ParameterParsingException("Node id identified illegal node type. Expected `" + PrimitiveNode.class + "` but found `" + node.getClass() +
					"`.");
		}
		return new Pair<>((PrimitiveNode) node, dataType);
	}

	@Override
	protected CombinedModel execute(CombinedModel model, Pair<PrimitiveNode, DataType> input) {
        PrimitiveNode primitiveNode = input.getLeft();
        DataType dataType = input.getRight();
        primitiveNode.setDataType(dataType);
        DeltaModification modification = new DeltaModification("local_operation", null, null, List.of(primitiveNode), null);
        model.apply(modification);
        return model;
    }

	@Override
	protected Handle getHandleOnNode(SchemaNode node) {
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, node.getUuid());
		parameterDefinitionClone.replaceValue(DATA_TYPE_PARAMETER_NAME, ((PrimitiveNode) node).getDataType());
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<SchemaNode, Set<Handle>> generateHandlesForApplicableNodes(@NotNull CombinedModel model) {
		Map<SchemaNode, Set<Handle>> result = new HashMap<>();

		// get all primitive nodes
		model.getSyntaxModel().getNodes().stream()
				.filter(schemaNode -> schemaNode instanceof PrimitiveNode)
				.forEach(schemaNode -> result.computeIfAbsent(schemaNode, ignored -> new HashSet<>()).add(getHandleOnNode(schemaNode)));
		return result;
	}
}
