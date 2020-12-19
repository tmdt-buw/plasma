package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityConceptDTO;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class EntityConceptSuggestionDTO implements Serializable {

	private static final String WEIGHT_PROPERTY = "weight";
	private static final String ENTITY_CONCEPT_PROPERTY = "entityConcept";
	private static final long serialVersionUID = 1081418799636219498L;

	private final double weight;
	private final EntityConceptDTO entityConceptDTO;

	@JsonCreator
	public EntityConceptSuggestionDTO(
			@JsonProperty(WEIGHT_PROPERTY) double weight,
			@NotNull @JsonProperty(ENTITY_CONCEPT_PROPERTY) EntityConceptDTO entityConceptDTO
	) {
		if (weight < 0) {
			throw new IllegalArgumentException("Weight mus be greater or equal zero but was " + weight);
		}
		this.weight = weight;
		this.entityConceptDTO = entityConceptDTO;
	}

	@JsonProperty(WEIGHT_PROPERTY)
	public double getWeight() {
		return weight;
	}

	@JsonProperty(ENTITY_CONCEPT_PROPERTY)
	@NotNull
	public EntityConceptDTO getEntityConceptDTO() {
		return entityConceptDTO;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EntityConceptSuggestionDTO\""
		       + ", \"weight\":\"" + weight + '"'
		       + ", \"entityConceptDTO\":" + entityConceptDTO
		       + '}';
	}
}
