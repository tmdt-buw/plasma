package de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class SemanticModelDTO implements Serializable {

	private static final long serialVersionUID = 8522383284345885691L;

	public static final String NODES_PROPERTY = "nodes";
	private static final String EDGES_PROPERTY = "edges";
	private static final String ID_PROPERTY = "id";
	private static final String UUID_PROPERTY = "uuid";
	private static final String LABEL_PROPERTY = "label";
	private static final String DESCRIPTION_PROPERTY = "description";

	private final Long id;
	private final String uuid;
	private final String label;
	private final String description;
	private final List<EntityTypeDTO> nodes;
	private final List<RelationDTO> edges;

	public SemanticModelDTO(
			@Nullable Long id,
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String description,
			List<EntityTypeDTO> nodes,
			List<RelationDTO> edges
	) {
		this(null, null, id, uuid, label, description, nodes, edges);
	}

	public SemanticModelDTO(
			@Nullable Double xCoordinate,
			@Nullable Double yCoordinate,
			@Nullable Long id,
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String description,
			List<EntityTypeDTO> nodes,
			List<RelationDTO> edges
	) {
		this.id = id;
		this.uuid = uuid;
		this.label = label;
		this.description = description;
		this.nodes = nodes != null ? new ArrayList<>(nodes) : Collections.emptyList();
		this.edges = edges != null ? new ArrayList<>(edges) : Collections.emptyList();
	}

	@JsonCreator
	public SemanticModelDTO(
			@Nullable @JsonProperty(ID_PROPERTY) String id,
			@Nullable @JsonProperty(UUID_PROPERTY) String uuid,
			@NotNull @JsonProperty(LABEL_PROPERTY) String label,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@JsonProperty(NODES_PROPERTY) List<EntityTypeDTO> nodes,
			@JsonProperty(EDGES_PROPERTY) List<RelationDTO> edges
	) {
		this(
				id != null ? Long.parseLong(id) : null,
				uuid,
				label,
				description,
				nodes,
				edges
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

	@JsonProperty(UUID_PROPERTY)
	public String getSerializedUuid() {
		return StringUtilities.toStringIfExists(uuid);
	}

	@JsonIgnore
	public String getUuid() {
		return uuid;
	}

	@JsonProperty(LABEL_PROPERTY)
	public String getLabel() {
		return label;
	}

	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@JsonProperty(NODES_PROPERTY)
	public List<EntityTypeDTO> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	@JsonProperty(EDGES_PROPERTY)
	public List<RelationDTO> getEdges() {
		return Collections.unmodifiableList(edges);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, label, description, nodes, edges);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		SemanticModelDTO that = (SemanticModelDTO) o;
		return Objects.equals(id, that.id) &&
		       Objects.equals(label, that.label) &&
		       Objects.equals(description, that.description) &&
		       Objects.equals(nodes, that.nodes) &&
		       Objects.equals(edges, that.edges);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SemanticModelDTO\""
		       + ", \"@super\":" + super.toString()
		       + ", \"id\":\"" + id + '"'
		       + ", \"label\":\"" + label + '"'
		       + ", \"description\":\"" + description + '"'
		       + ", \"nodes\":" + StringUtilities.listToJson(nodes)
		       + ", \"edges\":" + StringUtilities.listToJson(edges)
		       + '}';
	}
}