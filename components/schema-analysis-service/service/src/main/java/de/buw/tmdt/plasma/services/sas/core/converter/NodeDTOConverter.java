package de.buw.tmdt.plasma.services.sas.core.converter;

import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityConcept;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.*;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.members.*;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NodeDTOConverter {

    public NodeDTO nodeToDTO(Node node, int exampleLimit) {

        if (null == node) {
            return null;
        } else if (node instanceof SetNode) {
            return setNodeToDTO((SetNode) node, exampleLimit);
        } else if (node instanceof ObjectNode) {
            return objectNodeToDTO((ObjectNode) node, exampleLimit);
        } else if (node instanceof PrimitiveNode) {
            return primitiveNodeToDTO((PrimitiveNode) node, exampleLimit);
        } else if (node instanceof CompositeNode) {
            return compositeNodeToDTO((CompositeNode) node, exampleLimit);
        } else if (node instanceof CollisionNode) {
            return collisionNodeToDTO((CollisionNode) node, exampleLimit);
        } else {
            return null;
        }
    }

    private SetNodeDTO setNodeToDTO(SetNode setNode, int exampleLimit) {

        if (null == setNode) {
            return null;
        }

        return new SetNodeDTO(
                setNode.getUuid(),
                setNode.getChildren().stream().map(x -> childToDTO(x, exampleLimit)).collect(Collectors.toList())
        );
    }

    private SetNodeDTO.ChildDTO childToDTO(SetNode.Child child, int exampleLimit) {

        if (null == child) {
            return null;
        }

        return new SetNodeDTO.ChildDTO(
                child.getId(),
                nodeToDTO(child.getNode(), exampleLimit),
                selectorToDTO(child.getSelector())
        );
    }

    private ObjectNodeDTO objectNodeToDTO(ObjectNode objectNode, int exampleLimit) {

        if (null == objectNode) {
            return null;
        }

        Map<String, NodeDTO> children = new HashMap<>();
        for (Map.Entry<String, Node> stringNodeEntry : objectNode.getChildren().entrySet()) {
            children.put(stringNodeEntry.getKey(), nodeToDTO(stringNodeEntry.getValue(), exampleLimit));
        }
        return new ObjectNodeDTO(children, objectNode.getUuid());
    }

    private PrimitiveNodeDTO primitiveNodeToDTO(PrimitiveNode primitiveNode, int exampleLimit) {

        if (null == primitiveNode) {
            return null;
        }

        return new PrimitiveNodeDTO(
                primitiveNode.getUuid(),
                null == primitiveNode.getEntityType() ? null : entityTypeToDTO(primitiveNode.getEntityType()),
                dataTypeToDTO(primitiveNode.getDataType()),
                primitiveNode.getCleansingPattern(),
                primitiveNode.getExamples().stream()
                        .distinct()
                        .limit(exampleLimit)
                        .collect(Collectors.toList()),
                primitiveNode.getEntityConceptSuggestions().stream().map(this::toDTO).collect(Collectors.toList())
        );
    }

    private PrimitiveNodeDTO.DataTypeDTO dataTypeToDTO(PrimitiveNode.DataType dataType) {

        if (null == dataType) {
            return null;
        }

        return PrimitiveNodeDTO.DataTypeDTO.fromIdentifier(dataType.identifier);
    }

    private CollisionNodeDTO collisionNodeToDTO(CollisionNode collisionNode, int exampleLimit) {

        if (null == collisionNode) {
            return null;
        }

        return new CollisionNodeDTO(
                collisionNode.getUuid(),
                primitiveNodeToDTO(collisionNode.getPrimitiveNode(), exampleLimit),
                objectNodeToDTO(collisionNode.getObjectNode(), exampleLimit),
                setNodeToDTO(collisionNode.getSetNode(), exampleLimit)
        );
    }

    private CompositeNodeDTO compositeNodeToDTO(CompositeNode compositeNode, int exampleLimit) {

        if (null == compositeNode) {
            return null;
        }

        return new CompositeNodeDTO(
                compositeNode.getComponents().stream().map(primitiveNode -> primitiveNodeToDTO(primitiveNode, exampleLimit)).collect(Collectors.toList()),
                compositeNode.getSplitter().stream().map(this::splitterToDTO).collect(Collectors.toList()),
                compositeNode.getExamples(),
                compositeNode.getCleansingPattern(),
                compositeNode.getUuid()
        );
    }

    private EntityConceptDTO toDTO(EntityConcept entityConcept) {

        if (null == entityConcept) {
            return null;
        }

        return new EntityConceptDTO(
                (String) null,//entityConcept.getId(),       //todo: whats here?
                entityConcept.getUuid(),
                entityConcept.getName(),
                entityConcept.getDescription(),
                entityConcept.getSourceURI()
        );
    }

    private EntityConceptSuggestionDTO toDTO(EntityConceptSuggestion entityConceptSuggestion) {

        if (null == entityConceptSuggestion) {
            return null;
        }

        return new EntityConceptSuggestionDTO(
                toDTO(entityConceptSuggestion.getEntityConcept()),
                entityConceptSuggestion.getWeight()
        );
    }

    private EntityTypeDTO entityTypeToDTO(EntityType entityType) {

        if (null == entityType) {
            return null;
        }

        return new EntityTypeDTO(
                entityType.getId(),
                entityType.getUuid(),
                entityType.getLabel(),
                entityType.getOriginalLabel(),
                entityType.getDescription(),
                toDTO(entityType.getEntityConcept()),
                null
        );
    }

    private SplitterDTO splitterToDTO(Splitter splitter) {

        if (null == splitter) {
            return null;
        }

        return new SplitterDTO(splitter.getPattern());
    }

    private SelectorDTO selectorToDTO(Selector selector) {

        if (null == selector) {
            return null;
        } else if (selector instanceof WildcardSelector) {
            return new WildCardSelectorDTO();
        } else if (selector instanceof IndexSelector) {
            return indexSelectorToDTO((IndexSelector) selector);
        } else if (selector instanceof PatternSelector) {
            return patternSelectorToDTO((PatternSelector) selector);
        }
        return null;
    }

    private IndexSelectorDTO indexSelectorToDTO(IndexSelector indexSelector) {

        if (null == indexSelector) {
            return null;
        }

        return new IndexSelectorDTO(indexSelector.getElementIndex());
    }

    private PatternSelectorDTO patternSelectorToDTO(PatternSelector patternSelector) {

        if (null == patternSelector) {
            return null;
        }

        return new PatternSelectorDTO(patternSelector.getSelections(), patternSelector.getOffset(), patternSelector.getModulus());
    }
}
