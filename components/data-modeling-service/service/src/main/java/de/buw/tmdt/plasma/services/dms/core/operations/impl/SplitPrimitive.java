package de.buw.tmdt.plasma.services.dms.core.operations.impl;

import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
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

/**
 * This operation is splitting a PrimitiveNode by the passed on patterns. The reference to the Node is its NodeId. There
 * can be multiple Patterns attached. The operation is attached to all PrimitiveNodes that haven't been split before, so
 * they don't have a CompositeNode as a parent Node.
 */
@Service
public class SplitPrimitive extends Operation<Pair<PrimitiveNode, List<Splitter>>> {

	private static final String OPERATION_NAME = "SplitPrimitive";
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
	protected Pair<PrimitiveNode, List<Splitter>> parseParameterDefinition(
			DataSourceSchema schema,
			@NotNull ParameterDefinition<?, ?> parameterDefinition
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
		if (!(node instanceof PrimitiveNode)) {
			throw new ParameterParsingException(
					"Node id identified illegal node type. Expected `" + PrimitiveNode.class + "` but found `" + node.getClass() + "`."
			);
		}

		final List<Splitter> splitters = Arrays.stream(patterns)
				.map(Splitter::new)
				.collect(Collectors.toList());

		return new Pair<>((PrimitiveNode) node, splitters);
	}

	@Override
	protected DataSourceSchema execute(DataSourceSchema schema, Pair<PrimitiveNode, List<Splitter>> primitiveListPair) {
		PrimitiveNode primitive = primitiveListPair.getLeft();
		List<Splitter> splitter = primitiveListPair.getRight();

		if (splitter.isEmpty()) {
			throw new IllegalArgumentException("Splitter list must contain at least one element.");
		}
		if (CollectionUtilities.collectionContains(splitter, null)) {
			throw new IllegalArgumentException("Splitter list must not contain null.");
		}

		List<PrimitiveNode> components = new ArrayList<>(splitter.size() + 1);
		for (int i = 0; i < splitter.size() + 1; i++) {
			components.add(new PrimitiveNode(null, DataType.UNKNOWN));
		}

		for (String example : primitive.getExamples()) {

			String leftOver = example;
			for (int i = 0; i < splitter.size(); i++) {
				Pair<String, String> split = splitter.get(i).apply(leftOver);
				components.get(i).addExample(split.getLeft());
				leftOver = split.getRight();
			}
			components.get(components.size() - 1).addExample(leftOver);
		}

		CompositeNode result = new CompositeNode(components, splitter, primitive.getExamples(), null, primitive.getPosition());
		return schema.replace(primitive.getIdentity(), result);
	}

	@Override
	public Handle getHandleOnNode(Node node) {
		ParameterDefinition<?, ?> parameterDefinitionClone = getParameterPrototype();
		parameterDefinitionClone.replaceValue(NODE_ID_PARAMETER_NAME, node.getUuid());
		return new Handle(this, parameterDefinitionClone);
	}

	@Override
	public Map<Node, Set<Handle>> generateParameterDefinitionsOnGraph(@NotNull Node root) {
		HashSet<PrimitiveNode> compositeChildren = new HashSet<>();
		Map<Node, Set<Handle>> result = new HashMap<>();
		root.execute(traversable -> {
			if (traversable instanceof PrimitiveNode && !compositeChildren.contains(traversable)) {
				result.computeIfAbsent((PrimitiveNode) traversable, ignored -> new HashSet<>()).add(getHandleOnNode((PrimitiveNode) traversable));
			} else if (traversable instanceof CompositeNode) {
				compositeChildren.addAll(((CompositeNode) traversable).getComponents());
			}
		});
		return result;
	}
}
