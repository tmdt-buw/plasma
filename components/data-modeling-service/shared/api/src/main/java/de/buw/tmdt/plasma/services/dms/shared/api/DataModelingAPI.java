package de.buw.tmdt.plasma.services.dms.shared.api;

import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.recommendation.DeltaRecommendationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.SchemaNodeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping(value = "/api/plasma-dms/modeling")
public interface DataModelingAPI {

	//############################################### Schema Definition ###############################################

	@PostMapping(value = "/init")
	void initDataSourceSchema(
			@NotNull @RequestParam("uuid") String uuid,
			@NotNull @RequestParam(value = "exampleLimit", defaultValue = "100") Integer exampleLimit
	);

	@NotNull
	@GetMapping(value = "/{uuid}/schema", produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO getDataSourceSchema(@NotNull @PathVariable("uuid") String uuid);

	@PatchMapping(value = "/{uuid}/schema", consumes = MediaType.APPLICATION_JSON_VALUE)
	void updatePositions(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestBody DataSourceSchemaDTO dataSourceSchemaDTO);

	@NotNull
	@GetMapping(value = "/{uuid}/entityconcepts", produces = MediaType.APPLICATION_JSON_VALUE)
	List<EntityConceptDTO> getEntityConcepts(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestParam("prefix") String prefix);

	@NotNull
	@GetMapping(value = "/{uuid}/relationconcepts", produces = MediaType.APPLICATION_JSON_VALUE)
	List<RelationConceptDTO> getRelationConcepts(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestParam("prefix") String prefix);

	@NotNull
	@PostMapping(value = "/{uuid}/invoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO modifySchema(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestBody SyntacticOperationDTO syntacticOperationDTO);

	@PatchMapping(value = "/{dataSourceUuid}/syntaxmodel/{schemaNodeUuid}", consumes = MediaType.APPLICATION_JSON_VALUE)
	void positionSchemaNode(
			@NotNull @PathVariable("dataSourceUuid") String dataSourceUuid,
			@NotNull @PathVariable("schemaNodeUuid") String schemaNodeUuid,
			@NotNull @RequestBody SchemaNodeDTO schemaNodeDTO
	);

	/* Cache operations */

	@NotNull
	@PostMapping(value = "/{uuid}/entityconcept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	EntityConceptDTO cacheEntityConcept(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestBody EntityConceptDTO entityConceptDTO);

	@NotNull
	@DeleteMapping(value = "/{dataSourceUuid}/entityconcept/{entityConceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
	List<EntityConceptDTO> uncacheEntityConcept(
			@NotNull @PathVariable("dataSourceUuid") String dataSourceUuid,
			@NotNull @PathVariable("entityConceptId") Long entityConceptId
	);

	@NotNull
	@PostMapping(value = "/{uuid}/relationconcept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	RelationConceptDTO cacheRelationConcept(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestBody RelationConceptDTO relationConceptDTO);

	@NotNull
	@DeleteMapping(value = "/{dataSourceUuid}/relationconcept/{relationConceptId}", produces = MediaType.APPLICATION_JSON_VALUE)
	List<RelationConceptDTO> uncacheRelationConcept(
			@NotNull @PathVariable("dataSourceUuid") String dataSourceUuid,
			@NotNull @PathVariable("relationConceptId") Long relationConceptId
	);

	/* Modeling operations (EntityTypes)*/

	@NotNull
	@PostMapping(value = "/{uuid}/entitytype", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO addEntityType(
			@NotNull @PathVariable("uuid") String uuid,
			@NotNull @RequestBody EntityTypeDTO entityTypeDTO,
			@Nullable @RequestParam(value = "primitiveNodeId", required = false) String primitiveNodeIdString
	);

	@NotNull
	@PutMapping(value = "/{uuid}/entitytype/{entityTypeId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO updateEntityType(
			@NotNull @PathVariable("uuid") String uuid,
			@NotNull @PathVariable("entityTypeId") Long entityTypeId,
			@NotNull @RequestBody EntityTypeDTO entityTypeDTO
	);

	@PatchMapping(value = "/{uuid}/entitytype/{entityTypeId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	void positionEntityType(
			@NotNull @PathVariable("uuid") String uuid,
			@NotNull @PathVariable("entityTypeId") Long entityTypeId,
			@NotNull @RequestBody EntityTypeDTO entityTypeDTO
	);

	@NotNull
	@DeleteMapping(value = "/{uuid}/entitytype/{entityTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO removeEntityType(@NotNull @PathVariable("uuid") String uuid, @NotNull @PathVariable("entityTypeId") Long entityTypeId);

	/* Modeling operations (Relations)*/

	@NotNull
	@PostMapping(value = "/{uuid}/relation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO addRelation(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestBody RelationDTO relationDto);

	@NotNull
	@PutMapping(value = "/{uuid}/relation/{relationId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO updateRelation(
			@NotNull @PathVariable("uuid") String uuid,
			@NotNull @PathVariable("relationId") Long entityTypeId,
			@NotNull @RequestBody RelationDTO relationDto
	);

	@NotNull
	@DeleteMapping(value = "/{uuid}/relation/{relationId}", produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO removeRelation(@NotNull @PathVariable("uuid") String uuid, @NotNull @PathVariable("relationId") Long entityTypeId);

	/* Recommendation */

	@NotNull
	@PostMapping(value = "/{uuid}/recommendation/accept", produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO acceptRecommendation(@NotNull @PathVariable("uuid") String uuid, @NotNull @RequestBody DeltaRecommendationDTO recommendation);

	/* Undo / Redo / finish */

	@NotNull
	@PostMapping(value = "/{uuid}/schema/undo", produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO undoDataSourceSchemaModification(@NotNull @PathVariable("uuid") String uuid);

	@NotNull
	@PostMapping(value = "/{uuid}/schema/redo", produces = MediaType.APPLICATION_JSON_VALUE)
	DataSourceSchemaDTO redoDataSourceSchemaModification(@NotNull @PathVariable("uuid") String uuid);

	@NotNull
	@PostMapping(value = "/{uuid}/schema/finish")
	DataSourceSchemaDTO finishModeling(@NotNull @PathVariable("uuid") String uuid);

	@DeleteMapping(value = "/{uuid}")
	void deleteModel(@NotNull @PathVariable("uuid") String uuid);
}
