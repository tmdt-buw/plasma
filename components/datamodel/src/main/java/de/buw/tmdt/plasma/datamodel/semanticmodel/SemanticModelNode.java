package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.buw.tmdt.plasma.datamodel.CombinedModelIntegrityException;
import de.buw.tmdt.plasma.datamodel.PositionedCombinedModelElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Literal.class, name = "Literal"),
        @JsonSubTypes.Type(value = Class.class, name = "Class"),
        @JsonSubTypes.Type(value = NamedEntity.class, name = "NamedEntity")
})
public abstract class SemanticModelNode extends PositionedCombinedModelElement implements NodeTemplate {

    public static final String URI_PROPERTY = "uri";
    public static final String PROVISIONAL_PROPERTY = "provisional";

    private final String uri;

    private boolean provisional = false;

    public SemanticModelNode(@Nullable String uri, @NotNull String label) {
        this(uri, label, null, null, null);
    }

    public SemanticModelNode(@Nullable String uri, @NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate) {
        this(uri, label, xCoordinate, yCoordinate, null);
    }

    public SemanticModelNode(@Nullable String uri, @NotNull String label, @Nullable Double xCoordinate, @Nullable Double yCoordinate, @Nullable String uuid) {
        super(uuid, label, xCoordinate, yCoordinate);
        this.uri = uri;
    }

    /**
     * Identifies if two {@link SemanticModelNode}s represent the same entity.
     * Needed as there might exist multiple {@link SemanticModelNode} instances that actually represent the same concept
     * or class but have different uuids.
     *
     * @param ec The {@link SemanticModelNode} to check
     * @return true of the {@code uri}s of both instances match
     */
    public boolean sameAs(SemanticModelNode ec) {
        return Objects.equals(getURI(), ec.getURI());
    }

    @JsonProperty(URI_PROPERTY)
    public String getURI() {
        return uri;
    }

    @JsonProperty(PROVISIONAL_PROPERTY)
    public boolean isProvisional() {
        return provisional;
    }

    public void setProvisional(boolean provisional) {
        this.provisional = provisional;
    }

    public abstract SemanticModelNode copy();

    public abstract SemanticModelNode instanciate();

    public abstract void validate() throws CombinedModelIntegrityException;

    @Override
    public SemanticModelNode convertToTemplate() {
        removeUuid();
        return this;
    }

    @Override
    public boolean isTemplate() {
        return getUuid() == null;
    }

    public abstract boolean equals(Object smn);

    public boolean isMapped() {
        return false;
    }

}
