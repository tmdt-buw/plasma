package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrimitiveNode.class, name = "PrimitiveNode"),
        @JsonSubTypes.Type(value = SetNode.class, name = "SetNode"),
        @JsonSubTypes.Type(value = ObjectNode.class, name = "ObjectNode"),
})
public abstract class MappableSyntaxNode extends SchemaNode {
    public MappableSyntaxNode(@NotNull String label, boolean isValid) {
        super(label, isValid);
    }

    public MappableSyntaxNode(@NotNull String uuid, @NotNull String label, @Nullable List<String> path, @Nullable Double xCoordinate, @Nullable Double yCoordinate, boolean isValid, boolean visible, boolean disabled) {
        super(uuid, label, path, xCoordinate, yCoordinate, isValid, visible, disabled);
    }
}
