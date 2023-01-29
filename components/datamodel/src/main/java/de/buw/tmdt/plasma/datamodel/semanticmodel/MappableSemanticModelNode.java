package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Class.class, name = "Class"),
        @JsonSubTypes.Type(value = Literal.class, name = "Literal"),
})
public abstract class MappableSemanticModelNode extends SemanticModelNode {

    public static final String MAPPED_SYNTAX_NODE_UUID_PROPERTY = "syntaxNodeUuid";
    public static final String MAPPED_SYNTAX_NODE_LABEL_PROPERTY = "syntaxLabel";
    public static final String MAPPED_SYNTAX_NODE_PATH_PROPERTY = "syntaxPath";

    /**
     * The id of the syntax {@link de.buw.tmdt.plasma.datamodel.syntaxmodel.MappableSyntaxNode} this object is mapped to.
     * Will be null if not mapped.
     */
    private String mappedSyntaxNodeUuid;

    /**
     * The original label of an attribute if this instantiation is mapped to one, null otherwise.
     * This may only be set when {@link #mappedSyntaxNodeUuid} is set also.
     */
    private String mappedSyntaxNodeLabel;

    /**
     * The path in the original data schema to identify this node.
     * This may only be set when {@link #mappedSyntaxNodeUuid} is set also.
     * Form is 'hop1->hop2->hop3->nodeLabel' where 'nodeLabel' is the original node label
     */
    private String mappedSyntaxNodePath;

    public MappableSemanticModelNode(@Nullable String uri, @NotNull String label) {
        super(uri, label, null, null, null);
    }

    public MappableSemanticModelNode(@Nullable String uri, @NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate) {
        super(uri, label, xCoordinate, yCoordinate, null);
    }

    public MappableSemanticModelNode(@Nullable String uri, @NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate, @Nullable String uuid) {
        super(uri, label, xCoordinate, yCoordinate, uuid);
    }

    public MappableSemanticModelNode(@Nullable String uri, @NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate, String mappedSyntaxNodeUuid, String mappedSyntaxNodeLabel, String mappedSyntaxNodePath, @Nullable String uuid) {
        super(uri, label, xCoordinate, yCoordinate, uuid);
        setMappedSyntaxNodeUuid(mappedSyntaxNodeUuid);
        setMappedSyntaxNodeLabel(mappedSyntaxNodeLabel);
        setMappedSyntaxNodePath(mappedSyntaxNodePath);
    }

    @JsonProperty(MAPPED_SYNTAX_NODE_UUID_PROPERTY)
    public String getMappedSyntaxNodeUuid() {
        return mappedSyntaxNodeUuid;
    }

    public void setMappedSyntaxNodeUuid(String mappedSyntaxNodeUuid) {
        this.mappedSyntaxNodeUuid = mappedSyntaxNodeUuid;
    }

    @JsonProperty(MAPPED_SYNTAX_NODE_LABEL_PROPERTY)
    public String getMappedSyntaxNodeLabel() {
        return mappedSyntaxNodeLabel;
    }

    public void setMappedSyntaxNodeLabel(String mappedSyntaxNodeLabel) {
        this.mappedSyntaxNodeLabel = mappedSyntaxNodeLabel;
    }

    @JsonProperty(MAPPED_SYNTAX_NODE_PATH_PROPERTY)
    public String getMappedSyntaxNodePath() {
        return mappedSyntaxNodePath;
    }

    public void setMappedSyntaxNodePath(String mappedSyntaxNodePath) {
        this.mappedSyntaxNodePath = mappedSyntaxNodePath;
    }

    @Override
    public boolean isMapped() {
        return getMappedSyntaxNodeUuid() != null;
    }

    public void unmap() {
        setMappedSyntaxNodeUuid(null);
        setMappedSyntaxNodeLabel(null);
        setMappedSyntaxNodePath(null);
    }
}
