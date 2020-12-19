package de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SemanticModelCreationResponse implements Serializable {
	private static final long serialVersionUID = -1729363261701693387L;

	private static final String ID_MAPPING_PROPERTY = "oldToNewIdLookup";
	private static final String SEMANTIC_MODEL_PROPERTY = "semanticModelDto";

	private final Map<String, String> oldToNewIdLookup;
	private final SemanticModelDTO semanticModelDTO;

	@JsonCreator
	public SemanticModelCreationResponse(
			@NotNull @JsonProperty(ID_MAPPING_PROPERTY) Map<String, String> oldToNewIdLookup,
			@NotNull @JsonProperty(SEMANTIC_MODEL_PROPERTY) SemanticModelDTO semanticModelDTO
	) {
		this.oldToNewIdLookup = new HashMap<>(oldToNewIdLookup);
		this.semanticModelDTO = semanticModelDTO;
	}

	@NotNull
	@JsonProperty(ID_MAPPING_PROPERTY)
	public Map<String, String> getOldToNewIdLookup() {
		return Collections.unmodifiableMap(oldToNewIdLookup);
	}

	@NotNull
	@JsonProperty(SEMANTIC_MODEL_PROPERTY)
	public SemanticModelDTO getSemanticModelDTO() {
		return semanticModelDTO;
	}
}
