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
public class SetNode extends MappableSyntaxNode {

    private static final long serialVersionUID = -5424468053886038114L;

    public static final String ARRAY_INDICATOR = "0";

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
            @JsonProperty(VALID_PROPERTY) boolean isValid,
            @JsonProperty(VISIBLE_PROPERTY) boolean visible,
            @JsonProperty(DISABLED_PROPERTY) boolean disabled
    ) {
        super(uuid, label, path, xCoordinate, yCoordinate, isValid, visible, disabled);
    }

    @Override
    public SetNode copy() {
        return new SetNode(
                getUuid(),
                getLabel(),
                getPath(),
                getXCoordinate(),
                getYCoordinate(),
                isValid(),
                isVisible(),
                isDisabled()
        );
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return super.toString() + " | SetNode: " + getPathAsJSONPointer();
    }
}
