package de.buw.tmdt.plasma.services.sas.core.converter;

import de.buw.tmdt.plasma.services.sas.core.model.EntityRule;
import de.buw.tmdt.plasma.services.sas.core.model.RelationRule;
import de.buw.tmdt.plasma.services.sas.shared.dto.EntityRuleDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.RelationRuleDTO;
import org.springframework.stereotype.Service;

@Service
public class RuleDTOConverter {

	public EntityRule fromDTO(EntityRuleDTO entityRuleDTO) {
		return new EntityRule(
				"".equals(entityRuleDTO.getOriginalAttribute()) ? null : entityRuleDTO.getOriginalAttribute(),
				entityRuleDTO.getConceptName(),
				entityRuleDTO.getConceptDescription(),
				entityRuleDTO.getRequiredPredecessor()
		);
	}

	public RelationRule fromDTO(RelationRuleDTO relationRuleDTO) {
		return new RelationRule(
				relationRuleDTO.getFromConcept(),
				relationRuleDTO.getToConcept(),
				relationRuleDTO.getConceptName(),
				relationRuleDTO.getConceptDescription()
		);
	}
	

}
