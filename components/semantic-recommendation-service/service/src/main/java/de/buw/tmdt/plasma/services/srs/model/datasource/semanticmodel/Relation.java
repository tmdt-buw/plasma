package de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel;

import de.buw.tmdt.plasma.services.srs.model.Position;
import de.buw.tmdt.plasma.services.srs.model.Traversable;
import de.buw.tmdt.plasma.services.srs.model.TraversableModelBase;
import de.buw.tmdt.plasma.utilities.collections.ArrayUtilities;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class Relation extends TraversableModelBase {

	private final String uuid;

	private EntityType from;

	private EntityType to;

	private RelationConcept relationConcept;

	private String description;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	@Deprecated
	private Relation() {
		uuid = null;
		from = to = null;
		relationConcept = null;
		description = null;
	}

	public Relation(
			@Nullable String uuid,
			@NotNull EntityType from,
			@NotNull EntityType to,
			@NotNull RelationConcept relationConcept,
			@NotNull String description,
			@Nullable Position position
	) {
		this(uuid, from, to, relationConcept, description, position, Identity.random());
	}

	public Relation(
			@Nullable String uuid,
			@NotNull EntityType from,
			@NotNull EntityType to,
			@NotNull RelationConcept relationConcept,
			@NotNull String description,
			@Nullable Position position,
			@NotNull Long id
	) {
		super(position, id);
		this.uuid = uuid;
		init(from, to, relationConcept, description);
	}

	private Relation(
			@Nullable String uuid,
			@NotNull EntityType from,
			@NotNull EntityType to,
			@NotNull RelationConcept relationConcept,
			@NotNull String description,
			@Nullable Position position,
			@NotNull Traversable.Identity<?> identity
	) {
		super(position, identity);
		this.uuid = uuid;
		init(from, to, relationConcept, description);
	}

	private void init(
			@NotNull EntityType from,
			@NotNull EntityType to,
			@NotNull RelationConcept relationConcept,
			@NotNull String description
	) {
		this.from = from;
		this.to = to;
		this.relationConcept = relationConcept;
		this.description = description;
	}

	public String getUuid() {
		return uuid;
	}

	public @NotNull EntityType getFrom() {
		return from;
	}

	public void setFrom(@NotNull EntityType from) {
		this.from = from;
	}

	public @NotNull EntityType getTo() {
		return to;
	}

	public void setTo(@NotNull EntityType to) {
		this.to = to;
	}

	public @NotNull RelationConcept getRelationConcept() {
		return relationConcept;
	}

	public void setRelationConcept(@NotNull RelationConcept relationConcept) {
		this.relationConcept = relationConcept;
	}

	public @NotNull String getDescription() {
		return description;
	}

	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Relation\""
		       + ", \"@super\":" + super.toString()
		       + ", \"from\":" + from
		       + ", \"to\":" + to
		       + ", \"relationConcept\":" + relationConcept
		       + ", \"description\":\"" + description + '"'
		       + '}';
	}

	@Override
	public Relation copy() {
		return this.copy(new HashMap<>());
	}

	@Override
	public Relation copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new Relation(
						this.getUuid(),
						this.from.copy(copyableLookup),
						this.to.copy(copyableLookup),
						this.relationConcept, //don't copy the relation concept
						this.description,
						this.getPosition(),
						this.getIdentity()
				)),
				Relation.class
		);
	}

	@Override
	public Relation replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
		if (getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, this.getClass());
		}
		this.from = from.replace(identity, replacement);
		this.to = to.replace(identity, replacement);
		this.relationConcept = relationConcept.replace(identity, replacement);
		return this;
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());
		return Traversable.removeFromChildren(
				identity,
				visited,
				collateralRemoveQueue,
				ArrayUtilities.<TraversableModelBase, List<TraversableModelBase>>toCollection(ArrayList::new, this.relationConcept, this.from, this.to),
				COMPONENT
		);
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, this.relationConcept);
	}

	@Override
	public Traversable find(@NotNull Traversable.Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			Traversable result;
			for (Traversable child : Arrays.asList(from, to, relationConcept)) {
				if (child != null && (result = child.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}
}
