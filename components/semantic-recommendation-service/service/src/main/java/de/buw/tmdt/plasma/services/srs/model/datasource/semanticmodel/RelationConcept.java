package de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel;

import de.buw.tmdt.plasma.services.srs.model.ModelBase;
import de.buw.tmdt.plasma.services.srs.model.Position;
import de.buw.tmdt.plasma.services.srs.model.Traversable;
import de.buw.tmdt.plasma.services.srs.model.TraversableModelBase;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotEmpty;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RelationConcept extends TraversableModelBase {

	private final String uuid;

	@NotEmpty
	private final String name;

	private final String description;

	private final Set<Property> properties;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	@SuppressWarnings("unused - hibernate constructor")
	private RelationConcept() {
		name = null;
		description = null;
		uuid = null;
		properties = null;
	}

	public RelationConcept(
			@Nullable String uuid,
			@NotNull String name,
			@NotNull String description,
			@NotNull Set<Property> properties,
			@Nullable Position position
	) {
		this(uuid, name, description, properties, position, Identity.random());
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public RelationConcept(
			@Nullable String uuid,
			@NotNull String name,
			@NotNull String description,
			@NotNull Set<Property> properties,
			@Nullable Position position,
			@Nullable Long id
	) {
		super(position, id, TraversableModelBase.computeIdentity(id, uuid));
		this.uuid = uuid;
		this.name = name;
		this.description = description;
		this.properties = properties;
	}

	private RelationConcept(
			@Nullable String uuid,
			@NotNull String name,
			@NotNull String description,
			@NotNull Set<Property> properties,
			@Nullable Position position,
			@NotNull Traversable.Identity<?> identity
	) {
		super(position, identity);
		this.uuid = uuid;
		this.name = name;
		this.description = description;
		this.properties = properties;
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

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"RelationConcept\""
		       + ", \"@super\":" + super.toString()
		       + ", \"uuid\":" + uuid
		       + ", \"name\":\"" + name + '"'
		       + ", \"description\":\"" + description + '"'
		       + ", \"properties\":" + StringUtilities.setToJson(properties)
		       + '}';
	}

	@Override
	public RelationConcept copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new RelationConcept(
						this.getUuid(),
						this.getName(),
						this.getDescription(),
						this.getProperties(),
						this.getPosition(),
						this.getIdentity()
				)), RelationConcept.class
		);
	}

	@Override
	public RelationConcept replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
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

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - hibernate shenanigans prohibit fixing this")
	public Set<Property> getProperties() {
		return properties;
	}

	@SuppressWarnings("ClassWithOnlyPrivateConstructors - hibernate shenanigans prohibit fixing this")
	public static class Property extends ModelBase {
		private static final Property REFLEXIVE = new Property("Reflexive", "f.a. x: xRx");
		private static final Property IRREFLEXIVE = new Property("Irreflexive", "f.a. x: NOT xRx");
		private static final Property SYMMETRIC = new Property("Symmetric", "f.a. x,y: if xRy, then yRx");
		private static final Property ANTISYMMETRIC = new Property("Antisymmetric", "f.a. x,y: if xRy and yRx, then x=y");
		private static final Property ASYMMETRIC = new Property("Asymmetric", "f.a. x,y: if xRy, then NOT yRx");
		private static final Property TRANSITIVE = new Property("Transitive", "f.a. x,y,z: if xRy and yRz, then xRz");


		private final String name;


		private String formalDescription;

		@SuppressWarnings("unused - hibernate constructor")
		private Property() {
			name = null;
			formalDescription = null;
		}

		private Property(@NotNull String name, @NotNull String formalDescription) {
			this.name = name;
			this.formalDescription = formalDescription;
		}

		public String getName() {
			return name;
		}

		public String getFormalDescription() {
			return formalDescription;
		}

		private void setFormalDescription(String formalDescription) {
			this.formalDescription = formalDescription;
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), name);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || !getClass().equals(o.getClass())) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			Property property = (Property) o;
			return Objects.equals(name, property.name);
		}

		@Override
		@SuppressWarnings("MagicCharacter")
		public String toString() {
			return "{\"@class\":\"Property\""
			       + ", \"@super\":" + super.toString()
			       + ", \"name\":\"" + name + '"'
			       + ", \"formalDescription\":\"" + formalDescription + '"'
			       + '}';
		}
	}
}
