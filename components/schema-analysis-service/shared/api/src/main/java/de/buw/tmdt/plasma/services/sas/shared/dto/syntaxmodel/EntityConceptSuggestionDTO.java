package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityConceptDTO;

import java.io.Serializable;

public class EntityConceptSuggestionDTO implements Serializable {
	private static final long serialVersionUID = 1756023778708784565L;
	private static final String ENTITY_CONCEPT_PROPERTY = "entityConcept";
	private static final String WEIGHT_PROPERTY = "weight";

	@JsonProperty(ENTITY_CONCEPT_PROPERTY)
	private final EntityConceptDTO entityConcept;

	@JsonProperty(WEIGHT_PROPERTY)
	private final double weight;

	@JsonCreator
	public EntityConceptSuggestionDTO(@JsonProperty(ENTITY_CONCEPT_PROPERTY) EntityConceptDTO entityConcept,
                                      @JsonProperty(WEIGHT_PROPERTY) double weight) {
		this.entityConcept = entityConcept;
		this.weight = weight;
	}

	@JsonProperty(ENTITY_CONCEPT_PROPERTY)
	public EntityConceptDTO getEntityConcept() {
		return entityConcept;
	}

	@JsonProperty(WEIGHT_PROPERTY)
	public double getWeight() {
		return weight;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EntityConceptSuggestionDTO\""
		       + ", \"entityConcept\":" + entityConcept
		       + ", \"weight\":" + weight
		       + '}';
	}
}
