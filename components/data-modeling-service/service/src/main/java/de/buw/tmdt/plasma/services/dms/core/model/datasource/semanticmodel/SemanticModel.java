package de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.TraversableModelBase;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Entity
@DynamicUpdate
@Table(name = "semantic_models")
public class SemanticModel extends TraversableModelBase {

	@SuppressWarnings("DefaultAnnotationParam - this is not part of the primary key in the middleware, entity concepts are created redundantly")
	@Column(nullable = true, unique = false)
	private String uuid;

	@Column(nullable = false)
	private String label;

	@Lob
	@Column(nullable = false)
	private String description;

	@OneToMany(cascade = CascadeType.ALL)
	private List<EntityType> entityTypes;

	@OneToMany(cascade = CascadeType.ALL)
	private List<Relation> relations;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	@Deprecated
	private SemanticModel() {

	}

	public SemanticModel(
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String description,
			@NotNull List<EntityType> entityTypes,
			@NotNull List<Relation> relations,
			@Nullable Position position
	) {
		this(uuid, label, description, entityTypes, relations, position, Identity.nullSafe(uuid));
	}

	public SemanticModel(
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String description,
			@NotNull List<EntityType> entityTypes,
			@NotNull List<Relation> relations,
			@Nullable Position position,
			@Nullable Long id
	) {
		super(position, id, TraversableModelBase.computeIdentity(id, uuid));
		this.uuid = uuid;
		this.label = label;
		this.description = description;
		this.entityTypes = new ArrayList<>(entityTypes);
		this.relations = new ArrayList<>(relations);
	}

	private SemanticModel(
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String description,
			@NotNull List<EntityType> entityTypes,
			@NotNull List<Relation> relations,
			@Nullable Position position,
			@NotNull Identity<?> identity
	) {
		super(position, identity);
		this.uuid = uuid;
		this.label = label;
		this.description = description;
		this.entityTypes = new ArrayList<>(entityTypes);
		this.relations = new ArrayList<>(relations);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<EntityType> getEntityTypes() {
		return entityTypes;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setEntityTypes(List<EntityType> entityTypes) {
		this.entityTypes = entityTypes;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<Relation> getRelations() {
		return relations;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SemanticModel\""
		       + ", \"@super\":" + super.toString()
		       + ", \"uuid\":" + uuid
		       + ", \"label\":\"" + label + '"'
		       + ", \"description\":\"" + description + '"'
		       + ", \"entityTypes\":" + StringUtilities.listToJson(entityTypes)
		       + ", \"relations\":" + StringUtilities.listToJson(relations)
		       + '}';
	}

	@Override
	public SemanticModel copy() {
		return this.copy(new HashMap<>());
	}

	@Override
	public SemanticModel copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new SemanticModel(
						this.uuid,
						this.label,
						this.description,
						this.entityTypes.stream().map(et -> et.copy(copyableLookup)).collect(Collectors.toList()),
						this.relations.stream().map(r -> r.copy(copyableLookup)).collect(Collectors.toList()),
						this.getPosition(),
						this.getIdentity()
				)), SemanticModel.class
		);
	}

	@Override
	public SemanticModel replace(@NotNull Identity<?> identity, @NotNull Traversable replacement) {
		if (getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, this.getClass());
		}
		this.entityTypes = this.entityTypes.stream().map(et -> et.replace(identity, replacement)).collect(Collectors.toList());
		this.relations = this.relations.stream().map(r -> r.replace(identity, replacement)).collect(Collectors.toList());
		return this;
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());

		boolean result = Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, entityTypes, OWNED);
		result &= Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, relations, OWNED);

		return result;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		execute(consumer, visited, getAllChildren());
	}

	@Override
	public Traversable find(@NotNull Traversable.Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			Traversable result;
			for (Traversable child : getAllChildren()) {
				if (child != null && (result = child.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	private ArrayList<Traversable> getAllChildren() {
		ArrayList<Traversable> children = new ArrayList<>(relations.size() + entityTypes.size());
		children.addAll(relations);
		children.addAll(entityTypes);
		return children;
	}

	@PrePersist
	protected void prePersist() {
		//This may happen if the object was initialized by Hibernate
		if(this.relations == null){
			return;
		}

		if(this.relations .size() > 0 && entityTypes == null){
			throw new IllegalStateException("Relation contains an entity type but the entity types are null");
		}

		for (Relation relation : this.relations) {
			EntityType missingEntityType = null;
			if (!this.entityTypes.contains(relation.getFrom())) {
				missingEntityType = relation.getFrom();
			} else if (!this.entityTypes.contains(relation.getTo())) {
				missingEntityType = relation.getTo();
			}

			if (missingEntityType != null) {
				throw new IllegalStateException(String.format(
						"Relation %s contains an entity type (%s) which is not part of the semantic model.", relation, missingEntityType
				));
			}
		}
	}
}
