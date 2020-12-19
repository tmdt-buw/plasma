package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.CompositeNode;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members.Splitter;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.TypeDefinitionDTO;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ModifyComposite extends Operation<Pair<CompositeNode, List<Splitter>>> {

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
	protected Pair<CompositeNode, List<Splitter>> parseParameterDefinition(
			DataSourceSchema schema,
			ParameterDefinition<?, ?> parameterDefinition
	) throws ParameterParsingException {
		final UUID nodeId;
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

		Traversable node = schema.find(new Traversable.Identity<>(nodeId));
		if (node == null) {
			throw new ParameterParsingException("Did not find node with id `" + nodeId + "` in schema.");
		}
		if (!(node instanceof CompositeNode)) {
			throw new ParameterParsingException(
					"Node id identified illegal node type. Expected `" + CompositeNode.class + "` but found `" + node.getClass() + "`."
			);
		}
		final List<Splitter> splitters = Arrays.stream(patterns)
				.map(Splitter::new)
				.collect(Collectors.toList());

		return new Pair<>((CompositeNode) node, splitters);
	}

	@Override
	protected DataSourceSchema execute(DataSourceSchema schema, Pair<CompositeNode, List<Splitter>> input) {
		CompositeNode composite = input.getLeft();
		List<Splitter> splitter = input.getRight();

		if (splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter list must contain at least one element.");
		}
		if (CollectionUtilities.collectionContains(splitter, null)) {
			throw new IllegalArgumentException("Splitter list must not contain null.");
		}

		ArrayList<PrimitiveNode> newComponents = new ArrayList<>(splitter.size() + 1);
		List<PrimitiveNode> oldComponents = composite.getComponents();
		for (int i = 0; i < splitter.size() + 1; i++) {
			EntityType entityType = null;
			DataType dataType = DataType.UNKNOWN;
			if (i < oldComponents.size()) {
				entityType = oldComponents.get(i).getEntityType();
				dataType = oldComponents.get(i).getDataType();
			}
			newComponents.add(new PrimitiveNode(entityType, dataType));
		}

		for (String example : composite.getExamples()) {
			String leftOver = example;
			for (int i = 0; i < splitter.size(); i++) {
				Pair<String, String> split = splitter.get(i).apply(leftOver);
				newComponents.get(i).addExample(split.getLeft());
				leftOver = split.getRight();
			}
			newComponents.get(newComponents.size() - 1).addExample(leftOver);
		}

		CompositeNode result = new CompositeNode(newComponents, splitter, composite.getExamples(), null, composite.getPosition());
		return schema.replace(composite.getIdentity(), result);
	}

	@Override
	protected Handle getHandleOnNode(Node node) {
		if (!(node instanceof CompositeNode)) {
			throw new IllegalArgumentException("Can't generate handle for node with wrong type: " + node);
		}
		CompositeNode compositeNode = (CompositeNode) node;
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, compositeNode.getUuid());

		String[] splitters = compositeNode.getSplitter().stream().map(Splitter::getPattern).toArray(String[]::new);
		parameterDefinitionClone.replaceValue(SPLITTER_PARAMETER_NAME, splitters);
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<Node, Set<Handle>> generateParameterDefinitionsOnGraph(@NotNull Node root) {
		Map<Node, Set<Handle>> result = new HashMap<>();
		root.execute(traversable -> {
			if (traversable instanceof CompositeNode) {
				result.computeIfAbsent((CompositeNode) traversable, ignored -> new HashSet<>()).add(getHandleOnNode((CompositeNode) traversable));
			}
		});
		return result;
	}
}