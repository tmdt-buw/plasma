package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a set. All outgoing {@link Edge}s' target nodes represent members of that set.
 */
@JsonTypeName("SetNode")
public class SetNode extends SchemaNode {

    private static final long serialVersionUID = -5424468053886038114L;

    public SetNode(
            boolean isValid
    ) {
        super("",  isValid);
    }

	@JsonCreator
	public SetNode(
            @JsonProperty(UUID_PROPERTY) @NotNull String uuid,
            @JsonProperty(LABEL_PROPERTY) @NotNull String label,
            @Nullable @JsonProperty(PATH_PROPERTY) List<String> path,
            @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
            @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
            @JsonProperty(VALID_PROPERTY) boolean isValid
    ) {
        super(uuid, label, path, xCoordinate, yCoordinate, isValid);
    }

    @Override
    public SetNode copy() {
        return new SetNode(
                getUuid(),
                getLabel(),
                getPath(),
                getXCoordinate(),
                getYCoordinate(),
                isValid()
        );
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"" + this.getClass().getSimpleName() + "\""
                + ", \"@super\":" + super.toString()
                + '}';
    }
}
