package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation;

import de.buw.tmdt.plasma.utilities.misc.ReflectionUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Fakes an enum because it conceptually is one but java enums don't support generics.
 */
public final class Type<RAW extends Serializable, PARSED extends Serializable> implements Serializable {
	//INSTANCES
	public static final Type<TypeDefinitionDTO, ParameterDefinition> COMPLEX;
	public static final Type<String, UUID> SYNTAX_NODE_ID;
	public static final Type<Integer, Integer> INTEGER;
	public static final Type<String, String> PATTERN;
	public static final Type<Long, Long> ENTITY_TYPE_ID;
	public static final Type<String, DataType> DATA_TYPE;
	//LOOKUP
	private static final HashMap<String, Type<?, ?>> NAME_LOOK_UP;
	private static final long serialVersionUID = 122197702135871685L;

	static {
		NAME_LOOK_UP = new HashMap<>();

		COMPLEX = new Type<TypeDefinitionDTO, ParameterDefinition>(
				"Complex",
				TypeDefinitionDTO.class,
				ParameterDefinition.class,
				ParameterDefinition::parse,
				ParameterDefinition::serialize
		);
		SYNTAX_NODE_ID = new Type<>(
				"SyntaxNodeId",
				String.class,
				UUID.class,
				UUID::fromString,
				UUID::toString
		);
		INTEGER = new Type<>(
				"Integer",
				Integer.class,
				Integer.class,
				SerializableFunction.identity(),
				SerializableFunction.identity()
		);
		PATTERN = new Type<>(
				"Pattern",
				String.class,
				String.class,
				SerializableFunction.identity(),
				SerializableFunction.identity()
		);
		ENTITY_TYPE_ID = new Type<>(
				"EntityTypeId",
				Long.class,
				Long.class,
				SerializableFunction.identity(),
				SerializableFunction.identity()
		);
		DATA_TYPE = new Type<>(
				"DataType",
				String.class,
				DataType.class,
				DataType::fromIdentifier,
				pn -> pn.identifier
		);
	}

	private final String name;
	private final Class<RAW> rawClass;
	private final Class<PARSED> parsedClass;
	private final SerializableFunction<RAW, PARSED> parser;
	private final SerializableFunction<PARSED, RAW> serializer;

	@SuppressWarnings("SingleCharacterStringConcatenation")
	private Type(
			String name,
			Class<RAW> rawClass,
			Class<PARSED> parsedClass,
			SerializableFunction<RAW, PARSED> parser,
			SerializableFunction<PARSED, RAW> serializer
	) {
		Type duplicate;
		if ((duplicate = NAME_LOOK_UP.put(name, this)) != null) {
			throw new IllegalArgumentException(
					"Duplicate Name: " + name + " was used for:\n"
					+ duplicate + " and\n"
					+ this + "\n"
			);
		}
		this.name = name;
		this.rawClass = rawClass;
		this.parsedClass = parsedClass;
		this.parser = parser;
		this.serializer = serializer;
	}

	public static Type<?, ?> valueOf(String name) {
		return NAME_LOOK_UP.get(name);
	}

	public @NotNull ParameterDefinition<RAW, PARSED> castParameterDefinition(@NotNull ParameterDefinition<?, ?> parameterDefinition) {
		if (!this.equals(parameterDefinition.getType())) {
			throw new IllegalArgumentException(
					"Provided parameter definition `" + parameterDefinition
					+ "` was not of type `" + this
					+ "` but `" + parameterDefinition.getType() + "`."
			);
		}
		Class<PARSED[]> parsedArrayType = ReflectionUtilities.getArrayTypeOf(this.getParsedClass());
		Class<?> valueType = parameterDefinition.getValue().getClass();
		if (!parsedArrayType.isAssignableFrom(valueType)) {
			throw new IllegalArgumentException("Cannot assign " + valueType + " to " + parameterDefinition);
		}
		//noinspection unchecked
		return (ParameterDefinition<RAW, PARSED>) parameterDefinition;
	}

	public String getName() {
		return name;
	}

	public PARSED parse(RAW rawData) {
		return parser.apply(rawData);
	}

	public RAW serialize(PARSED parsed) {
		return serializer.apply(parsed);
	}

	@Override
	public String toString() {
		return this.name;
	}

	public Class<RAW> getRawClass() {
		return rawClass;
	}

	public Class<PARSED> getParsedClass() {
		return parsedClass;
	}

	private interface SerializableFunction<I, O> extends Function<I, O>, Serializable {
		static <T> SerializableFunction<T, T> identity() {
			return x -> x;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, rawClass, parsedClass);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		Type<?, ?> type = (Type<?, ?>) o;
		return Objects.equals(name, type.name) &&
		       Objects.equals(rawClass, type.rawClass) &&
		       Objects.equals(parsedClass, type.parsedClass);
	}
}
