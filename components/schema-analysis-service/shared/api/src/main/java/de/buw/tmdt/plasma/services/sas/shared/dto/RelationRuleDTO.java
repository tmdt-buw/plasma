package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class RelationRuleDTO implements Serializable {

	private static final long serialVersionUID = -3313200983207222361L;
	private static final String CONCEPT_NAME_PROPERTY = "conceptName";
	private static final String CONCEPT_DESCRIPTION_PROPERTY = "conceptDescription";
	private static final String FROM_CONCEPT_PROPERTY = "fromConcept";
	private static final String TO_CONCEPT_PROPERTY = "toConcept";

	private final String conceptName;
	private final String fromConcept;
	private final String toConcept;
	private final String conceptDescription;

	@JsonCreator
	public RelationRuleDTO(
			@Nullable @JsonProperty(CONCEPT_NAME_PROPERTY) String conceptName,
			@NotNull @JsonProperty(CONCEPT_DESCRIPTION_PROPERTY) String conceptDescription,
			@NotNull @JsonProperty(FROM_CONCEPT_PROPERTY) String fromConcept,
			@NotNull @JsonProperty(TO_CONCEPT_PROPERTY) String toConcept) {
		this.conceptName = conceptName;
		this.conceptDescription = conceptDescription;
		this.fromConcept = fromConcept;
		this.toConcept = toConcept;
	}

	@NotNull
	@JsonProperty(CONCEPT_NAME_PROPERTY)
	public String getConceptName() {
		return conceptName;
	}

	@NotNull
	@JsonProperty(FROM_CONCEPT_PROPERTY)
	public String getFromConcept() {
		return fromConcept;
	}

	@NotNull
	@JsonProperty(TO_CONCEPT_PROPERTY)
	public String getToConcept() {
		return toConcept;
	}

	@NotNull
	@JsonProperty(CONCEPT_DESCRIPTION_PROPERTY)
	public String getConceptDescription() {
		return conceptDescription;
	}
}
