package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.modification.operation.ParameterDefinition;
import de.buw.tmdt.plasma.datamodel.modification.operation.Type;
import de.buw.tmdt.plasma.datamodel.modification.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.CompositeNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.Splitting;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This operation is splitting a PrimitiveNode by the passed on patterns. The reference to the Node is its NodeId. There
 * can be multiple Patterns attached. The operation is attached to all PrimitiveNodes that haven't been split before, so
 * they don't have a CompositeNode as a parent Node.
 */
@Service
public class SplitPrimitive extends Operation<Pair<PrimitiveNode, List<Splitting>>> {

	public static final String OPERATION_NAME = "SplitPrimitive";
	public static final String NODE_ID_PARAMETER_NAME = "NodeId";
	public static final String SPLITTER_PARAMETER_NAME = "Splitting";

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
							"The identifier of the primitive node which shall be split into multiple primitive nodes.",
							1,
							1,
							true
					),
					new ParameterDefinition<>(
							Type.PATTERN,
							SPLITTER_PARAMETER_NAME,
							"Split patterns",
							"The list of patterns to split the value into multiple primitives."
							+ " Each pattern must be a valid java.util.regex.Pattern literal."
							+ " The pattern describe the location where the value is split NOT what should remain."
							+ " Each pattern is applied exactly once to cut of one token from the start of the value."
							+ " Thus n patterns split the value into n+1 tokens which become separate primitive nodes.",
							1,
							Integer.MAX_VALUE
					)
			);

	@Autowired
	SplitPrimitive(OperationLookUp operationLookUp) {
		super(
				OPERATION_NAME,
				PARAMETER_PROTOTYPE,
				"Split",
				"Splits the primitive node into multiple primitives by applying a list of regular expressions.",
				operationLookUp
		);
	}

	@Override
	protected Pair<PrimitiveNode, List<Splitting>> parseParameterDefinition(
			CombinedModel model,
			@NotNull ParameterDefinition<?, ?> parameterDefinition
	) throws ParameterParsingException {
		final String nodeId;
		final String[] patterns;

		validateParameterDefinition(parameterDefinition);

		ParameterDefinition<?, ParameterDefinition> parameter = Type.COMPLEX.castParameterDefinition(parameterDefinition);

		//validate node id child element
		ParameterDefinition<?, ?> uuidParameterDefinition = super.findChildParameterDefinition(parameter, NODE_ID_PARAMETER_NAME);
		if (uuidParameterDefinition == null) {
			throw new ParameterParsingException("uuidParameterDefinition is missing");
		}
		nodeId = getValueAs(uuidParameterDefinition, Type.SYNTAX_NODE_ID, 0, false);

		//validate splitter child element
		ParameterDefinition<?, ?> splitterParameterDefinition = findChildParameterDefinition(parameter, SPLITTER_PARAMETER_NAME);
		if (splitterParameterDefinition == null) {
			throw new ParameterParsingException("splitterParameterDefinition is missing");
		}
		patterns = splitterParameterDefinition.probeValueAs(Type.PATTERN);

		PrimitiveNode node = getPrimitiveNode(model, nodeId);

		final List<Splitting> splitters = Arrays.stream(patterns)
				.map(Splitting::new)
				.collect(Collectors.toList());

		return new Pair<>(node, splitters);
	}

	@Override
	protected CombinedModel execute(CombinedModel model, Pair<PrimitiveNode, List<Splitting>> primitiveListPair) {
		PrimitiveNode primitive = primitiveListPair.getLeft();
		List<Splitting> splitter = primitiveListPair.getRight();

		if (splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitting list must contain at least one element.");
		}
		if (CollectionUtilities.collectionContains(splitter, null)) {
			throw new IllegalArgumentException("Splitting list must not contain null.");
		}

		// define what we want to have
		CompositeNode composite = new CompositeNode(primitive.getUuid(),
                primitive.getLabel(),
                null,
                primitive.getXCoordinate(),
                primitive.getYCoordinate(),
                primitive.isValid(),
                primitive.getExamples(),
                primitive.getCleansingPattern(),
                primitiveListPair.getRight());

		DeltaModification modification = new DeltaModification("local_operation", null, null, List.of(composite), null);
		model.apply(modification); // this will to the magic for us

		return model;
	}

	@Override
	public Handle getHandleOnNode(SchemaNode node) {
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, node.getUuid());
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<SchemaNode, Set<Handle>> generateHandlesForApplicableNodes(@NotNull CombinedModel model) {
		Map<SchemaNode, Set<Handle>> result = new HashMap<>();

		// get all composite node children uuids (those cannot be split again)
		List<String> compositeChildrenIds = model.getSyntaxModel().getNodes().stream()
				.filter(schemaNode -> schemaNode instanceof CompositeNode)
				.flatMap(schemaNode -> model.getSyntaxModel().getChildNodesForNode(schemaNode.getUuid()).stream())
				.map(CombinedModelElement::getUuid)
				.collect(Collectors.toList());

		// get all primitive nodes that are not child of a composite
		model.getSyntaxModel().getNodes().stream()
				.filter(schemaNode -> schemaNode instanceof PrimitiveNode)
				.filter(schemaNode -> !compositeChildrenIds.contains(schemaNode.getUuid()))
				.forEach(schemaNode -> result.computeIfAbsent(schemaNode, ignored -> new HashSet<>()).add(getHandleOnNode(schemaNode)));
		return result;
	}
}
