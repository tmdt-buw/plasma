package de.buw.tmdt.plasma.services.dms.core.services;

import de.buw.tmdt.plasma.services.dms.core.converter.DataSourceSchemaDTOConverter;
import de.buw.tmdt.plasma.services.dms.core.converter.SchemaAnalysisDTOConverter;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceModel;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.*;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.*;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members.EntityConceptSuggestion;
import de.buw.tmdt.plasma.services.dms.core.repository.*;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationConceptDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.NodeDTO;
import de.buw.tmdt.plasma.services.sas.shared.feignclient.SchemaAnalysisApiClient;
import de.buw.tmdt.plasma.services.srs.feignclient.SemanticRecommendationApiClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.FeignException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSourceModelInitService {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceModelInitService.class);

	private final EntityTypeRepository entityTypeRepository;
	private final EntityConceptRepository entityConceptRepository;
	private final RelationRepository relationRepository;
	private final RelationConceptRepository relationConceptRepository;
	private final RelationConceptPropertyRepository relationConceptPropertyRepository;
	private final SchemaAnalysisApiClient schemaAnalysisApiClient;
	private final SchemaAnalysisDTOConverter schemaAnalysisDTOConverter;
	private final SemanticRecommendationApiClient semanticRecommendationApiClient;
	private final DataSourceSchemaDTOConverter dataSourceSchemaDTOConverter;
	private final DataSourceRepository dataSourceRepository;

	public DataSourceModelInitService(
			@NotNull EntityTypeRepository entityTypeRepository,
			@NotNull EntityConceptRepository entityConceptRepository,
			@NotNull RelationRepository relationRepository,
			@NotNull RelationConceptRepository relationConceptRepository,
			@NotNull RelationConceptPropertyRepository relationConceptPropertyRepository,
			@NotNull SchemaAnalysisApiClient schemaAnalysisApiClient,
			@NotNull SchemaAnalysisDTOConverter schemaAnalysisDTOConverter,
			SemanticRecommendationApiClient semanticRecommendationApiClient, DataSourceSchemaDTOConverter dataSourceSchemaDTOConverter, DataSourceRepository dataSourceRepository) {
		this.entityTypeRepository = entityTypeRepository;
		this.entityConceptRepository = entityConceptRepository;
		this.relationRepository = relationRepository;
		this.relationConceptRepository = relationConceptRepository;
		this.relationConceptPropertyRepository = relationConceptPropertyRepository;
		this.schemaAnalysisApiClient = schemaAnalysisApiClient;
		this.schemaAnalysisDTOConverter = schemaAnalysisDTOConverter;
		this.semanticRecommendationApiClient = semanticRecommendationApiClient;
		this.dataSourceSchemaDTOConverter = dataSourceSchemaDTOConverter;
		this.dataSourceRepository = dataSourceRepository;
	}

	public DataSourceSchema createInitialSchema(UUID uuid, int exampleLimit, DataSourceModel dataSourceModel) {
		NodeDTO result;
		try {
			result = schemaAnalysisApiClient.getResult(uuid.toString(), exampleLimit);
		} catch (FeignException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not reach SchemaAnalysisService", e);
		}
		SemanticModel semanticModel;
		semanticModel = new SemanticModel(
				uuid.toString(),
				"",
				"",
				new ArrayList<>(),
				new ArrayList<>(),
				null
		);

		DataSourceSchema  schema = new DataSourceSchema(
				dataSourceModel,
				schemaAnalysisDTOConverter.fromNodeDTO(result),
				semanticModel,
				new ArrayList<>(),
				new ArrayList<>()
		);

		dataSourceModel.pushDataSourceSchema(schema);
		dataSourceModel = dataSourceRepository.save(dataSourceModel);

		// request semantic labeling
		DataSourceSchemaDTO dataSourceSchemaDTO1 = dataSourceSchemaDTOConverter.toDTO(dataSourceModel.peekDataSourceSchema());
		dataSourceSchemaDTO1.getSyntaxModelDTO().getNodes().forEach(node -> {
			// no operations when sending to other services
			node.getOperations().clear();
		});
		DataSourceSchemaDTO dataSourceSchemaDTO = semanticRecommendationApiClient.performSemanticLabeling(
				uuid.toString(), dataSourceSchemaDTO1);

		// request semantic modeling
		dataSourceSchemaDTO = semanticRecommendationApiClient.performSemanticModeling(
				uuid.toString(), dataSourceSchemaDTO);

		SemanticModelDTO semanticModelDTO = dataSourceSchemaDTO.getSemanticModelDTO();

		//Convert the fetched schema to the internal model
		Node syntaxModel = schemaAnalysisDTOConverter.fromNodeDTO(result);

		Map<Long, Long> entityTypeMapping = new HashMap<>();

		Map<String, EntityConcept> entityConceptMap = new HashMap<>();
		List<EntityType> entityTypes = refreshEntityConcepts(syntaxModel, entityTypeMapping, entityConceptMap);
		List<EntityType> classEntityTypes = new ArrayList<>();

		for (EntityTypeDTO entityType : semanticModelDTO.getNodes()) {

			if (!entityTypeMapping.containsKey(entityType.getId())) {

				EntityConcept entityConcept;
				if (entityConceptMap.containsKey(entityType.getConcept().getUuid())) {
					entityConcept = entityConceptMap.get(entityType.getConcept().getUuid());
				} else {
					entityConcept = new EntityConcept(
							entityType.getConcept().getUuid(),
							entityType.getConcept().getName(),
							entityType.getConcept().getDescription(),
							entityType.getConcept().getSourceURI(),
							null
					);
					entityConcept = entityConceptRepository.save(entityConcept);
					entityConceptMap.put(entityConcept.getUuid(), entityConcept);
				}

				EntityType newEntityType = new EntityType(
						null,
						entityType.getLabel(),
						entityType.getOriginalLabel(),
						entityType.getDescription(),
						entityConcept,
						null
				);

				newEntityType = entityTypeRepository.save(newEntityType);
				long oldId = entityType.getId();
				long newId = newEntityType.getId();

				entityTypeMapping.put(oldId, newId);
				classEntityTypes.add(newEntityType);
			}
		}
		entityTypes.addAll(classEntityTypes);

		List<Relation> newRelations = new ArrayList<>();
		Map<String, RelationConcept> relationConceptMap = new HashMap<>();

		for (RelationDTO relation : semanticModelDTO.getEdges()) {
			RelationConceptDTO relationConcept = relation.getConcept();

			RelationConcept persistingRelationConcept;

			if (relationConceptMap.containsKey(relation.getConcept().getUuid())) {
				persistingRelationConcept = relationConceptMap.get(relation.getConcept().getUuid());
			} else {
				Set<RelationConcept.Property> properties = new HashSet<>(relationConceptPropertyRepository.findAllByNameIn(relationConcept.getProperties()));
				persistingRelationConcept = new RelationConcept(
						relation.getConcept().getUuid(),
						relation.getConcept().getName(),
						relation.getConcept().getDescription(),
						relation.getConcept().getSourceURI(),
						properties,
						null
				);
				persistingRelationConcept = relationConceptRepository.save(persistingRelationConcept);
				relationConceptMap.put(persistingRelationConcept.getUuid(), persistingRelationConcept);
			}

			long newFromId = entityTypeMapping.get(relation.getFromId());
			long newToId = entityTypeMapping.get(relation.getToId());
			EntityType fromEntity = getEntityTypeById(entityTypes, newFromId);
			EntityType toEntity = getEntityTypeById(entityTypes, newToId);

			Relation newRelation = new Relation(null, fromEntity, toEntity, persistingRelationConcept, relation.getDescription(), null);

			newRelation = relationRepository.save(newRelation);
			newRelations.add(newRelation);
		}

		String uuidS = semanticModelDTO.getUuid() == null ? null : semanticModelDTO.getUuid();

		semanticModel = new SemanticModel(
				uuidS,
				semanticModelDTO.getLabel(),
				semanticModelDTO.getDescription(),
				entityTypes,
				newRelations,
				null
		);

		List<EntityConcept> entityConceptCache = entityTypes.stream().map(EntityType::getEntityConcept).distinct().collect(Collectors.toList());
		List<RelationConcept> relationConceptCache = newRelations.stream().map(Relation::getRelationConcept).distinct().collect(Collectors.toList());

		return new DataSourceSchema(
				dataSourceModel,
				syntaxModel,
				semanticModel,
				entityConceptCache,
				relationConceptCache
		);
	}

	private List<EntityType> refreshEntityConcepts(Node node, Map<Long, Long> entityTypeMapping, Map<String, EntityConcept> entityConceptMap) {
		List<EntityType> entityTypes = new ArrayList<>();
		if (node instanceof SetNode) {
			entityTypes.addAll(refreshEntityConcepts((SetNode) node, entityTypeMapping, entityConceptMap));
		} else if (node instanceof ObjectNode) {
			entityTypes.addAll(refreshEntityConcepts((ObjectNode) node, entityTypeMapping, entityConceptMap));
		} else if (node instanceof PrimitiveNode) {
			entityTypes.addAll(refreshEntityConcepts((PrimitiveNode) node, entityTypeMapping, entityConceptMap));
		} else if (node instanceof CollisionNode) {
			entityTypes.addAll(refreshEntityConcepts((CollisionNode) node, entityTypeMapping, entityConceptMap));
		} else if (null == node) {
			logger.debug("NullSchema is not going to get a suggested EntityConcept.");
		}
		return entityTypes;
	}

	private List<EntityType> refreshEntityConcepts(SetNode setNode, Map<Long, Long> entityTypeMapping, Map<String, EntityConcept> entityConceptMap) {
		List<EntityType> entityTypes = new ArrayList<>();
		for (SetNode.Child n : setNode.getChildren()) {
			entityTypes.addAll(refreshEntityConcepts(n.getNode(), entityTypeMapping, entityConceptMap));
		}
		return entityTypes;
	}

	private List<EntityType> refreshEntityConcepts(ObjectNode objectNode, Map<Long, Long> entityTypeMapping, Map<String, EntityConcept> entityConceptMap) {
		List<EntityType> entityTypes = new ArrayList<>();
		for (Map.Entry<String, Node> child : objectNode.getChildren().entrySet()) {
			entityTypes.addAll(refreshEntityConcepts(child.getValue(), entityTypeMapping, entityConceptMap));
		}
		return entityTypes;
	}

	private List<EntityType> refreshEntityConcepts(
			CollisionNode collisionNode,
			Map<Long, Long> entityTypeMapping,
			Map<String, EntityConcept> entityConceptMap
	) {
		List<EntityType> entityTypes = new ArrayList<>();
		if (collisionNode.getPrimitiveNode() != null) {
			entityTypes.addAll(refreshEntityConcepts(collisionNode.getPrimitiveNode(), entityTypeMapping, entityConceptMap));
		}
		if (collisionNode.getObjectNode() != null) {
			entityTypes.addAll(refreshEntityConcepts(collisionNode.getObjectNode(), entityTypeMapping, entityConceptMap));
		}
		if (collisionNode.getSetNode() != null) {
			entityTypes.addAll(refreshEntityConcepts(collisionNode.getSetNode(), entityTypeMapping, entityConceptMap));
		}
		return entityTypes;
	}

	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Won't be null")
	private List<EntityType> refreshEntityConcepts(
			PrimitiveNode primitiveNode,
			Map<Long, Long> entityTypeMapping,
			Map<String, EntityConcept> entityConceptMap
	) {

		List<EntityType> entityTypes = new ArrayList<>();

		List<EntityConceptSuggestion> correctedEntityConceptSuggestions = new ArrayList<>();

		for (EntityConceptSuggestion ec : primitiveNode.getEntityConceptSuggestions()) {

			if (entityConceptMap.containsKey(ec.getEntityConcept().getUuid())) {
				correctedEntityConceptSuggestions.add(new EntityConceptSuggestion(entityConceptMap.get(ec.getEntityConcept().getUuid()), ec.getWeight()));
			} else {
				EntityConcept persistedConcept = new EntityConcept(
						ec.getEntityConcept().getUuid(),
						ec.getEntityConcept().getName(),
						ec.getEntityConcept().getDescription(),
						ec.getEntityConcept().getSourceURI(),
						null
				);
				persistedConcept = entityConceptRepository.save(persistedConcept);
				entityConceptMap.put(persistedConcept.getUuid(), persistedConcept);
				correctedEntityConceptSuggestions.add(new EntityConceptSuggestion(persistedConcept, ec.getWeight()));
			}
		}
		primitiveNode.setEntityConceptSuggestions(correctedEntityConceptSuggestions);

		if (primitiveNode.getEntityType() != null) {
			EntityConcept fittingConcept = null;
			for (EntityConceptSuggestion ecs : primitiveNode.getEntityConceptSuggestions()) {

				if (Objects.equals(ecs.getEntityConcept().getUuid(), primitiveNode.getEntityType().getEntityConcept().getUuid())) {
					fittingConcept = ecs.getEntityConcept();
					break;
				}
			}
			long oldId = primitiveNode.getEntityType().getId();

			if (fittingConcept != null) {

				EntityType entityType = new EntityType(
						null,
						primitiveNode.getEntityType().getLabel(),
						primitiveNode.getEntityType().getOriginalLabel(),
						primitiveNode.getEntityType().getDescription(),
						fittingConcept,
						null
				);

				primitiveNode.getEntityType().setEntityConcept(fittingConcept);

				entityType = entityTypeRepository.save(entityType);
				long newId = entityType.getId();
				entityTypeMapping.put(oldId, newId);
				primitiveNode.setEntityType(entityType);
				entityTypes.add(entityType);
			}
		}
		return entityTypes;
	}

	private EntityType getEntityTypeById(List<EntityType> entityTypes, long id) {
		return entityTypes.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
	}
}
