package de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.members;

import de.buw.tmdt.plasma.services.sas.core.model.ModelBase;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityConcept;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Objects;

@Entity
@DynamicUpdate
@Table(name = "entity_concept_suggestions")
public class EntityConceptSuggestion extends ModelBase {
	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
	private final EntityConcept entityConcept;
	@Column
	private final double weight;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	protected EntityConceptSuggestion() {
		entityConcept = null;
		weight = -1;
	}

	public EntityConceptSuggestion(@NotNull EntityConcept entityConcept, double weight) {
		if (weight < 0) {
			throw new IllegalArgumentException("Weight must be greater or equal zero but was " + weight);
		}
		this.entityConcept = entityConcept;
		this.weight = weight;
	}

	public EntityConcept getEntityConcept() {
		return entityConcept;
	}

	public double getWeight() {
		return weight;
	}

	@NotNull
	public EntityConceptSuggestion copy() {
		return new EntityConceptSuggestion(this.entityConcept, this.weight);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entityConcept, weight);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		EntityConceptSuggestion that = (EntityConceptSuggestion) o;
		return Double.compare(that.weight, weight) == 0 &&
		       Objects.equals(entityConcept, that.entityConcept);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EntityConceptSuggestion\""
		       + ", \"@super\":" + super.toString()
		       + ", \"entityConcept\":" + entityConcept
		       + ", \"weight\":\"" + weight + '"'
		       + '}';
	}
}
