package de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicUpdate
@Table(name = "nodes")
public abstract class Node implements Traversable {

	@Id
	@GeneratedValue
	@Column(nullable = false, unique = true, updatable = false, length = 16)
	protected UUID uuid;

	@Embedded
	private Position position;

	private transient Identity<?> identity;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	protected Node() {

	}

	protected Node(@Nullable Position position) {
		this(position, Identity.random());
	}

	protected Node(@Nullable Position position, @NotNull UUID uuid) {
		this.position = position;
		this.uuid = uuid;
		this.identity = new Identity<>(uuid);
	}

	protected Node(@Nullable Position position, @NotNull Identity<?> identity) {
		this.position = position;
		this.uuid = null;
		this.identity = identity;
	}

	@PostPersist
	@PostLoad
	public void deriveIdentity() {
		if (this.identity == null) {
			if (this.uuid != null) {
				identity = new Identity<>(uuid);
			} else {
				identity = Identity.random();
			}
		}
	}

	@Override
	public abstract Node copy(@NotNull Map<Identity<?>, Traversable> copyableLookup);

	@Nullable
	public Position getPosition() {
		return position;
	}

	public void setPosition(@NotNull Position position) {
		this.position = position;
	}

	public boolean isValid() {
		return true;
	}

	@Override
	public abstract Node replace(@NotNull Identity<?> identity, @NotNull Traversable replacement);

	@NotNull
	@Override
	public Identity<?> getIdentity() {
		if (identity == null) {
			//this should never happen, however, it if happens, this is a fallback
			deriveIdentity();
		}
		return identity;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(@NotNull UUID uuid) {
		this.uuid = uuid;
	}

	public Set<Fault> evaluateConstraints() {
		Set<Fault> faults = new HashSet<>();
		faults = hasCollision(faults);
		faults = idGloballyDisjoint(new HashSet<>(), faults);
		faults = otherFaults(faults);
		return faults;
	}

	protected Set<Fault> otherFaults(Set<Fault> faults) {
		return faults;
	}

	protected Set<Fault> hasCollision(Set<Fault> faults) {
		return faults;
	}

	protected Set<Fault> idGloballyDisjoint(Set<UUID> collectedIds, Set<Fault> faults) {
		if (!collectedIds.add(this.uuid)) {
			faults.add(new Fault("Duplicate uuid found: " + uuid));
		}
		return faults;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Node\""
		       + ", \"uuid\":" + uuid
		       + ", \"position\":" + position
		       + '}';
	}

	public static class Fault {
		private final String message;

		public Fault(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		@Override
		@SuppressWarnings("MagicCharacter")
		public String toString() {
			return "{\"@class\":\"Fault\""
			       + ", \"message\":" + message
			       + '}';
		}
	}
}
