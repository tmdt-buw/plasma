package de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.dms.shared.dto.Positioned;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SemanticModelDTO extends Positioned {

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
			@Nullable String id,
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String description,
			List<EntityTypeDTO> nodes,
			List<RelationDTO> edges
	) {
		this(null, null, id, uuid, label, description, nodes, edges);
	}

	@JsonCreator
	public SemanticModelDTO(
			@Nullable @JsonProperty(X_COORDINATE_PROPERTY) Double xCoordinate,
			@Nullable @JsonProperty(Y_COORDINATE_PROPERTY) Double yCoordinate,
			@Nullable @JsonProperty(ID_PROPERTY) String id,
			@Nullable @JsonProperty(UUID_PROPERTY) String uuid,
			@NotNull @JsonProperty(LABEL_PROPERTY) String label,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@JsonProperty(NODES_PROPERTY) List<EntityTypeDTO> nodes,
			@JsonProperty(EDGES_PROPERTY) List<RelationDTO> edges
	) {
		super(xCoordinate, yCoordinate);
		this.id = id != null ? Long.parseLong(id) : null;
		this.uuid = uuid;
		this.label = label;
		this.description = description;
		this.nodes = nodes != null ? new ArrayList<>(nodes) : Collections.emptyList();
		this.edges = edges != null ? new ArrayList<>(edges) : Collections.emptyList();
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
		return nodes;
	}

	@JsonProperty(EDGES_PROPERTY)
	public List<RelationDTO> getEdges() {
		return edges;
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