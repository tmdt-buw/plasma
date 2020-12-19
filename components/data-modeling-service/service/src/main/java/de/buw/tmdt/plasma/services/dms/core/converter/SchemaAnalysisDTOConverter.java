package de.buw.tmdt.plasma.services.dms.core.converter;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityConcept;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.*;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members.*;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SchemaAnalysisDTOConverter {

	@NotNull
	public Node fromNodeDTO(@NotNull NodeDTO nodeDTO) {
		if (nodeDTO instanceof PrimitiveNodeDTO) {
			return fromPrimitiveNodeDTO((PrimitiveNodeDTO) nodeDTO);
		} else if (nodeDTO instanceof SetNodeDTO) {
			return fromSetNodeDTO((SetNodeDTO) nodeDTO);
		} else if (nodeDTO instanceof ObjectNodeDTO) {
			return fromObjectNodeDTO((ObjectNodeDTO) nodeDTO);
		} else if (nodeDTO instanceof CollisionNodeDTO) {
			return fromCollisionNodeDTO((CollisionNodeDTO) nodeDTO);
		} else if (nodeDTO instanceof CompositeNodeDTO) {
			return fromCompositeNodeDTO((CompositeNodeDTO) nodeDTO);
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid NodeType");
	}

	@NotNull
	private PrimitiveNode fromPrimitiveNodeDTO(@NotNull PrimitiveNodeDTO primitiveNodeDTO) {
		return new PrimitiveNode(
				fromEntityTypeDTO(primitiveNodeDTO.getEntityType()),
				fromDataTypeDTO(primitiveNodeDTO.getDataType()),
				primitiveNodeDTO.getCleansingPattern(),
				primitiveNodeDTO.getEntityConceptSuggestions().stream().map(this::fromEntityConceptSuggestionDTO).collect(Collectors.toList()),
				null,
				primitiveNodeDTO.getExamples(),
				primitiveNodeDTO.getUuid()
		);
	}

	@NotNull
	private SetNode fromSetNodeDTO(@NotNull SetNodeDTO primitiveNodeDTO) {
		return new SetNode(
				primitiveNodeDTO.getChildren().stream().map(this::fromChildDTO).collect(Collectors.toList()),
				null,
				primitiveNodeDTO.getUuid()
		);
	}

	@NotNull
	private ObjectNode fromObjectNodeDTO(@NotNull ObjectNodeDTO objectNodeDTO) {
		Map<String, Node> children = new HashMap<>();
		for (Map.Entry<String, NodeDTO> stringNodeDTOEntry : objectNodeDTO.getChildren().entrySet()) {
			children.put(stringNodeDTOEntry.getKey(), fromNodeDTO(stringNodeDTOEntry.getValue()));
		}
		return new ObjectNode(
				children, null, objectNodeDTO.getUuid()
		);
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "these objects are allowed to be null")
	private CollisionNode fromCollisionNodeDTO(@NotNull CollisionNodeDTO collisionNodeDTO) {
		return new CollisionNode(
				collisionNodeDTO.getPrimitiveNode() != null ? fromPrimitiveNodeDTO(collisionNodeDTO.getPrimitiveNode()) : null,
				collisionNodeDTO.getObjectNode() != null ? fromObjectNodeDTO(collisionNodeDTO.getObjectNode()) : null,
				collisionNodeDTO.getSetNode() != null ? fromSetNodeDTO(collisionNodeDTO.getSetNode()) : null,
				null,
				collisionNodeDTO.getUuid()
		);
	}

	@NotNull
	private CompositeNode fromCompositeNodeDTO(@NotNull CompositeNodeDTO compositeNodeDTO) {
		return new CompositeNode(
				compositeNodeDTO.getComponents().stream().map(this::fromPrimitiveNodeDTO).collect(Collectors.toList()),
				compositeNodeDTO.getSplitter().stream().map(this::fromSplitterDTO).collect(Collectors.toList()),
				compositeNodeDTO.getExamples(),
				compositeNodeDTO.getCleansingPattern(),
				null,
				compositeNodeDTO.getUuid()
		);
	}

	@NotNull
	private SetNode.Child fromChildDTO(@NotNull SetNodeDTO.ChildDTO childDTO) {
		return new SetNode.Child(
				fromSelectorDTO(childDTO.getSelector()),
				fromNodeDTO(childDTO.getNode())
		);
	}

	@NotNull
	private Selector fromSelectorDTO(@NotNull SelectorDTO selectorDTO) {
		if (selectorDTO instanceof WildCardSelectorDTO) {
			return new WildcardSelector();
		} else if (selectorDTO instanceof IndexSelectorDTO) {
			return fromIndexSelectorDTO((IndexSelectorDTO) selectorDTO);
		} else if (selectorDTO instanceof PatternSelectorDTO) {
			return fromPatternSelectorDTO((PatternSelectorDTO) selectorDTO);
		}
		throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid NodeType");
	}

	@NotNull
	private IndexSelector fromIndexSelectorDTO(@NotNull IndexSelectorDTO indexSelectorDTO) {
		return new IndexSelector(indexSelectorDTO.getElementIndex());
	}

	@NotNull
	private PatternSelector fromPatternSelectorDTO(@NotNull PatternSelectorDTO patternSelectorDTO) {
		return new PatternSelector(
				patternSelectorDTO.getSelections(),
				patternSelectorDTO.getOffset(),
				patternSelectorDTO.getModulus()
		);
	}

	@NotNull
	private Splitter fromSplitterDTO(@NotNull SplitterDTO splitterDTO) {
		return new Splitter(
				splitterDTO.getPattern()
		);
	}

	@Nullable
	private EntityType fromEntityTypeDTO(@Nullable EntityTypeDTO entityTypeDTO) {
		if (entityTypeDTO == null) {
			return null;
		}
		return new EntityType(
				entityTypeDTO.getUuid(),
				entityTypeDTO.getLabel(),
				entityTypeDTO.getOriginalLabel(),
				entityTypeDTO.getDescription(),
				fromEntityConceptDTO(entityTypeDTO.getConcept()),
				null,
				entityTypeDTO.getId()
		);
	}

	@NotNull
	private EntityConcept fromEntityConceptDTO(@NotNull EntityConceptDTO entityConceptDTO) {
		return new EntityConcept(
				entityConceptDTO.getUuid(),
				entityConceptDTO.getName(),
				entityConceptDTO.getDescription(),
				entityConceptDTO.getSourceURI(),
				null,
				entityConceptDTO.getId()
		);
	}

	@NotNull
	private EntityConceptSuggestion fromEntityConceptSuggestionDTO(@NotNull EntityConceptSuggestionDTO entityConceptSuggestionDTO) {
		return new EntityConceptSuggestion(
				fromEntityConceptDTO(entityConceptSuggestionDTO.getEntityConcept()),
				entityConceptSuggestionDTO.getWeight()
		);
	}

	@NotNull
	private DataType fromDataTypeDTO(@NotNull PrimitiveNodeDTO.DataTypeDTO dataTypeDTO) {
		return DataType.fromIdentifier(dataTypeDTO.identifier);
	}
}