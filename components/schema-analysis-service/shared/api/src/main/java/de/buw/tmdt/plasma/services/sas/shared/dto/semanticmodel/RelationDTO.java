package de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class RelationDTO implements Serializable {
	private static final long serialVersionUID = -2964347710102077197L;

	public static final String ID_PROPERTY = "id";
	public static final String FROM_ID_PROPERTY = "from";
	public static final String TO_ID_PROPERTY = "to";
	private static final String DESCRIPTION_PROPERTY = "description";
	public static final String CONCEPT_PROPERTY = "concept";

	private final Long id;
	private final Long fromId;
	private final Long toId;
	private final String description;
	private final RelationConceptDTO concept;

	public RelationDTO(
			Long id,
			@NotNull Long fromId,
			@NotNull Long toId,
			@NotNull String description,
			@NotNull RelationConceptDTO concept
	) {
		this.id = id;
		this.fromId = fromId;
		this.toId = toId;
		this.description = description;
		this.concept = concept;
	}

	@JsonCreator
	public RelationDTO(
			@JsonProperty(ID_PROPERTY) String id,
			@NotNull @JsonProperty(FROM_ID_PROPERTY) String fromId,
			@NotNull @JsonProperty(TO_ID_PROPERTY) String toId,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@NotNull @JsonProperty(CONCEPT_PROPERTY) RelationConceptDTO concept
	) {
		this(
				id != null ? Long.parseLong(id) : null,
				Long.parseLong(fromId),
				Long.parseLong(toId),
				description,
				concept
		);
	}

	@JsonProperty(ID_PROPERTY)
	public String getSerializedId() {
		return StringUtilities.toStringIfExists(id);
	}

	@JsonIgnore
	public Long getId() {
		return id;
	}

	@JsonProperty(FROM_ID_PROPERTY)
	public String getSerializedFromId() {
		return fromId.toString();
	}

	@JsonIgnore
	public Long getFromId() {
		return fromId;
	}

	@JsonProperty(TO_ID_PROPERTY)
	public String getSerializedToId() {
		return toId.toString();
	}

	@JsonIgnore
	public Long getToId() {
		return toId;
	}

	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@JsonProperty(CONCEPT_PROPERTY)
	public RelationConceptDTO getConcept() {
		return concept;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fromId, toId, description, concept);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		RelationDTO that = (RelationDTO) o;
		return Objects.equals(fromId, that.fromId) &&
		       Objects.equals(toId, that.toId) &&
		       Objects.equals(description, that.description) &&
		       Objects.equals(concept, that.concept);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"RelationDTO\""
		       + ", \"fromId\":\"" + fromId + '"'
		       + ", \"toId\":\"" + toId + '"'
		       + ", \"description\":\"" + description + '"'
		       + ", \"concept\":" + concept
		       + '}';
	}
}
