package de.buw.tmdt.plasma.services.dms.rest.controller;

import de.buw.tmdt.plasma.services.dms.core.converter.DataSourceSchemaDTOConverter;
import de.buw.tmdt.plasma.services.dms.core.converter.SemanticModelDTOConverter;
import de.buw.tmdt.plasma.services.dms.core.handler.DataModelingHandler;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceModel;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.shared.api.DataModelingAPI;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.recommendation.DeltaRecommendationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.SchemaNodeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController("Modeling Controller")
public class DataModelingController implements DataModelingAPI {

	private final DataModelingHandler dataModelingHandler;
	private final DataSourceSchemaDTOConverter dataSourceSchemaDTOConverter;
	private final SemanticModelDTOConverter semanticModelDTOConverter;

	@Autowired
	public DataModelingController(
			@NotNull DataModelingHandler dataModelingHandler,
			@NotNull DataSourceSchemaDTOConverter dataSourceSchemaDTOConverter,
			@NotNull SemanticModelDTOConverter semanticModelDTOConverter
	) {
		this.dataModelingHandler = dataModelingHandler;
		this.dataSourceSchemaDTOConverter = dataSourceSchemaDTOConverter;
		this.semanticModelDTOConverter = semanticModelDTOConverter;
	}

	@Override
	@Operation(description = "Initializes the current schema of the specified data source.")
	public void initDataSourceSchema(@NotNull String uuid, @NotNull Integer exampleLimit) {
		dataModelingHandler.initDataSourceSchema(UUID.fromString(uuid), exampleLimit);
	}

	@NotNull
	@Override
	@Operation(description = "Returns the current schema of the specified data source.")
	public DataSourceSchemaDTO getDataSourceSchema(@NotNull String uuid) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.getDataSourceSchema(UUID.fromString(uuid)));
	}

	@Override
	@Operation(description = "Stores the position of all nodes in the schema.")
	public void updatePositions(@NotNull String uuid, @NotNull DataSourceSchemaDTO dataSourceSchemaDTO) {
		dataModelingHandler.updatePositions(UUID.fromString(uuid), dataSourceSchemaDTO);
	}

	@NotNull
	@Override
	@Operation(description = "Returns all entity concepts either persistent in the backend or cached for the referenced data source sorted alphabetically.")
	public List<EntityConceptDTO> getEntityConcepts(@NotNull String uuid, @NotNull String prefix) {
		return dataModelingHandler.getEntityConcepts(UUID.fromString(uuid), prefix);
	}

	@NotNull
	@Override
	@Operation(description = "Returns all entity concepts either persistent in the backend or cached for the referenced data source sorted alphabetically.")
	public List<RelationConceptDTO> getRelationConcepts(@NotNull String uuid, @NotNull String prefix) {
		return dataModelingHandler.getRelationConcepts(UUID.fromString(uuid), prefix);
	}

	@NotNull
	@Override
	@Operation(description = "Modify the syntactic schema of a datasource by invoking an operation.")
	public DataSourceSchemaDTO modifySchema(@NotNull String uuid, @NotNull SyntacticOperationDTO syntacticOperationDTO) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.modifySchema(UUID.fromString(uuid), syntacticOperationDTO));
	}

	@Override
	@Operation(description = "Patches the position of an arbitrary schema node.")
	public void positionSchemaNode(
			@NotNull String dataSourceUuid,
			@NotNull String schemaNodeUuid,
			@NotNull SchemaNodeDTO schemaNodeDTO
	) {
		dataModelingHandler.placeSchemaNode(UUID.fromString(dataSourceUuid), UUID.fromString(schemaNodeUuid), schemaNodeDTO);
	}

	@NotNull
	@Override
	@Operation(description = "Adds a new entity concept to the cache of the data source.")
	public EntityConceptDTO cacheEntityConcept(@NotNull String uuid, @NotNull EntityConceptDTO entityConceptDTO) {
		return semanticModelDTOConverter.toDTO(dataModelingHandler.cacheEntityConcept(UUID.fromString(uuid), entityConceptDTO));
	}

	@NotNull
	@Override
	@Operation(description = "Removes a temporary entity concept from the cache of the data source.")
	public List<EntityConceptDTO> uncacheEntityConcept(@NotNull String dataSourceUuid, @NotNull Long entityConceptId) {
		dataModelingHandler.uncacheEntityConcept(UUID.fromString(dataSourceUuid), entityConceptId);
		return dataModelingHandler.getEntityConcepts(UUID.fromString(dataSourceUuid), "");
	}

	@NotNull
	@Override
	@Operation(description = "Adds a new relation concept to the cache of the data source.")
	public RelationConceptDTO cacheRelationConcept(@NotNull String uuid, @NotNull RelationConceptDTO relationConceptDTO) {
		return semanticModelDTOConverter.toDTO(dataModelingHandler.cacheRelationConcept(UUID.fromString(uuid), relationConceptDTO));
	}

	@NotNull
	@Override
	@Operation(description = "Removes a temporary entity concept from the cache of the data source.")
	public List<RelationConceptDTO> uncacheRelationConcept(@NotNull String dataSourceUuid, @NotNull Long relationConceptId) {
		dataModelingHandler.uncacheRelationConcept(UUID.fromString(dataSourceUuid), relationConceptId);
		return dataModelingHandler.getRelationConcepts(UUID.fromString(dataSourceUuid), "");
	}

	@NotNull
	@Override
	@Operation(description = "Adds an entity type and caches or creates the referenced entity concept if it is not present yet.")
	public DataSourceSchemaDTO addEntityType(
			@NotNull String uuid,
			@NotNull EntityTypeDTO entityTypeDTO,
			@Nullable String primitiveNodeIdString
	) {
		@Nullable UUID primitiveNodeUuid;
		if (primitiveNodeIdString != null && !primitiveNodeIdString.isEmpty()) {
			primitiveNodeUuid = UUID.fromString(primitiveNodeIdString);
		} else {
			primitiveNodeUuid = null;
		}
		// modify the sementic model
		DataSourceSchema dataSourceSchema = dataModelingHandler.addEntityType(UUID.fromString(uuid), entityTypeDTO, primitiveNodeUuid);
		// request recommendations
		List<DeltaRecommendationDTO> recommendations = dataModelingHandler.getRecommendations(uuid,dataSourceSchema);
		DataSourceSchemaDTO dto = dataSourceSchemaDTOConverter.toDTO(dataSourceSchema);
		dto.setRecommendations(recommendations);
		return dto;
	}

	@NotNull
	@Override
	@Operation(description = "Updates an entity type and caches the referenced entity concept if it is not present yet.")
	public DataSourceSchemaDTO updateEntityType(
			@NotNull String uuid,
			@NotNull Long entityTypeId,
			@NotNull EntityTypeDTO entityTypeDTO
	) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.updateEntityType(UUID.fromString(uuid), entityTypeId, entityTypeDTO));
	}

	@NotNull
	@PostMapping(value = "/recommendation/accept", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(description = "Accepts a recommendation and adds the contents to the semantic model.")
	public DataSourceSchemaDTO acceptRecommendation(@NotNull String uuid, @NotNull DeltaRecommendationDTO recommendation){
		DataSourceModel model = dataModelingHandler.acceptRecommendation(UUID.fromString(uuid),recommendation);

		// get new recommendations here
		List<DeltaRecommendationDTO> recommendations = dataModelingHandler.getRecommendations(uuid,model.peekDataSourceSchema());
		// request recommendations
		DataSourceSchemaDTO dto = dataSourceSchemaDTOConverter.toDTO(model.peekDataSourceSchema());
		dto.setRecommendations(recommendations);
		return dto;
	}

	@Override
	@Operation(description = "Patches the position of an entity type node.")
	public void positionEntityType(
			@NotNull String uuid,
			@NotNull Long entityTypeId,
			@NotNull EntityTypeDTO entityTypeDTO
	) {
		dataModelingHandler.placeEntityType(UUID.fromString(uuid), entityTypeId, entityTypeDTO);
	}

	@NotNull
	@Override
	@Operation(description = "Removes an entity type.")
	public DataSourceSchemaDTO removeEntityType(@NotNull String uuid, @NotNull Long entityTypeId) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.removeEntityTypeFromSchema(UUID.fromString(uuid), entityTypeId));
	}

	@NotNull
	@Override
	@Operation(description = "Adds a relation and caches or creates the referenced relation concept if it is not present yet.")
	public DataSourceSchemaDTO addRelation(@NotNull String uuid, @NotNull RelationDTO relationDto) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.addRelation(UUID.fromString(uuid), relationDto));
	}

	@NotNull
	@Override
	@Operation(description = "Updates a relation and caches the referenced relation concept if it is not present yet.")
	public DataSourceSchemaDTO updateRelation(
			@NotNull String uuid,
			@NotNull Long entityTypeId,
			@NotNull RelationDTO relationDto
	) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.updateRelation(UUID.fromString(uuid), entityTypeId, relationDto));
	}

	@NotNull
	@Override
	@Operation(description = "Removes a relation.")
	public DataSourceSchemaDTO removeRelation(@NotNull String uuid, @NotNull Long entityTypeId) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.removeRelationFromSchema(UUID.fromString(uuid), entityTypeId));
	}

	@NotNull
	@Override
	@Operation(description = "pop last element from datasource's schema stack")
	public DataSourceSchemaDTO undoDataSourceSchemaModification(@NotNull String uuid) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.undoDataSourceSchemaModification(UUID.fromString(uuid)));
	}

	@NotNull
	@Override
	@Operation(description = "push last element from datasource's schema stack")
	public DataSourceSchemaDTO redoDataSourceSchemaModification(@NotNull String uuid) {
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.redoDataSourceSchemaModification(UUID.fromString(uuid)));
	}

	@NotNull
	@Override
	@Operation(description = "finish schema modelling process")
	public DataSourceSchemaDTO finishModeling(@NotNull String uuid) {
		UUID dataSourceUuid = UUID.fromString(uuid);
		return dataSourceSchemaDTOConverter.toDTO(dataModelingHandler.finishModeling(dataSourceUuid));
	}

	@Override
	public void deleteModel(@NotNull String uuid) {
		UUID dataSourceUuid = UUID.fromString(uuid);
		dataModelingHandler.deleteModel(dataSourceUuid);
	}
}