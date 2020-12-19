package de.buw.tmdt.plasma.services.dms.core.model.datasource.semanticmodel;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.services.dms.core.model.TraversableModelBase;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "entity_concepts")
public class EntityConcept extends TraversableModelBase {

	@Column(nullable = true, unique = false)
	private final String uuid;

	@NotEmpty
	@Column(nullable = false)
	private final String name;

	@Lob
	@Column(nullable = false)
	private final String description;

	@Column(nullable = false)
	private final String sourceURI;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	@Deprecated
	private EntityConcept() {
		uuid = null;
		name = description = null;
		sourceURI = null;
	}

	public EntityConcept(
			@Nullable String uuid,
			@NotNull String name,
			@NotNull String description,
			@NotNull String sourceURI,
			@Nullable Position position
	) {
		this(uuid, name, description, sourceURI, position, Identity.nullSafe(uuid));
	}

	public EntityConcept(
			@Nullable String uuid,
			@NotNull String name,
			@NotNull String description,
			@NotNull String sourceURI,
			@Nullable Position position,
			@Nullable Long id
	) {
		super(position, id, TraversableModelBase.computeIdentity(id, uuid));
		this.uuid = uuid;
		this.name = name;
		this.description = description;
		this.sourceURI = sourceURI;
	}

	private EntityConcept(
			@Nullable String uuid,
			@NotNull String name,
			@NotNull String description,
			@NotNull String sourceURI,
			@Nullable Position position,
			@NotNull Identity<?> identity
	) {
		super(position, identity);
		this.uuid = uuid;
		this.name = name;
		this.description = description;
		this.sourceURI = sourceURI;
	}

	@Nullable
	public String getUuid() {
		return uuid;
	}

	@NotNull
	public String getName() {
		return name;
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
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EntityConcept\""
		       + ", \"@super\":" + super.toString()
		       + ", \"uuid\":" + uuid
		       + ", \"name\":\"" + name + '"'
		       + ", \"sourceURI\":\"" + sourceURI + '"'
		       + ", \"description\":\"" + description + '"'
		       + '}';
	}

	@Override
	public EntityConcept copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new EntityConcept(
						this.getUuid(),
						this.getName(),
						this.getDescription(),
						this.getSourceURI(),
						this.getPosition(),
						this.getIdentity()
				)), EntityConcept.class
		);
	}

	@Override
	public EntityConcept replace(@NotNull Identity<?> identity, @NotNull Traversable replacement) {
		if (getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, this.getClass());
		}
		return this;
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());
		return true;
	}

	@Override
	public Traversable find(@NotNull Traversable.Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		}
		return null;
	}
}