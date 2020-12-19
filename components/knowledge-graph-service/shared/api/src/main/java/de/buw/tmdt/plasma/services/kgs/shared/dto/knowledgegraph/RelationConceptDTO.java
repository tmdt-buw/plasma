package de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RelationConceptDTO implements Serializable {

    private static final long serialVersionUID = 6286906797161468912L;

    private static final String ID_PROPERTY = "id";
    private static final String LABEL_PROPERTY = "label";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String SOURCE_URI_PROPERTY = "sourceURI";
    private static final String PROPERTIES_PROPERTY = "properties";

    private final String id;
    private final String label;
    private final String description;
    private final String sourceURI;
    private final Set<String> properties;

    @JsonCreator
    public RelationConceptDTO(
            @NotNull @JsonProperty(ID_PROPERTY) String id,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @NotNull @JsonProperty(SOURCE_URI_PROPERTY) String sourceURI,
            @NotNull @JsonProperty(PROPERTIES_PROPERTY) Set<String> properties
    ) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.sourceURI = sourceURI;
        this.properties = new HashSet<>(properties);
    }

    @JsonProperty(ID_PROPERTY)
    @NotNull
    public String getId() {
        return id;
    }

    @JsonProperty(LABEL_PROPERTY)
    @NotNull
    public String getLabel() {
        return label;
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

    @JsonProperty(PROPERTIES_PROPERTY)
    @NotNull
    public Set<String> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RelationConceptDTO that = (RelationConceptDTO) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getLabel(), that.getLabel()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLabel(), getDescription(), getSourceURI(), getProperties());
    }

    @Override
    public String toString() {
        return "RelationConceptDTO{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", sourceURI='" + sourceURI + '\'' +
                ", properties=" + properties +
                '}';
    }
}