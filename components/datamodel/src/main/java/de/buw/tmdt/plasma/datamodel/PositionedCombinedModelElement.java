package de.buw.tmdt.plasma.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Instance;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Literal;
import de.buw.tmdt.plasma.datamodel.semanticmodel.NamedEntity;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Base class for all elements that can be used inside a combined model that have an explicit placement.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrimitiveNode.class, name = "PrimitiveNode"),
        @JsonSubTypes.Type(value = ObjectNode.class, name = "ObjectNode"),
        @JsonSubTypes.Type(value = SetNode.class, name = "SetNode"),
        @JsonSubTypes.Type(value = CollisionSchema.class, name = "CollisionSchema"),
        @JsonSubTypes.Type(value = CompositeNode.class, name = "CompositeNode"),
        @JsonSubTypes.Type(value = Instance.class, name = "Instance"),
        @JsonSubTypes.Type(value = Literal.class, name = "Literal"),
        @JsonSubTypes.Type(value = Class.class, name = "Class"),
        @JsonSubTypes.Type(value = NamedEntity.class, name = "NamedEntity")
})
public abstract class PositionedCombinedModelElement extends CombinedModelElement implements Serializable {

    private static final long serialVersionUID = -3619721225687115309L;

    public static final String XCOORDINATE_PROPERTY = "x";
    public static final String YCOORDINATE_PROPERTY = "y";

    private Double xCoordinate;
    private Double yCoordinate;

    public PositionedCombinedModelElement(
            @NotNull String label
    ) {
        super(label);
        this.xCoordinate = null;
        this.yCoordinate = null;
    }

    public PositionedCombinedModelElement(
            @NotNull String label,
            @Nullable Double xCoordinate,
            @Nullable Double yCoordinate
    ) {
        super(label);
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    @JsonCreator
    public PositionedCombinedModelElement(
            @Nullable @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
            @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate
    ) {
        super(label);
        setUuid(uuid);
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
    }

    @Nullable
    @JsonProperty(XCOORDINATE_PROPERTY)
    public Double getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(Double xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    @Nullable
    @JsonProperty(YCOORDINATE_PROPERTY)
    public Double getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(Double yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    @Override
    public abstract PositionedCombinedModelElement copy();

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"PositionedCombinedModelElement\""
                + ",\"parent\":\"" + super.toString() + '"'
                + ",\"xCoordinate\":\"" + xCoordinate + '"'
                + ",\"yCoordinate\":\"" + yCoordinate + '"'
                + '}';
    }
}
