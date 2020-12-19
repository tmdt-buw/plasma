package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.core.operations.impl.misc.BooleanHomogenizer;
import de.buw.tmdt.plasma.services.dms.core.operations.impl.misc.NumberHomogenizer;
import de.buw.tmdt.plasma.services.dms.core.operations.impl.misc.SLTDataTypeException;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

/**
 * This operation is setting the EntityType for a denoted PrimitiveNode, referred to by its NodeId. The operation is
 * attached to all PrimitiveNodes.
 */
@Service
public class SetEntityType extends Operation<Pair<PrimitiveNode, EntityType>> {

	private static final String OPERATION_NAME = "ConnectEntityType";
	static final String NODE_ID_PARAMETER_NAME = "NodeId";
	static final String ENTITY_TYPE_ID_PARAMETER_NAME = "EntityTypeId";

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
							Strings.PRIMITIVE_NODE_IDENTIFIER,
							"The identifier of the primitive node to which the entity type shall be connected.",
							1,
							1,
							true
					),
					new ParameterDefinition<>(
							Type.ENTITY_TYPE_ID,
							ENTITY_TYPE_ID_PARAMETER_NAME,
							Strings.ENTITY_TYPE_IDENTIFIER,
							"The identifier of the entity type which shall be connected.",
							1,
							1
					)

			);

	@Autowired
	SetEntityType(OperationLookUp operationLookUp) {
		super(
				OPERATION_NAME,
				PARAMETER_PROTOTYPE,
				"Connect entity type",
				"Connects the selected data structure node to a entity type.",
				operationLookUp
		);
	}

	@Override
	public Pair<PrimitiveNode, EntityType> parseParameterDefinition(
			DataSourceSchema schema, ParameterDefinition<?, ?> parameterDefinition
	) throws ParameterParsingException {
		final UUID nodeId;
		final EntityType entityType;

		validateParameterDefinition(parameterDefinition);

		ParameterDefinition<?, ParameterDefinition> parameter = Type.COMPLEX.castParameterDefinition(parameterDefinition);

		//validate node id child element
		ParameterDefinition<?, ?> uuidParameterDefinition = super.findChildParameterDefinition(parameter, NODE_ID_PARAMETER_NAME);
		if (uuidParameterDefinition == null) {
			throw new ParameterParsingException("uuidParameterDefinition is missing");
		}
		nodeId = getValueAs(uuidParameterDefinition, Type.SYNTAX_NODE_ID, 0, false);

		//validate entityTypeId element
		ParameterDefinition<?, ?> entityTypeIdDefinition = super.findChildParameterDefinition(parameter, ENTITY_TYPE_ID_PARAMETER_NAME);
		if (entityTypeIdDefinition == null) {
			throw new ParameterParsingException("entityTypeIdDefinition is missing");
		}
		final Long entityTypeId = getValueAs(entityTypeIdDefinition, Type.ENTITY_TYPE_ID, 0, false);
		final Traversable entityTypeTraversable = schema.find(new Traversable.Identity<>(entityTypeId));
		entityType = ObjectUtilities.checkedReturn(entityTypeTraversable, EntityType.class);

		final Traversable node = schema.find(new Traversable.Identity<>(nodeId));
		if (node == null) {
			throw new ParameterParsingException("Did not find node with id `" + nodeId + "` in schema.");
		}
		if (!(node instanceof PrimitiveNode)) {
			throw new ParameterParsingException(
					"Node id identified illegal node type. Expected `"
					+ PrimitiveNode.class + "` but found `"
					+ node.getClass() + "`."
			);
		}

		return new Pair<>((PrimitiveNode) node, entityType);
	}

	/**
	 * make first good estimation of datatype based on the given example values. gracefully user friendly fallback to String rather than unknown for
	 * available fields.
	 *
	 * @param exampleValues List of example data point values.
	 *
	 * @return DataTpye Unknown iff empty values, Number or Boolean iff Homogenizer accepts or String otherwise.
	 */
	public static DataType determineDataType(List<String> exampleValues) {
		if (exampleValues == null) {
			return DataType.UNKNOWN;
		}
		List<DataType> possibleDataTypes = exampleValues.stream().map(s -> {
			if (s.isBlank()) {
				return DataType.UNKNOWN;
			}
			try {
				BooleanHomogenizer.parseHomogenizedRepresentation(s);
				return DataType.BOOLEAN;
			} catch (SLTDataTypeException ignore) {
				// not a Boolean
			}

			try {
				NumberHomogenizer.parseHomogenizedRepresentation(s);
				return DataType.NUMBER;
			} catch (SLTDataTypeException ignore) {
				// not a Double
			}

			// nothing specific, fallthrough String, user can choose otherwise
			return DataType.STRING;
		}).collect(Collectors.toList());

		// check for encapsulating data type (it is enough if one is String or Number, so that all have to be)
		if (possibleDataTypes.contains(DataType.STRING)) {
			return DataType.STRING;
		} else if (possibleDataTypes.contains(DataType.NUMBER)) {
			return DataType.NUMBER;
		} else {
			// only then get the most occurring DataType
			return possibleDataTypes.stream()
					.reduce(BinaryOperator.maxBy(Comparator.comparingInt(o -> Collections.frequency(possibleDataTypes, o))))
					.orElse(DataType.UNKNOWN);
		}
	}

	@Override
	protected DataSourceSchema execute(DataSourceSchema schema, Pair<PrimitiveNode, EntityType> input) {
		PrimitiveNode primitiveNode = input.getLeft();
		EntityType entityType = input.getRight();

		primitiveNode.setEntityType(entityType);
		if (primitiveNode.getDataType() == DataType.UNKNOWN) {
			// if data type is still unknown, try to determine one
			primitiveNode.setDataType(SetEntityType.determineDataType(primitiveNode.getExamples()));
		}

		entityType.setPosition(primitiveNode.getPosition());

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
			if (traversable instanceof PrimitiveNode && ((PrimitiveNode) traversable).getEntityType() == null) {
				result.computeIfAbsent((PrimitiveNode) traversable, ignored -> new HashSet<>())
						.add(getHandleOnNode((PrimitiveNode) traversable));
			}
		});
		return result;
	}
}
