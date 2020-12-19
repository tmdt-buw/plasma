package de.buw.tmdt.plasma.services.dms.core.converter;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.*;
import de.buw.tmdt.plasma.services.dms.core.repository.RelationConceptPropertyRepository;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.collections.MapUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class KnowledgeGraphServiceDTO2ModelConverter {

    private final RelationConceptPropertyRepository relationConceptPropertyRepository;

    @Autowired
    public KnowledgeGraphServiceDTO2ModelConverter(RelationConceptPropertyRepository relationConceptPropertyRepository) {
        this.relationConceptPropertyRepository = relationConceptPropertyRepository;
    }

    @NotNull
    public SemanticModelDTO convert(@NotNull SemanticModel semanticModel, @NotNull String dataSourceId) {
        return this.convert(semanticModel, new ConversionContext(), dataSourceId);
    }

    @NotNull
    public SemanticModelDTO convert(@NotNull SemanticModel semanticModel, @NotNull ConversionContext conversionContext, @NotNull String dataSourceId) {
        String semanticModelId = semanticModel.getUuid() != null ? semanticModel.getUuid() : UUID.randomUUID().toString();
        conversionContext.middlewareToKgsIdLookup.put(semanticModel.getId(), semanticModelId);

        final ArrayList<@NotNull EntityTypeDTO> entityTypes = CollectionUtilities.map(
                semanticModel.getEntityTypes(),
                et -> this.convert(et, conversionContext),
                ArrayList::new
        );
        final ArrayList<@NotNull RelationDTO> relations = CollectionUtilities.map(
                semanticModel.getRelations(),
                r -> this.convert(r, conversionContext),
                ArrayList::new
        );

        return new SemanticModelDTO(
                semanticModelId,
                semanticModel.getLabel(),
                semanticModel.getDescription(),
                entityTypes,
                relations,
                dataSourceId
        );
    }

    @NotNull
    public EntityTypeDTO convert(@NotNull EntityType entityType) {
        return this.convert(entityType, new ConversionContext());
    }

    @NotNull
    protected EntityTypeDTO convert(@NotNull EntityType entityType, @NotNull ConversionContext conversionContext) {
        if (conversionContext.entityTypes.containsKey(entityType.getId())) {
            return conversionContext.entityTypes.get(entityType.getId());
        }

        final String entityTypeId = convertId(entityType.getUuid(), entityType.getId(), conversionContext);
        final EntityConceptDTO entityConceptDTO = convert(entityType.getEntityConcept(), conversionContext);

        final EntityTypeDTO entityTypeDTO = new EntityTypeDTO(
                entityTypeId,
                entityType.getLabel(),
                entityType.getOriginalLabel(),
                entityConceptDTO,
                entityType.getPosition().getXCoordinate(),
                entityType.getPosition().getYCoordinate(),
                entityType.getDescription(),
                conversionContext.entityTypesWithData.contains(entityType.getId())
        );
        conversionContext.entityTypes.put(entityType.getId(), entityTypeDTO);

        return entityTypeDTO;
    }

    @NotNull
    public EntityConceptDTO convert(@NotNull EntityConcept entityConcept) {
        return this.convert(entityConcept, new ConversionContext());
    }

    @NotNull
    protected EntityConceptDTO convert(@NotNull EntityConcept entityConcept, @NotNull ConversionContext conversionContext) {
        if (conversionContext.entityConcepts.containsKey(entityConcept.getId())) {
            return conversionContext.entityConcepts.get(entityConcept.getId());
        }

        final String entityConceptId = convertId(entityConcept.getUuid(), entityConcept.getId(), conversionContext);

        final EntityConceptDTO entityConceptDTO = new EntityConceptDTO(
                entityConceptId,
                entityConcept.getName(),
                entityConcept.getDescription(),
                entityConcept.getSourceURI()
        );
        conversionContext.entityConcepts.put(entityConcept.getId(), entityConceptDTO);

        return entityConceptDTO;
    }

    @NotNull
    protected RelationDTO convert(@NotNull Relation relation, @NotNull ConversionContext conversionContext) {
        if (conversionContext.relations.containsKey(relation.getId())) {
            return conversionContext.relations.get(relation.getId());
        }

        final String relationId = convertId(relation.getUuid(), relation.getId(), conversionContext);

        final String fromId;
        if (conversionContext.middlewareToKgsIdLookup.containsKey(relation.getFrom().getId())) {
            fromId = conversionContext.middlewareToKgsIdLookup.get(relation.getFrom().getId());
        } else {
            assert relation.getFrom().getUuid() != null : "Entity Type is expected to have either an id or uuid: " + relation.getFrom();
            fromId = String.valueOf(relation.getFrom().getUuid());
        }
        final String toId;
        if (conversionContext.middlewareToKgsIdLookup.containsKey(relation.getTo().getId())) {
            toId = conversionContext.middlewareToKgsIdLookup.get(relation.getTo().getId());
        } else {
            assert relation.getTo().getUuid() != null : "Entity Type is expected to have either an id or uuid: " + relation.getTo();
            toId = String.valueOf(relation.getTo().getUuid());
        }

        final RelationConceptDTO relationConceptDTO = convert(relation.getRelationConcept(), conversionContext);

        final RelationDTO relationDTO = new RelationDTO(
                relationId,
                fromId,
                toId,
                relationConceptDTO
        );
        conversionContext.relations.put(relation.getId(), relationDTO);

        return relationDTO;
    }

    @NotNull
    protected RelationConceptDTO convert(@NotNull RelationConcept relationConcept, @NotNull ConversionContext conversionContext) {
        if (conversionContext.relationConcepts.containsKey(relationConcept.getId())) {
            return conversionContext.relationConcepts.get(relationConcept.getId());
        }

        final String relationConceptId = convertId(relationConcept.getUuid(), relationConcept.getId(), conversionContext);

        final RelationConceptDTO relationConceptDTO = new RelationConceptDTO(
                relationConceptId,
                relationConcept.getName(),
                relationConcept.getDescription(),
                relationConcept.getSourceURI(),
                CollectionUtilities.map(relationConcept.getProperties(), RelationConcept.Property::getName, HashSet::new)
        );
        conversionContext.relationConcepts.put(relationConcept.getId(), relationConceptDTO);

        return relationConceptDTO;
    }

    @NotNull
    private String convertId(@Nullable String uuid, @NotNull Long databaseId, @NotNull ConversionContext conversionContext) {
        final String result;
        if (uuid == null) {
            result = UUID.randomUUID().toString();
            conversionContext.middlewareToKgsIdLookup.put(databaseId, result);
        } else {
            result = uuid;
        }
        return result;
    }

    @NotNull
    public SemanticModel convert(@NotNull SemanticModelDTO semanticModelDTO) {
        final List<@NotNull EntityType> entityTypes = CollectionUtilities.map(semanticModelDTO.getNodes(), this::convert, ArrayList::new);
        final Map<String, @NotNull EntityType> map = MapUtilities.map(entityTypes, et -> String.valueOf(et.getUuid()));

        return new SemanticModel(
                semanticModelDTO.getId(),
                semanticModelDTO.getLabel(),
                semanticModelDTO.getDescription(),
                entityTypes,
                CollectionUtilities.map(semanticModelDTO.getEdges(), (RelationDTO r) -> this.convert(r, map), ArrayList::new),
                null
        );
    }

    @NotNull
    public EntityType convert(@NotNull EntityTypeDTO entityTypeDTO) {
        String description = entityTypeDTO.getDescription();
        return new EntityType(
                entityTypeDTO.getId(),
                entityTypeDTO.getLabel(),
                entityTypeDTO.getOriginalLabel(),
                description != null ? description : "",
                convert(entityTypeDTO.getConcept()),
                new Position(entityTypeDTO.getXCoordinate(), entityTypeDTO.getYCoordinate())
        );
    }

    @NotNull
    public EntityConcept convert(@NotNull EntityConceptDTO entityConceptDTO) {
        return new EntityConcept(
                entityConceptDTO.getId(),
                entityConceptDTO.getMainLabel(),
                entityConceptDTO.getDescription(),
                entityConceptDTO.getSourceURI(),
                null
        );
    }

    @NotNull
    protected Relation convert(@NotNull RelationDTO relationDTO, @NotNull Map<String, EntityType> entityTypeLookup) {
        return new Relation(
                relationDTO.getId(),
                entityTypeLookup.get(relationDTO.getFromId()),
                entityTypeLookup.get(relationDTO.getToId()),
                convert(relationDTO.getConcept()),
                "",
                null
        );
    }

    @NotNull
    public RelationConcept convert(@NotNull RelationConceptDTO relationConceptDTO) {
        return new RelationConcept(
                relationConceptDTO.getId(),
                relationConceptDTO.getLabel(),
                relationConceptDTO.getDescription(),
                relationConceptDTO.getSourceURI(),
                new HashSet<>(relationConceptPropertyRepository.findAllByNameIn(relationConceptDTO.getProperties())),
                null
        );
    }

    public static final class ConversionContext {
        private Map<Long, String> middlewareToKgsIdLookup = new HashMap<>();
        private Map<Long, EntityTypeDTO> entityTypes = new HashMap<>();
        private Map<Long, EntityConceptDTO> entityConcepts = new HashMap<>();
        private Map<Long, RelationDTO> relations = new HashMap<>();
        private Map<Long, RelationConceptDTO> relationConcepts = new HashMap<>();
        private Set<Long> entityTypesWithData;

        public ConversionContext() {
            this.entityTypesWithData = new HashSet<>();
        }

        public ConversionContext(@NotNull Set<Long> entityTypesWithData) {
            this.entityTypesWithData = new HashSet<>(entityTypesWithData);
        }

        public Map<Long, String> getMiddlewareToKgsIdLookup() {
            return Collections.unmodifiableMap(middlewareToKgsIdLookup);
        }
    }
}
