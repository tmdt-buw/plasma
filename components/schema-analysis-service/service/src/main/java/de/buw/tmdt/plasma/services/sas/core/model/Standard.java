package de.buw.tmdt.plasma.services.sas.core.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
public class Standard {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(nullable = false, unique = true, length = 16)
	protected UUID uuid;

	@Column
	private String name;

	@Column
	private String description;

	@OneToMany(cascade = CascadeType.ALL)
	private List<EntityRule> entityRules;

	@OneToMany(cascade = CascadeType.ALL)
	private List<RelationRule> relationRules;

	protected Standard() {

	}

	public Standard(String name, String description, List<EntityRule> entityRules, List<RelationRule> relationRules) {
		this.name = name;
		this.description = description;
		this.entityRules = entityRules;
		this.relationRules = relationRules;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<EntityRule> getEntityRules() {
		return Collections.unmodifiableList(entityRules);
	}

	public void setEntityRules(List<EntityRule> entityRules) {
		this.entityRules = entityRules;
	}

	public List<RelationRule> getRelationRules() {
		return Collections.unmodifiableList(relationRules);
	}

	public void setRelationRules(List<RelationRule> relationRules) {
		this.relationRules = relationRules;
	}
}
