package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
public class StandardDTO implements Serializable {
	private static final long serialVersionUID = -7417813433683071191L;

	private static final String UUID_PROPERTY = "uuid";
	private static final String NAME_PROPERTY = "name";
	private static final String DESCRIPTION_PROPERTY = "description";

	private final String uuid;
	private final String name;
	private final String description;

	@JsonCreator
	public StandardDTO(
			@NotNull @JsonProperty(UUID_PROPERTY) String uuid,
			@NotNull @JsonProperty(NAME_PROPERTY) String name,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description
	) {
		this.uuid = uuid;
		this.name = name;
		this.description = description;
	}

	@NotNull
	@JsonProperty(UUID_PROPERTY)
	public String getUuid() {
		return uuid;
	}

	@NotNull
	@JsonProperty(NAME_PROPERTY)
	public String getName() {
		return name;
	}

	@NotNull
	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}
}
