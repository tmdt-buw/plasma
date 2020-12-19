package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityConceptDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ConceptReplacementDTO implements Serializable {

	private static final long serialVersionUID = 8111566266289676218L;
	private static final String ORIGINAL_ATTRIBUTE_NAME_PROPERTY = "originalAttributeName";
	private static final String ENTITY_CONCEPT_DTO_PROPERTY = "entityConcept";

	private final String originalAttributeName;
	private final EntityConceptDTO entityConceptDTO;

	@JsonCreator
	public ConceptReplacementDTO(
			@Nullable @JsonProperty(ORIGINAL_ATTRIBUTE_NAME_PROPERTY) String originalAttributeName,
			@NotNull @JsonProperty(ENTITY_CONCEPT_DTO_PROPERTY) EntityConceptDTO entityConceptDTO
	) {
		this.originalAttributeName = originalAttributeName;
		this.entityConceptDTO = entityConceptDTO;
	}

	@Nullable
	@JsonProperty(ORIGINAL_ATTRIBUTE_NAME_PROPERTY)
	public String getOriginalAttributeName() {
		return originalAttributeName;
	}

	@NotNull
	@JsonProperty(ENTITY_CONCEPT_DTO_PROPERTY)
	public EntityConceptDTO getEntityConceptDTO() {
		return entityConceptDTO;
	}
}
