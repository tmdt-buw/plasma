package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class EntityRuleDTO implements Serializable {

	private static final long serialVersionUID = 3833724719264857498L;
	private static final String ORIGINAL_ATTRIBUTE_PROPERTY = "originalAttribute";
	private static final String CONCEPT_NAME_PROPERTY = "conceptName";
	private static final String CONCEPT_DESCRIPTION_PROPERTY = "conceptDescription";
	private static final String REQUIRED_PREDECESSOR_PROPERTY = "requiredPredecessor";

	private final String originalAttribute;
	private final String conceptName;
	private final String conceptDescription;
	private final String requiredPredecessor;

	@JsonCreator
	public EntityRuleDTO(
			@Nullable @JsonProperty(ORIGINAL_ATTRIBUTE_PROPERTY) String originalAttribute,
			@NotNull @JsonProperty(CONCEPT_NAME_PROPERTY) String conceptName,
			@NotNull @JsonProperty(CONCEPT_DESCRIPTION_PROPERTY) String conceptDescription,
			@Nullable @JsonProperty(REQUIRED_PREDECESSOR_PROPERTY) String requiredPredecessor) {
				this.originalAttribute = originalAttribute;
				this.conceptName = conceptName;
				this.conceptDescription = conceptDescription;
				this.requiredPredecessor = requiredPredecessor;
	}

	@Nullable
	@JsonProperty(ORIGINAL_ATTRIBUTE_PROPERTY)
	public String getOriginalAttribute() {
		return originalAttribute;
	}

	@NotNull
	@JsonProperty(CONCEPT_NAME_PROPERTY)
	public String getConceptName() {
		return conceptName;
	}

	@NotNull
	@JsonProperty(CONCEPT_DESCRIPTION_PROPERTY)
	public String getConceptDescription() {
		return conceptDescription;
	}

	@Nullable
	@JsonProperty(REQUIRED_PREDECESSOR_PROPERTY)
	public String getRequiredPredecessor() {
		return requiredPredecessor;
	}
}
