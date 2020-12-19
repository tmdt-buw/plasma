package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class SyntacticOperationDTO implements Serializable {

	public static final String NAME_PROPERTY = "name";
	public static final String LABEL_PROPERTY = "label";
	public static final String DESCRIPTION_PROPERTY = "description";
	public static final String TYPE_DEFINITION_PROPERTY = "parameter";
	private static final long serialVersionUID = -6396493681242743373L;

	private final String name;
	private final String label;
	private final String description;
	private final TypeDefinitionDTO<?> parameter;

	@JsonCreator
	public SyntacticOperationDTO(
			@NotNull @JsonProperty(NAME_PROPERTY) String name,
			@NotNull @JsonProperty(LABEL_PROPERTY) String label,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@NotNull @JsonProperty(TYPE_DEFINITION_PROPERTY) TypeDefinitionDTO<?> parameter
	) {
		this.name = name;
		this.label = label;
		this.description = description;
		this.parameter = parameter;
	}

	@NotNull
	@JsonProperty(NAME_PROPERTY)
	public String getName() {
		return name;
	}

	@NotNull
	@JsonProperty(LABEL_PROPERTY)
	public String getLabel() {
		return label;
	}

	@NotNull
	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@NotNull
	@JsonProperty(TYPE_DEFINITION_PROPERTY)
	public TypeDefinitionDTO<?> getParameter() {
		return parameter;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SyntacticOperationDTO\""
		       + ", \"name\":\"" + name + '"'
		       + ", \"parameter\":" + parameter
		       + '}';
	}
}
