package de.buw.tmdt.plasma.services.dms.shared.dto.recommendation;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel.RelationDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Defines a Delta recommendation, a partial update of a semantic model
 */
public class DeltaRecommendationDTO {

    private static final String REFERENCE_PROPERTY = "reference";
    private static final String ENTITIES_PROPERTY = "entities";
    private static final String RELATIONS_PROPERTY = "relations";
    private static final String CONFIDENCE_PROPERTY = "confidence";

    /**
     * A unique id identifying this recommendation
     */
    public String reference;
    public Set<EntityTypeDTO> entities;

    public Set<RelationDTO> relations;

    public Double confidence;

    public DeltaRecommendationDTO(@NotNull @JsonProperty(REFERENCE_PROPERTY) String reference,
                                  @Nullable @JsonProperty(ENTITIES_PROPERTY) Set<EntityTypeDTO> entities,
                                  @Nullable @JsonProperty(RELATIONS_PROPERTY) Set<RelationDTO> relations,
                                  @Nullable @JsonProperty(CONFIDENCE_PROPERTY) Double confidence) {
        this.reference = reference;
        this.entities = entities == null ? new HashSet<>() : entities;
        this.relations = relations == null ? new HashSet<>() : relations;
        this.confidence = confidence;
    }

    @NotNull
    @JsonProperty(REFERENCE_PROPERTY)
    public String getReference() {
        return reference;
    }

    @Nullable
    @JsonProperty(ENTITIES_PROPERTY)
    public Set<EntityTypeDTO> getEntities() {
        return entities;
    }

    @Nullable
    @JsonProperty(RELATIONS_PROPERTY)
    public Set<RelationDTO> getRelations() {
        return relations;
    }

    @Nullable
    @JsonProperty(CONFIDENCE_PROPERTY)
    public Double getConfidence() {
        return confidence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeltaRecommendationDTO that = (DeltaRecommendationDTO) o;
        return Objects.equals(getReference(), that.getReference());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReference());
    }

    @Override
    public String toString() {
        return "DeltaRecommendationDTO{" +
                "reference='" + reference + '\'' +
                ", entities=" + entities +
                ", relations=" + relations +
                ", confidence=" + confidence +
                '}';
    }
}
