package de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class EntityConceptRelationDTO implements Serializable {

	private static final long serialVersionUID = 3835818285745799914L;

	private static final String ID_PROPERTY = "id";
	private static final String RELATION_CONCEPT_ID_PROPERTY = "relID";
	private static final String TAIL_ENTITY_CONCEPT_ID_PROPERTY = "tailEntityConceptID";
	private static final String HEAD_ENTITY_TYPE_ID_PROPERTY = "headEntityTypeID";

	private final String id;
	private final String relationConceptID;
	private final String tailEntityConceptID;
	private final String headEntityConceptID;

	@JsonCreator
	public EntityConceptRelationDTO(
			@NotNull @JsonProperty(ID_PROPERTY) String id,
			@NotNull @JsonProperty(RELATION_CONCEPT_ID_PROPERTY) String relationConceptID,
			@NotNull @JsonProperty(TAIL_ENTITY_CONCEPT_ID_PROPERTY) String tailEntityConceptID,
			@NotNull @JsonProperty(HEAD_ENTITY_TYPE_ID_PROPERTY) String headEntityConceptID
	) {
		this.id = id;
		this.relationConceptID = relationConceptID;
		this.tailEntityConceptID = tailEntityConceptID;
		this.headEntityConceptID = headEntityConceptID;
	}

	@NotNull
	@JsonProperty(ID_PROPERTY)
	public String getId() {
		return id;
	}

	@NotNull
	@JsonProperty(RELATION_CONCEPT_ID_PROPERTY)
	public String getRelationConceptID() {
		return relationConceptID;
	}

	@NotNull
	@JsonProperty(TAIL_ENTITY_CONCEPT_ID_PROPERTY)
	public String getTailEntityConceptID() {
		return tailEntityConceptID;
	}

	@NotNull
	@JsonProperty(HEAD_ENTITY_TYPE_ID_PROPERTY)
	public String getHeadEntityConceptID() {
		return headEntityConceptID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, relationConceptID, tailEntityConceptID, headEntityConceptID);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}

		EntityConceptRelationDTO that = (EntityConceptRelationDTO) o;

		return Objects.equals(this.id, that.id) &&
				Objects.equals(this.relationConceptID, that.relationConceptID) &&
				Objects.equals(this.tailEntityConceptID, that.tailEntityConceptID) &&
				Objects.equals(this.headEntityConceptID, that.headEntityConceptID);
	}

	@Override
	public String toString() {
		return "EntityConceptRelationDTO{" +
		       "id='" + id + '\'' +
		       ", relationConceptID='" + relationConceptID + '\'' +
		       ", tailEntityConceptID='" + tailEntityConceptID + '\'' +
		       ", headEntityConceptID='" + headEntityConceptID + '\'' +
		       '}';
	}
}