package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static de.buw.tmdt.plasma.services.dms.core.operations.Operation.Strings.PRIMITIVE_NODE_IDENTIFIER;

/**
 * This operation removes an EntityType from a node referred by its nodeId. The operation is attached to all
 * PrimitiveNodes.
 */
@Service
public class RemoveEntityType extends Operation<PrimitiveNode> {

	private static final String OPERATION_NAME = "RemoveEntityType";
	private static final String NODE_ID_PARAMETER_NAME = "NodeId";

	private static final ParameterDefinition<String, UUID> PARAMETER_PROTOTYPE =
			new ParameterDefinition<>(
					Type.SYNTAX_NODE_ID,
					NODE_ID_PARAMETER_NAME,
					PRIMITIVE_NODE_IDENTIFIER,
					"The identifier of the primitive node of which the entity type shall be disconnected.",
					1,
					1,
					true
			);

	@Autowired
	protected RemoveEntityType(OperationLookUp operationLookUp) {
		super(
				OPERATION_NAME,
				PARAMETER_PROTOTYPE,
				"Disconnect entity type",
				"Removes the connection of the selected data structure node to it's entity type.",
				operationLookUp
		);
	}

	@Override
	public PrimitiveNode parseParameterDefinition(DataSourceSchema schema, ParameterDefinition<?, ?> parameterDefinition) throws ParameterParsingException {
		final UUID nodeId;
		validateParameterDefinition(parameterDefinition);

		ParameterDefinition<String, UUID> uuidParameterDefinition = Type.SYNTAX_NODE_ID.castParameterDefinition(parameterDefinition);
		nodeId = getValueAs(uuidParameterDefinition, Type.SYNTAX_NODE_ID, 0, false);

		final Traversable node = schema.find(new Traversable.Identity<>(nodeId));
		if (node == null) {
			throw new ParameterParsingException("Did not find node with id `" + nodeId + "` in schema.");
		}
		if (!(node instanceof PrimitiveNode)) {
			throw new ParameterParsingException(
					"Node id identified illegal node type. Expected `" + PrimitiveNode.class + "` but found `" + node.getClass() + "`."
			);
		}

		return (PrimitiveNode) node;
	}

	@Override
	protected DataSourceSchema execute(DataSourceSchema schema, PrimitiveNode input) {
		input.setEntityType(null);
		return schema;
	}

	@Override
	protected Handle getHandleOnNode(Node node) {
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, node.getUuid());
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<Node, Set<Handle>> generateParameterDefinitionsOnGraph(@NotNull Node root) {
		Map<Node, Set<Handle>> result = new HashMap<>();
		root.execute(traversable -> {
			if (traversable instanceof PrimitiveNode) {
				PrimitiveNode primitiveNode = (PrimitiveNode) traversable;
				if (primitiveNode.getEntityType() != null) {
					result.computeIfAbsent(primitiveNode, ignored -> new HashSet<>()).add(getHandleOnNode(primitiveNode));
				}
			}
		});
		return result;
	}
}
