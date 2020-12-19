package de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel;

import de.buw.tmdt.plasma.services.srs.model.Position;
import de.buw.tmdt.plasma.services.srs.model.Traversable;
import de.buw.tmdt.plasma.services.srs.model.TraversableModelBase;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotEmpty;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class EntityType extends TraversableModelBase {

	private final String uuid;

	@NotEmpty
	private String label;

	private String originalLabel;

	private String description;

	private EntityConcept entityConcept;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	@Deprecated
	private EntityType() {
		uuid = null;
		label = description = null;
		entityConcept = null;
	}

	public EntityType(
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConcept entityConcept,
			@Nullable Position position
	) {
		this(uuid, label, originalLabel, description, entityConcept, position, Identity.random());
	}

	public EntityType(
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConcept entityConcept,
			@Nullable Position position,
			@NotNull Long id
	) {
		super(position, id);
		this.uuid = uuid;
		this.label = label;
		this.originalLabel = originalLabel;
		this.description = description;
		this.entityConcept = entityConcept;
	}

	private EntityType(
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConcept entityConcept,
			@Nullable Position position,
			@NotNull Traversable.Identity<?> identity
	) {
		super(position, identity);
		this.uuid = uuid;
		this.label = label;
		this.originalLabel = originalLabel;
		this.description = description;
		this.entityConcept = entityConcept;
	}

	@Nullable
	public String getUuid() {
		return uuid;
	}

	@NotNull
	public String getLabel() {
		return label;
	}

	@NotNull
	public String getOriginalLabel() {
		return originalLabel;
	}

	@NotNull
	public String getDescription() {
		return description;
	}

	@NotNull
	public EntityConcept getEntityConcept() {
		return entityConcept;
	}

	public void setEntityConcept(@NotNull EntityConcept entityConcept) {
		this.entityConcept = entityConcept;
	}

	@Override
	public EntityType copy() {
		return this.copy(new HashMap<>());
	}

	@Override
	public EntityType copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new EntityType(
						this.getUuid(),
						this.getLabel(),
						this.getOriginalLabel(),
						this.getDescription(),
						this.getEntityConcept(), //don't copy the entity concept
						this.getPosition(),
						this.getIdentity()
				)), EntityType.class
		);
	}

	@Override
	public EntityType replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
		if (getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, this.getClass());
		}
		this.entityConcept = entityConcept.replace(identity, replacement);
		return this;
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());
		//returns false if removing the entity concept or if the entity concept becomes inconsistent
		return !this.entityConcept.getIdentity().equals(identity) && this.getEntityConcept().remove(identity, visited, collateralRemoveQueue);
	}

	@Override
	public Traversable find(@NotNull Traversable.Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		}
		return this.entityConcept != null ? this.entityConcept.find(identity) : null;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EntityType\""
		       + ", \"@super\":" + super.toString()
		       + ", \"entityConcept\":" + entityConcept
		       + ", \"label\":\"" + label + '"'
		       + ", \"originalLabel\":\"" + originalLabel + '"'
		       + ", \"description\":\"" + description + '"'
		       + '}';
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, this.entityConcept);
	}
}
