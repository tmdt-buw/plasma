package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.modification.operation.ParameterDefinition;
import de.buw.tmdt.plasma.datamodel.modification.operation.Type;
import de.buw.tmdt.plasma.datamodel.modification.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.CompositeNode;
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

@Service
public class ModifyComposite extends Operation<Pair<CompositeNode, List<Splitting>>> {

	private static final String OPERATION_NAME = "ModifyCompositeSplitting";
	private static final String NODE_ID_PARAMETER_NAME = "NodeId";
	private static final String SPLITTER_PARAMETER_NAME = "Splitter";

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
							+ " Each pattern is applied exactly once to cut of one token from the start of the value."
							+ " Thus n patterns split the value into n+1 tokens which become separate primitive nodes.",
							1,
							Integer.MAX_VALUE
					)
			);

	@Autowired
	protected ModifyComposite(@NotNull OperationLookUp operationLookUp) {
		super(
				OPERATION_NAME,
				PARAMETER_PROTOTYPE,
				"Edit splitting",
				"Modify the patterns for splitting a value."
				+ " Each pattern must be a valid java.util.regex.Pattern literal."
				+ " The pattern describe the location where the value is split NOT what should remain."
				+ " Each pattern is applied exactly once to cut of one token from the start of the value."
				+ " Thus n patterns split the value into n+1 tokens which become separate primitive nodes.",
				operationLookUp
		);
	}

	@Override
	protected Pair<CompositeNode, List<Splitting>> parseParameterDefinition(
			CombinedModel model,
			ParameterDefinition<?, ?> parameterDefinition
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

		SchemaNode node = model.getSyntaxModel().getNode(nodeId);

		final List<Splitting> splitters = Arrays.stream(patterns)
				.map(Splitting::new)
				.collect(Collectors.toList());

		return new Pair<>((CompositeNode) node, splitters);
	}

	@Override
	protected CombinedModel execute(CombinedModel model, Pair<CompositeNode, List<Splitting>> input) {
		CompositeNode composite = input.getLeft();
		List<Splitting> splitter = input.getRight();

		if (splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter list must contain at least one element.");
		}
		if (CollectionUtilities.collectionContains(splitter, null)) {
			throw new IllegalArgumentException("Splitter list must not contain null.");
		}
		composite.setSplitter(splitter);

		DeltaModification modification = new DeltaModification("local_operation", null, null, List.of(composite), null);
		model.apply(modification); // this will take care of all needed stuff

		return model;
	}

	@Override
	protected Handle getHandleOnNode(SchemaNode node) {
		if (!(node instanceof CompositeNode)) {
			throw new IllegalArgumentException("Can't generate handle for node with wrong type: " + node);
		}
		CompositeNode compositeNode = (CompositeNode) node;
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, compositeNode.getUuid());

		String[] splitters = compositeNode.getSplitter().stream().map(Splitting::getPattern).toArray(String[]::new);
		parameterDefinitionClone.replaceValue(SPLITTER_PARAMETER_NAME, splitters);
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<SchemaNode, Set<Handle>> generateHandlesForApplicableNodes(@NotNull CombinedModel model) {
		Map<SchemaNode, Set<Handle>> result = new HashMap<>();

		// get all primitive nodes
		model.getSyntaxModel().getNodes().stream()
				.filter(schemaNode -> schemaNode instanceof CompositeNode)
				.forEach(schemaNode -> result.computeIfAbsent(schemaNode, ignored -> new HashSet<>()).add(getHandleOnNode(schemaNode)));
		return result;
	}
}