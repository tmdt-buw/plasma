package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * This node represents a node that is the root of an object.
 * All outgoing edges are to be considered child nodes are to be considered to represent attributes.
 */
@JsonTypeName("ObjectNode")
public class ObjectNode extends MappableSyntaxNode {

    private static final long serialVersionUID = 8596616883718165165L;

    public ObjectNode(@NotNull String label) {
        super(label);
    }

    public ObjectNode(
            @NotNull String label,
            @Nullable Double xCoordinate,
            @Nullable Double yCoordinate
    ) {
        this(UUID.randomUUID().toString(), label, null, xCoordinate, yCoordinate, true, false);
    }

    public ObjectNode(
            @NotNull String label,
            List<String> path,
            @Nullable Double xCoordinate,
            @Nullable Double yCoordinate
    ) {
        super(UUID.randomUUID().toString(), label, path, xCoordinate, yCoordinate, true, false);
    }

    @JsonCreator
    public ObjectNode(
            @NotNull @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @Nullable @JsonProperty(PATH_PROPERTY) List<String> path,
            @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
            @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
            @JsonProperty(VISIBLE_PROPERTY) boolean visible,
            @JsonProperty(DISABLED_PROPERTY) boolean disabled
    ) {
        super(uuid, label, path, xCoordinate, yCoordinate, visible, disabled);
    }

    @Override
    public ObjectNode copy() {
        return new ObjectNode(getUuid(),
                getLabel(),
                getPath(),
                getXCoordinate(),
                getYCoordinate(),
                isVisible(),
                isDisabled());
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return super.toString() + " | ObjectNode: " + getPathAsJSONPointer();
    }
}
