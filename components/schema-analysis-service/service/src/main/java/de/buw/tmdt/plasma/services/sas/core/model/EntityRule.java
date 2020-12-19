package de.buw.tmdt.plasma.services.sas.core.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class EntityRule {

	@Id
	@Column(nullable = false, unique = true, length = 16)
	protected UUID uuid;

	@Column
	private String originalAttribute;

	@Column
	private String conceptName;

	@Column
	private String conceptDescription;

	@Column
	private String requiredPredecessor;

	protected EntityRule() {}

	public EntityRule(@Nullable String originalAttribute, @NotNull String conceptName, @NotNull String conceptDescription, @Nullable String requiredPredecessor) {
		this.uuid = UUID.randomUUID();
		this.originalAttribute = originalAttribute;
		this.conceptName = conceptName;
		this.conceptDescription = conceptDescription;
		this.requiredPredecessor = requiredPredecessor;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getOriginalAttribute() {
		return originalAttribute;
	}

	public String getConceptName() {
		return conceptName;
	}

	public String getConceptDescription() {
		return conceptDescription;
	}

	public String getRequiredPredecessor() { return requiredPredecessor; }
}
