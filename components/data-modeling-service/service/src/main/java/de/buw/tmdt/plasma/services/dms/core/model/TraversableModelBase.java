package de.buw.tmdt.plasma.services.dms.core.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;

@MappedSuperclass
public abstract class TraversableModelBase extends ModelBase implements Traversable {
	@Embedded
	private Position position;

	protected transient Identity<?> identity;

	//hibernate constructor
	protected TraversableModelBase() {
		this.identity = null;
	}

	protected TraversableModelBase(@Nullable Position position) {
		this(position, Identity.random());
	}

	protected TraversableModelBase(@Nullable Position position, @NotNull Long id) {
		super(id);
		this.position = position;
		this.identity = new Identity<>(id);
	}

	protected TraversableModelBase(@Nullable Position position, @NotNull Traversable.Identity<?> identity) {
		super(null);
		this.position = position;
		this.identity = identity;
	}

	protected TraversableModelBase(@Nullable Position position, @Nullable Long id, @NotNull Traversable.Identity<?> identity) {
		super(id);
		this.position = position;
		this.identity = identity;
	}

	protected static Identity<?> computeIdentity(Long id, String uuid) {
		if (uuid != null) {
			return new Identity<>(uuid);
		} else if (id != null) {
			return new Identity<>(id);
		} else {
			return Identity.random();
		}
	}

	@PostPersist
	@PostLoad
	public void deriveIdentity() {
		if (this.identity == null) {
			if (this.id != null) {
				identity = new Identity<>(id);
			} else {
				identity = Identity.random();
			}
		}
	}

	@Override
	public @NotNull Traversable.Identity<?> getIdentity() {
		if (identity == null) {
			//this should never happen, however, it if happens, this is a fallback
			deriveIdentity();
		}
		return this.identity;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"TraversableModelBase\""
		       + ", \"@super\":" + super.toString()
		       + ", \"position\":" + position
		       + ", \"identity\":" + identity
		       + '}';
	}
}
