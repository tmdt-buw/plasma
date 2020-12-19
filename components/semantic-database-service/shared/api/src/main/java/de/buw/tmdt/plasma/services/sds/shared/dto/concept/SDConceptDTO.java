package de.buw.tmdt.plasma.services.sds.shared.dto.concept;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SDConceptDTO {

    private static final String PROPERTY_LABEL = "label";
    private static final String PROPERTY_DESCRIPTION = "description";
    private static final String PROPERTY_SYNONYMS = "synonyms";
    private static final String PROPERTY_SOURCE_URI = "sourceURI";
    private static final String PROPERTY_RELATED_CONCEPTS = "relatedConcepts";
    private static final String PROPERTY_SCORE = "score";

    private final String label;
    private final String description;
    private final Set<String> synonyms;
    private final String sourceURI;
    private final Set<SDConceptDTO> relatedConcepts;
    private final double score;

    @JsonCreator
    public SDConceptDTO(
            @NotNull @JsonProperty(PROPERTY_LABEL) String label,
            @Nullable @JsonProperty(PROPERTY_DESCRIPTION) String description,
            @Nullable @JsonProperty(PROPERTY_SYNONYMS) Set<String> synonyms,
            @Nullable @JsonProperty(PROPERTY_SOURCE_URI) String sourceURI,
            @Nullable @JsonProperty(PROPERTY_RELATED_CONCEPTS) Set<SDConceptDTO> relatedConcepts,
            @JsonProperty(PROPERTY_SCORE) double score
    ) {
        this.label = label;
        this.description = (description == null) ? "" : description;
        this.synonyms = (synonyms == null) ? new HashSet<>() : new HashSet<>(synonyms);
        this.synonyms.add(label); // Ensure that the main label is included in synonyms.
        this.sourceURI = (sourceURI == null) ? "UNKNOWN_URI" : sourceURI;
        this.relatedConcepts = (relatedConcepts == null) ? new HashSet<>() : new HashSet<>(relatedConcepts);
        this.score = score;
    }

    @NotNull
    @JsonProperty(PROPERTY_LABEL)
    public String getLabel() {
        return label;
    }

    @NotNull
    @JsonProperty(PROPERTY_DESCRIPTION)
    public String getDescription() {
        return description;
    }

    @NotNull
    @JsonProperty(PROPERTY_SYNONYMS)
    public Set<String> getSynonyms() {
        return Collections.unmodifiableSet(synonyms);
    }

    @NotNull
    @JsonProperty(PROPERTY_SOURCE_URI)
    public String getSourceURI() {
        return sourceURI;
    }

    public void addRelatedConcept(SDConceptDTO concept) {
        this.relatedConcepts.add(concept);
    }

    @NotNull
    @JsonProperty(PROPERTY_RELATED_CONCEPTS)
    public Set<SDConceptDTO> getRelatedConcepts() {
        return Collections.unmodifiableSet(relatedConcepts);
    }

    @JsonProperty(PROPERTY_SCORE)
    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SDConceptDTO)) {
            return false;
        }
        SDConceptDTO that = (SDConceptDTO) o;
        return Double.compare(that.score, score) == 0 &&
                label.equals(that.label) &&
                description.equals(that.description) &&
                synonyms.equals(that.synonyms) &&
                sourceURI.equals(that.sourceURI) &&
                relatedConcepts.equals(that.relatedConcepts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, description, synonyms, sourceURI, relatedConcepts, score);
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"SDConceptDTO\""
                + ", \"label\":\"" + label + '"'
                + ", \"description\":\"" + description + '"'
                + ", \"synonyms\":" + StringUtilities.setToJson(synonyms)
                + ", \"sourceURI\":\"" + sourceURI + '"'
                + ", \"relatedConcepts\":" + StringUtilities.setToJson(relatedConcepts)
                + ", \"score\":\"" + score + '"'
                + '}';
    }
}