package de.buw.tmdt.plasma.datamodel.modification.operation;

import de.buw.tmdt.plasma.utilities.misc.ReflectionUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

/**
 * Fakes an enum because it conceptually is one but java enums don't support generics.
 *
 * @param <TRaw>    The raw type
 * @param <TParsed> The parsed type
 */
public final class Type<TRaw extends Serializable, TParsed extends Serializable> implements Serializable {
    //INSTANCES
    public static final Type<TypeDefinitionDTO, ParameterDefinition> COMPLEX;
    public static final Type<String, String> SYNTAX_NODE_ID;
    public static final Type<Integer, Integer> INTEGER;
    public static final Type<String, String> PATTERN;
    public static final Type<String, String> ENTITY_TYPE_ID;
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
                String.class,
                SerializableFunction.identity(),
                SerializableFunction.identity()
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
                String.class,
                String.class,
                SerializableFunction.identity(),
                SerializableFunction.identity()
        );
        DATA_TYPE = new Type<>(
                "DataType",
                String.class,
                DataType.class,
                DataType::valueOf,
                DataType::name
        );
    }

    private final String name;
    private final Class<TRaw> rawClass;
    private final Class<TParsed> parsedClass;
    private final SerializableFunction<TRaw, TParsed> parser;
    private final SerializableFunction<TParsed, TRaw> serializer;

    @SuppressWarnings("SingleCharacterStringConcatenation")
    private Type(
            String name,
            Class<TRaw> rawClass,
            Class<TParsed> parsedClass,
            SerializableFunction<TRaw, TParsed> parser,
            SerializableFunction<TParsed, TRaw> serializer
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

    public @NotNull ParameterDefinition<TRaw, TParsed> castParameterDefinition(@NotNull ParameterDefinition<?, ?> parameterDefinition) {
        if (!this.equals(parameterDefinition.getType())) {
            throw new IllegalArgumentException(
                    "Provided parameter definition `" + parameterDefinition
                            + "` was not of type `" + this
                            + "` but `" + parameterDefinition.getType() + "`."
            );
        }
        Class<TParsed[]> parsedArrayType = ReflectionUtilities.getArrayTypeOf(this.getParsedClass());
        Class<?> valueType = parameterDefinition.getValue().getClass();
        if (!parsedArrayType.isAssignableFrom(valueType)) {
            throw new IllegalArgumentException("Cannot assign " + valueType + " to " + parameterDefinition);
        }
        //noinspection unchecked
        return (ParameterDefinition<TRaw, TParsed>) parameterDefinition;
    }

    public String getName() {
        return name;
    }

    public TParsed parse(TRaw rawData) {
        return parser.apply(rawData);
    }

    public TRaw serialize(TParsed parsed) {
        return serializer.apply(parsed);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Class<TRaw> getRawClass() {
        return rawClass;
    }

    public Class<TParsed> getParsedClass() {
        return parsedClass;
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

    private interface SerializableFunction<I, O> extends Function<I, O>, Serializable {
        static <T> SerializableFunction<T, T> identity() {
            return x -> x;
        }
    }
}
