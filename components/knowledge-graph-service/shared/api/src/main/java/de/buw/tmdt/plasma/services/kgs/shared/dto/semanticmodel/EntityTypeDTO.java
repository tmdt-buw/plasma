package de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class EntityTypeDTO implements Serializable {

    private static final long serialVersionUID = 6931771802375094000L;

    private static final String ID_PROPERTY = "id";
    private static final String X_COORDINATE_PROPERTY = "x";
    private static final String Y_COORDINATE_PROPERTY = "y";
    private static final String LABEL_PROPERTY = "label";
    private static final String ORIGINAL_LABEL_PROPERTY = "originalLabel";
    private static final String CONCEPT_PROPERTY = "concept";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String MAPPED_TO_DATA_PROPERTY = "mappedToData";

    private final String id;
    private final String label;
    private final String originalLabel;
    private final EntityConceptDTO concept;
    private final Double xCoordinate;
    private final Double yCoordinate;
    private final String description;
    private final boolean mappedToData;

    @JsonCreator
    public EntityTypeDTO(
            @NotNull @JsonProperty(ID_PROPERTY) String id,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @Nullable @JsonProperty(ORIGINAL_LABEL_PROPERTY) String originalLabel,
            @NotNull @JsonProperty(CONCEPT_PROPERTY) EntityConceptDTO concept,
            @NotNull @JsonProperty(X_COORDINATE_PROPERTY) Double xCoordinate,
            @NotNull @JsonProperty(Y_COORDINATE_PROPERTY) Double yCoordinate,
            @Nullable @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @JsonProperty(MAPPED_TO_DATA_PROPERTY) boolean mappedToData
    ) {
        this.id = id;
        this.label = label;
        this.originalLabel = (originalLabel == null) ? label : originalLabel;
        this.concept = concept;
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.description = (description == null) ? "" : description;
        this.mappedToData = mappedToData;
    }

    @NotNull
    @JsonProperty(ID_PROPERTY)
    public String getId() {
        return id;
    }

    @NotNull
    @JsonProperty(X_COORDINATE_PROPERTY)
    public Double getXCoordinate() {
        return xCoordinate;
    }

    @NotNull
    @JsonProperty(Y_COORDINATE_PROPERTY)
    public Double getYCoordinate() {
        return yCoordinate;
    }

    @NotNull
    @JsonProperty(LABEL_PROPERTY)
    public String getLabel() {
        return label;
    }

    @NotNull
    @JsonProperty(CONCEPT_PROPERTY)
    public EntityConceptDTO getConcept() {
        return concept;
    }

    @Nullable
    @JsonProperty(DESCRIPTION_PROPERTY)
    public String getDescription() {
        return description;
    }

    @JsonProperty(MAPPED_TO_DATA_PROPERTY)
    public boolean isMappedToData() {
        return mappedToData;
    }

    @JsonProperty(ORIGINAL_LABEL_PROPERTY)
    @NotNull
    public String getOriginalLabel() {
        return originalLabel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCoordinate, yCoordinate, label, originalLabel, concept, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityTypeDTO)) {
            return false;
        }
        EntityTypeDTO that = (EntityTypeDTO) o;
        return mappedToData == that.mappedToData &&
                Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(originalLabel, that.originalLabel) &&
                Objects.equals(concept, that.concept) &&
                Objects.equals(xCoordinate, that.xCoordinate) &&
                Objects.equals(yCoordinate, that.yCoordinate) &&
                Objects.equals(description, that.description);
    }

    @Override
    public String toString() {
        return "CreateEntityTypeDTO{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", originalLabel='" + originalLabel +
                ", concept=" + concept +
                ", xCoordinate=" + xCoordinate +
                ", yCoordinate=" + yCoordinate +
                ", description='" + description + '\'' +
                ", mappedToData=" + mappedToData +
                '}';
    }
}