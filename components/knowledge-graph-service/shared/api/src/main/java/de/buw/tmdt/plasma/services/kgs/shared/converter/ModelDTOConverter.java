package de.buw.tmdt.plasma.services.kgs.shared.converter;

import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptRelationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.kgs.shared.model.Position;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.Relation;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ModelDTOConverter implements Serializable {

    private static final long serialVersionUID = 7628881165346207837L;

    @NotNull
    public EntityConceptDTO mapEntityConcept(@NotNull EntityConcept entityConcept) {
        return new EntityConceptDTO(
                entityConcept.getId(),
                entityConcept.getMainLabel(),
                entityConcept.getDescription(),
                entityConcept.getSourceURI()
        );
    }

    @NotNull
    public RelationConceptDTO mapRelationConcept(@NotNull RelationConcept relationConcept) {
        return new RelationConceptDTO(
                relationConcept.getId(),
                relationConcept.getLabel(),
                relationConcept.getDescription(),
                relationConcept.getSourceURI(),
                relationConcept.getProperties().stream()
                        .map(RelationConcept.Property::getClassName)
                        .collect(Collectors.toSet())
        );
    }

    @NotNull
    public EntityConceptRelationDTO mapEntityConceptRelation(@NotNull EntityConceptRelation entityConceptRelation) {
        return new EntityConceptRelationDTO(
                entityConceptRelation.getId(),
                entityConceptRelation.getRelationConcept().getId(),
                entityConceptRelation.getTailNode().getId(),
                entityConceptRelation.getHeadNode().getId()
        );
    }

    /**
     * Flattens {@code semanticModel} into serializable semanticModelDTO. All EntityConcepts, EntityTypes, RelationConcepts and Relations
     * that are referenced by {@code semanticModel} are resolved by invoking, {@code resolveEntityConcept(..)}, {@code resolveEntityType(..)}, {@code
     * resolveRelationConcept(..)} and {@code resolveRelation(..)} respectively.
     *
     * @param semanticModel semanticModel vertex
     * @return resolved/flattened semanticModelDTO
     */
    @NotNull
    public SemanticModelDTO mapSemanticModel(@NotNull SemanticModel semanticModel) {
        //remap EntityTypes
        List<EntityTypeDTO> entityTypeDTOs = semanticModel.getEntityTypes().stream().map(this::mapEntityType).collect(Collectors.toList());

        //remap Relations
        List<RelationDTO> relationDTOs = semanticModel.getRelations().stream().map(this::mapRelation).collect(Collectors.toList());

        //create response resolved DTOs
        return new SemanticModelDTO(
                semanticModel.getId(),
                semanticModel.getLabel(),
                semanticModel.getDescription(),
                entityTypeDTOs,
                relationDTOs,
                semanticModel.getDataSourceId()
        );
    }

    @NotNull
    public EntityTypeDTO mapEntityType(@NotNull EntityType entityType) {
        return new EntityTypeDTO(
                entityType.getId(),
                entityType.getLabel(),
                entityType.getOriginalLabel(),
                mapEntityConcept(entityType.getEntityConcept()),
                entityType.getPosition().getX(),
                entityType.getPosition().getY(),
                entityType.getDescription(),
                entityType.isMappedToData()
        );
    }

    @NotNull
    public RelationDTO mapRelation(@NotNull Relation relation) {
        return new RelationDTO(
                relation.getId(),
                relation.getTailNode().getId(),
                relation.getHeadNode().getId(),
                mapRelationConcept(relation.getRelationConcept())
        );
    }

    @NotNull
    public EntityConcept mapEntityConceptDTO(@NotNull EntityConceptDTO entityConceptDTO) {
        return new EntityConcept(
                entityConceptDTO.getId(),
                entityConceptDTO.getMainLabel(),
                entityConceptDTO.getDescription(),
                entityConceptDTO.getSourceURI()
        );
    }

    @NotNull
    public RelationConcept mapRelationConceptDTO(@NotNull RelationConceptDTO relationConceptDTO) {
        return new RelationConcept(
                relationConceptDTO.getId(),
                relationConceptDTO.getLabel(),
                relationConceptDTO.getDescription(),
                relationConceptDTO.getSourceURI(),
                RelationConcept.Property.byClassNames(relationConceptDTO.getProperties())
        );
    }

    @NotNull
    public EntityType mapEntityTypeDTO(@NotNull EntityTypeDTO entityTypeDTO) {
        return new EntityType(
                entityTypeDTO.getId(),
                entityTypeDTO.getLabel(),
                entityTypeDTO.getOriginalLabel(),
                mapEntityConceptDTO(entityTypeDTO.getConcept()),
                new Position(entityTypeDTO.getXCoordinate(), entityTypeDTO.getYCoordinate()),
                entityTypeDTO.getDescription(),
                entityTypeDTO.isMappedToData()
        );
    }

    @NotNull
    public Relation mapRelationDTO(
            @NotNull RelationDTO relationDTO,
            @NotNull EntityType tail,
            @NotNull EntityType head
    ) {
        return new Relation(relationDTO.getId(), tail, head, mapRelationConceptDTO(relationDTO.getConcept()));
    }

    /**
     * Converts a SemanticModelDTO into a matching SemanticModel object.
     *
     * @param semanticModelDTO the SemanticModelDTO to convert.
     * @return a new SemanticModel object matching all information in the given DTO.
     */
    @NotNull
    public SemanticModel mapSemanticModelDTO(@NotNull SemanticModelDTO semanticModelDTO) {
        // map of entityTypes from passed dto's
        Map<String, EntityType> entityTypes = semanticModelDTO.getNodes().stream()
                .map(this::mapEntityTypeDTO)
                .collect(Collectors.toMap(EntityType::getId, Function.identity()));

        // list of the relations from passed dto's
        Map<String, Relation> relations = semanticModelDTO.getEdges().stream()
                .map(relationDto -> mapRelationDTO(
                        relationDto,
                        getOrThrow(relationDto.getFromId(), entityTypes),
                        getOrThrow(relationDto.getToId(), entityTypes)
                ))
                .collect(Collectors.toMap(
                        Relation::getId,
                        Function.identity()
                ));

        //Create the new semantic model
        return new SemanticModel(
                semanticModelDTO.getId(),
                semanticModelDTO.getLabel(),
                semanticModelDTO.getDescription(),
                entityTypes.values(),
                relations.values(),
                semanticModelDTO.getDataSourceId()
        );
    }

    @NotNull
    private EntityType getOrThrow(@NotNull String id, @NotNull Map<String, EntityType> cache) {
        EntityType result = cache.get(id);
        if (result == null) {
            throw new RuntimeException("Could not find entityType with id `" + id + "`.");
        }
        return result;
    }
}