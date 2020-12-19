package de.buw.tmdt.plasma.services.dms.core.handler;

import de.buw.tmdt.plasma.services.dms.core.converter.*;
import de.buw.tmdt.plasma.services.dms.core.model.ModelBase;
import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.TraversableModelBase;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceModel;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.*;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.core.operations.impl.SetEntityType;
import de.buw.tmdt.plasma.services.dms.core.repository.*;
import de.buw.tmdt.plasma.services.dms.core.services.DataSourceModelInitService;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.recommendation.DeltaRecommendationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.SchemaNodeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelCreationResponse;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.kgs.shared.feignclient.LocalKnowledgeApiClient;
import de.buw.tmdt.plasma.services.kgs.shared.feignclient.UniversalKnowledgeApiClient;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.NodeDTO;
import de.buw.tmdt.plasma.services.srs.feignclient.SemanticRecommendationApiClient;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.collections.MapUtilities;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DataModelingHandler {

    private static final Logger logger = LoggerFactory.getLogger(DataModelingHandler.class);

    private final LocalKnowledgeApiClient localKnowledgeApiClient;
    private final UniversalKnowledgeApiClient universalKnowledgeApiClient;
    private final SchemaOperationDTOConverter schemaOperationDTOConverter;
    private final SemanticModelDTOConverter semanticModelDTOConverter;
    private final KnowledgeGraphServiceDTO2ModelConverter knowledgeGraphServiceDTO2ModelConverter;
    private final KnowledgeGraphServiceDTO2DTOConverter knowledgeGraphServiceDTO2DTOConverter;
    private final DataSourceRepository dataSourceRepository;
    private final SchemaNodeRepository schemaNodeRepository;
    private final EntityConceptRepository entityConceptRepository;
    private final EntityTypeRepository entityTypeRepository;
    private final RelationConceptRepository relationConceptRepository;
    private final DataSourceModelInitService dataSourceModelInitService;
    private final SemanticRecommendationApiClient semanticRecommendationApiClient;
    private final DataSourceSchemaDTOConverter dataSourceSchemaDTOConverter;
    private final SchemaAnalysisDTOConverter schemaAnalysisDTOConverter;

    @Autowired
    public DataModelingHandler(
            @NotNull LocalKnowledgeApiClient localKnowledgeApiClient,
            @NotNull UniversalKnowledgeApiClient universalKnowledgeApiClient,
            @NotNull SchemaOperationDTOConverter schemaOperationDTOConverter,
            @NotNull SemanticModelDTOConverter semanticModelDTOConverter,
            @NotNull KnowledgeGraphServiceDTO2ModelConverter knowledgeGraphServiceDTO2ModelConverter,
            @NotNull KnowledgeGraphServiceDTO2DTOConverter knowledgeGraphServiceDTO2DTOConverter,
            @NotNull DataSourceRepository dataSourceRepository,
            @NotNull SchemaNodeRepository schemaNodeRepository,
            @NotNull EntityConceptRepository entityConceptRepository,
            @NotNull EntityTypeRepository entityTypeRepository,
            @NotNull RelationConceptRepository relationConceptRepository,
            @NotNull DataSourceModelInitService dataSourceModelInitService,
            SemanticRecommendationApiClient semanticRecommendationApiClient, DataSourceSchemaDTOConverter dataSourceSchemaDTOConverter, SchemaAnalysisDTOConverter schemaAnalysisDTOConverter) {
        this.localKnowledgeApiClient = localKnowledgeApiClient;
        this.universalKnowledgeApiClient = universalKnowledgeApiClient;
        this.schemaOperationDTOConverter = schemaOperationDTOConverter;
        this.semanticModelDTOConverter = semanticModelDTOConverter;
        this.knowledgeGraphServiceDTO2ModelConverter = knowledgeGraphServiceDTO2ModelConverter;
        this.knowledgeGraphServiceDTO2DTOConverter = knowledgeGraphServiceDTO2DTOConverter;
        this.dataSourceRepository = dataSourceRepository;
        this.schemaNodeRepository = schemaNodeRepository;
        this.entityConceptRepository = entityConceptRepository;
        this.entityTypeRepository = entityTypeRepository;
        this.relationConceptRepository = relationConceptRepository;
        this.dataSourceModelInitService = dataSourceModelInitService;
        this.semanticRecommendationApiClient = semanticRecommendationApiClient;
        this.dataSourceSchemaDTOConverter = dataSourceSchemaDTOConverter;
        this.schemaAnalysisDTOConverter = schemaAnalysisDTOConverter;
    }

    public void initDataSourceSchema(@NotNull UUID uuid, int exampleLimit) {
        Optional<DataSourceModel> dataSourceResult = dataSourceRepository.findById(uuid);
        DataSourceModel dataSourceModel;
        if (dataSourceResult.isPresent()) {
            logger.info("Data source already exists - the current schema is replaced with the last existing one in the SAS");
            dataSourceModel = dataSourceResult.get();
            //dataSourceModel.setDataSourceSchemaStack(Collections.emptyList());
        } else {
            logger.info("Data source does not exist");
            DataSourceModel dsm = new DataSourceModel(uuid);
            dataSourceModel = dataSourceRepository.saveAndFlush(dsm);
            logger.info("Created a new data source");
        }
        dataSourceModel.pushDataSourceSchema(dataSourceModelInitService.createInitialSchema(uuid, exampleLimit, dataSourceModel));

        //store the schema
        dataSourceRepository.save(dataSourceModel);
    }

    @NotNull
    public DataSourceSchema getDataSourceSchema(@NotNull UUID uuid) {
        return peekDataSourceSchema(findDataSourceOrThrow(uuid), false);
    }

    @NotNull
    public List<EntityConceptDTO> getEntityConcepts(@NotNull UUID uuid, @NotNull String prefix) {
        //Retrieve the concepts from the backend
        ArrayList<EntityConceptDTO> backendEntityConceptDTOs;
        try {
            backendEntityConceptDTOs = CollectionUtilities.map(
                    universalKnowledgeApiClient.getEntityConcepts(prefix),
                    knowledgeGraphServiceDTO2DTOConverter::convert,
                    ArrayList::new
            );
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach KnowledgeGraphService");
        }

        //Retrieve the concepts that were created within this semantic model
        DataSourceSchema schema = peekDataSourceSchema(findDataSourceOrThrow(uuid), false);
        List<EntityConceptDTO> middleWareEntityConceptDTOs = schema.getEntityConceptCache().stream()
                .filter(entityConcept -> entityConcept.getName().startsWith(prefix))
                .map(semanticModelDTOConverter::toDTO)
                .collect(Collectors.toList());

        List<String> cachedEntityConcepts = middleWareEntityConceptDTOs.stream()
                .filter(entityConceptDTO -> entityConceptDTO.getUuid() != null)
                .map(EntityConceptDTO::getUuid)
                .collect(Collectors.toList());

        //copy middle ware entity concepts and replace ambiguous elements
        backendEntityConceptDTOs.stream()
                .filter(entityConceptDTO -> !cachedEntityConcepts.contains(entityConceptDTO.getUuid()))
                .forEach(middleWareEntityConceptDTOs::add);

        return middleWareEntityConceptDTOs.stream()
                .sorted(Comparator.comparing(EntityConceptDTO::getName))
                .collect(Collectors.toList());
    }

    @NotNull
    public List<RelationConceptDTO> getRelationConcepts(@NotNull UUID uuid, @NotNull String prefix) {
        //Retrieve the concepts from the backend
        ArrayList<RelationConceptDTO> backendRelationConceptDTOs;
        try {
            backendRelationConceptDTOs = CollectionUtilities.map(
                    universalKnowledgeApiClient.getRelationConcepts(prefix),
                    knowledgeGraphServiceDTO2DTOConverter::convert,
                    ArrayList::new
            );
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach KnowledgeGraphService");
        }

        DataSourceSchema schema = peekDataSourceSchema(findDataSourceOrThrow(uuid), false);
        List<RelationConceptDTO> middleWareEntityConceptDTOs = schema.getRelationConceptCache().stream()
                .filter(relationConcept -> relationConcept.getName().startsWith(prefix))
                .map(semanticModelDTOConverter::toDTO)
                .collect(Collectors.toList());

        List<String> cachedRelationConcepts = middleWareEntityConceptDTOs.stream()
                .filter(entityConceptDTO -> entityConceptDTO.getUuid() != null)
                .map(RelationConceptDTO::getUuid)
                .collect(Collectors.toList());

        //copy middle ware entity concepts and replace ambiguous elements
        backendRelationConceptDTOs.stream()
                .filter(entityConceptDTO -> !cachedRelationConcepts.contains(entityConceptDTO.getUuid()))
                .forEach(middleWareEntityConceptDTOs::add);

        return middleWareEntityConceptDTOs.stream()
                .sorted(Comparator.comparing(RelationConceptDTO::getName))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePositions(@NotNull UUID uuid, @NotNull DataSourceSchemaDTO dataSourceSchemaDTO) {
        //load referenced data source
        DataSourceModel dataSourceModel = findDataSourceOrThrow(uuid);
        DataSourceSchema loadedSchema = peekDataSourceSchema(dataSourceModel, true);

        final Map<Long, EntityType> entityTypeLookUp = MapUtilities.map(
                loadedSchema.getSemanticModel().getEntityTypes(),
                TraversableModelBase::getId
        );

        List<EntityTypeDTO> nodes = dataSourceSchemaDTO.getSemanticModelDTO().getNodes();
        for (EntityTypeDTO entityTypeDTO : nodes) {
            final Double xCoordinate = entityTypeDTO.getXCoordinate();
            final Double yCoordinate = entityTypeDTO.getYCoordinate();
            if (xCoordinate != null && yCoordinate != null) {
                EntityType entityType = entityTypeLookUp.get(entityTypeDTO.getId());
                if (entityType == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passed EntityType not found in model. ");
                }
                entityType.setPosition(new Position(xCoordinate, yCoordinate));
            }
        }

        Map<UUID, Node> nodeLookUp = new HashMap<>();
        loadedSchema.getSyntaxModel().execute(n -> {
            if (n instanceof Node) {
                Node node = (Node) n;
                nodeLookUp.put(node.getUuid(), node);
            }
        });

        List<SchemaNodeDTO> nodes1 = dataSourceSchemaDTO.getSyntaxModelDTO().getNodes();
        for (SchemaNodeDTO schemaNodeDTO : nodes1) {
            final Double xCoordinate = schemaNodeDTO.getXCoordinate();
            final Double yCoordinate = schemaNodeDTO.getYCoordinate();
            if (xCoordinate != null && yCoordinate != null) {
                Node node = nodeLookUp.get(schemaNodeDTO.getUuid());
                if (node == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passed syntax model node not found in model.");
                }
                node.setPosition(new Position(xCoordinate, yCoordinate));
            }
        }

        dataSourceRepository.save(dataSourceModel);
    }

    @NotNull
    @Transactional
    public DataSourceSchema modifySchema(@NotNull UUID uuid, @NotNull SyntacticOperationDTO syntacticOperationDTO) {
        //load referenced data source
        DataSourceModel dataSourceModel = findDataSourceOrThrow(uuid);
        DataSourceSchema loadedSchema = peekDataSourceSchema(dataSourceModel, true);
        DataSourceSchema newSchema = loadedSchema.copy();

        //deserialize syntactic operation
        Operation.Handle handle = schemaOperationDTOConverter.fromDTO(syntacticOperationDTO);

        final DataSourceSchema modifiedSchema;
        try {
            modifiedSchema = handle.invoke(newSchema);
        } catch (ParameterParsingException e) {
            logger.warn("Failed to execute operation: {}", syntacticOperationDTO, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametrization for operation " + syntacticOperationDTO.getName() + " was invalid.");
        }

        //persist and return copy
        dataSourceModel.pushDataSourceSchema(modifiedSchema);
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @Transactional
    public void placeSchemaNode(@NotNull UUID dataSourceUuid, @NotNull UUID schemaNodeUuid, @NotNull SchemaNodeDTO schemaNodeDTO) {
        if (!schemaNodeUuid.equals(schemaNodeDTO.getUuid())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passed uuid in syntax model node is inconsistent to resource to update. "
                            + "Resource: " + schemaNodeUuid + " Payload: " + schemaNodeDTO.getUUIDString()
            );
        }
        if (schemaNodeDTO.getXCoordinate() == null || schemaNodeDTO.getYCoordinate() == null) {
            StringJoiner stringJoiner = new StringJoiner(" and ", "Missing ", " coordinate in schema node dto.");
            if (schemaNodeDTO.getXCoordinate() == null) {
                stringJoiner.add("X");
            }
            if (schemaNodeDTO.getYCoordinate() == null) {
                stringJoiner.add("Y");
            }
            //noinspection HardcodedFileSeparator
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    stringJoiner.toString()
            );
        }
        final DataSourceSchema loadedSchema = peekDataSourceSchema(findDataSourceOrThrow(dataSourceUuid), true);
        final Traversable referencedTraversable = loadedSchema.getSyntaxModel().find(new Traversable.Identity<>(schemaNodeUuid));
        if (referencedTraversable == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Datasource {" + dataSourceUuid + "} does not contain specified schema node {" + schemaNodeUuid + "}."
            );
        }
        if (!(referencedTraversable instanceof Node)) {
            //noinspection MagicCharacter
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Referenced node {" + schemaNodeUuid + "} was not a schema node but of type " + referencedTraversable.getClass() + '.'
            );
        }
        final Node nodeToUpdate = (Node) referencedTraversable;
        Double xCoordinate = schemaNodeDTO.getXCoordinate();
        Double yCoordinate = schemaNodeDTO.getYCoordinate();
        if (xCoordinate != null && yCoordinate != null) {
            nodeToUpdate.setPosition(new Position(xCoordinate, yCoordinate));
            schemaNodeRepository.save(nodeToUpdate);
        }
    }

    @Transactional
    public EntityConcept cacheEntityConcept(@NotNull UUID dataSourceUuid, @NotNull EntityConceptDTO entityConceptDTO) {
        //convert
        EntityConcept receivedEntityConcept = semanticModelDTOConverter.fromDTO(entityConceptDTO);

        if (receivedEntityConcept.getId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New entity concept must not have an id."
            );
        }

        //load schema - DON'T COPY since caching of transient concepts is not "undoable"
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema currentSchema = peekDataSourceSchema(dataSourceModel, true);

        //compute entity concept
        final EntityConcept usedEntityConcept = getAndUpdateCache(receivedEntityConcept, EntityConcept::getUuid, currentSchema.getEntityConceptCache());

        dataSourceRepository.save(dataSourceModel);

        if (usedEntityConcept.getId() != null) {
            return usedEntityConcept;
        } else {
            for (EntityConcept entityConcept : currentSchema.getEntityConceptCache()) {
                if (entityConcept.getName().equals(usedEntityConcept.getName()) && entityConcept.getDescription().equals(usedEntityConcept.getDescription())) {
                    return entityConcept;
                }
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cache entity concept.");
        }
    }

    @Transactional
    public void uncacheEntityConcept(@NotNull UUID dataSourceUuid, long entityConceptId) {
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema currentSchema = peekDataSourceSchema(dataSourceModel, true);

        currentSchema.getEntityConceptCache().removeIf(o -> o.getId().equals(entityConceptId));
        dataSourceRepository.save(dataSourceModel);
    }

    @Transactional
    public RelationConcept cacheRelationConcept(@NotNull UUID dataSourceUuid, @NotNull RelationConceptDTO relationConceptDTO) {
        //convert
        RelationConcept receivedRelationConcept = semanticModelDTOConverter.fromDTO(relationConceptDTO);

        if (receivedRelationConcept.getId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New relation concept must not have an id."
            );
        }

        //load schema - DON'T COPY since caching of transient concepts is not "undoable"
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema currentSchema = peekDataSourceSchema(dataSourceModel, true);

        //compute entity concept
        final RelationConcept usedRelationConcept = getAndUpdateCache(
                receivedRelationConcept,
                RelationConcept::getUuid,
                currentSchema.getRelationConceptCache()
        );

        dataSourceRepository.save(dataSourceModel);

        if (usedRelationConcept.getId() != null) {
            return usedRelationConcept;
        } else {
            for (RelationConcept relationConcept : currentSchema.getRelationConceptCache()) {
                if (relationConcept.getName().equals(usedRelationConcept.getName()) && relationConcept.getDescription().equals(usedRelationConcept
                        .getDescription())) {
                    return relationConcept;
                }
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cache entity concept.");
        }
    }

    @Transactional
    public void uncacheRelationConcept(@NotNull UUID dataSourceUuid, long relationConceptId) {
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema currentSchema = peekDataSourceSchema(dataSourceModel, true);

        currentSchema.getRelationConceptCache().removeIf(o -> o.getId().equals(relationConceptId));
        dataSourceRepository.save(dataSourceModel);
    }

    @Transactional
    @NotNull
    public DataSourceSchema addEntityType(@NotNull UUID dataSourceUuid, @NotNull EntityTypeDTO entityTypeDTO, @Nullable UUID primitiveNodeUuid) {
        //deserialize
        EntityType receivedEntityType = semanticModelDTOConverter.fromDTO(entityTypeDTO);

        if (receivedEntityType.getId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New entity type must not have an id."
            );
        }

        //load
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema newSchema = peekDataSourceSchema(dataSourceModel, true).copy();

        //compute entity concept
        final EntityConcept usedEntityConcept;
        if (receivedEntityType.getEntityConcept().getId() != null) {
            usedEntityConcept = loadFromRepository(receivedEntityType.getEntityConcept(), EntityConcept::getUuid, entityConceptRepository);
        } else {
            usedEntityConcept = getAndUpdateCache(receivedEntityType.getEntityConcept(), EntityConcept::getUuid, newSchema.getEntityConceptCache());
        }

        //update model
        receivedEntityType.setEntityConcept(usedEntityConcept);
        newSchema.getSemanticModel().getEntityTypes().add(receivedEntityType);

        //link entity type to primitive node if one [primitive node] was provided
        if (primitiveNodeUuid != null) {
            Traversable traversable = newSchema.getSyntaxModel().find(new Traversable.Identity<>(primitiveNodeUuid));
            if (!(traversable instanceof PrimitiveNode)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Referenced syntax model node was not of type `" + PrimitiveNode.class.getSimpleName()
                                + "` but was " + (traversable != null ? traversable.getClass().getSimpleName() : null)
                );
            }
            PrimitiveNode primitiveNode = (PrimitiveNode) traversable;
            final EntityType oldEntityType = primitiveNode.getEntityType();
            if (oldEntityType != null) {
                newSchema.remove(oldEntityType.getIdentity());
            }
            primitiveNode.setEntityType(receivedEntityType);
            if (primitiveNode.getDataType() == DataType.UNKNOWN) {
                // if data type is still unknown, try to determine one
                primitiveNode.setDataType(SetEntityType.determineDataType(primitiveNode.getExamples()));
            }
            receivedEntityType.setPosition(primitiveNode.getPosition());
        }
        dataSourceModel.pushDataSourceSchema(newSchema);
        //persist and return copy
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @NotNull
    @Transactional
    public DataSourceSchema updateEntityType(@NotNull UUID dataSourceUuid, @NotNull Long entityTypeResourceId, @NotNull EntityTypeDTO entityTypeDTO) {
        final EntityType retrievedEntityType = semanticModelDTOConverter.fromDTO(entityTypeDTO);
        if (!entityTypeResourceId.equals(entityTypeDTO.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passed id in entity type is inconsistent to resource to update. Resource: " + entityTypeResourceId + " Payload: " + entityTypeDTO.getId()
            );
        }

        if (retrievedEntityType.getEntityConcept().getId() == null && retrievedEntityType.getEntityConcept().getUuid() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Creating new entity concepts is not supported on updating an entity type. Either set id or uuid of referenced entity concept."
            );
        }

        //load schema copy
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        final DataSourceSchema newSchema = peekDataSourceSchema(dataSourceModel, true).copy();

        EntityConcept entityConcept = loadFromRepository(retrievedEntityType.getEntityConcept(), EntityConcept::getUuid, entityConceptRepository);
        retrievedEntityType.setEntityConcept(entityConcept);
        EntityType newEntityType = retrievedEntityType.copy();
        newSchema.replace(retrievedEntityType.getIdentity(), newEntityType);
        dataSourceModel.pushDataSourceSchema(newSchema);

        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @Transactional
    public void placeEntityType(@NotNull UUID dataSourceUuid, @NotNull Long entityTypeResourceId, @NotNull EntityTypeDTO entityTypeDTO) {
        if (!entityTypeResourceId.equals(entityTypeDTO.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passed id in entity type is inconsistent to resource to update. Resource: " + entityTypeResourceId + " Payload: " + entityTypeDTO.getId()
            );
        }
        if (entityTypeDTO.getXCoordinate() == null || entityTypeDTO.getYCoordinate() == null) {
            StringJoiner stringJoiner = new StringJoiner(" and ", "Missing ", " coordinate in entity type dto.");
            if (entityTypeDTO.getXCoordinate() == null) {
                stringJoiner.add("X");
            }
            if (entityTypeDTO.getYCoordinate() == null) {
                stringJoiner.add("Y");
            }
            //noinspection HardcodedFileSeparator
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    stringJoiner.toString()
            );
        }
        final DataSourceSchema loadedSchema = peekDataSourceSchema(findDataSourceOrThrow(dataSourceUuid), true);
        for (EntityType entityType : loadedSchema.getSemanticModel().getEntityTypes()) {
            if (entityType.getId().equals(entityTypeResourceId)) {
                Double xCoordinate = entityTypeDTO.getXCoordinate();
                Double yCoordinate = entityTypeDTO.getYCoordinate();
                if (xCoordinate != null && yCoordinate != null) {
                    entityType.setPosition(new Position(xCoordinate, yCoordinate));
                    entityTypeRepository.save(entityType);
                }
                return;
            }
        }
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Datasource {" + dataSourceUuid + "} does not contain specified entity type {" + entityTypeDTO + "}."
        );
    }

    @NotNull
    @Transactional
    public DataSourceSchema removeEntityTypeFromSchema(@NotNull UUID dataSourceUuid, @NotNull Long entityTypeResourceId) {
        //load schema copy
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        final DataSourceSchema newSchema = peekDataSourceSchema(dataSourceModel, true).copy();

        if (!newSchema.remove(new Traversable.Identity<>(entityTypeResourceId))) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove entity type. Removing would cause inconsistencies.");
        }
        dataSourceModel.pushDataSourceSchema(newSchema);
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @NotNull
    @Transactional
    public DataSourceSchema addRelation(@NotNull UUID dataSourceUuid, @NotNull RelationDTO relationDTO) {
        //deserialize
        Relation receivedRelation = semanticModelDTOConverter.fromDTO(relationDTO);

        if (receivedRelation.getId() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New relation must not have an id."
            );
        }

        //load
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema newSchema = peekDataSourceSchema(dataSourceModel, true).copy();

        //compute relation concept
        final RelationConcept usedRelationConcept;
        if (receivedRelation.getRelationConcept().getId() != null) {
            usedRelationConcept = loadFromRepository(receivedRelation.getRelationConcept(), RelationConcept::getUuid, relationConceptRepository);
        } else {
            usedRelationConcept = getAndUpdateCache(receivedRelation.getRelationConcept(), RelationConcept::getUuid, newSchema.getRelationConceptCache());
        }

        //update referenced entity types to matching ones from schema copy
        Traversable from = newSchema.find(receivedRelation.getFrom().getIdentity());
        if (from == null) {
            logger.warn(
                    "Failed to find tail entity type with id {} and identity {} in schema copy.",
                    receivedRelation.getFrom().getId(),
                    receivedRelation.getFrom().getIdentity()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id of tail entity of relation is not valid: " + receivedRelation.getFrom().getId());
        }

        Traversable to = newSchema.find(receivedRelation.getTo().getIdentity());
        if (to == null) {
            logger.warn(
                    "Failed to find head entity type with id {} and identity {} in schema copy.",
                    receivedRelation.getTo().getId(),
                    receivedRelation.getTo().getIdentity()
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id of head entity of relation is not valid: " + receivedRelation.getTo().getId());
        }

        final EntityType fromEntityType = ObjectUtilities.checkedReturn(from, EntityType.class);
        final EntityType toEntityType = ObjectUtilities.checkedReturn(to, EntityType.class);

        //update model
        receivedRelation.setRelationConcept(usedRelationConcept);
        receivedRelation.setFrom(fromEntityType);
        receivedRelation.setTo(toEntityType);
        newSchema.getSemanticModel().getRelations().add(receivedRelation);

        //persist and return copy
        dataSourceModel.pushDataSourceSchema(newSchema);
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @NotNull
    @Transactional
    public DataSourceSchema updateRelation(@NotNull UUID dataSourceUuid, @NotNull Long relationResourceId, @NotNull RelationDTO relationDTO) {
        final Relation retrievedRelation = semanticModelDTOConverter.fromDTO(relationDTO);
        if (!relationResourceId.equals(relationDTO.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passed id in relation is inconsistent to resource to update. Resource: " + relationResourceId + " Payload: " + relationDTO.getId()
            );
        }

        if (retrievedRelation.getRelationConcept().getId() == null && retrievedRelation.getRelationConcept().getUuid() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Creating new relation concepts is not supported on updating a relation. Either set id or uuid of referenced relation concept."
            );
        }

        //load schema copy
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        final DataSourceSchema newSchema = peekDataSourceSchema(dataSourceModel, true).copy();

        RelationConcept relationConcept = loadFromRepository(retrievedRelation.getRelationConcept(), RelationConcept::getUuid, relationConceptRepository);
        retrievedRelation.setRelationConcept(relationConcept);

        Relation newRelation = retrievedRelation.copy();

        newSchema.replace(retrievedRelation.getIdentity(), newRelation);

        dataSourceModel.pushDataSourceSchema(newSchema);
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @NotNull
    @Transactional
    public DataSourceSchema removeRelationFromSchema(@NotNull UUID dataSourceUuid, @NotNull Long relationResourceId) {
        //load schema copy
        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        final DataSourceSchema newSchema = peekDataSourceSchema(dataSourceModel, true).copy();

        if (!newSchema.remove(new Traversable.Identity<>(relationResourceId))) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove entity type. Removing would cause inconsistencies.");
        }

        dataSourceModel.pushDataSourceSchema(newSchema);
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    /**
     * NOTE: this has side effects on the passed list
     * Finds the entry in the cache which is equivalent to {@code element} concerning it's identity function and returns it. If there is none it uses the
     * passed element and adds it to the cache. <i>Equivalence</i> of two elements {@code x} and {@code y} here means:
     * {@code identityFunction.apply(x).equals(identityFunction.apply(y)}
     *
     * @param element          the entry which is looked for
     * @param identityFunction the function which maps an element to it's {@link UUID} (usually just a method reference to T::getUuid)
     * @param elementCache     the cache of known entries
     * @return an entity concept which is equivalent to {@code entityConcept}
     */
    private <T extends ModelBase> T getAndUpdateCache(
            @NotNull T element,
            @NotNull Function<T, String> identityFunction,
            @NotNull List<T> elementCache
    ) {
        final T result;
        if (element.getId() != null) {
            throw new IllegalArgumentException("Element to cache must not have a numeric id.");
        }
        String uuid = identityFunction.apply(element);
        //if uuid exists see if one exists in the cache all-ready
        if (uuid != null) {
            Optional<T> cachedEntityConcept = elementCache.stream()
                    .filter(ec -> identityFunction.apply(ec) != null && uuid.equals(identityFunction.apply(ec)))
                    .findAny();
            result = cachedEntityConcept.orElse(element);
            if (cachedEntityConcept.isEmpty()) {
                elementCache.add(result);
            }
        } else {
            result = element;
            elementCache.add(result);
        }

        return result;
    }

    /**
     * Loads a ModelBase element from the given repository and validates if the loaded element's UUID matches the given element's UUID in relation to the
     * passed globalIdFunction.
     *
     * @param element       the element to load
     * @param uuidGenerator the function to determine a global UUID from the given elements type
     * @param repository    the repository to load from
     * @return attached element matching the given transient element
     */
    @NotNull
    private <T extends ModelBase> T loadFromRepository(
            @NotNull T element,
            @NotNull Function<T, String> uuidGenerator,
            @NotNull JpaRepository<T, Long> repository
    ) {
        String uuid = uuidGenerator.apply(element);
        Long longId = element.getId();
        final T result = repository.findById(longId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Not a valid " + element.getClass().getSimpleName() + " id: " + longId
                ));

        if (uuid != null && !uuid.equals(uuidGenerator.apply(result))) {
            //noinspection MagicCharacter
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inconsistent " + element.getClass().getSimpleName() + " received: id and uuid did not match data base state."
            );
        }
        return result;
    }

    @NotNull
    private DataSourceSchema peekDataSourceSchema(@NotNull DataSourceModel dataSourceModel, boolean schemaWillBeModified) {
        DataSourceSchema currentSchema = dataSourceModel.peekDataSourceSchema();
        if (schemaWillBeModified && currentSchema.isFinalized()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Finalized models are not allowed to be modified");
        }
        return currentSchema;
    }

    @NotNull
    @Transactional
    public DataSourceSchema undoDataSourceSchemaModification(@NotNull UUID uuid) {
        //Get the data source from the repository
        DataSourceModel dataSourceModel = dataSourceRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Data Source does not exists"
                ));
        dataSourceModel.popDataSourceSchema();
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel.peekDataSourceSchema();
    }

    @NotNull
    @Transactional
    public DataSourceSchema redoDataSourceSchemaModification(@NotNull UUID uuid) {
        //Get the data source from the repository
        DataSourceModel dataSourceModel = dataSourceRepository.findById(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Data Source does not exists"
                ));

        DataSourceSchema schema = dataSourceModel.restoreDataSourceSchema();
        dataSourceRepository.save(dataSourceModel);
        return schema;
    }

    @NotNull
    public DataSourceSchema finishModeling(@NotNull UUID dataSourceUuid) {
        //Load the data source from the repository.
        DataSourceModel dataSourceModel = dataSourceRepository.findById(dataSourceUuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Data Source does not exists"
                ));

        final DataSourceSchema dataSourceSchema = dataSourceModel.peekDataSourceSchema();

        final Node syntaxModel = dataSourceSchema.getSyntaxModel();
        final Set<Node.Fault> faults = syntaxModel.evaluateConstraints();
        if (!faults.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Model has faults which prevent its integration: " + faults);
        }

        final Pair<SemanticModel, Map<Long, String>> semanticModelCreationResult = createSemanticModel(dataSourceSchema, dataSourceModel.getUuid().toString());
        SemanticModel semanticModel = semanticModelCreationResult.getLeft();
        final Map<Long, String> middlewareToActualUUIDMapping = semanticModelCreationResult.getRight();

        DataSourceSchema finalDataSourceSchema = createFinalDataSourceSchema(dataSourceSchema, semanticModel, middlewareToActualUUIDMapping);
        dataSourceModel.pushDataSourceSchema(finalDataSourceSchema);
        DataSourceModel finalDataSource = dataSourceRepository.save(dataSourceModel);
        return finalDataSource.peekDataSourceSchema();
    }

    @NotNull
    private DataSourceModel findDataSourceOrThrow(@NotNull UUID dataSourceUuid) {
        return dataSourceRepository.findById(dataSourceUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data source uuid " + dataSourceUuid + ". "));
    }

    @NotNull
    private Pair<SemanticModel, Map<Long, String>> createSemanticModel(@NotNull DataSourceSchema dataSourceSchema, @NotNull String dataSourceId) {
        SemanticModel semanticModel = dataSourceSchema.getSemanticModel();
        Set<Long> entityTypesWithData = new HashSet<>();
        dataSourceSchema.getSyntaxModel().execute(traversable -> {
            if (traversable instanceof PrimitiveNode && ((PrimitiveNode) traversable).getEntityType() != null) {
                entityTypesWithData.add(((PrimitiveNode) traversable).getEntityType().getId());
            }
        });

        //create request
        final KnowledgeGraphServiceDTO2ModelConverter.ConversionContext conversionContext =
                new KnowledgeGraphServiceDTO2ModelConverter.ConversionContext(entityTypesWithData);
        final SemanticModelDTO semanticModelDTO = knowledgeGraphServiceDTO2ModelConverter.convert(semanticModel, conversionContext, dataSourceId);
        final Map<Long, String> middlewareToRequestIdLookup = conversionContext.getMiddlewareToKgsIdLookup();

        //send request
        final SemanticModelCreationResponse semanticModelCreationResponse;
        try {
            logger.info("Creating Semantic Model");
            semanticModelCreationResponse = localKnowledgeApiClient.createSemanticModel(semanticModelDTO);
            logger.info("Created Semantic Model");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when creating semantic model in the KGS", e);
        }

        //parse response and resolve id mapping
        final Map<Long, String> middlewareToActualKGSIdLookup = MapUtilities.compose(
                middlewareToRequestIdLookup,
                MapUtilities.mapValues(semanticModelCreationResponse.getOldToNewIdLookup(), Function.identity())
        );
        final SemanticModel convertedModel = knowledgeGraphServiceDTO2ModelConverter.convert(semanticModelCreationResponse.getSemanticModelDTO());

        return Pair.of(convertedModel, middlewareToActualKGSIdLookup);
    }

    @NotNull
    private DataSourceSchema createFinalDataSourceSchema(
            @NotNull DataSourceSchema currentDataSourceSchema,
            @NotNull SemanticModel retrievedSemanticModel,
            @NotNull Map<Long, String> modeling2KGSmapping
    ) {
        Map<Traversable.Identity<?>, Traversable> oldIdentityToNewTraversable = new HashMap<>();
        final Node newSyntaxModel = currentDataSourceSchema.getSyntaxModel().copy(oldIdentityToNewTraversable);

        //find all entity types which are mapped by a primitive node
        final Set<EntityType> mappedEntityTypes = oldIdentityToNewTraversable.values().stream()
                .filter(PrimitiveNode.class::isInstance)
                .map(PrimitiveNode.class::cast)
                .filter(p -> p.getEntityType() != null)
                .map(PrimitiveNode::getEntityType)
                .collect(Collectors.toSet());

        for (EntityType mappedEntityType : mappedEntityTypes) {
            final Traversable.Identity<?> oldEntityTypeIdentity = MapUtilities.keyOfValue(oldIdentityToNewTraversable, mappedEntityType);
            final Traversable traversable = currentDataSourceSchema.find(oldEntityTypeIdentity);
            assert traversable instanceof EntityType;
            EntityType oldEntityType = (EntityType) traversable;
            final String actualEntityTypeUuid = modeling2KGSmapping.get(oldEntityType.getId());

            EntityType replacement = null;
            for (EntityType entityType : retrievedSemanticModel.getEntityTypes()) {
                if (actualEntityTypeUuid.equals(entityType.getUuid())) {
                    replacement = entityType;
                    break;
                }
            }
            if (replacement == null) {
                logger.warn(
                        "Defective response for semantic model creation.\n" + "Mapping: {}\n" + "SemanticModel: {}",
                        modeling2KGSmapping,
                        retrievedSemanticModel
                );
                throw new RuntimeException(
                        "Failed to find entity type for id `" + oldEntityType.getId() + "` which should be mapped to `" + actualEntityTypeUuid + "`."
                );
            }
            newSyntaxModel.replace(mappedEntityType.getIdentity(), replacement);
        }

        //Update positions
        //Create a map between all old entity types and their new backend ID
        Map<String, EntityType> backendIDtoOldEntityTypeMapping =
                currentDataSourceSchema.getSemanticModel().getEntityTypes().stream().collect(Collectors.toMap(
                        x -> modeling2KGSmapping.get(x.getId()),
                        Function.identity()
                ));

        //Update positions
        for (EntityType entityType : retrievedSemanticModel.getEntityTypes()) {
            entityType.setPosition(backendIDtoOldEntityTypeMapping.get(entityType.getUuid()).getPosition());
        }

        DataSourceSchema finalSchema = new DataSourceSchema(
                currentDataSourceSchema.getOwningNode(),
                newSyntaxModel,
                retrievedSemanticModel,
                Collections.emptyList(),
                Collections.emptyList()
        );
        finalSchema.setFinalized(true);
        return finalSchema;
    }

    @Transactional
    public void deleteModel(@NotNull UUID dataSourceUuid) {
        Optional<DataSourceModel> possibleSchema = dataSourceRepository.findById(dataSourceUuid);
        if (possibleSchema.isPresent()) {
            dataSourceRepository.delete(possibleSchema.get());
        } else {
            logger.warn("Model with id {} was not available", dataSourceUuid);
        }
    }

    /* Recommendations */

    public List<DeltaRecommendationDTO> getRecommendations(@NotNull String dataSourceUuid, @NotNull DataSourceSchema dataSourceSchema) {
        DataSourceSchemaDTO dataSourceSchemaDTO = semanticRecommendationApiClient.performSemanticRefinement(dataSourceUuid, dataSourceSchemaDTOConverter.toDTO(dataSourceSchema));
        return dataSourceSchemaDTO.getRecommendations();
    }

    @Transactional
    public DataSourceModel acceptRecommendation(@NotNull UUID dataSourceUuid, @NotNull DeltaRecommendationDTO recommendation) {

        DataSourceModel dataSourceModel = findDataSourceOrThrow(dataSourceUuid);
        DataSourceSchema dataSourceSchema = peekDataSourceSchema(dataSourceModel, true).copy();
        SemanticModel semanticModel = dataSourceSchema.getSemanticModel();
        if (recommendation.getEntities() != null) {
            List<EntityType> recommendedET = recommendation.getEntities().stream().map(semanticModelDTOConverter::fromDTO).collect(Collectors.toList());
            for (final EntityType et : recommendedET) {
                if (et.getId() == null) {
                    // this is a new one, so just add it to the SM for now
                    entityTypeRepository.save(et);
                    semanticModel.getEntityTypes().add(et);

                } else {
                    // this is a replacement, find the associated existing ET
                    Optional<EntityType> match = semanticModel.getEntityTypes().stream().filter(currentET -> Objects.equals(currentET.getId(), et.getId())).findFirst();
                    if (match.isPresent()) {
                        // this one is to be replaced by et
                        EntityType toReplace = match.get();
                        semanticModel.getEntityTypes().remove(toReplace);
                        EntityType savedET = entityTypeRepository.save(et); // update the database for the upcoming relation matching
                        semanticModel.getEntityTypes().add(savedET);

                    }
                }
            }
        }

        if (recommendation.getRelations() != null) {
            // all used entity types should be available as this once accesses the DB
            List<Relation> recommendedRelation = recommendation.getRelations().stream().map(semanticModelDTOConverter::fromDTO).collect(Collectors.toList());

            for (Relation r : recommendedRelation) {
                // we can currently not remove relations in a recommendation
                Traversable from = dataSourceSchema.find(r.getFrom().getIdentity());
                if (from == null) {
                    logger.warn(
                            "Failed to find tail entity type with id {} and identity {} in schema copy.",
                            r.getFrom().getId(),
                            r.getFrom().getIdentity()
                    );
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id of tail entity of relation is not valid: " + r.getFrom().getId());
                }

                Traversable to = dataSourceSchema.find(r.getTo().getIdentity());
                if (to == null) {
                    logger.warn(
                            "Failed to find head entity type with id {} and identity {} in schema copy.",
                            r.getTo().getId(),
                            r.getTo().getIdentity()
                    );
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id of head entity of relation is not valid: " + r.getTo().getId());
                }

                final EntityType fromEntityType = ObjectUtilities.checkedReturn(from, EntityType.class);
                final EntityType toEntityType = ObjectUtilities.checkedReturn(to, EntityType.class);

                //update model
                r.setFrom(fromEntityType);
                r.setTo(toEntityType);
                semanticModel.getRelations().add(r);
            }
        }

        dataSourceModel.pushDataSourceSchema(dataSourceSchema);
        //persist and return copy
        dataSourceModel = dataSourceRepository.save(dataSourceModel);
        return dataSourceModel;
    }
}