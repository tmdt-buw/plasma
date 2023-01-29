package de.buw.tmdt.plasma.services.dps.conversion.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


/**
 * Maps data to JSON objects according to a given structure.
 * Packets are stored until Message has {@code TaskAdditionalDataType.EXTRACTION_IS_LAST_PACKET} set to true.
 * In case of last packet, all packets stored until this point are emitted.
 */
public class MapJSON {
    private static final long serialVersionUID = 7241358429325261272L;
    private static final Logger log = LoggerFactory.getLogger(MapJSON.class);
    private static final String JSON_LINE_DELIMITER = "\n";
    private ObjectMapper mapper = new ObjectMapper();
    private transient TreeNode structure = null;
    private boolean addLineDelimiter = false;

    /**
     * Set Line Delimiter.
     * Append a line delimiter to each json object.
     * @param addLineDelimiter The delimiter
     */
    public void setAddLineDelimiter(String addLineDelimiter) {
        this.addLineDelimiter = Boolean.parseBoolean(addLineDelimiter);
    }

    /**
     * Set JSON structure template.
     * The structure of the resulting JSON object, including the defined mappings.
     * @param structure The structure in a serialized form
     * @throws IllegalArgumentException In case the serialized string cannot be interpreted
     */
    public void setStructure(String structure) throws IllegalArgumentException {
        try {
            this.structure = mapper.readValue(structure, TreeNode.class);
        } catch (IOException e) {
            log.error("Failed to parse entity mapping", e);
            throw new IllegalArgumentException("Failed parse entity mapping", e);
        }
    }

    /**
     * Set JSON structure template.
     * The structure of the resulting JSON object, including the defined mappings.
     * @param structure The structure
     * @throws IllegalArgumentException In case the structure contains errors
     */
    public void setStructure(TreeNode structure) throws IllegalArgumentException {
            this.structure = structure;
    }

    /*

    public void execute(Data data) throws MappingException {
        SLTObject payload = (SLTObject) data.getValue(OUTPUT_PROCESSING_PAYLOAD);
        try {
            ObjectNode resultObject = createObject(structure, ObjectUtilities.deepCopy(payload));
            Data emitData = new Data();
            emitData.addField(OUTPUT_EXTRACTION_FORMATTED_DATA, this.addLineDelimiter ? resultObject.toString() + JSON_LINE_DELIMITER
                    : resultObject.toString());
            dataEmitter.emitData(emitData);
        } catch (ClassCastException | NullPointerException e) {
            log.error("Unexpected field or value in the tuple. Abort execution.", e);
            throw new MappingException("Values of the input Tuple couldn't be casted correctly.", e);
        } catch (IOException | ClassNotFoundException e) {
            log.error("Couldn't create deepCopy of the ingoing value.", e);
            throw new MappingException("Couldn't create deepCopy of the ingoing value.", e);
        }
    }

    private ObjectNode createObject(TreeNode structureNode, SLTObject payload) throws MappingException {
        ObjectNode resultObject = objectMapper.createObjectNode();

        // ensure that there are no duplicate keys for the children
        long distinctCount = structureNode.getChildren().stream().map(TreeNode::getLabel).distinct().count();
        if (distinctCount != structureNode.getChildren().size()) {
            throw new MappingException("Found duplicate key in structure node " + structureNode);
        }

        for (TreeNode child : structureNode.getChildren()) {
            if (child.isLeaf()) {
                String value = createLeaf(child, payload);
                resultObject.put(child.getLabel(), value);
            } else if (child.isArrayRoot()) {
                resultObject.set(child.getLabel(), createArray(child, payload));
            } else {
                resultObject.set(child.getLabel(), createObject(child, payload));
            }
        }
        return resultObject;

    }

    private String createLeaf(TreeNode structureNode, SLTObject payload) throws MappingException {
        String mappedEntityId = structureNode.getMappedEntityId();
        if (mappedEntityId == null) {
            return null;
        }
        String value = getValueForEntityId(mappedEntityId, payload);
        if (value == null) {
            throw new MappingException("Could not obtain value for entityId " + mappedEntityId + " + from SLT");
        }
        return value;

    }

    private ArrayNode createArray(TreeNode structureNode, SLTObject payload) throws MappingException {
        ArrayNode resultArray = objectMapper.createArrayNode();
        if (!structureNode.isArrayRoot()) {
            throw new MappingException("TreeNode " + structureNode.getUuid() + " is not declared as array");
        }

        List<String> nodeTypes = structureNode.getChildren().stream()
                .map(c -> {
                    if (c.isLeaf()) {
                        return "leaf";
                    }
                    if (c.isArrayRoot()) {
                        return "array";
                    }
                    return "object";
                })
                .distinct()
                .collect(Collectors.toList());

        if (nodeTypes.size() > 1) {
            throw new MappingException("Cannot map array " + structureNode.getLabel() + " of different types, currently present: " + nodeTypes);
        }

        for (TreeNode child : structureNode.getChildren()) {
            if (child.isLeaf()) {
                String value = createLeaf(child, payload);
                resultArray.add(value);
            } else if (child.isArrayRoot()) {
                resultArray.add(createArray(child, payload));
            } else {
                resultArray.add(createObject(child, payload));
            }
        }
        return resultArray;
    }


     * Searches the given SLT node for the needed value for the given entity id.
     * Calls itself recursively if nested.
     *
     * @param entityId The entity id to obtain the value for
     * @param node     The object containing the data
     * @return The value for the given leaf, identified by the EntityType id

    private String getValueForEntityId(String entityId, SLTNode node) {

        if (node instanceof SLTValue) {
            SLTValue value = (SLTValue) node;
            if (value.getEntityType().getLabel().equals(entityId)) {
                return value.getAsString();
            }
            return null;
        }

        if (node instanceof SLTObject) {
            SLTObject value = (SLTObject) node;
            for (Map.Entry<SLTKey, SLTObject.Entry> entry : value.entrySet()) {
                String valueForLeafNode = getValueForEntityId(entityId, entry.getValue());
                if (valueForLeafNode != null) {
                    return valueForLeafNode;
                }
            }
        }

        if (node instanceof SLTValueSet) {
            SLTValueSet valueSet = (SLTValueSet) node;
            for (SLTValue entry : valueSet) {
                String valueForLeafNode = getValueForEntityId(entityId, entry);
                if (valueForLeafNode != null) {
                    return valueForLeafNode;
                }
            }
        }

        if (node instanceof SLTObjectSet) {
            SLTObjectSet valueSet = (SLTObjectSet) node;
            for (SLTObject entry : valueSet) {
                String valueForLeafNode = getValueForEntityId(entityId, entry);
                if (valueForLeafNode != null) {
                    return valueForLeafNode;
                }
            }
        }
        log.warn("Unknown type of SLTNode found");
        return null;
    }

     */
}
