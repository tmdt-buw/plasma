package de.buw.tmdt.plasma.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Contains mappings needed to render supplementary edges in the frontend.
 * Only used for frontend issues, can be ignored everywhere else
 */
public class ModelMapping implements Serializable {

    private static final long serialVersionUID = -6888269571341658964L;

    public static final String PRIMITIVE_PROPERTY = "schemaNodeUuid";
    public static final String NODE_PROPERTY = "semanticNodeUuid";
    public static final String PARENT_PROPERTY = "parentUuid";

    @NotNull
    private String primitiveUUID;

    @NotNull
    private String nodeUUID;

    /**
     * The parent syntax node of the node identified by primitiveUUID.
     * Used for rendering.
     */
    @NotNull
    private String parentUUID;

    @JsonCreator
    public ModelMapping(
            @JsonProperty(PRIMITIVE_PROPERTY) @NotNull String primitiveUUID,
            @JsonProperty(NODE_PROPERTY) @NotNull String nodeUUID,
            @JsonProperty(PARENT_PROPERTY) @NotNull String parentUUID

    ) {
        this.parentUUID = parentUUID;
        this.nodeUUID = nodeUUID;
        this.primitiveUUID = primitiveUUID;
    }

    @NotNull
    @JsonProperty(PARENT_PROPERTY)
    public String getParentUUID() {
        return parentUUID;
    }

    public void setParentUUID(@NotNull String parentUUID) {
        this.parentUUID = parentUUID;
    }

    @NotNull
    @JsonProperty(PRIMITIVE_PROPERTY)
    public String getPrimitiveUUID() {
        return primitiveUUID;
    }

    public void setPrimitiveUUID(@NotNull String primitiveUUID) {
        this.primitiveUUID = primitiveUUID;
    }

    @NotNull
    @JsonProperty(NODE_PROPERTY)
    public String getNodeUUID() {
        return this.nodeUUID;
    }

    public void setNodeUUID(@NotNull String nodeUUID) {
        this.nodeUUID = nodeUUID;
    }

    /**
     * Creates a deep clone of the current entity.
     *
     * @return The cloned entity
     */
    public ModelMapping copy() {
        return new ModelMapping(getPrimitiveUUID(), getNodeUUID(), getParentUUID());
    }

    @Override
    public String toString() {
        return "ModelMapping{" +
                "parentUUID=" + parentUUID +
                ", nodeId=" + nodeUUID +
                ", primitiveUUID=" + primitiveUUID +
                '}';
    }
}
