package de.buw.tmdt.plasma.services.sas.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class StandardProvisionDTO implements Serializable {

	private static final long serialVersionUID = 1286058454066435072L;
	private static final String NAME_PROPERTY = "name";
	private static final String DESCRIPTION_PROPERTY = "description";
	private static final String ENTITY_RULES_PROPERTY = "entityRules";
	private static final String RELATION_RULES_PROPERTY = "relationRules";

	private final String name;
	private final String description;
	private final List<EntityRuleDTO> entityRules;
	private final List<RelationRuleDTO> relationRules;

	@JsonCreator
	public StandardProvisionDTO(
			@NotNull @JsonProperty(NAME_PROPERTY) String name,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@NotNull @JsonProperty(ENTITY_RULES_PROPERTY) List<EntityRuleDTO> entityRules,
			@NotNull @JsonProperty(RELATION_RULES_PROPERTY) List<RelationRuleDTO> relationRules
	) {
		this.name = name;
		this.description = description;
		this.entityRules = entityRules;
		this.relationRules = relationRules;
	}

	@NotNull
	@JsonProperty(NAME_PROPERTY)
	public String getName() {
		return name;
	}

	@NotNull
	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@NotNull
	@JsonProperty(ENTITY_RULES_PROPERTY)
	public List<EntityRuleDTO> getEntityRules() {
		return Collections.unmodifiableList(entityRules);
	}

	@NotNull
	@JsonProperty(RELATION_RULES_PROPERTY)
	public List<RelationRuleDTO> getRelationRules() {
		return Collections.unmodifiableList(relationRules);
	}
}
