package de.buw.tmdt.plasma.datamodel.modification.operation;

import de.buw.tmdt.plasma.utilities.misc.ReflectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Array;

// fix those issues once the future of this class is determined
@SuppressFBWarnings({"NP_NONNULL_RETURN_VIOLATION", "NP_NONNULL_RETURN_VIOLATION"})
public class ParameterDefinition<R extends Serializable, T extends Serializable> implements Serializable {
	private static final long serialVersionUID = 8679109630152788539L;
	private final int minCardinality;
	private final int maxCardinality;
	private final Type<R, T> type;
	private final String name;
	private final String label;
	private final String description;
	private final boolean hidden;
	private T[] value;

	public ParameterDefinition(
			@NotNull Type<R, T> type,
			@NotNull String name,
			@NotNull String label,
			String description, int minCardinality,
			int maxCardinality,
			T... value
	) {
		this(type, name, label, description, minCardinality, maxCardinality, false, value);
	}

	public ParameterDefinition(
			@NotNull Type<R, T> type,
			@NotNull String name,
			@NotNull String label,
			String description, int minCardinality,
			int maxCardinality,
			boolean hidden,
			T... value

	) {
		this.label = label;
		this.description = description;
		if (type.equals(Type.COMPLEX) && (minCardinality != 1 || maxCardinality != 1)) {
			//noinspection HardcodedFileSeparator
			throw new RuntimeException("Error - requires fix");
		}
		this.type = type;
		this.name = name;
		this.minCardinality = minCardinality;
		this.maxCardinality = maxCardinality;
		//noinspection unchecked
		this.value = value != null ? value : (T[]) Array.newInstance(type.getParsedClass(), 0);
		this.hidden = hidden;
	}

	public boolean isHidden() {
		return hidden;
	}

	@NotNull
	static <R extends Serializable, P extends Serializable> TypeDefinitionDTO<R> serialize(@NotNull ParameterDefinition<R, P> parameterDefinition) {
		Type<R, P> type = parameterDefinition.getType();
		P[] values = parameterDefinition.getValue();

		R[] serializedValues = serializeParameterDefinition(values, type);

		return new TypeDefinitionDTO<>(
				parameterDefinition.getType().getName(),
				parameterDefinition.getName(),
				parameterDefinition.getLabel(),
				parameterDefinition.getDescription(),
				parameterDefinition.getMinCardinality(),
				parameterDefinition.getMaxCardinality(),
				serializedValues,
				parameterDefinition.isHidden()
		);
	}

	public int getMinCardinality() {
		return minCardinality;
	}

	public int getMaxCardinality() {
		return maxCardinality;
	}

	static <TPayload extends Serializable, TParsed extends Serializable> ParameterDefinition<TPayload, TParsed>
	parse(TypeDefinitionDTO<TPayload> typeDefinitionDTO) {
		@SuppressWarnings("unchecked") final Class<? extends TPayload[]> payloadType = (Class<? extends TPayload[]>) typeDefinitionDTO.getValue().getClass();

		Type<?, ?> resultType = Type.valueOf(typeDefinitionDTO.getType());

		Class<TParsed> parsedType = (Class<TParsed>) resultType.getParsedClass();

		Type<TPayload, TParsed> type = ParameterDefinition.castType(resultType, payloadType, parsedType);

		if (type == null) {
			throw new IllegalArgumentException("Unknown type: " + typeDefinitionDTO.getType());
		}

		TPayload[] values = typeDefinitionDTO.getValue();

		return new ParameterDefinition<>(
				type,
				typeDefinitionDTO.getName(),
				typeDefinitionDTO.getLabel(),
				typeDefinitionDTO.getDescription(),
				typeDefinitionDTO.getMinCardinality(),
				typeDefinitionDTO.getMaxCardinality(),
				parseParameterDefinition(values, type)
		);
	}

	@Nullable
	private static <TPayload extends Serializable, TParsed extends Serializable>
	Type<TPayload, TParsed> castType(@Nullable Type<?, ?> type, Class<? extends TPayload[]> payloadClass, Class<TParsed> parsedType) {
		if (type == null) {
			return null;
		}
		Class<?> rawArrayClass = ReflectionUtilities.getArrayTypeOf(type.getRawClass());

		if (!type.getParsedClass().equals(parsedType)) {
			throw new IllegalArgumentException("Expected " + Type.class + ".parsedClass to be `" + parsedType + " but found: " + type.getParsedClass());
		}

		if (!rawArrayClass.isAssignableFrom(payloadClass)) {
			throw new IllegalArgumentException(
					TypeDefinitionDTO.class.getSimpleName() + ".type was `" + type.getName()
							+ "` and it's values were not of type `" + rawArrayClass +
							"` but of type `" + payloadClass + "`."
			);
		}

		//sure... that code above is just for shits and giggles... such unchecked, much ifs, wow
		@SuppressWarnings("unchecked")
		Type<TPayload, TParsed> castType = (Type<TPayload, TParsed>) type;
		return castType;
	}

	private static <TRaw extends Serializable, TParsed extends Serializable> @Nullable TRaw @NotNull [] serializeParameterDefinition(
			TParsed @NotNull [] parsedData,
			Type<TRaw, TParsed> type
	) {
		@SuppressWarnings("unchecked - because no one needs generic signatures in the standard library")
		TRaw[] result = (TRaw[]) Array.newInstance(type.getRawClass(), parsedData.length);

		for (int i = 0; i < parsedData.length; i++) {
			Class<TParsed> parsedClass = type.getParsedClass();
			try {
				result[i] = type.serialize(parsedClass.cast(parsedData[i]));
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Raw data array contained element of incompatible type.", e);
			}
		}
		return result;
	}

	private static <TRaw extends Serializable, TParsed extends Serializable> @Nullable TParsed @NotNull [] parseParameterDefinition(
			TRaw @NotNull [] rawData,
			Type<TRaw, TParsed> type
	) {
		@SuppressWarnings("unchecked - because no one needs generic signatures in the standard library")
		TParsed[] result = (TParsed[]) Array.newInstance(type.getParsedClass(), rawData.length);
		for (int i = 0; i < rawData.length; i++) {
			Class<TRaw> rawClass = type.getRawClass();
			try {
				result[i] = type.parse(rawClass.cast(rawData[i]));
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Raw data array contained element of incompatible type.", e);
			}
		}
		return result;
	}

	public T @NotNull [] getValue() {
		return value.clone();
	}

	public <P extends Serializable> P @NotNull [] probeValueAs(@NotNull Type<?, P> type) {
		try {
			@SuppressWarnings("unchecked - this fails only if this.getType() is not equal to the parameter which is an illegal call.")
			P[] result = (P[]) getValue();
			return result;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(
					"Value of type " + value.getClass() + " is not assignable to type " + Array.newInstance(type.getParsedClass(), 0).getClass(),
					e
			);
		}
	}

	public <E> boolean replaceValue(@NotNull String name, E... value) {
		if (this.name.equals(name)) {
			if (this.value.getClass().isAssignableFrom(value.getClass())) {
				this.value = (T[]) this.value.getClass().cast(value);
				return true;
			} else {
				throw new IllegalArgumentException(
						"Type of name " + name +
								" expected parameter value of type " + this.value.getClass() +
								" but tried to write " + value.getClass());
			}
		}

		if (this.getType().equals(Type.COMPLEX)) {
			for (ParameterDefinition<?, ?> parameterDefinition : this.probeValueAs(Type.COMPLEX)) {
				if (parameterDefinition != null && parameterDefinition.replaceValue(name, value)) {
					return true;
				}
			}
		}

		return false;
	}

	@NotNull
	public Type<R, T> getType() {
		return type;
	}

	@NotNull
	public String getName() {
		return name;
	}

	@NotNull
	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"ParameterDefinition\""
				+ ", \"minCardinality\":\"" + minCardinality + '"'
				+ ", \"maxCardinality\":\"" + maxCardinality + '"'
				+ ", \"type\":" + type
				+ ", \"name\":\"" + name + '"'
				+ ", \"label\":\"" + label + '"'
				+ ", \"description\":\"" + description + '"'
				+ ", \"hidden\":\"" + hidden + '"'
				+ ", \"value\":" + StringUtilities.arrayToJson(value)
				+ '}';
	}
}
