package de.buw.tmdt.plasma.services.dms.core.operations;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.operation.ParameterDefinition;
import de.buw.tmdt.plasma.datamodel.modification.operation.Type;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Specifies operations applicable to {@link SchemaNode}s.
 *
 * @param <I> The type of {@link SchemaNode} this operation is applicable to
 */
public abstract class Operation<I> {

	protected final String name;
	protected final String label;
	protected final String description;

	private final ParameterDefinition<?, ?> parameterPrototype;

	protected Operation(
			@NotNull String name,
			@NotNull ParameterDefinition<?, ?> parameterPrototype,
			@NotNull String label,
			@NotNull String description,
			@NotNull OperationLookUp operationLookUp
	) {
		this.name = name;
		this.parameterPrototype = parameterPrototype;
		this.label = label;
		this.description = description;
		operationLookUp.registerOperation(this);
	}

	/**
	 * Helper function to find single {@link PrimitiveNode}s in the given {@link CombinedModel}.
	 *
	 * @param model    The model containing the nodes
	 * @param nodeUuid The uuid to search for
	 * @return The found node
	 * @throws ParameterParsingException If no matching node could be found or was not a {@link PrimitiveNode}
	 */
	public static PrimitiveNode getPrimitiveNode(CombinedModel model, String nodeUuid) throws ParameterParsingException {
		SchemaNode node = model.getSyntaxModel().getNode(nodeUuid);
		if (node == null) {
			throw new ParameterParsingException("Did not find node with id `" + nodeUuid + "` in model.");
		}
		if (!(node instanceof PrimitiveNode)) {
			throw new ParameterParsingException(
					"Node id identified illegal node type. Expected `" + PrimitiveNode.class + "` but found `" + node.getClass() + "`."
			);
		}
		return (PrimitiveNode) node;
	}

	/**
	 * Parses the parameter definition to identify an object this operation is to be applied to.
	 *
	 * @param model               The model to use
	 * @param parameterDefinition The parameter to parse
	 * @return The object, most likely a sub type of SchemaNode
	 * @throws ParameterParsingException In case the parameter cannot be parsed or the target element does not exist
	 */
	protected abstract I parseParameterDefinition(CombinedModel model, ParameterDefinition<?, ?> parameterDefinition) throws ParameterParsingException;

	/**
	 * Generates a {@link Handle} (an {@link Operation} with a set of {@link ParameterDefinition}s) on the node.
	 * The operation will always be that of the operation type itself
	 *
	 * @param node The syntax node, most likely a sub type of SchemaNode
	 * @return The generated handle
	 */
	protected abstract Handle getHandleOnNode(SchemaNode node);

	/**
	 * Executes the operation on the identified object.
	 *
	 * @param model  The model to use
	 * @param object The object, most likely a sub type of SchemaNode
	 * @return The modified model
	 */
	protected abstract CombinedModel execute(CombinedModel model, I object);

	/**
	 * Invokes the {@link Operation} with the given {@link ParameterDefinition} on the model.
	 * Each {@link ParameterDefinition} contains the uuid of the target node and other parameters to use during execution.
	 *
	 * @param model               The model to use
	 * @param parameterDefinition The single parameter set to use (contains in most cases at least the node id)
	 * @return The modified model
	 * @throws ParameterParsingException If the parameter cannot be parsed or the operation not applied for any reason
	 */
	public CombinedModel invoke(CombinedModel model, ParameterDefinition<?, ?> parameterDefinition) throws ParameterParsingException {
		return this.execute(model, this.parseParameterDefinition(model, parameterDefinition));
	}

	@Nullable
	protected final <P extends Serializable> P getValueAs(
			@NotNull ParameterDefinition<?, ?> parameterDefinition,
			@NotNull Type<?, P> type,
			int index
	) throws ParameterParsingException {
		return getValueAs(parameterDefinition, type, index, true);
	}

	protected final <P extends Serializable> P getValueAs(
			@NotNull ParameterDefinition<?, ?> parameterDefinition,
			@NotNull Type<?, P> type,
			int index,
			boolean nullable
	) throws ParameterParsingException {

		P value = parameterDefinition.probeValueAs(type)[index];

		if (!nullable && value == null) {
			throw new ParameterParsingException("Value at " + index + " of `" + parameterDefinition.getName() + "` was null.");
		}

		return value;
	}

	protected @Nullable ParameterDefinition<?, ?> findChildParameterDefinition(
			@NotNull ParameterDefinition<?, ParameterDefinition> parameterDefinition,
			String name
	) {
		for (ParameterDefinition<?, ?> child : parameterDefinition.getValue()) {
			if (child != null && Objects.equals(child.getName(), name)) {
				return child;
			}
		}
		return null;
	}

	protected ParameterDefinition<?, ?> getParameterPrototype() {
		try {
			return ObjectUtilities.deepCopy(parameterPrototype);
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Failed to clone parametrization prototype", e);
		}
	}

	public String getName() {
		return this.name;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	protected final void validateParameterDefinition(ParameterDefinition<?, ?> parameterDefinition) throws ParameterParsingException {
		validateParameterDefinition(parameterDefinition, parameterPrototype);
	}

	/**
	 * Validates that the passed {@link ParameterDefinition} is valid w.r.t. the given boundaries
	 *
	 * @param testee    The definition to test
	 * @param prototype The default (raw) definition
	 * @throws ParameterParsingException If the parameter cannot be parsed or is invalid / inconsistent
	 */
	private void validateParameterDefinition(
			ParameterDefinition<?, ?> testee,
			ParameterDefinition<?, ?> prototype
	) throws ParameterParsingException {
		// Validate the type
		if (!prototype.getType().equals(testee.getType())) {
			throw new ParameterParsingException("Parameter type was not `" + prototype.getType() + "`.");
		}
		// Validate the label
		if (!prototype.getName().equals(testee.getName())) {
			throw new ParameterParsingException(
					"Name of parameterDefinition (" + testee.getName() +
							") doesn't match expected name of parameterPrototype: " + prototype.getName()
			);
		}

		// Validate complex types
		if (testee.getType().equals(Type.COMPLEX)) {
			if (!(testee.getMaxCardinality() == 1 || testee.getMinCardinality() == 1)) {
				//noinspection HardcodedFileSeparator
				throw new ParameterParsingException(
				);
			}

			Map<String, ParameterDefinition<?, ?>> prototypeChildren = new HashMap<>();
			for (Object pV : prototype.getValue()) {
				ParameterDefinition<?, ?> prototypeValue = (ParameterDefinition<?, ?>) pV;
				prototypeChildren.put(((ParameterDefinition<?, ?>) pV).getName(), prototypeValue);
			}
			for (Object childValue : testee.getValue()) {
				if (!(childValue instanceof ParameterDefinition)) {
					throw new ParameterParsingException("Child values of a complex parameterDefinition have to be of ParameterDefinitons");
				}
				validateParameterDefinition((ParameterDefinition<?, ?>) childValue, prototypeChildren.get(((ParameterDefinition<?, ?>) childValue).getName
						()));
			}
			return;
		}

		// Validate min cardinality
		if (prototype.getMinCardinality() > testee.getValue().length) {
			throw new ParameterParsingException(
					"Parameter values of `" + testee.getName()
							+ "` did contain less than " + prototype.getMinCardinality()
							+ " elements: " + testee.getValue().length
							+ ". "
			);
		}

		// Validate max cardinality
		if (prototype.getMaxCardinality() < testee.getValue().length) {
			throw new ParameterParsingException(
					"Parameter values of `" + testee.getName()
							+ "` did contain more than " + prototype.getMaxCardinality()
							+ " elements: " + testee.getValue().length
							+ ". "
			);
		}
	}

	/**
	 * Generates a map of {@link Handle}s for each node of the {@link de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel}.
	 * Each operation type decides to which node it is applicable
	 *
	 * @param model The model to use
	 * @return A map of possible operations for each node
	 */
	public abstract Map<SchemaNode, Set<Handle>> generateHandlesForApplicableNodes(@NotNull CombinedModel model);

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		Operation<?> operation = (Operation<?>) o;
		return Objects.equals(name, operation.name);
	}

	public static class Handle {
		private final Operation<?> operation;
		private final ParameterDefinition<?, ?> parameterDefinition;

		public Handle(Operation<?> operation, ParameterDefinition<?, ?> parameterDefinition) {
			this.operation = operation;
			this.parameterDefinition = parameterDefinition;
		}

		public Operation<?> getOperation() {
			return operation;
		}

		public ParameterDefinition<?, ?> getParameterDefinition() {
			return parameterDefinition;
		}

		public CombinedModel invoke(CombinedModel model) throws ParameterParsingException {
			model = model.copy();
			return operation.invoke(model, parameterDefinition);
		}
	}

	public interface Strings {
		String PRIMITIVE_NODE_IDENTIFIER = "Identifier of primitive node";
		String ENTITY_TYPE_IDENTIFIER = "Identifier of entity type";
	}
}
