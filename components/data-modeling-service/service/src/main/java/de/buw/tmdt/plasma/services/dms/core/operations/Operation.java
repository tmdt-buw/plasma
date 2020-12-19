package de.buw.tmdt.plasma.services.dms.core.operations;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.ParameterDefinition;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.Type;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

	protected abstract I parseParameterDefinition(DataSourceSchema schema, ParameterDefinition<?, ?> parameterDefinition) throws ParameterParsingException;

	protected abstract DataSourceSchema execute(DataSourceSchema schema, I input);

	protected abstract Handle getHandleOnNode(Node node);

	public abstract Map<Node, Set<Handle>> generateParameterDefinitionsOnGraph(@NotNull Node root);

	public DataSourceSchema invoke(DataSourceSchema schema, ParameterDefinition<?, ?> parameterDefinition) throws ParameterParsingException {
		return this.execute(schema, this.parseParameterDefinition(schema, parameterDefinition));
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
						"Something was changed on the Complex types. " +
						"Have a look here: http://youtrack.zlw-ima.rwth-aachen.de/issue/DP-686 " +
						"or you just created a malformed Complex parameterDefinition"
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

		public DataSourceSchema invoke(DataSourceSchema dataSourceSchema) throws ParameterParsingException {
			return operation.invoke(dataSourceSchema, parameterDefinition);
		}
	}

	public interface Strings {
		String PRIMITIVE_NODE_IDENTIFIER = "Identifier of primitive node";
		String ENTITY_TYPE_IDENTIFIER = "Identifier of entity type";
	}
}
