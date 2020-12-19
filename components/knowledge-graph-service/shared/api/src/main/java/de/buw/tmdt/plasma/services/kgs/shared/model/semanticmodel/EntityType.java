package de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel;

import de.buw.tmdt.plasma.services.kgs.shared.model.Node;
import de.buw.tmdt.plasma.services.kgs.shared.model.Position;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityType extends Node {

    private static final long serialVersionUID = -1201859868700726948L;

    private final String label;
    private final String originalLabel;
    private final EntityConcept entityConcept;
    private final Position position;
    private final String description;
    private final boolean mappedToData;

    public EntityType(
            @NotNull String id,
            @NotNull String label,
            @Nullable String originalLabel,
            @NotNull EntityConcept entityConcept,
            @NotNull Position position,
            @Nullable String description,
            boolean mappedToData
    ) {
        super(id);
        this.label = label;
        this.originalLabel = (originalLabel == null) ? label : originalLabel;
        this.entityConcept = entityConcept;
        this.position = position;
        this.description = description == null ? "" : description;
        this.mappedToData = mappedToData;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public EntityConcept getEntityConcept() {
        return entityConcept;
    }

    @NotNull
    public Position getPosition() {
        return position;
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    public boolean isMappedToData() {
        return mappedToData;
    }

    @NotNull
    public String getOriginalLabel() {
        return originalLabel;
    }

    @Override
    public String toString() {
        return this.toPropertyValuePairStringBuilder().toString();
    }

    @Override
    protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
        StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
        propertyValuePairStringBuilder.addPair("label", label);
        propertyValuePairStringBuilder.addPair("originalLabel", originalLabel);
        propertyValuePairStringBuilder.addPair("entityConcept", entityConcept);
        propertyValuePairStringBuilder.addPair("position", position);
        propertyValuePairStringBuilder.addPair("description", description);
        propertyValuePairStringBuilder.addPair("mappedToData", mappedToData);
        return propertyValuePairStringBuilder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), label, originalLabel, entityConcept, position, description, mappedToData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityType)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        EntityType that = (EntityType) o;
        return mappedToData == that.mappedToData &&
                Objects.equals(label, that.label) &&
                Objects.equals(originalLabel, that.originalLabel) &&
                Objects.equals(entityConcept, that.entityConcept) &&
                Objects.equals(position, that.position) &&
                Objects.equals(description, that.description);
    }
}