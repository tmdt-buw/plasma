package de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel;

import de.buw.tmdt.plasma.services.kgs.shared.model.Node;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SemanticModel extends Node {

	private static final long serialVersionUID = 112865931417888497L;

	private final String label;
	private final String description;
	private final HashSet<EntityType> entityTypes;
	private final HashSet<Relation> relations;
	private final String dataSourceId;

	public SemanticModel(
			@NotNull String id,
			@NotNull String label,
			@Nullable String description,
			@NotNull Collection<EntityType> entityTypes,
			@Nullable Collection<Relation> relations,
			@NotNull String dataSourceId
	) {
		super(id);
		this.label = label;
		this.description = (description == null) ? "" : description;
		this.entityTypes = new HashSet<>(entityTypes);
		if (this.entityTypes.isEmpty()) {
			throw new IllegalArgumentException("Semantic Models must contain at least one entity type.");
		}

		this.relations = (relations == null) ? new HashSet<>() : new HashSet<>(relations);

		// Verify that all ETs used in Rs are included in the ETs.
		for (Relation relation : this.relations) {
			if (!this.entityTypes.contains(relation.getTailNode()) || !this.entityTypes.contains(relation.getHeadNode())) {
				throw new RuntimeException("The Relations of an SemanticModel must not contain EntityTypes which are not explicitly provided.");
			}
		}
		this.dataSourceId = dataSourceId;
	}

	@NotNull
	public String getLabel() {
		return this.label;
	}

	@NotNull
	public String getDescription() {
		return this.description;
	}

	@NotNull
	public Set<EntityType> getEntityTypes() {
		return Collections.unmodifiableSet(this.entityTypes);
	}

	@NotNull
	public Set<Relation> getRelations() {
		return Collections.unmodifiableSet(this.relations);
	}

	@NotNull
	public String getDataSourceId() {
		return dataSourceId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), label, description, entityTypes, relations);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SemanticModel)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		SemanticModel that = (SemanticModel) o;
		return Objects.equals(this.label, that.label) &&
		       Objects.equals(this.description, that.description) &&
		       Objects.equals(this.entityTypes, that.entityTypes) &&
		       Objects.equals(this.relations, that.relations) &&
		       Objects.equals(this.dataSourceId, that.dataSourceId);
	}

	@Override
	public String toString() {
		return this.toPropertyValuePairStringBuilder().toString();
	}

	@Override
	protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
		StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
		propertyValuePairStringBuilder.addPair("label", this.getLabel());
		propertyValuePairStringBuilder.addPair("description", this.getDescription());
		propertyValuePairStringBuilder.addPair("entityTypes", this.getEntityTypes());
		propertyValuePairStringBuilder.addPair("relations", this.getRelations());
		propertyValuePairStringBuilder.addPair("dataSourceId", this.getDataSourceId());
		return propertyValuePairStringBuilder;
	}
}