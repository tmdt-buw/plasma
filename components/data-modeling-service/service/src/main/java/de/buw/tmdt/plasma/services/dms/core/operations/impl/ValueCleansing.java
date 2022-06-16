package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
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
			CombinedModel model,
			ParameterDefinition<?, ?> parameterDefinition
	) throws ParameterParsingException {
		final String nodeId;
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

		PrimitiveNode node = getPrimitiveNode(model, nodeId);

		return Pair.of(node, cleansingPattern);
	}

	@Override
	protected CombinedModel execute(CombinedModel model, Pair<PrimitiveNode, String> input) {
		final PrimitiveNode primitiveNode = input.getLeft();
		final String cleansingPatternString = input.getRight();

		if (cleansingPatternString.isEmpty()) {
			throw new IllegalArgumentException("Cleansing pattern must not be empty.");
		}

		final List<String> cleansedExamples = primitiveNode.getExamples().stream().map(example -> example.replaceAll(
				cleansingPatternString,
				""
		)).collect(Collectors.toList());

		primitiveNode.setExamples(cleansedExamples);

		DeltaModification modification = new DeltaModification("local_operation", null, null, List.of(primitiveNode), null);
		model.apply(modification);

		return model;
	}

	@Override
	protected Handle getHandleOnNode(SchemaNode node) {
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, node.getUuid());
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<SchemaNode, Set<Handle>> generateHandlesForApplicableNodes(@NotNull CombinedModel model) {
		Map<SchemaNode, Set<Handle>> result = new HashMap<>();

		// get all primitive nodes with empty cleansing pattern
		model.getSyntaxModel().getNodes().stream()
				.filter(schemaNode -> schemaNode instanceof PrimitiveNode)
				.map(schemaNode -> (PrimitiveNode) schemaNode)
				.filter(primitiveNode -> primitiveNode.getCleansingPattern() == null || primitiveNode.getCleansingPattern().isEmpty())
				.forEach(schemaNode -> result.computeIfAbsent(schemaNode, ignored -> new HashSet<>()).add(getHandleOnNode(schemaNode)));
		return result;
	}
}
