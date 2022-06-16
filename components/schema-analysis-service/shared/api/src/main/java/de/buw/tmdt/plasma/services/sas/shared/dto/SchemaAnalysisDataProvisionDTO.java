package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class SchemaAnalysisDataProvisionDTO implements Serializable {

	private static final String DATA_PROPERTY = "data";
	private static final long serialVersionUID = 8440887265048974038L;

	@JsonProperty(DATA_PROPERTY)
	private	String data;

	@JsonCreator
	public SchemaAnalysisDataProvisionDTO(
			@NotNull @JsonProperty(DATA_PROPERTY) String data
	) {
		this.data = data;
	}

	@JsonProperty(DATA_PROPERTY)
	public String getData() {
		return data;
	}

}
