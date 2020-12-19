package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

public class SyntaxModelDTO implements Serializable {

	private static final String ROOT_PROPERTY = "root";
	public static final String NODES_PROPERTY = "nodes";
	private static final String EDGES_PROPERTY = "edges";
	private static final long serialVersionUID = -7924812162247359979L;

	private final UUID root;
	private final ArrayList<SchemaNodeDTO> nodes;
	private final ArrayList<EdgeDTO> edges;

	public SyntaxModelDTO(
			@JsonProperty(ROOT_PROPERTY) @NotNull UUID root,
			@JsonProperty(NODES_PROPERTY) @NotNull List<SchemaNodeDTO> nodes,
			@JsonProperty(EDGES_PROPERTY) @NotNull List<EdgeDTO> edges
	) {
		this.root = root;
		if (CollectionUtilities.containsNull(nodes)) {
			throw new IllegalArgumentException("Nodes must not contain null.");
		}
		this.nodes = new ArrayList<>(nodes);
		if (CollectionUtilities.containsNull(edges)) {
			throw new IllegalArgumentException("Edges must not contain null.");
		}
		this.edges = new ArrayList<>(edges);
	}

	@JsonProperty(ROOT_PROPERTY)
	public UUID getRoot() {
		return root;
	}

	@JsonProperty(NODES_PROPERTY)
	public List<SchemaNodeDTO> getNodes() {
		return nodes;
	}

	@JsonProperty(EDGES_PROPERTY)
	public List<EdgeDTO> getEdges() {
		return edges;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nodes, edges);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		SyntaxModelDTO that = (SyntaxModelDTO) o;
		return Objects.equals(nodes, that.nodes) &&
		       Objects.equals(edges, that.edges) &&
		       Objects.equals(root, that.root);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SyntaxModelDTO\""
		       + ", \"root\":" + root
		       + ", \"nodes\":" + StringUtilities.listToJson(nodes)
		       + ", \"edges\":" + StringUtilities.listToJson(edges)
		       + '}';
	}
}
