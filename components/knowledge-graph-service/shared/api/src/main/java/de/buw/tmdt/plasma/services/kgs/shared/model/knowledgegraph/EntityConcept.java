package de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph;

import de.buw.tmdt.plasma.services.kgs.shared.model.Node;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityConcept extends Node {

    private static final long serialVersionUID = 7628881165346207857L;

    private final String mainLabel; //the main label node that was defined by the user
    private final String description; //the description as given by the user
    private final String sourceURI; //the source from which the entity concept was extracted

    public EntityConcept(
            @NotNull String id,
            @NotNull String mainLabel,
            @Nullable String description
    ) {
        this(id, mainLabel, description, null);
    }

    public EntityConcept(
            @NotNull String id,
            @NotNull String mainLabel,
            @Nullable String description,
            @Nullable String sourceURI
    ) {
        super(id);
        this.mainLabel = mainLabel;
        this.description = description == null ? "" : description;
        this.sourceURI = sourceURI == null ? "" : sourceURI;
    }

    @NotNull
    public String getMainLabel() {
        return this.mainLabel;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getSourceURI() {
        return sourceURI;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mainLabel, description, sourceURI);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityConcept)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        EntityConcept that = (EntityConcept) o;
        return Objects.equals(mainLabel, that.mainLabel) &&
                Objects.equals(description, that.description) &&
                Objects.equals(sourceURI, that.sourceURI);
    }

    @Override
    public String toString() {
        return this.toPropertyValuePairStringBuilder().toString();
    }

    @Override
    protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
        StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
        propertyValuePairStringBuilder.addPair("mainLabel", mainLabel);
        propertyValuePairStringBuilder.addPair("description", description);
        propertyValuePairStringBuilder.addPair("sourceURI", sourceURI);
        return propertyValuePairStringBuilder;
    }
}