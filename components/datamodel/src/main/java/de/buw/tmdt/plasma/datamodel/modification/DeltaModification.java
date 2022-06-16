package de.buw.tmdt.plasma.datamodel.modification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.Edge;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines a Delta recommendation, a partial update of a combined model.
 */
public class DeltaModification implements Serializable {

    public static final String REFERENCE_PROPERTY = "reference";
    public static final String DELETION_PROPERTY = "deletion";
    public static final String ENTITIES_PROPERTY = "entities";
    public static final String RELATIONS_PROPERTY = "relations";
    public static final String NODES_PROPERTY = "nodes";
    public static final String EDGES_PROPERTY = "edges";
    public static final String ANCHORS_PROPERTY = "anchors";
    public static final String CONFIDENCE_PROPERTY = "confidence";

    /**
     * A unique id identifying this recommendation.
     */
    private final String reference;

    /**
     * Indicates if this modification removes elements.
     */
    private boolean deletion = false;

    /**
     * The set of modified semantic entities.
     */
    @NotNull
    private List<SemanticModelNode> entities;

    /**
     * The set of modified semantic relations.
     */
    @NotNull
    private List<Relation> relations;

    /**
     * The set of modified syntactic nodes.
     */
    @NotNull
    private List<SchemaNode> nodes;

    /**
     * The set of modified syntactic edges.
     */
    @NotNull
    private List<Edge> edges;

    /**
     * Lists the ids of anchor elements.
     * An anchor is a node that belongs to the current state of the model and serves as a connection point
     * for this modification. This list might be empty, e.g. in case a single new semantic node is added.
     */
    @NotNull
    private List<String> anchors;

    private Double confidence;

    public DeltaModification(@NotNull String reference) {
        this(reference, false, null, null, null, null, null, 0.0);
    }

    public DeltaModification(@NotNull String reference,
                             @Nullable List<SemanticModelNode> entities,
                             @Nullable List<Relation> relations,
                             @Nullable List<SchemaNode> nodes,
                             @Nullable List<Edge> edges) {
        this(reference, false, entities, relations, nodes, edges, null, 0.0);
    }

    public DeltaModification(@NotNull String reference,
                             @Nullable List<SemanticModelNode> entities,
                             @Nullable List<Relation> relations,
                             @Nullable List<SchemaNode> nodes,
                             @Nullable List<Edge> edges,
                             @Nullable Double confidence) {
        this(reference, false, entities, relations, nodes, edges, null, confidence);
    }

    @JsonCreator
    public DeltaModification(@JsonProperty(REFERENCE_PROPERTY) String reference,
                             @JsonProperty(value = DELETION_PROPERTY, defaultValue = "false") boolean deletion,
                             @JsonProperty(ENTITIES_PROPERTY) @Nullable List<SemanticModelNode> entities,
                             @JsonProperty(RELATIONS_PROPERTY) @Nullable List<Relation> relations,
                             @JsonProperty(NODES_PROPERTY) @Nullable List<SchemaNode> nodes,
                             @JsonProperty(EDGES_PROPERTY) @Nullable List<Edge> edges,
                             @JsonProperty(ANCHORS_PROPERTY) @Nullable List<String> anchors,
                             @JsonProperty(CONFIDENCE_PROPERTY) @Nullable Double confidence) {
        this.reference = reference;
        this.deletion = deletion;
        this.entities = entities == null ? new ArrayList<>() : entities;
        this.relations = relations == null ? new ArrayList<>() : relations;
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
        this.edges = edges == null ? new ArrayList<>() : edges;
        this.anchors = anchors == null ? new ArrayList<>() : anchors;
        this.confidence = confidence;
    }

    @JsonProperty(REFERENCE_PROPERTY)
    public String getReference() {
        return reference;
    }

    @JsonProperty(value = DELETION_PROPERTY)
    public boolean isDeletion() {
        return deletion;
    }

    @NotNull
    @JsonProperty(ENTITIES_PROPERTY)
    public List<SemanticModelNode> getEntities() {
        return entities;
    }

    public void setEntities(@NotNull List<SemanticModelNode> entities) {
        this.entities = entities;
    }

    @NotNull
    @JsonProperty(NODES_PROPERTY)
    public List<SchemaNode> getNodes() {
        return nodes;
    }

    @NotNull
    @JsonProperty(EDGES_PROPERTY)
    public List<Edge> getEdges() {
        return edges;
    }

    @NotNull
    @JsonProperty(ANCHORS_PROPERTY)
    public List<String> getAnchors() {
        return anchors;
    }

    @Nullable
    @JsonProperty(CONFIDENCE_PROPERTY)
    public Double getConfidence() {
        return confidence;
    }

    public void setDeletion(boolean deletion) {
        this.deletion = deletion;
    }

    @NotNull
    @JsonProperty(RELATIONS_PROPERTY)
    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(@NotNull List<Relation> objectProperties) {
        this.relations = objectProperties;
    }

    public void setNodes(@NotNull List<SchemaNode> nodes) {
        this.nodes = nodes;
    }

    public void setEdges(@NotNull List<Edge> edges) {
        this.edges = edges;
    }

    public void setAnchors(@NotNull List<String> anchors) {
        this.anchors = anchors;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    /**
     * Checks if a given id is set as an anchor for this modification.
     *
     * @param id The element id to check.
     * @return true if the id is listed as an anchor node, false otherwise
     */
    public boolean hasAnchor(String id) {
        return anchors.contains(id);
    }

    /**
     * Creates a deep clone of the current entity.
     *
     * @return The cloned entity with also the {@link SemanticModelNode}s, {@link Relation}s, {@link SchemaNode}s and {@link Edge}s cloned
     */
    public DeltaModification copy() {
        return new DeltaModification(
                getReference(),
                isDeletion(),
                CollectionUtilities.map(getEntities(),
                        SemanticModelNode::copy,
                        ArrayList::new),
                CollectionUtilities.map(getRelations(),
                        Relation::copy,
                        ArrayList::new),
                CollectionUtilities.map(getNodes(),
                        SchemaNode::copy,
                        ArrayList::new),
                CollectionUtilities.map(getEdges(),
                        Edge::copy,
                        ArrayList::new),
                new ArrayList<>(anchors),
                getConfidence()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        de.buw.tmdt.plasma.datamodel.modification.DeltaModification that = (de.buw.tmdt.plasma.datamodel.modification.DeltaModification) o;
        return Objects.equals(getReference(), that.getReference());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReference());
    }

    @Override
    public String toString() {
        return "DeltaModification{" +
                "reference='" + reference + '\'' +
                ". deletion=" + isDeletion() +
                ", entities=" + getEntities().size() +
                ", relations=" + getRelations().size() +
                ", modes=" + getNodes().size() +
                ", edges=" + getEdges().size() +
                ", confidence=" + confidence +
                '}';
    }
}
