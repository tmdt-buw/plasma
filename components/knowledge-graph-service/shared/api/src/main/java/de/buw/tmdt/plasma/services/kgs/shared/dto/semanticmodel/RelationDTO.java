package de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class RelationDTO implements Serializable {

	private static final String ID_PROPERTY = "id";
	private static final String FROM_ID_PROPERTY = "from";
	private static final String TO_ID_PROPERTY = "to";
	private static final String CONCEPT_PROPERTY = "concept";

	private static final long serialVersionUID = 2671838076275658074L;

	private final String id;
	private final String fromId;
	private final String toId;
	private final RelationConceptDTO concept;

	@JsonCreator
	public RelationDTO(
			@NotNull @JsonProperty(ID_PROPERTY) String id,
			@NotNull @JsonProperty(FROM_ID_PROPERTY) String fromId,
			@NotNull @JsonProperty(TO_ID_PROPERTY) String toId,
			@NotNull @JsonProperty(CONCEPT_PROPERTY) RelationConceptDTO concept
	) {
		this.id = id;
		this.fromId = fromId;
		this.toId = toId;
		this.concept = concept;
	}

	@NotNull
	@JsonProperty(ID_PROPERTY)
	public String getId() {
		return id;
	}

	@NotNull
	@JsonProperty(FROM_ID_PROPERTY)
	public String getFromId() {
		return fromId;
	}

	@NotNull
	@JsonProperty(TO_ID_PROPERTY)
	public String getToId() {
		return toId;
	}

	@NotNull
	@JsonProperty(CONCEPT_PROPERTY)
	public RelationConceptDTO getConcept() {
		return concept;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fromId, toId, concept);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RelationDTO)) {
			return false;
		}
		RelationDTO that = (RelationDTO) o;
		return Objects.equals(id, that.id) &&
		       Objects.equals(fromId, that.fromId) &&
		       Objects.equals(toId, that.toId) &&
		       Objects.equals(concept, that.concept);
	}

	@Override
	public String toString() {
		return "CreateRelationDTO{" +
		       "id='" + id + '\'' +
		       ", fromId='" + fromId + '\'' +
		       ", toId='" + toId + '\'' +
		       ", concept=" + concept +
		       '}';
	}
}