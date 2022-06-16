package de.buw.tmdt.plasma.datamodel.modification.operation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class TypeDefinitionDTO<T extends Serializable> implements Serializable {

	public static final String MINIMUM_CARDINALITY_PROPERTY = "minCardinality";
	public static final String MAXIMUM_CARDINALITY_PROPERTY = "maxCardinality";
	public static final String TYPE_PROPERTY = "type";
	public static final String NAME_PROPERTY = "name";
	public static final String LABEL_PROPERTY = "label";
	public static final String DESCRIPTION_PROPERTY = "description";
	public static final String HIDDEN_PROPERTY = "hidden";
	public static final String VALUE_PROPERTY = "value";
	private static final long serialVersionUID = -5058056312857106310L;

	private final int minCardinality;
	private final int maxCardinality;
	private final String type;
	private final String name;
	private final String label;
	private final String description;
	private final boolean hidden;
	private final T[] value;

	public TypeDefinitionDTO() {
		minCardinality = maxCardinality = -1;
		type = name = label = description = null;
		hidden = true;
		value = null;
	}

	public TypeDefinitionDTO(
			@NotNull String type,
			@NotNull String name,
			@NotNull String label,
			@NotNull String description,
			int minCardinality,
			int maxCardinality
	) {
		this(type, name, label, description, minCardinality, maxCardinality, null, false);
	}

	@SuppressWarnings("WeakerAccess")
	@JsonCreator
	public TypeDefinitionDTO(
			@JsonProperty(TYPE_PROPERTY) @NotNull String type,
			@JsonProperty(NAME_PROPERTY) @NotNull String name,
			@JsonProperty(LABEL_PROPERTY) @NotNull String label,
			@JsonProperty(DESCRIPTION_PROPERTY) @NotNull String description,
			@JsonProperty(MINIMUM_CARDINALITY_PROPERTY) int minCardinality,
			@JsonProperty(MAXIMUM_CARDINALITY_PROPERTY) int maxCardinality,
			@JsonProperty(VALUE_PROPERTY) T[] value,
			@JsonProperty(HIDDEN_PROPERTY) boolean hidden
	) {
		this.type = type;
		this.name = name;
		this.label = label;
		this.description = description;
		this.minCardinality = minCardinality;
		this.maxCardinality = maxCardinality;
		this.value = value != null ? value.clone() : null;
		this.hidden = hidden;
	}

	@JsonProperty(MINIMUM_CARDINALITY_PROPERTY)
	public int getMinCardinality() {
		return minCardinality;
	}

	@JsonProperty(MAXIMUM_CARDINALITY_PROPERTY)
	public int getMaxCardinality() {
		return maxCardinality;
	}

	@JsonProperty(TYPE_PROPERTY)
	public String getType() {
		return type;
	}

	@JsonProperty(NAME_PROPERTY)
	public String getName() {
		return name;
	}

	@JsonProperty(LABEL_PROPERTY)
	public String getLabel() {
		return label;
	}

	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@JsonProperty(HIDDEN_PROPERTY)
	public boolean isHidden() {
		return hidden;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"TypeDefinitionDTO\""
		       + ", \"minCardinality\":\"" + minCardinality + '"'
		       + ", \"maxCardinality\":\"" + maxCardinality + '"'
		       + ", \"type\":\"" + type + '"'
		       + ", \"name\":\"" + name + '"'
		       + ", \"label\":\"" + label + '"'
				+ ", \"description\":\"" + description + '"'
				+ ", \"hidden\":\"" + hidden + '"'
				+ ", \"value\":" + StringUtilities.arrayToJson(value)
				+ '}';
	}

	@JsonProperty(VALUE_PROPERTY)
	public T[] getValue() {
		return value != null ? value.clone() : null;
	}

	public @NotNull TypeDefinitionDTO<?> copy() {
		return new TypeDefinitionDTO<>(
				getType(),
				getName(),
				getLabel(),
				getDescription(),
				getMinCardinality(),
				getMaxCardinality(),
				getValue(),
				isHidden());
	}
}
