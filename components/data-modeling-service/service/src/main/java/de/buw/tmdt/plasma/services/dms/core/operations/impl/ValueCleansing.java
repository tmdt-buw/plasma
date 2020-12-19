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
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This operation sets the cleansing pattern on a primitive node and processes the example values accordingly.
 */
@Service
public class ValueCleansing extends Operation<Pair<PrimitiveNode, String>> {

	private static final String OPERATION_NAME = "Cleanse Value";
	private static final String NODE_ID_PARAMETER_NAME = "NodeId";
	private static final String CLEANSING_PATTERN_NAME = "CleansingPattern";

	private static final ParameterDefinition<TypeDefinitionDTO, ParameterDefinition> PARAMETER_PROTOTYPE = new ParameterDefinition<>(
			Type.COMPLEX, "", "", "", 1, 1,
			new ParameterDefinition<>(
					Type.SYNTAX_NODE_ID,
					NODE_ID_PARAMETER_NAME,
					Strings.PRIMITIVE_NODE_IDENTIFIER,
					"The identifier of the primitive node on which the value cleansing should be applied.",
					1,
					1,
					true
			),
			new ParameterDefinition<>(
					Type.PATTERN,
					CLEANSING_PATTERN_NAME,
					"Cleansing Regex",
					"A regex which is applied on the values where each match is removed.",
					1,
					1
			)
	);

	@Autowired
	ValueCleansing(OperationLookUp operationLookUp) {
		super(
				OPERATION_NAME,
				PARAMETER_PROTOTYPE,
				"Value Cleansing",
				"Cleanses the raw value by removing matches of a regular expression.",
				operationLookUp
		);
	}

	@Override
	protected Pair<PrimitiveNode, String> parseParameterDefinition(
			DataSourceSchema schema,
			ParameterDefinition<?, ?> parameterDefinition
	) throws ParameterParsingException {
		final UUID nodeId;
		final String cleansingPattern;

		validateParameterDefinition(parameterDefinition);
		ParameterDefinition<?, ParameterDefinition> parameter = Type.COMPLEX.castParameterDefinition(parameterDefinition);

		//validate node id child element
		ParameterDefinition<?, ?> uuidParameterDefinition = super.findChildParameterDefinition(parameter, NODE_ID_PARAMETER_NAME);
		if (uuidParameterDefinition == null) {
			throw new ParameterParsingException("uuidParameterDefinition is missing");
		}
		nodeId = getValueAs(uuidParameterDefinition, Type.SYNTAX_NODE_ID, 0, false);

		//validate splitter child element
		ParameterDefinition<?, ?> splitterParameterDefinition = findChildParameterDefinition(parameter, CLEANSING_PATTERN_NAME);
		if (splitterParameterDefinition == null) {
			throw new ParameterParsingException("splitterParameterDefinition is missing");
		}
		cleansingPattern = getValueAs(splitterParameterDefinition, Type.PATTERN, 0, false);

		Traversable node = schema.find(new Traversable.Identity<>(nodeId));
		if (node == null) {
			throw new ParameterParsingException("Did not find node with id `" + nodeId + "` in schema.");
		}
		if (!(node instanceof PrimitiveNode)) {
			throw new ParameterParsingException(
					"Node id identified illegal node type. Expected `" + PrimitiveNode.class + "` but found `" + node.getClass() + "`."
			);
		}

		return Pair.of((PrimitiveNode) node, cleansingPattern);
	}

	@Override
	protected DataSourceSchema execute(DataSourceSchema schema, Pair<PrimitiveNode, String> input) {
		final PrimitiveNode node = input.getLeft();
		final String cleansingPatternString = input.getRight();

		if (cleansingPatternString.isEmpty()) {
			throw new IllegalArgumentException("Cleansing pattern must not be empty.");
		}

		final List<String> cleansedExamples = node.getExamples().stream().map(example -> example.replaceAll(
				cleansingPatternString,
				""
		)).collect(Collectors.toList());
		PrimitiveNode result = new PrimitiveNode(
				node.getEntityType(),
				node.getDataType(),
				cleansedExamples,
				node.getEntityConceptSuggestions(),
				cleansingPatternString
		);

		return schema.replace(node.getIdentity(), result);
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
			if (traversable instanceof PrimitiveNode && ((PrimitiveNode) traversable).getCleansingPattern().isEmpty()) {
				final PrimitiveNode primitive = (PrimitiveNode) traversable;
				result.computeIfAbsent(primitive, ignored -> new HashSet<>()).add(getHandleOnNode(primitive));
			}
		});
		return result;
	}
}
