package de.buw.tmdt.plasma.services.sas.core.converter;

import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityConcept;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.Relation;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.RelationConcept;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.SemanticModel;
import de.buw.tmdt.plasma.utilities.collections.Collectors;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.RelationConceptDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.SemanticModelDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SemanticModelDTOConverter {

    public SemanticModelDTO toDTO(@NotNull SemanticModel semanticModel) {

        List<EntityTypeDTO> entityTypeDTOS = semanticModel.getEntityTypes().stream().map(this::toDTO).collect(java.util.stream.Collectors.toList());

        List<RelationDTO> relations = semanticModel.getRelations().stream()
                .map(this::toDTO)
                .collect(Collectors.getSerializableListCollector());

        return new SemanticModelDTO(
                semanticModel.getId(),
                semanticModel.getUuid(),
                semanticModel.getLabel(),
                semanticModel.getDescription(),
                entityTypeDTOS,
                relations
        );
    }

    private EntityTypeDTO toDTO(@NotNull EntityType entityType) {
        Double xCoordinate = null;
        Double yCoordinate = null;
        if (entityType.getPosition() != null) {
            xCoordinate = entityType.getPosition().getXCoordinate();
            yCoordinate = entityType.getPosition().getYCoordinate();
        }

        EntityConceptDTO entityConceptDTO = toDTO(entityType.getEntityConcept());

        return new EntityTypeDTO(
                xCoordinate,
                yCoordinate,
                entityType.getId(),
                entityType.getUuid() == null ? null : UUID.fromString(entityType.getUuid()),
                entityType.getLabel(),
                entityType.getOriginalLabel(),
                entityType.getDescription(),
                entityConceptDTO,
                null
        );
    }

    private EntityConceptDTO toDTO(EntityConcept entityConcept) {
        return new EntityConceptDTO(entityConcept.getId(), entityConcept.getUuid(), entityConcept.getName(), entityConcept.getDescription(), entityConcept.getSourceURI());
    }

    private RelationDTO toDTO(Relation relation) {
        return new RelationDTO(
                relation.getId(),
                relation.getFrom().getId(),
                relation.getTo().getId(),
                relation.getDescription(),
                toDTO(relation.getRelationConcept())
        );
    }

    private RelationConceptDTO toDTO(@NotNull RelationConcept relationConcept) {
        return new RelationConceptDTO(
                relationConcept.getId().toString(),
                relationConcept.getUuid(),
                relationConcept.getName(),
                relationConcept.getDescription(),
                relationConcept.getSourceURI(),
                relationConcept.getProperties().stream().map(RelationConcept.Property::getName).collect(Collectors.getSerializableSetCollector())
        );
    }

}
