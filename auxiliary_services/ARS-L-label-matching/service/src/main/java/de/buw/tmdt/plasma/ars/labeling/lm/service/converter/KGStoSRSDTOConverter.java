package de.buw.tmdt.plasma.ars.labeling.lm.service.converter;


import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityConceptDTO;

import java.util.Collection;
import java.util.stream.Collectors;

public class KGStoSRSDTOConverter {

    public static EntityConceptDTO toDMS(de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO kgs) {
        return new EntityConceptDTO(0L, kgs.getId(), kgs.getMainLabel(), kgs.getDescription(), kgs.getSourceURI());
    }

    public static Collection<EntityConceptDTO> toDMS(Collection<de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO> kgs) {
        return kgs.stream().map(KGStoSRSDTOConverter::toDMS).collect(Collectors.toSet());
    }
}
