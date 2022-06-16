package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.datamodel.modification.operation.SyntacticOperationDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@JsonTypeName("CollisionSchema")
public class CollisionSchema extends SchemaNode {

    private static final long serialVersionUID = 5329441916665293203L;

    public CollisionSchema(
            @NotNull String label,
            @Nullable List<SyntacticOperationDTO> operations,
            boolean isValid
    ) {
        super(label, isValid);
    }

	@JsonCreator
	public CollisionSchema(

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
    public CollisionSchema copy() {
        return new CollisionSchema(getUuid(),
                getLabel(),
                getPath(),
                getXCoordinate(),
                getYCoordinate(),
                isValid());
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"CollisionSchema\""
                + ", \"@super\":" + super.toString()
                + '}';
    }
}
