package de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel;

import de.buw.tmdt.plasma.services.kgs.shared.model.Edge;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Relation extends Edge<EntityType, EntityType> {

	private static final long serialVersionUID = -4538724888596538548L;
	private final RelationConcept relationConcept;

	public Relation(
			@NotNull String id,
			@NotNull EntityType tailEntityType,
			@NotNull EntityType headEntityType,
			@NotNull RelationConcept relationConcept
	) {
		super(id, tailEntityType, headEntityType);
		this.relationConcept = relationConcept;
	}

	@NotNull
	public RelationConcept getRelationConcept() {
		return relationConcept;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), relationConcept);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Relation)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		Relation relation = (Relation) o;
		return Objects.equals(relationConcept, relation.relationConcept);
	}

	@Override
	public String toString() {
		return this.toPropertyValuePairStringBuilder().toString();
	}

	@Override
	protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
		StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
		propertyValuePairStringBuilder.addPair("relationConcept", this.getRelationConcept());
		return propertyValuePairStringBuilder;
	}
}
