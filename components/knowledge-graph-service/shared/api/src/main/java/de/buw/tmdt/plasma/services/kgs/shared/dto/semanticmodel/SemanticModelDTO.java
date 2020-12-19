package de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

public class SemanticModelDTO implements Serializable {

	public static final String NODES_PROPERTY = "nodes";
	private static final long serialVersionUID = 3311571044968680537L;
	private static final String ID_PROPERTY = "id";
	private static final String LABEL_PROPERTY = "label";
	private static final String DESCRIPTION_PROPERTY = "description";
	private static final String EDGES_PROPERTY = "edges";
	private static final String DATASOURCE_ID = "dataSourceId";

	private final String id;
	private final String label;
	private final String description;
	private final Set<EntityTypeDTO> nodes;
	private final Set<RelationDTO> edges;
	private final String dataSourceId;

	@JsonCreator
	public SemanticModelDTO(
			@NotNull @JsonProperty(ID_PROPERTY) String id,
			@NotNull @JsonProperty(LABEL_PROPERTY) String label,
			@JsonProperty(DESCRIPTION_PROPERTY) String description,
			@NotNull @JsonProperty(NODES_PROPERTY) List<EntityTypeDTO> nodes,
			@JsonProperty(EDGES_PROPERTY) List<RelationDTO> edges,
			@NotNull @JsonProperty(DATASOURCE_ID) String dataSourceId
	) {
		this.id = id;
		this.label = label;
		this.description = description != null ? description : "";
		this.nodes = new HashSet<>(nodes);
		this.edges = edges != null ? new HashSet<>(edges) : Collections.emptySet();
		this.dataSourceId = dataSourceId;
	}

	@NotNull
	@JsonProperty(ID_PROPERTY)
	public String getId() {
		return id;
	}

	@NotNull
	@JsonProperty(LABEL_PROPERTY)
	public String getLabel() {
		return label;
	}

	@NotNull
	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@NotNull
	@JsonProperty(NODES_PROPERTY)
	public Set<EntityTypeDTO> getNodes() {
		return Collections.unmodifiableSet(nodes);
	}

	@NotNull
	@JsonProperty(EDGES_PROPERTY)
	public Set<RelationDTO> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	@NotNull
	public String getDataSourceId() {
		return dataSourceId;
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

		return Objects.equals(this.id, that.id) &&
		       Objects.equals(this.label, that.label) &&
		       Objects.equals(this.description, that.description) &&
		       Objects.equals(this.nodes, that.nodes) &&
		       Objects.equals(this.edges, that.edges);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SemanticModelDTO\""
		       + ", \"@super\":" + super.toString()
		       + ", \"id\":\"" + id + '"'
		       + ", \"label\":" + label + '"'
		       + ", \"description\":" + description + '"'
		       + ", \"nodes\":" + StringUtilities.setToJson(nodes)
		       + ", \"edges\":" + StringUtilities.setToJson(edges)
		       + '}';
	}
}