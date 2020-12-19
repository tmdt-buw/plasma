package de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph;

import de.buw.tmdt.plasma.services.kgs.shared.model.Edge;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EntityConceptRelation extends Edge<EntityConcept, EntityConcept> {

    private static final long serialVersionUID = -9065848044379044649L;

    private final RelationConcept relationConcept;

    private final int absoluteNumberOfContacts; // How often this ECR was used in between the two ECs.
    private final double relativeNumberOfContacts; // How often the two ECs were used together (w/ or w/out this ECR).

    public EntityConceptRelation(
            @NotNull String id,
            @NotNull EntityConcept tailEntityConcept,
            @NotNull EntityConcept headEntityConcept,
            @NotNull RelationConcept relationConcept
    ) {
        this(id, tailEntityConcept, headEntityConcept, relationConcept, 1, 100.0);
    }

    public EntityConceptRelation(
            @NotNull String id,
            @NotNull EntityConcept tailEntityConcept,
            @NotNull EntityConcept headEntityConcept,
            @NotNull RelationConcept relationConcept,
            int absoluteNumberOfContacts,
            double relativeNumberOfContacts
    ) {
        super(id, tailEntityConcept, headEntityConcept);
        this.relationConcept = relationConcept;
        this.absoluteNumberOfContacts = absoluteNumberOfContacts;
        this.relativeNumberOfContacts = relativeNumberOfContacts;
    }

    @NotNull
    public RelationConcept getRelationConcept() {
        return relationConcept;
    }

    /**
     * Returns the usage, determined as the amount of usages per contacts.
     *
     * @return the usage as percentage between 0.0 and 1.0
     */
    public double getRelativeUsage() {
        return relativeNumberOfContacts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), relationConcept, relativeNumberOfContacts, absoluteNumberOfContacts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EntityConceptRelation)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        EntityConceptRelation that = (EntityConceptRelation) o;
        return relativeNumberOfContacts == that.relativeNumberOfContacts &&
               absoluteNumberOfContacts == that.absoluteNumberOfContacts &&
               Objects.equals(relationConcept, that.relationConcept);
    }

    @Override
    public String toString() {
        return this.toPropertyValuePairStringBuilder().toString();
    }

    @Override
    protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
        StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
        propertyValuePairStringBuilder.addPair("relationConcept", relationConcept);
        propertyValuePairStringBuilder.addPair("relativeNumberOfUsages", this.relativeNumberOfContacts);
        propertyValuePairStringBuilder.addPair("absoluteNumberOfUsages", this.absoluteNumberOfContacts);
        propertyValuePairStringBuilder.addPair("usage", this.getRelativeUsage());
        return propertyValuePairStringBuilder;
    }
}