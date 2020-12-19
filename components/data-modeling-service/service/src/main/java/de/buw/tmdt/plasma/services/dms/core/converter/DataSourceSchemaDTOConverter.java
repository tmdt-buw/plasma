package de.buw.tmdt.plasma.services.dms.core.converter;

import de.buw.tmdt.plasma.services.dms.core.model.datasource.DataSourceSchema;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.*;
import de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members.*;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.PrimitiveEntityTypeEdgeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.*;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSourceSchemaDTOConverter {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceSchemaDTOConverter.class);
	private static final String ROOT_ELEMENT_LABEL = "ROOT";
	private static final String COLLISION_ELEMENT_LABEL = "COLLISION";

	private final OperationLookUp operationLookUp;
	private final SchemaOperationDTOConverter schemaOperationDTOConverter;
	private final SemanticModelDTOConverter semanticModelDTOConverter;

	@Autowired
	public DataSourceSchemaDTOConverter(
			@NotNull OperationLookUp operationLookUp,
			@NotNull SchemaOperationDTOConverter schemaOperationDTOConverter,
			@NotNull SemanticModelDTOConverter semanticModelDTOConverter
	) {
		this.operationLookUp = operationLookUp;
		this.schemaOperationDTOConverter = schemaOperationDTOConverter;
		this.semanticModelDTOConverter = semanticModelDTOConverter;
	}

	@NotNull
	public DataSourceSchemaDTO toDTO(@NotNull DataSourceSchema dataSourceSchema) {
		SemanticModelDTO semanticModelDTO = semanticModelDTOConverter.toDTO(dataSourceSchema.getSemanticModel());
		SerializationContext serializationContext = new SerializationContext(
				operationLookUp.getOperationHandles(dataSourceSchema.getSyntaxModel()),
				semanticModelDTO.getNodes()
		);
		Pair<SyntaxModelDTO, List<PrimitiveEntityTypeEdgeDTO>> syntaxModel = toDTO(dataSourceSchema.getSyntaxModel(), serializationContext);

		SemanticModelDTO modifiedSemanticModel = new SemanticModelDTO(
				semanticModelDTO.getXCoordinate(),
				semanticModelDTO.getYCoordinate(),
				semanticModelDTO.getId().toString(),
				semanticModelDTO.getUuid(),
				semanticModelDTO.getLabel(),
				semanticModelDTO.getDescription(),
				serializationContext.entityTypes,
				semanticModelDTO.getEdges()
		);

		return new DataSourceSchemaDTO(
				syntaxModel.getLeft(),
				modifiedSemanticModel,
				syntaxModel.getRight(),
				// re-enitialize, as we have reloaded from db rendering all ids invalid
				// TODO fix ids
				new ArrayList<>(),
				dataSourceSchema.isFinalized()

		);
	}

	@NotNull
	private Pair<SyntaxModelDTO, List<PrimitiveEntityTypeEdgeDTO>> toDTO(@NotNull Node root, @NotNull SerializationContext serializationContext) {
		SchemaNodeDTO rootDTO;
		if (root instanceof SetNode) {
			rootDTO = toDTO((SetNode) root, ROOT_ELEMENT_LABEL, serializationContext);
		} else if (root instanceof ObjectNode) {
			rootDTO = toDTO((ObjectNode) root, ROOT_ELEMENT_LABEL, serializationContext);
		} else {
			throw new IllegalArgumentException(root.toString());
		}
		serializationContext.nodeLookUp.put(root.getUuid(), rootDTO);
		return new Pair<>(
				new SyntaxModelDTO(
						rootDTO.getUuid(),
						new ArrayList<>(serializationContext.nodeLookUp.values()),
						serializationContext.edges
				), serializationContext.syntaxToSemanticModelEdges
		);
	}

	private void createChild(@NotNull SchemaNodeDTO parent, @NotNull Node node, @NotNull String label, @NotNull SerializationContext serializationContext) {
		if (serializationContext.nodeLookUp.containsKey(node.getUuid())) {
			SchemaNodeDTO oldDTO = serializationContext.nodeLookUp.get(node.getUuid());
			logger.warn("Old Node: {}", oldDTO);
			logger.warn("Replacing Node: {}", node);
			throw new RuntimeException("Ambiguity in node keys detected while converting model to dto for node id `" + oldDTO.getUuid() + "`.");
		}

		SchemaNodeDTO newDTO;
		if (node instanceof SetNode) {
			newDTO = toDTO((SetNode) node, label, serializationContext);
		} else if (node instanceof ObjectNode) {
			newDTO = toDTO((ObjectNode) node, label, serializationContext);
		} else if (node instanceof CompositeNode) {
			newDTO = toDTO((CompositeNode) node, label, serializationContext);
		} else if (node instanceof CollisionNode) {
			newDTO = toDTO((CollisionNode) node, label, serializationContext);
		} else if (node instanceof PrimitiveNode) {
			newDTO = toDTO(parent, (PrimitiveNode) node, label, serializationContext);
		} else {
			throw new IllegalArgumentException("Unsupported sub-type of " + Node.class + " found: " + node.getClass());
		}
		serializationContext.edges.add(new EdgeDTO(parent, newDTO, ""));
		serializationContext.nodeLookUp.put(node.getUuid(), newDTO);
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private SetNodeDTO toDTO(@NotNull SetNode setNode, @NotNull String label, @NotNull SerializationContext serializationContext) {
		List<SyntacticOperationDTO> syntacticOperationDTOs = getOperationDTOs(setNode, serializationContext);

		Double xCoordinate = null;
		Double yCoordinate = null;
		if (setNode.getPosition() != null) {
			xCoordinate = setNode.getPosition().getXCoordinate();
			yCoordinate = setNode.getPosition().getYCoordinate();
		}

		SetNodeDTO root = new SetNodeDTO(xCoordinate, yCoordinate, setNode.getUuid(), label, syntacticOperationDTOs, setNode.isValid());

		for (SetNode.Child child : setNode.getChildren()) {
			createChild(root, child.getNode(), toLabel(child.getSelector()), serializationContext);
		}

		return root;
	}

	@NotNull
	private String toLabel(@NotNull Selector selector) {
		if (selector instanceof WildcardSelector) {
			return "*";
		} else if (selector instanceof IndexSelector) {
			return StringUtilities.integerToOrdinal(((IndexSelector) selector).getElementIndex(), true);
		} else if (selector instanceof PatternSelector) {
			return selector.serialize();
		} else {
			logger.warn("Unknown subtype of {} found: {}", Selector.class, selector.getClass());
			return selector.serialize();
		}
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private ObjectNodeDTO toDTO(@NotNull ObjectNode objectNode, @NotNull String label, @NotNull SerializationContext serializationContext) {
		List<SyntacticOperationDTO> syntacticOperationDTOs = getOperationDTOs(objectNode, serializationContext);

		Double xCoordinate = null;
		Double yCoordinate = null;
		if (objectNode.getPosition() != null) {
			xCoordinate = objectNode.getPosition().getXCoordinate();
			yCoordinate = objectNode.getPosition().getYCoordinate();
		}

		ObjectNodeDTO root = new ObjectNodeDTO(xCoordinate, yCoordinate, objectNode.getUuid(), label, syntacticOperationDTOs, objectNode.isValid());

		for (Map.Entry<String, Node> entry : objectNode.getChildren().entrySet()) {
			createChild(root, entry.getValue(), entry.getKey(), serializationContext);
		}

		return root;
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private CompositeDTO toDTO(@NotNull CompositeNode compositeNode, @NotNull String label, @NotNull SerializationContext serializationContext) {
		List<SyntacticOperationDTO> syntacticOperationDTOs = getOperationDTOs(compositeNode, serializationContext);

		Double xCoordinate = null;
		Double yCoordinate = null;
		if (compositeNode.getPosition() != null) {
			xCoordinate = compositeNode.getPosition().getXCoordinate();
			yCoordinate = compositeNode.getPosition().getYCoordinate();
		}

		CompositeDTO root = new CompositeDTO(
				xCoordinate,
				yCoordinate,
				compositeNode.getUuid(),
				label,
				syntacticOperationDTOs,
				compositeNode.isValid(),
				compositeNode.getCleansingPattern(),
				compositeNode.getSplitter().stream().map(this::splitterToDTO).collect(Collectors.toList())
		);

		List<PrimitiveNode> components = compositeNode.getComponents();
		List<Splitter> splitter = compositeNode.getSplitter();
		for (int i = 0; i < components.size(); i++) {
			createChild(root, components.get(i), StringUtilities.integerToOrdinal(i, true), serializationContext);
		}

		final List<@NotNull SchemaNodeDTO> componentDTOs = serializationContext.edges.stream()
				.filter(edgeDTO -> edgeDTO.getFrom().equals(compositeNode.getUuid()))
				.map(EdgeDTO::getTo)
				.map(serializationContext.nodeLookUp::get)
				.collect(Collectors.toList());

		for (int i = 0; i < componentDTOs.size() - 1; i++) {
			SchemaNodeDTO fromComponentDTO = componentDTOs.get(i);
			SchemaNodeDTO toComponentDTO = componentDTOs.get(i + 1);
			serializationContext.edges.add(new EdgeDTO(fromComponentDTO, toComponentDTO, splitter.get(i).getPattern()));
		}

		return root;
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private CollisionSchemaDTO toDTO(@NotNull CollisionNode collisionNode, @NotNull String label, @NotNull SerializationContext serializationContext) {
		List<SyntacticOperationDTO> syntacticOperationDTOs = getOperationDTOs(collisionNode, serializationContext);

		Double xCoordinate = null;
		Double yCoordinate = null;
		if (collisionNode.getPosition() != null) {
			xCoordinate = collisionNode.getPosition().getXCoordinate();
			yCoordinate = collisionNode.getPosition().getYCoordinate();
		}

		CollisionSchemaDTO root = new CollisionSchemaDTO(
				xCoordinate,
				yCoordinate,
				collisionNode.getUuid(),
				COLLISION_ELEMENT_LABEL,
				syntacticOperationDTOs,
				collisionNode.isValid()
		);

		for (Node node : Arrays.asList(collisionNode.getPrimitiveNode(), collisionNode.getSetNode(), collisionNode.getSetNode())) {
			createChild(root, node, label, serializationContext);
		}

		return root;
	}

	@Nullable
	private SplitterDTO splitterToDTO(@Nullable Splitter splitter) {
		if (null == splitter) {
			return null;
		}
		return new SplitterDTO(splitter.getPattern());
	}

	@NotNull
	@SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Explicit check that value is not null")
	private PrimitiveDTO toDTO(
			@NotNull SchemaNodeDTO parent,
			@NotNull PrimitiveNode primitiveNode,
			@NotNull String label,
			@NotNull SerializationContext serializationContext
	) {
		//map suggestions
		List<EntityConceptSuggestionDTO> entityConceptSuggestionDTOs = primitiveNode.getEntityConceptSuggestions().stream()
				.sorted((suggestion1, suggestion2) -> (int) Math.signum(suggestion2.getWeight() - suggestion1.getWeight()))
				.map(entityConceptSuggestion -> new EntityConceptSuggestionDTO(
						entityConceptSuggestion.getWeight(),
						semanticModelDTOConverter.toDTO(entityConceptSuggestion.getEntityConcept())
				)).collect(de.buw.tmdt.plasma.utilities.collections.Collectors.getSerializableListCollector());

		//inject operations
		List<SyntacticOperationDTO> syntacticOperationDTOs = getOperationDTOs(primitiveNode, serializationContext);
		syntacticOperationDTOs.sort(Comparator.comparing(SyntacticOperationDTO::getName));

		//transform position to coordinates
		Double xCoordinate = null;
		Double yCoordinate = null;
		if (primitiveNode.getPosition() != null) {
			xCoordinate = primitiveNode.getPosition().getXCoordinate();
			yCoordinate = primitiveNode.getPosition().getYCoordinate();
		}

		final PrimitiveDTO primitiveDTO = new PrimitiveDTO(
				xCoordinate,
				yCoordinate,
				primitiveNode.getUuid(),
				label,
				syntacticOperationDTOs,
				primitiveNode.isValid(),
				primitiveNode.getDataType().identifier,
				entityConceptSuggestionDTOs,
				primitiveNode.getExamples(),
				primitiveNode.getCleansingPattern()
		);

		if (primitiveNode.getEntityType() != null) {
			EntityTypeDTO unmappedEntityTypeDTO = serializationContext.entityTypes.stream()
					.filter(entityTypeDTO -> primitiveNode.getEntityType().getId().equals(entityTypeDTO.getId()))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("Missing entity type in semantic model which was referenced by: " + primitiveNode));
			unmappedEntityTypeDTO.setMappedToData(true);

			serializationContext.syntaxToSemanticModelEdges.add(new PrimitiveEntityTypeEdgeDTO(
					parent.getUuid(),
					unmappedEntityTypeDTO.getId(),
					primitiveDTO.getUuid()
			));
		}
		return primitiveDTO;
	}

	@NotNull
	private List<SyntacticOperationDTO> getOperationDTOs(@NotNull Node node, @NotNull SerializationContext serializationContext) {
		return serializationContext.parameterDefinitionLookup.getOrDefault(node, new HashSet<>()).stream()
				.map(handle -> new SyntacticOperationDTO(
						handle.getOperation().getName(),
						handle.getOperation().getLabel(),
						handle.getOperation().getDescription(),
						schemaOperationDTOConverter.toDTO(handle.getParameterDefinition())
				)).collect(Collectors.toList());
	}

	private static class SerializationContext {
		private final HashMap<@NotNull UUID, @NotNull SchemaNodeDTO> nodeLookUp;
		private final ArrayList<@NotNull EdgeDTO> edges;
		private final ArrayList<@NotNull PrimitiveEntityTypeEdgeDTO> syntaxToSemanticModelEdges;
		private final HashMap<Node, Set<Operation.Handle>> parameterDefinitionLookup;
		private final ArrayList<EntityTypeDTO> entityTypes;

		public SerializationContext(
				HashMap<Node, Set<Operation.Handle>> parameterDefinitionLookup,
				List<? extends EntityTypeDTO> entityTypes
		) {
			this.parameterDefinitionLookup = parameterDefinitionLookup;
			this.nodeLookUp = new HashMap<>();
			this.edges = new ArrayList<>();
			this.syntaxToSemanticModelEdges = new ArrayList<>();
			this.entityTypes = new ArrayList<>(entityTypes);
		}
	}
}
