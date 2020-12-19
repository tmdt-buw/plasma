package de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class EntityConceptDTO implements Serializable {

    private static final long serialVersionUID = 3817875292350088694L;

    protected static final String ID_PROPERTY = "id";
    protected static final String LABEL_PROPERTY = "mainLabel";
    protected static final String DESCRIPTION_PROPERTY = "description";
    protected static final String SOURCE_URI_PROPERTY = "sourceURI";

    private final String id;
    private final String mainLabel;
    private final String description;
    private final String sourceURI;

    @JsonCreator
    public EntityConceptDTO(
            @NotNull @JsonProperty(ID_PROPERTY) String id,
            @NotNull @JsonProperty(LABEL_PROPERTY) String mainLabel,
            @NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @NotNull @JsonProperty(SOURCE_URI_PROPERTY) String sourceURI
    ) {
        this.id = id;
        this.mainLabel = mainLabel;
        this.description = description;
        this.sourceURI = sourceURI;
    }

    @JsonProperty(ID_PROPERTY)
    @NotNull
    public String getId() {
        return id;
    }

    @JsonProperty(LABEL_PROPERTY)
    @NotNull
    public String getMainLabel() {
        return mainLabel;
    }

    @JsonProperty(DESCRIPTION_PROPERTY)
    @NotNull
    public String getDescription() {
        return description;
    }

    @JsonProperty(SOURCE_URI_PROPERTY)
    @NotNull
    public String getSourceURI() {
        return sourceURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityConceptDTO that = (EntityConceptDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getMainLabel(), that.getMainLabel()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getSourceURI(), that.getSourceURI());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMainLabel(), getDescription(), getSourceURI());
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "EntityConceptDTO{" +
                "id='" + id + '\'' +
                ", mainLabel='" + mainLabel + '\'' +
                ", description='" + description + '\'' +
                ", sourceURI='" + sourceURI + '\'' +
                '}';
    }
}