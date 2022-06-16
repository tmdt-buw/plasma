package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.datamodel.CombinedModelIntegrityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

@JsonTypeName("NamedEntity")
public class NamedEntity extends SemanticModelNode implements Serializable, NodeTemplate {

    public static final String DESCRIPTION_PROPERTY = "description";

    private String description;

    public NamedEntity(@Nullable String uri, @NotNull String label, @NotNull String description) {
        super(uri, label);
        this.description = description;
    }

    public NamedEntity(@Nullable String uri, @NotNull String label, @NotNull String description, @Nullable Double xCoordinate, @Nullable Double yCoordinate) {
        super(uri, label, xCoordinate, yCoordinate);
        this.description = description;
    }

    public NamedEntity(@Nullable String uri, @NotNull String label, @NotNull String description, @Nullable Double xCoordinate, @Nullable Double yCoordinate, @Nullable String uuid) {
        super(uri, label, xCoordinate, yCoordinate, uuid);
        this.description = description;
    }

    @JsonCreator
    public NamedEntity(@Nullable @JsonProperty(UUID_PROPERTY) String uuid,
                       @NotNull @JsonProperty(URI_PROPERTY) String uri,
                       @NotNull @JsonProperty(LABEL_PROPERTY) String label,
                       @NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
                       @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
                       @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
                       @JsonProperty(PROVISIONAL_PROPERTY) boolean provisional
    ) {
        super(uri, label, xCoordinate, yCoordinate, uuid);
        this.setProvisional(provisional);
        this.description = description;
    }

    @JsonProperty(DESCRIPTION_PROPERTY)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public NamedEntity copy() {
        return new NamedEntity(getUuid(), getURI(), getLabel(), getDescription(), getXCoordinate(), getYCoordinate(), isProvisional());
    }

    @Override
    public NamedEntity instanciate() {
        return new NamedEntity(getURI(), getLabel(), getDescription());
    }

    @Override
    public boolean sameAs(SemanticModelNode o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass().equals(o.getClass())) {
            NamedEntity that = (NamedEntity) o;
            return Objects.equals(this.getURI(), that.getURI()) &&
                    Objects.equals(this.getLabel(), that.getLabel()) &&
                    Objects.equals(this.getDescription(), that.getDescription()) &&
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
            NamedEntity that = (NamedEntity) o;
            return Objects.equals(this.getUuid(), that.getUuid()) &&
                    sameAs(that);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void validate() throws CombinedModelIntegrityException {

        if (getURI() == null || getURI().isBlank()) {
            throw new CombinedModelIntegrityException("NamedEntity " + getUuid() + " has no proper URL assigned");
        }
        if (getDescription() == null || getDescription().isBlank()) {
            throw new CombinedModelIntegrityException("NamedEntity " + getUuid() + " has no proper description assigned");
        }
        if (getLabel().isBlank()) {
            throw new CombinedModelIntegrityException("NamedEntity" + getUuid() + " has no proper label assigned");
        }

    }

    @Override
    public String toString() {
        return "NamedEntity{" +
                "uri='" + getURI() + '\'' +
                "label='" + getLabel() + '\'' +
                "description='" + description.length() + '\'' +
                '}';
    }
}
