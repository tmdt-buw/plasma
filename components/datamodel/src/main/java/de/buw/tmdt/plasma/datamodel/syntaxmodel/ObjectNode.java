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
public class ObjectNode extends SchemaNode {

    private static final long serialVersionUID = 8596616883718165165L;

    public ObjectNode(
            @NotNull String label,
            boolean isValid
    ) {
        super(label, isValid);
    }

    public ObjectNode(
            @NotNull String label,
            @Nullable Double xCoordinate,
            @Nullable Double yCoordinate,
            boolean isValid
    ) {
        super(UUID.randomUUID().toString(), label, null, xCoordinate, yCoordinate, isValid);
    }

    public ObjectNode(
            @NotNull String label,
            List<String> path,
            @Nullable Double xCoordinate,
            @Nullable Double yCoordinate,
            boolean isValid
    ) {
        super(UUID.randomUUID().toString(), label, path, xCoordinate, yCoordinate, isValid);
    }

    @JsonCreator
    public ObjectNode(
            @NotNull @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @Nullable @JsonProperty(PATH_PROPERTY) List<String> path,
            @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
            @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
            @JsonProperty(VALID_PROPERTY) boolean isValid
    ) {
        super(uuid, label, path, xCoordinate, yCoordinate, isValid);
    }

    @Override
    public ObjectNode copy() {
        return new ObjectNode(getUuid(),
                getLabel(),
                getPath(),
                getXCoordinate(),
                getYCoordinate(),
                isValid());
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"ObjectNodeDTO\""
                + ", \"@super\":" + super.toString()
                + '}';
    }
}
