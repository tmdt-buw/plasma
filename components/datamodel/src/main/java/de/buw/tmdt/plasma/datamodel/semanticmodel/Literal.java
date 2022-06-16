package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.datamodel.CombinedModelIntegrityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Literal extends MappableSemanticModelNode {

    public static final String VALUE_PROPERTY = "value";

    public Literal(String value) {
        super(null, value); // use the value as label
    }

    public Literal(@NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate) {
        this(null, label, xCoordinate, yCoordinate);
    }

    public Literal(@Nullable String uuid, @NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate) {
        super(null, label, xCoordinate, yCoordinate, uuid);
    }

    @JsonCreator
    public Literal(@Nullable @JsonProperty(UUID_PROPERTY) String uuid,
                   @NotNull @JsonProperty(VALUE_PROPERTY) String value,
                   @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
                   @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
                   @Nullable @JsonProperty(MAPPED_SYNTAX_NODE_UUID_PROPERTY) String mappedSyntaxNodeUuid,
                   @Nullable @JsonProperty(MAPPED_SYNTAX_NODE_LABEL_PROPERTY) String mappedSyntaxNodeLabel,
                   @Nullable @JsonProperty(MAPPED_SYNTAX_NODE_PATH_PROPERTY) String mappedSyntaxNodePath) {
        super(null, value, xCoordinate, yCoordinate, mappedSyntaxNodeUuid, mappedSyntaxNodeLabel, mappedSyntaxNodePath, uuid);
    }

    @Override
    public Literal copy() {
        return new Literal(getUuid(), getValue(), getXCoordinate(), getYCoordinate(), getMappedSyntaxNodeUuid(), getMappedSyntaxNodeLabel(), getMappedSyntaxNodePath());
    }

    @Override
    public Literal instanciate() {
        return new Literal(null, getValue(), getXCoordinate(), getYCoordinate());
    }

    @JsonProperty(VALUE_PROPERTY)
    public String getValue() {
        return getLabel();
    }

    public void validate() throws CombinedModelIntegrityException {
        if (getValue() == null) {
            throw new CombinedModelIntegrityException("Literal " + getUuid() + " has no proper value");
        }
    }

    @Override
    public boolean sameAs(SemanticModelNode o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass().equals(o.getClass())) {
            Literal that = (Literal) o;
            return Objects.equals(this.getURI(), that.getURI()) &&
                    Objects.equals(this.getLabel(), that.getLabel()) &&
                    Objects.equals(this.getXCoordinate(), that.getXCoordinate()) &&
                    Objects.equals(this.getYCoordinate(), that.getYCoordinate());
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass().equals(o.getClass())) {
            Literal that = (Literal) o;
            return Objects.equals(this.getUuid(), that.getUuid()) &&
                    sameAs(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


    @Override
    public String toString() {
        return "Literal{" +
                "value='" + getLabel() + '\'' +
                '}';
    }
}
