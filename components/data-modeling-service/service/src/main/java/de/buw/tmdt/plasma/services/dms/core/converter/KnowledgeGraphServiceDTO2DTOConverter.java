package de.buw.tmdt.plasma.services.dms.core.converter;

import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class KnowledgeGraphServiceDTO2DTOConverter {

	@NotNull
	public EntityConceptDTO convert(@NotNull de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO kgsDto) {
		return this.convert(kgsDto, () -> null);
	}

	@NotNull
	public SemanticModelDTO convert(@NotNull de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO semanticModelDTO) {
		return this.convert(semanticModelDTO, new Context());
	}

	@NotNull
	protected SemanticModelDTO convert(
			@NotNull de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO semanticModelDTO,
			@NotNull Context context
	) {
		for (var kgsEntityTypeDTO : semanticModelDTO.getNodes()) {
			var kgsEntityConceptDTO = kgsEntityTypeDTO.getConcept();
			EntityConceptDTO entityConceptDTO = context.entityConceptDTOs.computeIfAbsent(
					kgsEntityConceptDTO.getId(),
					ignored -> convert(kgsEntityConceptDTO, context.idSupplier)
			);

			context.entityTypeDTOs.computeIfAbsent(kgsEntityTypeDTO.getId(), ignored -> new EntityTypeDTO(
					null,
					null,
					context.idSupplier.get(),
					kgsEntityTypeDTO.getId(),
					kgsEntityTypeDTO.getLabel(),
					kgsEntityTypeDTO.getOriginalLabel(),
					kgsEntityTypeDTO.getDescription() != null ? kgsEntityTypeDTO.getDescription() : "",
					entityConceptDTO,
					kgsEntityTypeDTO.isMappedToData()
			));
		}

		for (var kgsRelationDTO : semanticModelDTO.getEdges()) {
			var kgsRelationConceptDTO = kgsRelationDTO.getConcept();
			RelationConceptDTO relationConceptDTO = context.relationConceptDTOs.computeIfAbsent(
					kgsRelationConceptDTO.getId(),
					ignored -> convert(kgsRelationConceptDTO, context.idSupplier)
			);

			context.relationDTOs.computeIfAbsent(kgsRelationDTO.getId(), ignored -> new RelationDTO(
					context.idSupplier.get(),
					context.entityTypeDTOs.get(kgsRelationDTO.getFromId()).getId(),
					context.entityTypeDTOs.get(kgsRelationDTO.getToId()).getId(),
					"",
					relationConceptDTO
			));
		}

		return new SemanticModelDTO(
				null,
				null,
				null,
				semanticModelDTO.getId(),
				semanticModelDTO.getLabel(),
				semanticModelDTO.getDescription(),
				new ArrayList<>(context.entityTypeDTOs.values()),
				new ArrayList<>(context.relationDTOs.values())
		);
	}

	@NotNull
	protected EntityConceptDTO convert(
			@NotNull de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO entityConceptDTO,
			@NotNull Supplier<Long> idSupplier
	) {
		return new EntityConceptDTO(
				idSupplier.get(),
				entityConceptDTO.getId(),
				entityConceptDTO.getMainLabel(),
				entityConceptDTO.getDescription(),
				entityConceptDTO.getSourceURI()
		);
	}

	@NotNull
	public RelationConceptDTO convert(@NotNull de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO kgsDto) {
		return this.convert(kgsDto, () -> null);
	}

	@NotNull
	protected RelationConceptDTO convert(
			@NotNull de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO kgsDto,
			Supplier<Long> idSupplier
	) {
		return new RelationConceptDTO(
				idSupplier.get(),
				kgsDto.getId(),
				kgsDto.getLabel(),
				kgsDto.getDescription(),
				kgsDto.getSourceURI(),
				kgsDto.getProperties()
		);
	}

	private static class Context {
		public final Map<String, EntityConceptDTO> entityConceptDTOs = new HashMap<>();
		public final Map<String, EntityTypeDTO> entityTypeDTOs = new HashMap<>();
		public final Map<String, RelationConceptDTO> relationConceptDTOs = new HashMap<>();
		public final Map<String, RelationDTO> relationDTOs = new HashMap<>();

		private final Supplier<Long> idSupplier = new Supplier<>() {
			private long counter = 0;

			@Override
			@NotNull
			public Long get() {
				return ++counter;
			}
		};
	}
}
