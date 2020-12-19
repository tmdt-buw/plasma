package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class ForceConceptProvisionDTO implements Serializable {

	private static final long serialVersionUID = 5019832082262324165L;

	private static final String CONCEPT_REPLACEMENTS_PROPERTY = "conceptReplacements";

	private final List<ConceptReplacementDTO> conceptReplacements;

	@JsonCreator
	public ForceConceptProvisionDTO(
			@JsonProperty(CONCEPT_REPLACEMENTS_PROPERTY) List<ConceptReplacementDTO> conceptReplacements
	) {
		this.conceptReplacements = conceptReplacements;
	}

	@JsonProperty(CONCEPT_REPLACEMENTS_PROPERTY)
	public List<ConceptReplacementDTO> getConceptReplacements() {
		return conceptReplacements;
	}
}
