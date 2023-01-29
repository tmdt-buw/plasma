package de.buw.tmdt.plasma.services.dps.conversion.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TreeNode {

    public static final String UUID_PROPERTY = "uuid";
    public static final String LABEL_PROPERTY = "label";
    public static final String ENTITY_ID_PROPERTY = "entityId";
    public static final String CHILDREN_PROPERTY = "children";
    public static final String ARRAY_PROPERTY = "isArray";


    @NotNull
    private final String uuid;

    @NotNull
    private final String label;

    /**
     * References the entity id of an EntityType to insert the data.
     */
    @Nullable
    private String mappedEntityId;

    @NotNull
    private final List<TreeNode> children;

    private boolean isArrayRoot;

    /**
     * Creates a new TreeNode to represent a nested structure to build files by inserting values referenced
     * by EntityType entityIds.
     *
     * @param uuid           The uuid for this element
     * @param label          The label this node will have in the resulting file
     * @param mappedEntityId The entityId of the EntityType that links to the data in the SLT.
     *                       Must be null for objects and arrays.
     *                       If this is set to null, the value of this pair will also be null.
     * @param children       The nested elements in case of an object or array. Empty implicates leaf node.
     */
    public TreeNode(@Nullable String uuid,
                    @NotNull String label,
                    @Nullable String mappedEntityId,
                    @Nullable List<TreeNode> children) {
        this(uuid, label, mappedEntityId, children, false);
    }

    /**
     * Creates a new TreeNode to represent a nested structure to build files by inserting values referenced
     * by EntityType entityIds. Can either be a leaf node to contain data, an array or an object each with nested values.
     *
     * @param uuid           The uuid for this element
     * @param label          The label this node will have in the resulting file
     * @param mappedEntityId The entityId of the EntityType that links to the data in the SLT.
     *                       Must be null for objects and arrays.
     *                       If this is set to null, the value of this pair will also be null.
     * @param children       The nested elements in case of an object or array. Empty when leaf node
     * @param isArrayRoot    Specifies if this node represents an array instead of an object.
     */
    @JsonCreator
    public TreeNode(@Nullable @JsonProperty(UUID_PROPERTY) String uuid,
                    @NotNull @JsonProperty(LABEL_PROPERTY) String label,
                    @Nullable @JsonProperty(ENTITY_ID_PROPERTY) String mappedEntityId,
                    @Nullable @JsonProperty(CHILDREN_PROPERTY) List<TreeNode> children,
                    @JsonProperty(ARRAY_PROPERTY) Boolean isArrayRoot) {
        this.uuid = uuid == null ? UUID.randomUUID().toString() : uuid;
        this.label = label;
        this.mappedEntityId = mappedEntityId;
        this.children = children == null ? new ArrayList<>() : children;
        this.isArrayRoot = isArrayRoot;
    }

    public void addChild(TreeNode child) throws MappingException {
        if (mappedEntityId != null) {
            throw new MappingException("Unable to add children to a leaf node, remove mapping first");
        }
        this.children.add(child);
    }

    public void removeChild(TreeNode child) {
        children.remove(child);
    }

    public void removeChild(String uuid) {
        children.removeIf(node -> Objects.equals(node.getUuid(), uuid));
    }

    @NotNull
    @JsonProperty(UUID_PROPERTY)
    public String getUuid() {
        return uuid;
    }

    @NotNull
    @JsonProperty(LABEL_PROPERTY)
    public String getLabel() {
        return label;
    }

    @Nullable
    @JsonProperty(ENTITY_ID_PROPERTY)
    public String getMappedEntityId() {
        return mappedEntityId;
    }

    public void setMappedEntityId(String mappedEntityId) throws MappingException {
        if (!children.isEmpty()) {
            throw new MappingException("Unable to map entity id to an inner node");
        }
        this.mappedEntityId = mappedEntityId;
    }

    @NotNull
    @JsonProperty(CHILDREN_PROPERTY)
    public List<TreeNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @JsonIgnore
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    @JsonProperty(ARRAY_PROPERTY)
    public boolean isArrayRoot() {
        return isArrayRoot;
    }

    public void setArrayRoot(boolean arrayRoot) {
        isArrayRoot = arrayRoot;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "uuid='" + uuid + '\'' +
                ", label='" + label + '\'' +
                ", mappedEntityId='" + mappedEntityId + '\'' +
                ", children=" + children +
                ", isArrayRoot=" + isArrayRoot +
                '}';
    }
}

