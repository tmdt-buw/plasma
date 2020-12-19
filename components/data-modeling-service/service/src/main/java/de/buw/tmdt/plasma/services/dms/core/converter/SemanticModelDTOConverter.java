package de.buw.tmdt.plasma.services.dms.core.converter;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.*;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.dms.core.repository.EntityTypeRepository;
import de.buw.tmdt.plasma.services.dms.core.repository.RelationConceptPropertyRepository;
import de.buw.tmdt.plasma.services.dms.shared.dto.Positioned;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.*;
import de.buw.tmdt.plasma.utilities.collections.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;

@Service
public class SemanticModelDTOConverter {
	private final EntityTypeRepository entityTypeRepository;
	private final RelationConceptPropertyRepository relationConceptPropertyRepository;

	@Autowired
	public SemanticModelDTOConverter(
			@NotNull EntityTypeRepository entityTypeRepository,
			@NotNull RelationConceptPropertyRepository relationConceptPropertyRepository
	) {
		this.entityTypeRepository = entityTypeRepository;
		this.relationConceptPropertyRepository = relationConceptPropertyRepository;
	}

	@NotNull
	public SemanticModelDTO toDTO(@NotNull SemanticModel semanticModel) {
		Map<Long, EntityTypeDTO> entityTypeDTOLookUp = semanticModel.getEntityTypes().stream()
				.map(this::toDTO)
				.collect(Collectors.getSerializableMapCollector(
						EntityTypeDTO::getId,
						Function.identity()
				));

		List<RelationDTO> relations = semanticModel.getRelations().stream()
				.map(this::toDTO)
				.collect(Collectors.getSerializableListCollector());

		return new SemanticModelDTO(
				semanticModel.getId().toString(),
				semanticModel.getUuid(),
				semanticModel.getLabel(),
				semanticModel.getDescription(),
				new ArrayList<>(entityTypeDTOLookUp.values()),
				relations
		);
	}

	@NotNull
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
				entityType.getUuid(),
				entityType.getLabel(),
				entityType.getOriginalLabel(),
				entityType.getDescription(),
				entityConceptDTO,
				false
		);
	}

	@NotNull
	public EntityConceptDTO toDTO(@NotNull EntityConcept entityConcept) {
		return new EntityConceptDTO(
				entityConcept.getId(), entityConcept.getUuid(), entityConcept.getName(), entityConcept.getDescription(), entityConcept.getSourceURI()
		);
	}

	@NotNull
	private RelationDTO toDTO(@NotNull Relation relation) {
		return new RelationDTO(
				relation.getId(),
				relation.getFrom().getId(),
				relation.getTo().getId(),
				relation.getDescription(),
				toDTO(relation.getRelationConcept())
		);
	}

	@NotNull
	public RelationConceptDTO toDTO(@NotNull RelationConcept relationConcept) {
		return new RelationConceptDTO(
				relationConcept.getId(),
				relationConcept.getUuid(),
				relationConcept.getName(),
				relationConcept.getDescription(),
				relationConcept.getSourceURI(),
				relationConcept.getProperties().stream().map(RelationConcept.Property::getName).collect(Collectors.getSerializableSetCollector())
		);
	}

//	public @NotNull SemanticModel fromDTO (@NotNull SemanticModelDTO dto){
//		// TODO;
//		return null;
//	}


	public @NotNull Relation fromDTO(@NotNull RelationDTO relationDTO) {
		final EntityType from = entityTypeRepository.findById(relationDTO.getFromId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Entity type id in relation is invalid."
				));

		final EntityType to = entityTypeRepository.findById(relationDTO.getToId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.BAD_REQUEST,
						"Entity type id in relation is invalid."
				));
		return buildRelation(from, to, fromDTO(relationDTO.getConcept()), relationDTO.getDescription(), null, relationDTO.getId());
	}

	private Relation buildRelation(
			@NotNull EntityType from,
			@NotNull EntityType to,
			@NotNull RelationConcept relationConcept,
			@NotNull String description,
			@Nullable Position position,
			@Nullable Long id
	) {
		if (id != null) {
			return new Relation(null, from, to, relationConcept, description, position, id);
		} else {
			return new Relation(null, from, to, relationConcept, description, position);
		}
	}

	@NotNull
	public RelationConcept fromDTO(@NotNull RelationConceptDTO concept) {
		Set<RelationConcept.Property> properties = new HashSet<>(relationConceptPropertyRepository.findAllByNameIn(concept.getProperties()));
		if (concept.getId() != null) {
			return new RelationConcept(
					concept.getUuid(),
					concept.getName(),
					concept.getDescription(),
					concept.getSourceURI(),
					properties,
					null,
					concept.getId()
			);
		} else {
			return new RelationConcept(
					concept.getUuid(),
					concept.getName(),
					concept.getDescription(),
					concept.getSourceURI(),
					properties,
					null
			);
		}
	}

	@NotNull
	public EntityType fromDTO(@NotNull EntityTypeDTO entityTypeDTO) {
		if (entityTypeDTO.getId() != null) {
			return new EntityType(
					entityTypeDTO.getUuid(),
					entityTypeDTO.getLabel(),
					entityTypeDTO.getOriginalLabel(),
					entityTypeDTO.getDescription(),
					fromDTO(entityTypeDTO.getConcept()),
					getPosition(entityTypeDTO),
					entityTypeDTO.getId()
			);
		} else {
			return new EntityType(
					entityTypeDTO.getUuid(),
					entityTypeDTO.getLabel(),
					entityTypeDTO.getOriginalLabel(),
					entityTypeDTO.getDescription(),
					fromDTO(entityTypeDTO.getConcept()),
					getPosition(entityTypeDTO)
			);
		}
	}

	@NotNull
	public EntityConcept fromDTO(@NotNull EntityConceptDTO entityConceptDTO) {
		if (entityConceptDTO.getId() != null) {
			return new EntityConcept(
					entityConceptDTO.getUuid(),
					entityConceptDTO.getName(),
					entityConceptDTO.getDescription(),
					entityConceptDTO.getSourceURI(),
					null,
					entityConceptDTO.getId()
			);
		} else {
			return new EntityConcept(
					entityConceptDTO.getUuid(),
					entityConceptDTO.getName(),
					entityConceptDTO.getDescription(),
					entityConceptDTO.getSourceURI(),
					null
			);
		}
	}

	@Nullable
	private Position getPosition(@NotNull Positioned positioned) {
		Double xCoordinate = positioned.getXCoordinate();
		Double yCoordinate = positioned.getYCoordinate();
		if (xCoordinate != null && yCoordinate != null) {
			return new Position(xCoordinate, yCoordinate);
		}
		return null;
	}
}
