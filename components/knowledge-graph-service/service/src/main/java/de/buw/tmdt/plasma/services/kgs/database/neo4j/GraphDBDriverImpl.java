package de.buw.tmdt.plasma.services.kgs.database.neo4j;

import com.steelbridgelabs.oss.neo4j.structure.Neo4JGraph;
import com.steelbridgelabs.oss.neo4j.structure.providers.Neo4JNativeElementIdProvider;
import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.*;
import de.buw.tmdt.plasma.services.kgs.shared.model.Element;
import de.buw.tmdt.plasma.services.kgs.shared.model.Position;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.Relation;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;

public class GraphDBDriverImpl implements GraphDBDriver {

    private static final Logger logger = LoggerFactory.getLogger(GraphDBDriverImpl.class);
    private static final String EDGE_BASE_CLASS = "E";
    private static final char UNDERSCORE = '_';

    private final Driver driver;

    public GraphDBDriverImpl(@NotNull Driver driver) {
        this.driver = driver;
    }

    /**
     * Uses an existent driver to connect to the remote Neo4j database, or creates one if needed.
     * Then, creates a {@link Graph} interface to operate Neo4j via Tinkerpop.
     *
     * @return A {@link Graph} interface to work with the remote Neo4j database.
     */
    @NotNull
    public Graph getGraph() {
        Neo4JGraph neo4JGraph = new Neo4JGraph(driver, new Neo4JNativeElementIdProvider(), new Neo4JNativeElementIdProvider());
        neo4JGraph.tx().onClose(org.apache.tinkerpop.gremlin.structure.Transaction.CLOSE_BEHAVIOR.ROLLBACK); // Rollback, if closed but not committed.
        return neo4JGraph;
    }

    void closeGraph(@NotNull Graph graph) {
        try {
            graph.close();
        } catch (Exception e) {
            logger.warn("Could not close graph", e);
        }
    }

    @NotNull
    @Override
    public SemanticModel retrieveSemanticModel(@NotNull String id) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .has(Neo4JModel.SemanticModel.PROPERTY_ID, new P<>(StringUtils::startsWithIgnoreCase, id));

            if (!traversal.hasNext()) {
                logger.warn("Can't find {} with id {}", Neo4JModel.SemanticModel.CLASS, id);
                throw new NoElementForIDException(id);
            }

            return marshalSemanticModel(traversal.next());
        } finally {
            closeGraph(graph);
        }
    }

    /* ################################### Semantic Model ################################### */

    @Override
    public boolean removeSemanticModel(@NotNull String id) {
        final Graph graph = getGraph();

        try {
            // First, get vertex of the SM itself.
            final GraphTraversal<Vertex, Vertex> semanticModelTraversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .has(Neo4JModel.SemanticModel.PROPERTY_ID, id);

            if (!semanticModelTraversal.hasNext()) {
                logger.warn("No SemanticModel for id {} found, unable to remove.", id);
                return false;
            }
            final Vertex semanticModelVertex = semanticModelTraversal.next();

            SemanticModel sm = marshalSemanticModel(semanticModelVertex);
            sm.getRelations().forEach(r -> decreaseNumberOfUsages(graph, r));

            // Vertices to remove satisfy both of the following:
            // a) They are directly referenced to by this SM (SM -> ET/R)
            // b) They are *not* referenced to by any other SM
            final Set<Vertex> verticesToRemove = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .has(Neo4JModel.SemanticModel.PROPERTY_ID, id)
                    // Get all vertices that are directly used in this SM (these are ETs and Rs).
                    .out()
                    .or(
                            has(Neo4JModel.EntityType.CLASS),
                            has(Neo4JModel.Relation.CLASS)
                    )
                    // Which are also used in an SM which has a *different* id from the current one.
                    .where(__.in().hasLabel(Neo4JModel.SemanticModel.CLASS).not(has(Neo4JModel.SemanticModel.PROPERTY_ID, id)))
                    .toSet();

            // Actually remove vertices and commit changes.
            semanticModelVertex.remove();
            verticesToRemove.forEach(org.apache.tinkerpop.gremlin.structure.Element::remove);
            graph.tx().commit();
            return true;
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Map<String, Element> createSemanticModelExposeMapping(@NotNull SemanticModel semanticModel) {
        // Create graph and write cache
        final Graph graph = getGraph();
        try {
            final Map<String, Pair<Vertex, ? extends Element>> writeCache = new HashMap<>();
            final Map<String, Element> readCache = new HashMap<>();

            // Create all missing EntityConcepts and cache all.
            final Set<EntityConcept> entityConcepts = semanticModel.getEntityTypes().stream()
                    .map(EntityType::getEntityConcept)
                    .collect(Collectors.toSet());
            for (EntityConcept entityConcept : entityConcepts) {
                createEntityConcept(entityConcept, graph, writeCache, readCache);
            }

            // Create missing RelationConcepts and cache all.
            final Set<RelationConcept> relationConcepts = semanticModel.getRelations().stream()
                    .map(Relation::getRelationConcept)
                    .collect(Collectors.toSet());
            for (RelationConcept relationConcept : relationConcepts) {
                createRelationConcept(relationConcept, graph, writeCache, readCache);
            }

            // Create SemanticModel, set its properties and add relations gradually.
            final Vertex semanticModelVertex = graph.addVertex(Neo4JModel.SemanticModel.CLASS);
            final String semanticModelId = generateId(Neo4JModel.SemanticModel.ID_PREFIX);
            semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_ID, semanticModelId);
            semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_LABEL, semanticModel.getLabel());
            semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_DESCRIPTION, semanticModel.getDescription());
            semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_DATA_SOURCE_ID, semanticModel.getDataSourceId());

            // Create EntityTypes, add to SemanticModel and add to cache.
            final HashSet<EntityType> createdEntityTypes = new HashSet<>();

            semanticModel.getEntityTypes().forEach(
                    entityType -> {
                        // Retrieve referenced EntityConcept from cache.
                        @SuppressWarnings("unchecked - The map entry actually is an EntityConcept")
                        Pair<Vertex, EntityConcept> entityConceptPair = (Pair<Vertex, EntityConcept>) writeCache.get(entityType.getEntityConcept().getId());
                        if (entityConceptPair == null) {
                            logger.error(
                                    "SemanticModel {} refers to the EntityConcept ID {}, which is not present in graph.",
                                    semanticModel.getId(),
                                    entityType.getEntityConcept().getId()
                            );
                            throw new NoElementForIDException(entityType.getEntityConcept().getId());
                        }

                        Vertex entityTypeVertex;
                        entityTypeVertex = retrieveVertex(graph, Neo4JModel.EntityType.PROPERTY_ID, entityType.getId(), Neo4JModel.EntityType.CLASS);
                        if (entityTypeVertex == null) {
                            entityTypeVertex = graph.addVertex(Neo4JModel.EntityType.CLASS);
                            entityTypeVertex.property(Neo4JModel.EntityType.PROPERTY_ID, generateId(Neo4JModel.EntityType.ID_PREFIX));
                            entityTypeVertex.property(Neo4JModel.EntityType.PROPERTY_LABEL, entityType.getLabel());
                            entityTypeVertex.property(Neo4JModel.EntityType.PROPERTY_MAPPED_TO_DATA, entityType.isMappedToData());
                            entityTypeVertex.property(Neo4JModel.EntityType.PROPERTY_DESCRIPTION, entityType.getDescription());
                            entityTypeVertex.property(Neo4JModel.EntityType.PROPERTY_ORIGINAL_LABEL, entityType.getOriginalLabel());
                            entityTypeVertex.addEdge(EDGE_BASE_CLASS, entityConceptPair.getLeft());

                            //create position node
                            Vertex positionVertex = graph.addVertex(Neo4JModel.Position.CLASS);
                            positionVertex.property(Neo4JModel.Position.PROPERTY_ID, generateId(Neo4JModel.Position.ID_PREFIX));
                            positionVertex.property(Neo4JModel.Position.PROPERTY_X_COORDINATE, entityType.getPosition().getX());
                            positionVertex.property(Neo4JModel.Position.PROPERTY_Y_COORDINATE, entityType.getPosition().getY());
                            entityTypeVertex.addEdge(Neo4JModel.Position.Edge.POSITION, positionVertex);
                        }

                        semanticModelVertex.addEdge(EDGE_BASE_CLASS, entityTypeVertex);

                        // Create EntityType, link to it from SemanticModel and add to cache.
                        final EntityType createdEntityType = new EntityType(
                                readProperty(entityTypeVertex, Neo4JModel.EntityType.PROPERTY_ID, String.class),
                                readProperty(entityTypeVertex, Neo4JModel.EntityType.PROPERTY_LABEL, String.class),
                                entityType.getOriginalLabel(),
                                entityConceptPair.getRight(),
                                marshalPosition(entityTypeVertex, readProperty(entityTypeVertex, Neo4JModel.EntityType.PROPERTY_ID, String.class)),
                                readProperty(entityTypeVertex, Neo4JModel.EntityType.PROPERTY_DESCRIPTION, String.class),
                                readProperty(entityTypeVertex, Neo4JModel.EntityType.PROPERTY_MAPPED_TO_DATA, Boolean.class)
                        );
                        createdEntityTypes.add(createdEntityType);
                        writeCache.put(entityType.getId(), new Pair<>(entityTypeVertex, createdEntityType));
                    }
            );

            // Create Relations, add to SemanticModel and add to cache.
            final HashSet<Relation> createdRelations = new HashSet<>();
            semanticModel.getRelations().forEach(
                    relation -> {
                        // Retrieve missing elements.
                        final Pair<Vertex, ? extends Element> relationConceptPair = writeCache.get(relation.getRelationConcept().getId());

                        if (relationConceptPair == null || relationConceptPair.getLeft() == null || relationConceptPair.getRight() == null) {
                            logger.error("Cached element missing {}", relation.getRelationConcept().getId());
                            throw new GraphDBException("Cached element missing");
                        }
                        final Pair<Vertex, ? extends Element> headEntityTypePair = writeCache.get(relation.getHeadNode().getId());
                        if (headEntityTypePair == null) {
                            logger.error("Cached element missing {}", relation.getHeadNode().getId());
                            throw new GraphDBException("Cached element missing");
                        }
                        final Pair<Vertex, ? extends Element> tailEntityTypePair = writeCache.get(relation.getTailNode().getId());
                        if (tailEntityTypePair == null) {
                            logger.error("Cached element missing {}", relation.getTailNode().getId());
                            throw new GraphDBException("Cached element missing");
                        }

                        Vertex relationVertex;
                        relationVertex = retrieveVertex(graph, Neo4JModel.Relation.PROPERTY_ID, relation.getId(), Neo4JModel.Relation.CLASS);
                        if (relationVertex == null) {
                            relationVertex = graph.addVertex(Neo4JModel.Relation.CLASS);
                            relationVertex.property(Neo4JModel.Relation.PROPERTY_ID, generateId(Neo4JModel.Relation.ID_PREFIX));
                            // Create edge to RelationConcept vertex (R->RC).
                            relationVertex.addEdge(EDGE_BASE_CLASS, relationConceptPair.getLeft());
                            // Create/merge head vertex (R->ET).
                            relationVertex.addEdge(EDGE_BASE_CLASS, headEntityTypePair.getLeft());
                            // Create/merge tail vertex (ET->R).
                            tailEntityTypePair.getLeft().addEdge(EDGE_BASE_CLASS, relationVertex);
                        }
                        // Create edge SM->R.
                        semanticModelVertex.addEdge(EDGE_BASE_CLASS, relationVertex);

                        // Create Relation, link to it from SemanticModel and add to cache.
                        final Relation createdRelation = new Relation(
                                (String) relationVertex.property(Neo4JModel.Relation.PROPERTY_ID).value(),
                                (EntityType) tailEntityTypePair.getRight(),
                                (EntityType) headEntityTypePair.getRight(),
                                (RelationConcept) relationConceptPair.getRight()
                        );
                        createdRelations.add(createdRelation);

                        createEntityConceptRelation(createdRelation, graph, writeCache, readCache);

                        writeCache.put(relation.getId(), new Pair<>(relationVertex, createdRelation));
                    }
            );

            // Commit the current transaction.
            graph.tx().commit();

            // Finally, create SemanticModel itself and add to cache.
            final SemanticModel createdSemanticModel = new SemanticModel(
                    (String) semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_ID).value(),
                    (String) semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_LABEL).value(),
                    (String) semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_DESCRIPTION).value(),
                    createdEntityTypes,
                    createdRelations,
                    (String) semanticModelVertex.property(Neo4JModel.SemanticModel.PROPERTY_DATA_SOURCE_ID).value()
            );

            writeCache.put(semanticModel.getId(), new Pair<>(semanticModelVertex, createdSemanticModel));

            return writeCache.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, // mappingId
                            e -> e.getValue().getRight() // Element
                    ));
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<SemanticModel> searchSemanticModelsForEntityType(@NotNull String label) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityType.CLASS)
                    .or(
                            has(Neo4JModel.EntityType.PROPERTY_LABEL, label),
                            has(Neo4JModel.EntityType.PROPERTY_ORIGINAL_LABEL, label)
                    )
                    .in(EDGE_BASE_CLASS)
                    .hasLabel(Neo4JModel.SemanticModel.CLASS);
            Set<Vertex> semanticModelVertices = traversal.toSet();

            if (semanticModelVertices.isEmpty()) {
                logger.warn("No SemanticModel refers to EntityType label {}.", label);
                return new HashSet<>();
            }

            return semanticModelVertices.stream().map(this::marshalSemanticModel).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<SemanticModel> searchSemanticModelsForEntityConceptMainLabel(@NotNull String mainLabel) {
        final Graph graph = getGraph();

        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS) // The entity concept of interest
                    .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::equalsIgnoreCase, mainLabel))
                    .in().hasLabel(Neo4JModel.EntityType.CLASS)
                    .in().hasLabel(Neo4JModel.SemanticModel.CLASS);
            final Set<Vertex> semanticModelVertices = traversal.toSet();

            return semanticModelVertices.stream().map(this::marshalSemanticModel).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<SemanticModel> searchSemanticModelsForEntityConceptSynonymLabel(@NotNull String synonymLabel) {
        final Graph graph = getGraph();

        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::equalsIgnoreCase, synonymLabel))
                    .in(Neo4JModel.EntityConcept.Edge.SYNONYM)
                    .hasLabel(Neo4JModel.EntityConcept.CLASS) // The entity concept of interest
                    .in().hasLabel(Neo4JModel.EntityType.CLASS)
                    .in().hasLabel(Neo4JModel.SemanticModel.CLASS);
            final Set<Vertex> semanticModelVertices = traversal.toSet();

            return semanticModelVertices.stream().map(this::marshalSemanticModel).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<SemanticModel> searchSemanticModelsForEntityConceptAnyLabel(@NotNull String anyLabel) {
        final Graph graph = getGraph();

        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .or(
                            __.hasLabel(Neo4JModel.EntityConcept.CLASS)
                                    .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::equalsIgnoreCase, anyLabel)),
                            __.hasLabel(Neo4JModel.EntityConcept.CLASS)
                                    .in(Neo4JModel.EntityConcept.Edge.SYNONYM)
                                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                                    .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::equalsIgnoreCase, anyLabel))
                    )
                    .in().hasLabel(Neo4JModel.EntityType.CLASS)
                    .in().hasLabel(Neo4JModel.SemanticModel.CLASS);
            final Set<Vertex> semanticModelVertices = traversal.toSet();

            return semanticModelVertices.stream().map(this::marshalSemanticModel).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<SemanticModel> searchSemanticModelByEntityTypeOrEntityConceptContainingLabel(@NotNull String label) {
        // Creates a P instance which applies a BiConsumer to check if a given String is contained.
        final P<String> containsLabelIgnoreCase = new P<>(StringUtils::containsIgnoreCase, label);
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .where(
                            // We might encounter four different cases:
                            or(
                                    // SM-->ET(label)
                                    out().hasLabel(Neo4JModel.EntityType.CLASS).has(Neo4JModel.EntityType.PROPERTY_LABEL, containsLabelIgnoreCase),
                                    out().hasLabel(Neo4JModel.EntityType.CLASS).has(Neo4JModel.EntityType.PROPERTY_ORIGINAL_LABEL, containsLabelIgnoreCase),

                                    // SM-->ET-->EC(label)
                                    out().hasLabel(Neo4JModel.EntityType.CLASS)
                                            .out().hasLabel(Neo4JModel.EntityConcept.CLASS)
                                            .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, containsLabelIgnoreCase)
                            )
                    );
            final Set<Vertex> semanticModelVertices = traversal.toSet();
            return semanticModelVertices.stream().map(this::marshalSemanticModel).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    /* ################################### Entity Concept ################################### */

    @NotNull
    @Override
    public EntityConcept retrieveEntityConcept(@NotNull String id) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, id);

            if (!traversal.hasNext()) {
                logger.warn("Can't find {} with id {}", Neo4JModel.EntityConcept.CLASS, id);
                throw new NoElementForIDException(id);
            }

            return marshalEntityConcept(traversal.next(), new HashMap<>());
        } finally {
            closeGraph(graph);
        }
    }

    @NotNull
    @Override
    public Set<String> retrieveEntityConceptSynonyms(@NotNull String id) throws NoElementForIDException {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, id);

            if (!traversal.hasNext()) {
                logger.warn("Can't find {} with id {}", Neo4JModel.EntityConcept.CLASS, id);
                throw new NoElementForIDException(id);
            }

            // Extract synonym labels
            final Iterator<Vertex> synonymLabelVertices = traversal.next().vertices(Direction.OUT, Neo4JModel.EntityConcept.Edge.SYNONYM);
            final HashSet<String> synonymLabels = new HashSet<>();
            while (synonymLabelVertices.hasNext()) {
                synonymLabels.add(readProperty(synonymLabelVertices.next(), Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, String.class));
            }
            return synonymLabels;
        } finally {
            closeGraph(graph);
        }
    }

    /**
     * Creates the given EntityConcept if it is not present in the graph yet, and caches it if needed.
     * Note: Transaction commit has to be done by the caller.
     *
     * @param entityConcept the EntityConcept to create
     * @param graph         the graph on which the traversal takes place
     * @param writeCache    the cache in which the newly written elements will be stored
     * @param readCache     the cache in which all elements will be stored
     *
     * @return a Pair holding the Vertex in the graph as left, and the created/retrieved EntityConcept as right
     */
    @NotNull
    private Pair<Vertex, EntityConcept> createEntityConcept(
            @NotNull EntityConcept entityConcept,
            @NotNull Graph graph,
            @NotNull Map<String, Pair<Vertex, ? extends Element>> writeCache,
            @NotNull Map<String, Element> readCache
    ) {
        // Fast return if created already.
        if (writeCache.get(entityConcept.getId()) != null) {
            //noinspection unchecked,unchecked
            return (Pair<Vertex, EntityConcept>) writeCache.get(entityConcept.getId());
        }

        // To avoid adding a duplicating EntityConcept, we need to check three different cases.

        // Case 1: The ID already exists.
        final GraphTraversal<Vertex, Vertex> sameID = graph.traversal().V().has(Neo4JModel.EntityConcept.PROPERTY_ID, entityConcept.getId());

        // Case 2: There already exists a concept with the same main label/description
        final GraphTraversal<Vertex, Vertex> sameConcept;

        final String mainLabel = entityConcept.getMainLabel();
        final String description = entityConcept.getDescription();

        sameConcept = graph.traversal().V()
                .hasLabel(Neo4JModel.EntityConcept.CLASS)
                .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::equalsIgnoreCase, mainLabel))
                .has(Neo4JModel.EntityConcept.PROPERTY_DESCRIPTION, new P<>(StringUtils::equalsIgnoreCase, description));

        // Case 3: There exists an EntityConcept with the same source URI
        final GraphTraversal<Vertex, Vertex> sameDataSourceAndID = graph.traversal().V()
                .has(Neo4JModel.EntityConcept.CLASS)
                .where(has(Neo4JModel.EntityConcept.PROPERTY_SOURCE_URI, entityConcept.getSourceURI()));

        final Vertex createdVertex;
        final EntityConcept createdEntityConcept;
        if (sameID.hasNext()) {
            // Duplicate case 1
            createdVertex = sameID.next();
//            logger.debug(
//                    "EntityConcept to be created is a duplicate wrt. ID: {}",
//                    readProperty(createdVertex, Neo4JModel.EntityConcept.PROPERTY_ID, String.class)
//            );
            createdEntityConcept = marshalEntityConcept(createdVertex, readCache);
        } else if (sameConcept.hasNext()) {
            // Duplicate case 2
            createdVertex = sameConcept.next();
//            logger.debug(
//                    "Labels of the EntityConcept to be created are already covered by the existing EntityConcept {}",
//                    readProperty(createdVertex, Neo4JModel.EntityConcept.PROPERTY_ID, String.class)
//            );
            createdEntityConcept = marshalEntityConcept(createdVertex, readCache);
        } else if (sameDataSourceAndID.hasNext()) {
            // Duplicate case 3
            createdVertex = sameDataSourceAndID.next();
//            logger.debug(
//                    "EntityConcept to be created is a duplicate wrt. data source and ID: {}",
//                    readProperty(createdVertex, Neo4JModel.EntityConcept.PROPERTY_ID, String.class)
//            );
            createdEntityConcept = marshalEntityConcept(createdVertex, readCache);
        } else {
            // No duplicate, create EntityConcept.

            final String id = generateId(Neo4JModel.EntityConcept.ID_PREFIX);
            logger.info("Creating EntityConcept '{}'({}) vertex at {}",entityConcept.getMainLabel(),entityConcept.getSourceURI(), id);
            // Create new vertex.
            createdVertex = graph.addVertex(Neo4JModel.EntityConcept.CLASS);

            // Set properties.
            createdVertex.property(Neo4JModel.EntityConcept.PROPERTY_ID, id);
            createdVertex.property(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, entityConcept.getMainLabel());
            createdVertex.property(Neo4JModel.EntityConcept.PROPERTY_DESCRIPTION, entityConcept.getDescription());
            createdVertex.property(Neo4JModel.EntityConcept.PROPERTY_SOURCE_URI, entityConcept.getSourceURI());

            // Merge labels: Reuse existing labels and create new ones. 1) synonym labels, 2) keyword labels, 3)
            // characteristics

            createdEntityConcept = new EntityConcept(
                    id,
                    entityConcept.getMainLabel(),
                    entityConcept.getDescription(),
                    entityConcept.getSourceURI()
            );
        }

        final Pair<Vertex, EntityConcept> result = new Pair<>(createdVertex, createdEntityConcept);
        writeCache.put(entityConcept.getId(), result);
        return result;
    }

    @NotNull
    @Override
    public Set<EntityConcept> retrieveEntityConceptsByPrefix(@NotNull String prefix) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::startsWithIgnoreCase, prefix));
            final Set<Vertex> entityConceptVertices = traversal.toSet();
            Map<String, Element> cache = new HashMap<>();
            return entityConceptVertices.stream()
                    .map(vertex -> marshalEntityConcept(vertex, cache))
                    .collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @NotNull
    @Override
    public Set<EntityConcept> retrieveEntityConceptsContainingString(@NotNull String string) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(StringUtils::containsIgnoreCase, string));
            final Set<Vertex> entityConceptVertices = traversal.toSet();
            Map<String, Element> cache = new HashMap<>();
            return entityConceptVertices.stream()
                    .map(vertex -> marshalEntityConcept(vertex, cache))
                    .collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<EntityConcept> queryRelatedEntityConcepts(@NotNull EntityConcept root, @NotNull RelationConcept relationConcept) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS) // The root EC itself.
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, root.getId())
                    .emit()
                    .repeat(out(EDGE_BASE_CLASS)
                                    .simplePath() // Avoids cyclic paths.
                                    .where(out(EDGE_BASE_CLASS)
                                                   .has(Neo4JModel.RelationConcept.PROPERTY_ID, relationConcept.getId()))
                                    .both(EDGE_BASE_CLASS)
                                    .simplePath() // Avoids cyclic paths.
                                    .where(__.hasLabel(Neo4JModel.EntityConcept.CLASS)));
            // One hop to a ECR node (which refers to the given RC, then the second hop yields the next EC we're looking for.
            final Set<Vertex> vertices = traversal.toSet();

            if (vertices.isEmpty()) {
                logger.warn("Can't find {} with id {}", Neo4JModel.EntityConcept.CLASS, root.getId());
                throw new NoElementForIDException(root.getId());
            }

            HashMap<String, Element> cache = new HashMap<>();
            return vertices.stream().map(vertex -> marshalEntityConcept(vertex, cache)).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    /**
     * This method creates the given EntityConcept in the graph.
     *
     * @param entityConcept the EntityConcept to create.
     * @return the created EntityConcept vertex
     */
    @NotNull
    @Override
    public EntityConcept createEntityConcept(@NotNull EntityConcept entityConcept) {
        final Graph graph = getGraph();
        try {
            EntityConcept result = createEntityConcept(entityConcept, graph, new HashMap<>(), new HashMap<>()).getRight();
            graph.tx().commit();
            return result;
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public boolean deleteEntityConcept(@NotNull String ecID) {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V().hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecID);

            if (!traversal.hasNext()) {
                logger.warn("No entity concept with id {} found for deletion", ecID);
                return false;
            }

            Vertex ecVertex = traversal.next();
            ecVertex.remove();
            return true;
        } finally {
            graph.tx().commit();
            closeGraph(graph);
        }
    }

    @Override
    public boolean createEdge(@NotNull String ecIDfrom, @NotNull String ecIDto, String edgeName) {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Vertex> traversalEcFrom = graph.traversal().V().hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecIDfrom);

            GraphTraversal<Vertex, Vertex> traversalEcTo = graph.traversal().V().hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecIDto);

            if (traversalEcFrom.hasNext() && traversalEcTo.hasNext()) {
                Vertex ecFrom = traversalEcFrom.next();
                Vertex ecTo = traversalEcTo.next();
                ecFrom.addEdge(edgeName, ecTo);
                return true;
            } else {
                logger.warn("Problem with finding entity concepts for {} and {}", ecIDfrom, ecIDto);
                return false;
            }
        } finally {
            graph.tx().commit();
            closeGraph(graph);
        }
    }

    @Override
    public boolean hasEdge(@NotNull String ecIDfrom, @NotNull String ecIDto, String edgeName) {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V().hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecIDfrom)
                    .out(edgeName)
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecIDto);

            //logger.warn("Problem with finding edge {} between entity concepts {} and {}", edgeName, ecIDfrom, ecIDto);
            return traversal.hasNext();
        } finally {
            graph.tx().commit();
            closeGraph(graph);
        }
    }

    @Override
    public boolean hasPath(@NotNull String ecIDfrom, @NotNull String ecIDto) {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Path> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecIDfrom)
                    .emit(cyclicPath().or().not(both()))
                    .repeat(both())
                    .until(__.hasLabel(Neo4JModel.EntityConcept.CLASS)
                                   .has(Neo4JModel.EntityConcept.PROPERTY_ID, ecIDto))
                    .path();

            if (traversal.hasNext()) {
                return true;
            } else {
                logger.warn("EC {} and {} are not connected", ecIDfrom, ecIDto);
                return false;
            }
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public int getNumberOfReferences(@NotNull String ecId) {
        final Graph graph = getGraph();

        try {
            GraphTraversal<Vertex, Vertex> ecrTraversal = graph.traversal().V()
                    .has(Neo4JModel.EntityConcept.CLASS)
                    .hasLabel(Neo4JModel.EntityConcept.PROPERTY_ID, ecId)
                    .out().has(Neo4JModel.EntityConceptRelation.CLASS);

            if (!ecrTraversal.hasNext()) {
                logger.debug("No ECRs for EC with id {} found", ecId);
                return 0;
            }

            return ecrTraversal.toStream()
                    .map(vertex -> readProperty(vertex, Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, Integer.class))
                    .reduce(0, Integer::sum);
        } finally {
            closeGraph(graph);
        }
    }

    /* ################################### Entity Type ################################### */

    @NotNull
    @Override
    public EntityType retrieveEntityType(@NotNull String id) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityType.CLASS)
                    .has(Neo4JModel.EntityType.PROPERTY_ID, id);

            if (!traversal.hasNext()) {
                logger.error("Can't find {} with id {}", Neo4JModel.EntityType.CLASS, id);
                throw new NoElementForIDException(id);
            }

            return marshalEntityType(traversal.next(), new HashMap<>());
        } finally {
            closeGraph(graph);
        }
    }

    /* ################################### Relation Concept ################################### */

    @NotNull
    @Override
    public RelationConcept retrieveRelationConcept(@NotNull String id) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.RelationConcept.CLASS)
                    .has(Neo4JModel.RelationConcept.PROPERTY_ID, id);

            if (!traversal.hasNext()) {
                logger.error("Can't find {} with id {}", Neo4JModel.RelationConcept.CLASS, id);
                throw new NoElementForIDException(id);
            }

            return marshalRelationConcept(traversal.next(), new HashMap<>());
        } finally {
            closeGraph(graph);
        }
    }

    @NotNull
    @Override
    public Set<RelationConcept> retrieveRelationConceptsByPrefix(@NotNull String prefix) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.RelationConcept.CLASS)
                    .has(Neo4JModel.RelationConcept.PROPERTY_LABEL, new P<>(StringUtils::startsWithIgnoreCase, prefix));
            final Set<Vertex> relationConceptVertices = traversal.toSet();
            Map<String, Element> cache = new HashMap<>();
            return relationConceptVertices.stream()
                    .map(vertex -> marshalRelationConcept(vertex, cache))
                    .collect(Collectors.toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @NotNull
    @Override
    public Set<RelationConcept> retrieveRelationConceptsContainingString(@NotNull String string) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.RelationConcept.CLASS)
                    .has(Neo4JModel.RelationConcept.PROPERTY_LABEL, new P<>(StringUtils::containsIgnoreCase, string));
            final Set<Vertex> relationConceptVertices = traversal.toSet();
            Map<String, Element> cache = new HashMap<>();
            return relationConceptVertices.stream()
                    .map(vertex -> marshalRelationConcept(vertex, cache))
                    .collect(Collectors.toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @NotNull
    @Override
    public RelationConcept createRelationConcept(@NotNull RelationConcept relationConcept) {
        Graph graph = getGraph();

        try {
            // Throw an exception, if there already exists a concept with the same name.
            GraphTraversal<Vertex, Vertex> graphTraversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.RelationConcept.CLASS)
                    .has(Neo4JModel.RelationConcept.PROPERTY_LABEL, relationConcept.getLabel())
                    .has(Neo4JModel.RelationConcept.PROPERTY_DESCRIPTION, relationConcept.getDescription());
            if (graphTraversal.hasNext()) {
                final Vertex existingVertex = graphTraversal.next();
                final String existingId = readProperty(existingVertex, Neo4JModel.RelationConcept.PROPERTY_ID, String.class);
                logger.error(
                        "Name uniqueness violated, RelationConcept with label \"{}\" already exists at {}",
                        relationConcept.getLabel(),
                        existingId
                );
                throw new ConceptNameAlreadyExistsException(relationConcept.getLabel());
            }

            final RelationConcept result = createRelationConcept(relationConcept, graph, new HashMap<>(), new HashMap<>()).getRight();
            graph.tx().commit(); // Commit transaction.
            return result;
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public boolean deleteRelationConcept(@NotNull String rcID) {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Vertex> rcTraversal = graph.traversal().V().hasLabel(Neo4JModel.RelationConcept.CLASS)
                    .has(Neo4JModel.RelationConcept.PROPERTY_ID, rcID);

            GraphTraversal<Vertex, Vertex> ecrTraversal = graph.traversal().V().hasLabel(Neo4JModel.EntityConceptRelation.CLASS)
                    .where(out().hasLabel(Neo4JModel.RelationConcept.CLASS)
                                   .has(Neo4JModel.RelationConcept.PROPERTY_ID, rcID));

            if (!rcTraversal.hasNext() || ecrTraversal.hasNext()) {
                logger.warn("No relation concept with id {} found for deletion or it is connected to an entity concept relation node", rcID);
                return false;
            }

            Vertex rcVertex = rcTraversal.next();
            rcVertex.remove();
            return true;
        } finally {
            graph.tx().commit();
            closeGraph(graph);
        }
    }

    @NotNull
    @Override
    public Set<EntityConcept> retrieveEntityConceptNeighbours(@NotNull EntityConcept entityConcept, String... relationConceptNames) {
        final Set<GraphTraversal<Vertex, Vertex>> graphTraversals = new HashSet<>();
        final String id = entityConcept.getId();

        if (relationConceptNames.length == 0) {
            // No name filter specified, collect all neighbors.
            logger.trace("Retrieving all EntityConcept neighbors for {}", entityConcept.getId());
            graphTraversals.add(has(Neo4JModel.EntityConcept.PROPERTY_ID)); // A tautology => collect all
        } else {
            // Name filter specified, collect neighbors matching it.
            logger.trace("Retrieving EntityConcept neighbors for {} with name filter: {}", entityConcept.getId(), relationConceptNames);
            for (String name : relationConceptNames) {
                graphTraversals.add(has(Neo4JModel.RelationConcept.PROPERTY_LABEL, name));
            }
        }

        final int graphTraversalsSize = graphTraversals.size();

        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> ecTraversal = graph.traversal().V().has(Neo4JModel.EntityConcept.PROPERTY_ID, id)
                    .hasLabel(Neo4JModel.EntityConcept.CLASS);
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, entityConcept.getId())
                    .hasLabel(Neo4JModel.EntityConcept.CLASS)
                    .out().hasLabel(Neo4JModel.EntityConceptRelation.CLASS) // 1 step to ECRs
                    .where(out()
                                   .hasLabel(Neo4JModel.RelationConcept.CLASS)
                                   .or(graphTraversals.toArray(new GraphTraversal[graphTraversalsSize])) // Apply name filter if present, or use tautology.
                    )
                    .out()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS);// Neighbor ECs

            if (ecTraversal.toList().isEmpty()) {
                logger.warn("No element for entity concept with id {} found", id);
                throw new NoElementForIDException(id);
            }

            final List<Vertex> neighborVertices = traversal.toList();

            Map<String, Element> cache = new HashMap<>();

            return neighborVertices.stream()
                    .map(vertex -> marshalEntityConcept(vertex, cache))
                    .collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    /* ################################### Entity Concept Relation ################################### */

    @NotNull
    @Override
    public EntityConceptRelation createEntityConceptRelation(@NotNull EntityConceptRelation entityConceptRelation) {
        Graph graph = getGraph();
        try {
            EntityConceptRelation result;

            // Transaction onClose is rollback by default.
            try (Transaction transaction = graph.tx()) {
                result = createEntityConceptRelation(entityConceptRelation, graph, new HashMap<>(), new HashMap<>()).getRight();
                transaction.commit();
            } catch (GraphDBException e) {
                logger.error("Error creating EntityConceptRelation {}", entityConceptRelation.getId());
                throw e;
            }
            return result;
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<EntityConceptRelation> retrieveEntityConceptRelations(boolean useRelativeCalculation) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConceptRelation.CLASS);
            final Set<Vertex> entityConceptRelationVertices = traversal.toSet();
            Map<String, Element> cache = new HashMap<>();
            return entityConceptRelationVertices.stream()
                    .map(vertex -> marshalEntityConceptRelation(vertex, graph, cache, useRelativeCalculation))
                    .collect(Collectors.toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public boolean deleteEntityConceptRelation(@NotNull String id) {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Vertex> ecrTraversal = graph.traversal().V().hasLabel(Neo4JModel.EntityConceptRelation.CLASS)
                    .has(Neo4JModel.EntityConceptRelation.PROPERTY_ID, id);

            if (!ecrTraversal.hasNext()) {
                logger.warn("No entity concept relation with id {} found for deletion ", id);
                return false;
            }

            Vertex ercVertex = ecrTraversal.next();
            ercVertex.remove();
            return true;
        } finally {
            graph.tx().commit();
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<EntityConceptRelation> queryAllRelatedEntityConcepts(@NotNull EntityConcept root) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.EntityConcept.CLASS) // The root EC itself.
                    .has(Neo4JModel.EntityConcept.PROPERTY_ID, root.getId())
                    .out(EDGE_BASE_CLASS)
                    .hasLabel(Neo4JModel.EntityConceptRelation.CLASS);

            final Set<Vertex> vertices = traversal.toSet();

            if (vertices.isEmpty()) {
                logger.warn("Can't find related concepts for {} with id {}", Neo4JModel.EntityConcept.CLASS, root.getId());
                throw new NoElementForIDException(root.getId());
            }

            HashMap<String, Element> cache = new HashMap<>();
            return vertices.stream().map(vertex -> marshalEntityConceptRelation(vertex, graph, cache, true)).collect(toSet());
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public boolean createRdfPrefixNode(Map<String, String> prefixMap) throws GraphDBException {
        final Graph graph = getGraph();
        try {

            GraphTraversal<Vertex, Vertex> existingPrefixMapping = graph.traversal()
                    .V()
                    .or( // If any of those nodes already exist an import has taken place before
                         hasLabel(Neo4JModel.PrefixRepository.CLASS),
                         hasLabel(Neo4JModel.PrefixNode.CLASS)
                    );

            if (existingPrefixMapping.hasNext()) {
                throw new GraphDBException("Call for prefix map creation found an already present mapping. A second import is not allowed.");
            }

            Vertex prefixStar = graph.addVertex(Neo4JModel.PrefixRepository.CLASS);
            String newId = generateId(Neo4JModel.PrefixRepository.ID_PREFIX);
            prefixStar.property(Neo4JModel.PrefixRepository.PROPERTY_ID, newId);

            for (Map.Entry<String, String> prefixMapping : prefixMap.entrySet()) {
                if (prefixMapping.getKey().isEmpty()) {
                    continue;
                }
                Vertex mappedPrefix = graph.addVertex(Neo4JModel.PrefixNode.CLASS);
                String prefixId = generateId(Neo4JModel.PrefixNode.ID_PREFIX);
                mappedPrefix.property(Neo4JModel.PrefixNode.PROPERTY_ID, prefixId);
                mappedPrefix.property(Neo4JModel.PrefixNode.PREFIX, prefixMapping.getKey());
                mappedPrefix.property(Neo4JModel.PrefixNode.MAPPED_URL, prefixMapping.getValue());

                prefixStar.addEdge(EDGE_BASE_CLASS, mappedPrefix);
            }

            logger.info("Successfully created prefix repository in graph with star ID: {}", newId);
            return true;
        } finally {
            graph.tx().commit();
            closeGraph(graph);
        }
    }

    @NotNull
    private Pair<Vertex, RelationConcept> createRelationConcept(
            @NotNull RelationConcept relationConcept,
            @NotNull Graph graph,
            @NotNull Map<String, Pair<Vertex, ? extends Element>> writeCache,
            @NotNull Map<String, Element> readCache
    ) {
        // Fast return if created already.
        if (writeCache.get(relationConcept.getId()) != null) {
            final Pair<Vertex, ?> pair = writeCache.get(relationConcept.getId());
            //noinspection unchecked
            return (Pair<Vertex, RelationConcept>) pair;
        }

        // Search for a RelationConcept with the given name.
        final GraphTraversal<Vertex, Vertex> sameName = graph.traversal().V()
                .hasLabel(Neo4JModel.RelationConcept.CLASS)
                .or(
                        and(
                                has(Neo4JModel.RelationConcept.PROPERTY_LABEL, relationConcept.getLabel()),
                                has(Neo4JModel.RelationConcept.PROPERTY_DESCRIPTION, relationConcept.getDescription())
                        ),
                        has(Neo4JModel.RelationConcept.PROPERTY_ID, relationConcept.getId()),
                        has(Neo4JModel.RelationConcept.PROPERTY_SOURCE_URI, relationConcept.getSourceURI())
                );

        if (!sameName.hasNext()) {
            // Create vertex RC.
            final Vertex newRelationConceptVertex = graph.addVertex(Neo4JModel.RelationConcept.CLASS);
            final String newId = generateId(Neo4JModel.RelationConcept.ID_PREFIX);
            newRelationConceptVertex.property(Neo4JModel.RelationConcept.PROPERTY_ID, newId);
            newRelationConceptVertex.property(Neo4JModel.RelationConcept.PROPERTY_LABEL, relationConcept.getLabel());
            newRelationConceptVertex.property(Neo4JModel.RelationConcept.PROPERTY_DESCRIPTION, relationConcept.getDescription());
            newRelationConceptVertex.property(Neo4JModel.RelationConcept.PROPERTY_SOURCE_URI, relationConcept.getSourceURI());

            // Create self-loops for flags like symmetric.
            for (RelationConcept.Property property : relationConcept.getProperties()) {
                if (property != null) {
                    newRelationConceptVertex.addEdge(property.getClassName(), newRelationConceptVertex);
                }
            }

            // Create and cache.
            final RelationConcept newRelationConcept = new RelationConcept(
                    newId,
                    relationConcept.getLabel(),
                    relationConcept.getDescription(),
                    relationConcept.getSourceURI(),
                    relationConcept.getProperties()
            );
            final Pair<Vertex, RelationConcept> pair = new Pair<>(newRelationConceptVertex, newRelationConcept);
            writeCache.put(relationConcept.getId(), pair);
            return pair;
        } else {
            // RelationConcept with that name already present in graph -> Fail if not equals, return if equals.
            final Vertex existentVertex = sameName.next();
            final RelationConcept concept = marshalRelationConcept(existentVertex, readCache);

            final Pair<Vertex, RelationConcept> pair = new Pair<>(existentVertex, concept);
            writeCache.put(relationConcept.getId(), pair);
            return pair;
        }
    }

    /* ################################### Queries ################################### */

    @Override
    @NotNull
    public Map<String, String> getPrefixMap() {
        final Graph graph = getGraph();
        try {
            GraphTraversal<Vertex, Vertex> prefixTraversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.PrefixRepository.CLASS)
                    .out().hasLabel(Neo4JModel.PrefixNode.CLASS);

            if (prefixTraversal.hasNext()) {
                return prefixTraversal.toStream().collect(Collectors.toMap(
                        node -> readProperty(node, Neo4JModel.PrefixNode.PREFIX, String.class),
                        node -> readProperty(node, Neo4JModel.PrefixNode.MAPPED_URL, String.class)
                                                          )
                );
            } else {
                logger.info("Call to obtain the prefix map found no prefix mappings");
                return new HashMap<>();
            }
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull Set<EntityConcept> recommendEntityConcepts(@NotNull SemanticModel semanticModel) {
        Graph graph = getGraph();
        try {
            //Get all entity types
            Set<EntityType> entityTypeSet = semanticModel.getEntityTypes();

            //Receive all concepts the EntityTypes are connected to
            Set<EntityConcept> entityConcepts = entityTypeSet.stream().map(EntityType::getEntityConcept).collect(Collectors.toSet());

            List<Pair<SemanticModel, EntityConcept>> conceptsCommonalities = new ArrayList<>();

            //Iterate through the concepts
            for (EntityConcept concept : entityConcepts) {
                //Find nodes with the same main label
                GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                        .hasLabel(Neo4JModel.EntityConcept.CLASS)
                        .has(Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, new P<>(
                                org.apache.commons.lang3.StringUtils::containsIgnoreCase,
                                concept.getMainLabel()
                        ));
                Set<Vertex> entityConceptVertices = traversal.toSet();

                //Find incoming relations
                for (Vertex vertex : entityConceptVertices) {
                    //receive vertex (type) from each incoming arrows of each concept vertex
                    final Iterable<Vertex> vertices = () -> vertex.vertices(Direction.IN);
                    for (Vertex neighbour : vertices) {
                        //if vertex is a neighbor
                        if (Neo4JModel.EntityType.CLASS.equals(neighbour.label())) { //receive neighbors of entityType in order to get semanticModel vertices
                            final Iterable<Vertex> vertexIterable = () -> neighbour.vertices(Direction.IN);
                            for (Vertex neighbours : vertexIterable) {
                                if (neighbours.label().equals(Neo4JModel.SemanticModel.CLASS)) {
                                    SemanticModel sm = retrieveSemanticModel(readProperty(
                                            neighbours,
                                            Neo4JModel.SemanticModel.PROPERTY_ID,
                                            String.class
                                    ));

                                    Pair<SemanticModel, EntityConcept> modelEntityConceptPair = new Pair<>(sm, concept);

                                    conceptsCommonalities.add(modelEntityConceptPair);
                                }
                            }
                        }
                    }
                }
            }

            conceptsCommonalities = conceptsCommonalities.stream().distinct().collect(toList());

            Map<SemanticModel, Integer> amountOfCommonConcepts = new HashMap<>();
            for (Pair<SemanticModel, EntityConcept> pair : conceptsCommonalities) {
                SemanticModel sm = pair.getLeft();
                amountOfCommonConcepts.compute(sm, (key, value) -> value != null ? ++value : 1);
            }

            List<SemanticModel> smWithMostConceptCommonalities = getHighestVotes(amountOfCommonConcepts);
            Set<EntityType> entityTypesForSM = new HashSet<>();
            for (SemanticModel sm : smWithMostConceptCommonalities) {
                entityTypesForSM.addAll(sm.getEntityTypes());
            }

            Set<EntityConcept> entityConceptForSM = entityTypesForSM.stream().map(EntityType::getEntityConcept).collect(Collectors.toSet());
            for (Iterator<EntityConcept> iterator = entityConceptForSM.iterator(); iterator.hasNext(); ) {
                EntityConcept s = iterator.next();
                for (EntityConcept e : entityConcepts) {
                    if (s.getMainLabel().equals(e.getMainLabel())) {
                        iterator.remove();
                    }
                }
            }
            return entityConceptForSM;
        } finally {
            closeGraph(graph);
        }
    }

    /* ################################### Utilities ################################### */

    @NotNull
    private SemanticModel marshalSemanticModel(@NotNull Vertex vertex) {
        String semanticModelID = readProperty(vertex, Neo4JModel.SemanticModel.PROPERTY_ID, String.class);
        String semanticModelLabel = readProperty(vertex, Neo4JModel.SemanticModel.PROPERTY_LABEL, String.class);
        String semanticModelDescription = readProperty(vertex, Neo4JModel.SemanticModel.PROPERTY_DESCRIPTION, String.class);
        String dataSourceId;
        try {
            dataSourceId = readProperty(vertex, Neo4JModel.SemanticModel.PROPERTY_DATA_SOURCE_ID, String.class);
        } catch (MissingPropertyException e) {
            logger.warn("DataSourceId is missing - might be due to an old Neo4J Schema - Setting default value");
            dataSourceId = "";
        }

        Map<String, Element> cache = new HashMap<>();

        // Load referenced EntityTypes and Relations.
        final HashSet<EntityType> entityTypes = new HashSet<>();
        final HashSet<Relation> relations = new HashSet<>();

        final Iterable<Vertex> outVertices = () -> vertex.vertices(Direction.OUT);
        for (Vertex neighbour : outVertices) {
            switch (neighbour.label()) {
                case Neo4JModel.EntityType.CLASS: {
                    entityTypes.add(marshalEntityType(neighbour, cache));
                    break;
                }
                case Neo4JModel.Relation.CLASS: {
                    relations.add(marshalRelation(neighbour, cache));
                    break;
                }
                default: {
                    logger.error("SemanticModel {} was referencing node of type {} which was not expected.", semanticModelID, neighbour.label());
                    throw new GraphDBException("SemanticModel was referencing illegal type.");
                }
            }
        }

        // Construct element and return. Do not store in cache, because SMs are not used in other elements.
        return new SemanticModel(
                semanticModelID,
                semanticModelLabel,
                semanticModelDescription,
                entityTypes,
                relations,
                dataSourceId
        );
    }

    @NotNull
    private RelationConcept marshalRelationConcept(@NotNull Vertex vertex, @NotNull Map<String, Element> cache) {
        final String relationConceptID = readProperty(vertex, Neo4JModel.RelationConcept.PROPERTY_ID, String.class);

        if (cache.get(relationConceptID) != null) {
            return (RelationConcept) cache.get(relationConceptID);
        } else {
            // Read RC properties like "reflexive", which are located on self-loops.
            final Set<RelationConcept.Property> properties = new HashSet<>();
            final Iterable<Edge> edges = () -> vertex.edges(Direction.OUT);
            for (Edge edge : edges) {
                if (!EDGE_BASE_CLASS.equals(edge.label())) {
                    // If not "E", then we assume the label to be a property.
                    properties.add(RelationConcept.Property.byClassName(edge.label()));
                }
            }

            // Create and cache.
            final RelationConcept relationConcept = new RelationConcept(
                    relationConceptID,
                    readProperty(vertex, Neo4JModel.RelationConcept.PROPERTY_LABEL, String.class),
                    readProperty(vertex, Neo4JModel.RelationConcept.PROPERTY_DESCRIPTION, String.class),
                    readProperty(vertex, Neo4JModel.RelationConcept.PROPERTY_SOURCE_URI, String.class),
                    properties
            );
            cache.put(relationConceptID, relationConcept);
            return relationConcept;
        }
    }

    @NotNull
    private EntityConcept marshalEntityConcept(@NotNull Vertex vertex, @NotNull Map<String, Element> cache) {
        final String entityConceptID = readProperty(vertex, Neo4JModel.EntityConcept.PROPERTY_ID, String.class);

        if (cache.get(entityConceptID) != null) {
            return (EntityConcept) cache.get(entityConceptID);
        } else {
            // Extract main label
            final String mainLabel = readProperty(vertex, Neo4JModel.EntityConcept.PROPERTY_MAIN_LABEL, String.class);

            // Build EntityConcept element and cache if needed.
            EntityConcept entityConcept = new EntityConcept(
                    entityConceptID,
                    mainLabel,
                    readProperty(vertex, Neo4JModel.EntityConcept.PROPERTY_DESCRIPTION, String.class),
                    readProperty(vertex, Neo4JModel.EntityConcept.PROPERTY_SOURCE_URI, String.class)
            );
            cache.put(entityConceptID, entityConcept);
            return entityConcept;
        }
    }

    @NotNull
    private Relation marshalRelation(@NotNull Vertex vertex, @NotNull Map<String, Element> cache) {
        final String relationID = readProperty(vertex, Neo4JModel.Relation.PROPERTY_ID, String.class);

        // Load referenced headEntityType and RelationConcept (outgoing edges).
        EntityType headEntityType = null;
        RelationConcept relationConcept = null;

        final Iterable<Vertex> outVertices = () -> vertex.vertices(Direction.OUT);
        for (Vertex neighbour : outVertices) {
            switch (neighbour.label()) {
                case Neo4JModel.EntityType.CLASS: {
                    if (headEntityType == null) {
                        headEntityType = marshalEntityType(neighbour, cache);
                        break;
                    } else {
                        logger.error("Relation {} refers to more than one headEntityType.", relationID);
                        throw new GraphDBException("Relation refers to more than one headEntityType.");
                    }
                }
                case Neo4JModel.RelationConcept.CLASS: {
                    if (relationConcept == null) {
                        relationConcept = marshalRelationConcept(neighbour, cache);
                        break;
                    } else {
                        logger.error("Relation {} refers to more than one RelationConcept.", relationID);
                        throw new GraphDBException("Relation refers to more than one RelationConcept.");
                    }
                }
                default: {
                    logger.error("Relation {} references node of type {} which is not expected.", relationID, neighbour.label());
                    throw new GraphDBException("Relation references illegal type.");
                }
            }
        }

        if (headEntityType == null) {
            throw new GraphDBException("Relation " + relationID + " has no head entity type.");
        }

        if (relationConcept == null) {
            throw new GraphDBException("Relation " + relationID + " has no relation concept.");
        }

        // Now, load referenced tailEntityType (incoming edge).
        EntityType tailEntityType = null;

        final Iterable<Vertex> inVertices = () -> vertex.vertices(Direction.IN);
        for (Vertex neighbour : inVertices) {
            switch (neighbour.label()) {
                case Neo4JModel.EntityType.CLASS: {
                    if (tailEntityType == null) {
                        tailEntityType = marshalEntityType(neighbour, cache);
                        break;
                    } else {
                        logger.error("Relation {} is referred to by more than one tailEntityType.", relationID);
                        throw new GraphDBException("Relation is referred to by more than one tailEntityType.");
                    }
                }
                case Neo4JModel.SemanticModel.CLASS: {
                    break;
                }
                default: {
                    logger.error("Relation {} is referenced by node of type {} which is not expected.", relationID, neighbour.label());
                    throw new GraphDBException("Relation is referenced by illegal type.");
                }
            }
        }

        if (tailEntityType == null) {
            throw new GraphDBException("Relation " + relationID + " has no tail entity type.");
        }

        final Relation relation = new Relation(
                relationID,
                tailEntityType,
                headEntityType,
                relationConcept
        );
        cache.put(relationID, relation);
        return relation;
    }

    @NotNull
    private Position marshalPosition(@NotNull Vertex vertex, @NotNull String entityTypeID) {
        final Iterator<Vertex> positionVertices = vertex.vertices(Direction.OUT, Neo4JModel.Position.Edge.POSITION);
        if (!positionVertices.hasNext()) {
            throw new GraphDBException("EntityType does not have any position" + entityTypeID);
        }
        Vertex positionVertex = positionVertices.next();
        if (positionVertices.hasNext()) {
            throw new GraphDBException("EntityType refers to more than one position: " + entityTypeID);
        }
        final double positionX = readProperty(positionVertex, Neo4JModel.Position.PROPERTY_X_COORDINATE, Double.class);
        final double positionY = readProperty(positionVertex, Neo4JModel.Position.PROPERTY_Y_COORDINATE, Double.class);
        return new Position(positionX, positionY);
    }

    @NotNull
    private Pair<Vertex, EntityConceptRelation> createEntityConceptRelation(
            @NotNull Pair<Vertex, EntityConcept> tailECPair,
            @NotNull Pair<Vertex, EntityConcept> headECPair,
            @NotNull Pair<Vertex, RelationConcept> relationECPair,
            @NotNull Graph graph,
            int usageValue
    ) {
        // Create vertex ECR and set its properties.
        final Vertex newEntityConceptRelationVertex = graph.addVertex(Neo4JModel.EntityConceptRelation.CLASS);
        final String newId = generateId(Neo4JModel.EntityConceptRelation.ID_PREFIX);
        newEntityConceptRelationVertex.property(Neo4JModel.EntityConceptRelation.PROPERTY_ID, newId);

        // Initialise usage counter with 1
        newEntityConceptRelationVertex.property(Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.valueOf(usageValue));

        // Create edge to RelationConcept vertex (ECR->RC).
        newEntityConceptRelationVertex.addEdge(EDGE_BASE_CLASS, relationECPair.getLeft());

        // Create edge to head vertex (ECR->EC).
        newEntityConceptRelationVertex.addEdge(EDGE_BASE_CLASS, headECPair.getLeft());

        // Create edge from tail vertex (EC->ECR).
        tailECPair.getLeft().addEdge(EDGE_BASE_CLASS, newEntityConceptRelationVertex);

        // Create and cache new Relation.
        final EntityConceptRelation newEntityConceptRelation = new EntityConceptRelation(
                newId,
                tailECPair.getRight(),
                headECPair.getRight(),
                relationECPair.getRight()
        );
        return new Pair<>(newEntityConceptRelationVertex, newEntityConceptRelation);
    }

    @NotNull
    private List<SemanticModel> getHighestVotes(@NotNull Map<SemanticModel, Integer> amountOfCommonConcepts) {
        List<SemanticModel> semanticModelList = new ArrayList<>();
        int highestVote = 0;
        for (Map.Entry<SemanticModel, Integer> entry : amountOfCommonConcepts.entrySet()) {
            if (entry.getValue() > highestVote) {
                semanticModelList.clear();
                semanticModelList.add(entry.getKey());
                highestVote = entry.getValue();
            } else if (entry.getValue() == highestVote) {
                semanticModelList.add(entry.getKey());
            }
        }
        return semanticModelList;
    }

    @NotNull
    private <X> X readProperty(
            @NotNull org.apache.tinkerpop.gremlin.structure.Element element,
            @NotNull String property,
            Class<X> clazz
    ) throws MissingPropertyException {
        if (element.property(property).isPresent()) {
            return clazz.cast(element.value(property));
        }
        final String label = element.label();
        throw new MissingPropertyException(property, label);
    }

    private Vertex retrieveVertex(@NotNull Graph graph, @NotNull String propertyKey, @NotNull String propertyValue, @NotNull String className) {
        Set<Vertex> vertices = graph.traversal().V()
                .has(propertyKey, propertyValue)
                .hasLabel(className).toSet();

        if (vertices.isEmpty()) {
            logger.trace("No Vertex found with propertyValue {}", propertyValue);
            return null;
        } else if (vertices.size() > 1) {
            throw new DuplicateIDException(propertyValue);
        }
        return vertices.iterator().next();
    }

    @NotNull
    private String generateId(String prefix) {
        return prefix + UNDERSCORE + UUID.randomUUID().toString();
    }

    @NotNull
    private EntityType marshalEntityType(@NotNull Vertex vertex, @NotNull Map<String, Element> cache) {
        final String entityTypeID = readProperty(vertex, Neo4JModel.EntityType.PROPERTY_ID, String.class);

        if (cache.get(entityTypeID) != null) {
            return (EntityType) cache.get(entityTypeID);
        } else {
            // Load referenced EntityConcept.
            EntityConcept entityConcept = null;
            final Iterable<Vertex> vertices = () -> vertex.vertices(Direction.OUT);
            for (Vertex neighbour : vertices) {
                switch (neighbour.label()) {
                    case Neo4JModel.EntityConcept.CLASS: {
                        if (entityConcept == null) {
                            entityConcept = marshalEntityConcept(neighbour, cache);
                            break;
                        } else {
                            logger.error(
                                    "EntityType {} refers to more than one EntityConcept.",
                                    entityTypeID
                            );
                            throw new GraphDBException("EntityType refers to more than one EntityConcept.");
                        }
                    }
                    case Neo4JModel.Relation.CLASS:
                    case Neo4JModel.Position.CLASS: {
                        break;
                    }
                    default: {
                        logger.error(
                                "EntityType {} was referencing node of type {} which was not expected.",
                                entityTypeID,
                                neighbour.label()
                        );
                        throw new GraphDBException("EntityType was referencing illegal type.");
                    }
                }
            }

            if (entityConcept == null) {
                throw new GraphDBException("EntityType " + entityTypeID + " has no entity concept.");
            }

            // Extract the position
            Position position = marshalPosition(vertex, entityTypeID);

            final EntityType entityType = new EntityType(
                    entityTypeID,
                    readProperty(vertex, Neo4JModel.EntityType.PROPERTY_LABEL, String.class),
                    readProperty(vertex, Neo4JModel.EntityType.PROPERTY_ORIGINAL_LABEL, String.class),
                    entityConcept,
                    position,
                    readProperty(vertex, Neo4JModel.EntityType.PROPERTY_DESCRIPTION, String.class),
                    readProperty(vertex, Neo4JModel.EntityType.PROPERTY_MAPPED_TO_DATA, Boolean.class)
            );

            cache.put(entityTypeID, entityType);

            return entityType;
        }
    }

    @NotNull
    private Pair<Vertex, EntityConceptRelation> createEntityConceptRelation(
            @NotNull EntityConceptRelation entityConceptRelation,
            @NotNull Graph graph,
            @NotNull Map<String, Pair<Vertex, ? extends Element>> writeCache,
            @NotNull Map<String, Element> readCache
    ) {
        GraphTraversal<Vertex, Vertex> existingECR = graph.traversal()
                .V()
                .hasLabel(Neo4JModel.EntityConceptRelation.CLASS)
                .or(
                        has(Neo4JModel.EntityConceptRelation.PROPERTY_ID, entityConceptRelation.getId()),
                        where(
                                and(
                                        in().has(Neo4JModel.EntityConcept.PROPERTY_SOURCE_URI, entityConceptRelation.getTailNode().getSourceURI()),
                                        out().has(Neo4JModel.EntityConcept.PROPERTY_SOURCE_URI, entityConceptRelation.getHeadNode().getSourceURI()),
                                        out().has(Neo4JModel.RelationConcept.PROPERTY_SOURCE_URI, entityConceptRelation.getRelationConcept().getSourceURI())
                                )
                        )

                );

        if (!existingECR.hasNext()) {
            // Retrieve missing elements.
            final Pair<Vertex, RelationConcept> relationConceptPair = createRelationConcept(
                    entityConceptRelation.getRelationConcept(),
                    graph,
                    writeCache,
                    readCache
            );
            // We still need to cache the ECs, because they might be the same. Thus, we avoid adding it twice.
            final Pair<Vertex, EntityConcept> headEntityConceptPair = createEntityConcept(entityConceptRelation.getHeadNode(), graph, writeCache, readCache);
            final Pair<Vertex, EntityConcept> tailEntityConceptPair = createEntityConcept(entityConceptRelation.getTailNode(), graph, writeCache, readCache);

            //Entity concept relations that are created via the API are not used in any semantic model => init with 0
            return createEntityConceptRelation(tailEntityConceptPair, headEntityConceptPair, relationConceptPair, graph, 0);
        } else {
            // EntityConceptRelation already present in graph -> load
            Vertex existentVertex = existingECR.next();
            EntityConceptRelation concept = marshalEntityConceptRelation(existentVertex, graph, readCache, true);
            return new Pair<>(existentVertex, concept);
        }
    }

    @Override
    public boolean removeSemanticModelByDataSourceId(@NotNull String dataSourceId) {
        final Graph graph = getGraph();

        try {
            // First, get vertex of the SM itself.
            final GraphTraversal<Vertex, Vertex> semanticModelTraversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .has(Neo4JModel.SemanticModel.PROPERTY_DATA_SOURCE_ID, dataSourceId);

            if (!semanticModelTraversal.hasNext()) {
                logger.warn("No SemanticModel for id {} found, unable to remove.", dataSourceId);
                return false;
            }
            final Vertex semanticModelVertex = semanticModelTraversal.next();

            // Vertices to remove satisfy both of the following:
            // a) They are directly referenced to by this SM (SM -> ET/R)
            // b) They are *not* referenced to by any other SM
            final Set<Vertex> verticesToRemove = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .has(Neo4JModel.SemanticModel.PROPERTY_ID, dataSourceId)
                    // Get all vertices that are directly used in this SM (these are ETs and Rs).
                    .out()
                    .or(
                            has(Neo4JModel.EntityType.CLASS),
                            has(Neo4JModel.Relation.CLASS)
                    )
                    // Which are also used in an SM which has a *different* id from the current one.
                    .where(__.in().hasLabel(Neo4JModel.SemanticModel.CLASS).not(has(Neo4JModel.SemanticModel.PROPERTY_ID, dataSourceId)))
                    .toSet();

            // Actually remove vertices and commit changes.
            semanticModelVertex.remove();
            verticesToRemove.forEach(org.apache.tinkerpop.gremlin.structure.Element::remove);
            graph.tx().commit();
            return true;
        } finally {
            closeGraph(graph);
        }
    }

    @Override
    public @NotNull SemanticModel retrieveSemanticModelByDataSourceId(@NotNull String dataSourceId) {
        final Graph graph = getGraph();
        try {
            final GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V()
                    .hasLabel(Neo4JModel.SemanticModel.CLASS)
                    .has(Neo4JModel.SemanticModel.PROPERTY_DATA_SOURCE_ID, new P<>(StringUtils::startsWithIgnoreCase, dataSourceId));

            if (!traversal.hasNext()) {
                logger.warn("Cannot find {} with dataSourceId {}", Neo4JModel.SemanticModel.CLASS, dataSourceId);
                throw new NoElementForIDException(dataSourceId);
            }

            return marshalSemanticModel(traversal.next());
        } finally {
            closeGraph(graph);
        }
    }

    private double calculateRelativeNumberOfEcrUsages(Graph graph, EntityConcept headEntityConcept, EntityConcept tailEntityConcept, Vertex v) {
        // Read number of contacts between adjacent ECs i.e. the number of ECRs connecting them (set to 1 if not present).
        final GraphTraversal<Vertex, Vertex> usedWithTraversal = graph.traversal().V().hasLabel(Neo4JModel.EntityConceptRelation.CLASS)
                .where(or(
                        and(
                                in().has(Neo4JModel.EntityConcept.PROPERTY_ID, tailEntityConcept.getId()),
                                out().has(Neo4JModel.EntityConcept.PROPERTY_ID, headEntityConcept.getId())
                        ),
                        and(
                                in().has(Neo4JModel.EntityConcept.PROPERTY_ID, headEntityConcept.getId()),
                                out().has(Neo4JModel.EntityConcept.PROPERTY_ID, tailEntityConcept.getId())
                        )
                ));

        int totalNumberOfRelationUsages = 0;

        while (usedWithTraversal.hasNext()) {
            Vertex ecrVertex = usedWithTraversal.next();
            totalNumberOfRelationUsages += Integer.parseInt(readProperty(ecrVertex, Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.class));
        }

        int absoluteUsages = Integer.parseInt(readProperty(v, Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.class));
        return (double) absoluteUsages / (double) totalNumberOfRelationUsages;
    }

    private void decreaseNumberOfUsages(Graph graph, Relation relation) {
        // Read number of contacts between adjacent ECs i.e. the number of ECRs connecting them (set to 1 if not present).
        GraphTraversal<Vertex, Vertex> existingECR = graph.traversal()
                .V()
                .hasLabel(Neo4JModel.EntityConceptRelation.CLASS)
                .where(and(
                        out().has(Neo4JModel.RelationConcept.PROPERTY_ID, relation.getRelationConcept().getId())
                                .in().has(Neo4JModel.Relation.PROPERTY_ID, relation.getId()),
                        in().hasLabel(Neo4JModel.EntityConcept.CLASS).in().has(Neo4JModel.EntityType.PROPERTY_ID, relation.getTailNode().getId()),
                        out().hasLabel(Neo4JModel.EntityConcept.CLASS).in().has(Neo4JModel.EntityType.PROPERTY_ID, relation.getHeadNode().getId())
                       )
                );

        if (!existingECR.hasNext()) {
            throw new GraphDBException("No entity concept relation found for relation: " + relation.getId());
        }

        Vertex ecrVertex = existingECR.next();

        int absoluteUsages = Integer.parseInt(readProperty(ecrVertex, Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.class));
        absoluteUsages -= 1;
        if (absoluteUsages <= 0) { //If not used anymore -> delete the vertex
            ecrVertex.remove();
        } else { //Otherwise update to the new absolute usage count
            ecrVertex.property(Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.valueOf(absoluteUsages));
        }

        if (existingECR.hasNext()) {
            throw new GraphDBException("Relation " + relation.getId() + " is referencing an entity concept relation with more than one relation concept");
        }
    }

    /* ################################### RDF ################################### */

    private void createEntityConceptRelation(
            @NotNull Relation etRelation,
            @NotNull Graph graph,
            @NotNull Map<String, Pair<Vertex, ? extends Element>> writeCache,
            @NotNull Map<String, Element> readCache
    ) {
        //Fetch the ECR that has the same relation concept as the given relation and which entity concepts are referenced by the entity types of the relation
        GraphTraversal<Vertex, Vertex> existingECR = graph.traversal()
                .V()
                .hasLabel(Neo4JModel.EntityConceptRelation.CLASS)
                .where(and(
                        out().has(Neo4JModel.RelationConcept.PROPERTY_ID, etRelation.getRelationConcept().getId()),
                        in().hasLabel(Neo4JModel.EntityConcept.CLASS).in().has(Neo4JModel.EntityType.PROPERTY_ID, etRelation.getTailNode().getId()),
                        out().hasLabel(Neo4JModel.EntityConcept.CLASS).in().has(Neo4JModel.EntityType.PROPERTY_ID, etRelation.getHeadNode().getId())
                       )
                );

        if (!existingECR.hasNext()) {
            // Retrieve missing elements.
            final Pair<Vertex, RelationConcept> relationConceptPair = createRelationConcept(
                    etRelation.getRelationConcept(),
                    graph,
                    writeCache,
                    readCache
            );
            // We still need to cache the ECs, because they might be the same. Thus, we avoid adding it twice.
            final Pair<Vertex, EntityConcept> headEntityConceptPair = createEntityConcept(etRelation.getHeadNode().getEntityConcept(), graph, writeCache,
                                                                                          readCache
            );
            final Pair<Vertex, EntityConcept> tailEntityConceptPair = createEntityConcept(
                    etRelation.getTailNode().getEntityConcept(),
                    graph,
                    writeCache,
                    readCache
            );

            //Entity concept relations that are created via this method are used in a relation => init with 1
            createEntityConceptRelation(tailEntityConceptPair, headEntityConceptPair, relationConceptPair, graph, 1);
        } else {
            // EntityConceptRelation already present in graph -> load and put into cache
            Vertex existentVertex = existingECR.next();
            int numberOfUsages = Integer.parseInt(readProperty(existentVertex, Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.class));
            existentVertex.property(Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.valueOf(numberOfUsages + 1));
            marshalEntityConceptRelation(existentVertex, graph, readCache, true);
        }
    }

    @NotNull
    private EntityConceptRelation marshalEntityConceptRelation(
            @NotNull Vertex vertex, @NotNull Graph graph, @NotNull Map<String, Element> readCache,
            boolean useRelativeCalculation
    ) {
        String entityConceptRelationID = readProperty(vertex, Neo4JModel.EntityConceptRelation.PROPERTY_ID, String.class);

        // Load referenced headEntityConcept and RelationConcept (outgoing edges).
        EntityConcept headEntityConcept = null;
        RelationConcept relationConcept = null;

        final Iterable<Vertex> outVertices = () -> vertex.vertices(Direction.OUT);
        for (Vertex neighbour : outVertices) {
            switch (neighbour.label()) {
                case Neo4JModel.EntityConcept.CLASS: {
                    if (headEntityConcept == null) {
                        headEntityConcept = marshalEntityConcept(neighbour, readCache);
                        break;
                    } else {
                        logger.error(
                                "EntityConceptRelation {} refers to more than one headEntityConcept.",
                                entityConceptRelationID
                        );
                        throw new GraphDBException("EntityConceptRelation refers to more than one headEntityConcept.");
                    }
                }
                case Neo4JModel.RelationConcept.CLASS: {
                    if (relationConcept == null) {
                        relationConcept = marshalRelationConcept(neighbour, readCache);
                        break;
                    } else {
                        logger.error(
                                "EntityConceptRelation {} refers to more than one RelationConcept.",
                                entityConceptRelationID
                        );
                        throw new GraphDBException("EntityConceptRelation refers to more than one RelationConcept.");
                    }
                }
                default: {
                    logger.error(
                            "EntityConceptRelation {} was referencing node of type {} which was not expected.",
                            entityConceptRelationID,
                            neighbour.label()
                    );
                    throw new GraphDBException("EntityConceptRelation was referencing illegal type.");
                }
            }
        }

        if (headEntityConcept == null) {
            throw new GraphDBException("EntityConceptRelation " + entityConceptRelationID + " has no head entity concept.");
        }

        if (relationConcept == null) {
            throw new GraphDBException("EntityConceptRelation " + entityConceptRelationID + " has no relation concept.");
        }

        // Now, load referenced tailEntityConcept (incoming edge).
        EntityConcept tailEntityConcept = null;

        final Iterable<Vertex> inVertices = () -> vertex.vertices(Direction.IN);
        for (Vertex neighbour : inVertices) {
            String s = neighbour.label();
            if (s.equals(Neo4JModel.EntityConcept.CLASS)) {
                if (tailEntityConcept == null) {
                    tailEntityConcept = marshalEntityConcept(neighbour, readCache);
                } else {
                    logger.error("EntityConceptRelation {} is referred to by more than one tailEntityConcept.", entityConceptRelationID);
                    throw new GraphDBException("EntityConceptRelation is referred to by more than one tailEntityConcept.");
                }
            } else {
                logger.error("EntityConceptRelation {} was referenced by node of type {} which was not expected.", entityConceptRelationID, neighbour.label());
                throw new GraphDBException("EntityConceptRelation was referenced by illegal type.");
            }
        }

        if (tailEntityConcept == null) {
            throw new GraphDBException("EntityConceptRelation " + entityConceptRelationID + " has no tail entity concept.");
        }

        int absoluteUsedCounter = Integer.parseInt(readProperty(vertex, Neo4JModel.EntityConceptRelation.PROPERTY_USAGE_ABSOLUTE, String.class));
        double relativeUsedCounter = useRelativeCalculation ? calculateRelativeNumberOfEcrUsages(graph, headEntityConcept, tailEntityConcept, vertex) : 1.0;

        return new EntityConceptRelation(
                entityConceptRelationID,
                tailEntityConcept,
                headEntityConcept,
                relationConcept,
                absoluteUsedCounter,
                relativeUsedCounter
        );
    }
}