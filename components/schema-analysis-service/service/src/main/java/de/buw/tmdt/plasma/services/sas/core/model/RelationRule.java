package de.buw.tmdt.plasma.services.sas.core.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class RelationRule {

	@Id
	@Column(nullable = false, unique = true, length = 16)
	protected UUID uuid;

	@Column
	private String fromConcept;

	@Column
	private String toConcept;

	@Column
	private String conceptName;

	@Column
	private String conceptDescription;

	protected RelationRule() {}

	public RelationRule(@NotNull String fromConcept, @NotNull String toConcept, @NotNull String conceptName, @NotNull String conceptDescription) {
		this.uuid = UUID.randomUUID();
		this.fromConcept = fromConcept;
		this.toConcept = toConcept;
		this.conceptName = conceptName;
		this.conceptDescription = conceptDescription;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getFromConcept() {
		return fromConcept;
	}

	public String getToConcept() {
		return toConcept;
	}

	public String getConceptName() {
		return conceptName;
	}

	public String getConceptDescription() {
		return conceptDescription;
	}
}
