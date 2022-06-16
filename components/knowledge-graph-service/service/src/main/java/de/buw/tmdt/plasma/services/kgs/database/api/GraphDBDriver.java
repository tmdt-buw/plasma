package de.buw.tmdt.plasma.services.kgs.database.api;

import de.buw.tmdt.plasma.services.kgs.database.api.exception.NoElementForIDException;
import de.buw.tmdt.plasma.services.kgs.shared.model.Element;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public interface GraphDBDriver {

    /* ################################### Semantic Model ################################### */

    /**
     * Retrieves the SemanticModel for the given id. The relations between EntityConcepts are not resolved in the returned SemanticModel object
     * If passed id doesn't exist, {@link NoElementForIDException} is thrown.
     *
     * @param id the id of the requested SemanticModel. Null value is illegal.
     * @return the SemanticModel with the matching id
     */
    @NotNull
    SemanticModel retrieveSemanticModel(@NotNull String id) throws NoElementForIDException;

    /**
     * Deletes the SemanticModel with the given id.
     * Also removes all referenced EntityTypes and Relations that are no longer used in any SemanticModel.
     *
     * @param id the id of the SemanticModel to delete.
     * @return true if the SemanticModel was successfully removed
     */
    boolean removeSemanticModel(@NotNull String id);

    /**
     * This method creates the given SemanticModel in the graph. All elements with existent ids are reused, and new elements are created.
     * If the commit is successful it returns a {@code Map<K, V>} where {@code K} is the provided id (temporary or actual)
     * and {@code V} is the corresponding element.
     *
     * @param semanticModel the SemanticModel to be created
     * @return a mapping of provided ids to elements containing database ids.
     */
    @NotNull
    Map<String, Element> createSemanticModelExposeMapping(@NotNull SemanticModel semanticModel);

    /**
     * Returns all SemanticModels containing an EntityType labeled by the given parameter label.
     *
     * @param label the label label to be searched for in the database
     * @return a set containing all SemanticModels found.
     */
    @NotNull
    Set<SemanticModel> searchSemanticModelsForEntityType(@NotNull String label);

    /**
     * Returns all SemanticModels that use an EntityType based on an EntityConcept with the specified main label. Ignores case.
     *
     * @param mainLabel the main label of the EntityConcept
     * @return a set containing all of all SemanticModels using this EntityConcept
     */
    @NotNull
    Set<SemanticModel> searchSemanticModelsForEntityConceptMainLabel(@NotNull String mainLabel);

    /**
     * Returns all SemanticModels that use an EntityType based on an EntityConcept with the specified synonym label. Ignores case.
     *
     * @param synonymLabel the synonym label of the EntityConcept
     * @return a set containing all of all SemanticModels using this EntityConcept
     */
    @NotNull
    Set<SemanticModel> searchSemanticModelsForEntityConceptSynonymLabel(@NotNull String synonymLabel);

    /**
     * Returns all SemanticModels that use an EntityType based on an EntityConcept with the specified main or synonym label. Ignores case.
     *
     * @param anyLabel main or synonym label the synonym label of the EntityConcept
     * @return a set containing all of all SemanticModels using this EntityConcept
     */
    @NotNull
    Set<SemanticModel> searchSemanticModelsForEntityConceptAnyLabel(@NotNull String anyLabel);

    /**
     * Returns all SemanticModels that contain EntityTypes or EntityConcepts based on the containing label.
     *
     * @param label the label or label of an EntityConcept or EntityType searched for in the database.
     * @return a set containing all of the SemanticModels found.
     */
    @NotNull
    Set<SemanticModel> searchSemanticModelByEntityTypeOrEntityConceptContainingLabel(@NotNull String label);

    boolean removeSemanticModelByDataSourceId(@NotNull String dataSourceId);

    @NotNull
    SemanticModel retrieveSemanticModelByDataSourceId(@NotNull String dataSourceId);

    /* ################################### Entity Concept ################################### */

    /**
     * Retrieves the EntityConcept for the given id.
     * If passed id doesn't exist, {@link NoElementForIDException} is thrown.
     *
     * @param id the id of the requested EntityConcept. Null value is illegal.
     * @return the EntityConcept with the matching id
     */
    @NotNull
    EntityConcept retrieveEntityConcept(@NotNull String id) throws NoElementForIDException;

    /**
     * Retrieves the Synonyms for the given EntityConcept ID.
     * If passed id doesn't exist, {@link NoElementForIDException} is thrown.
     *
     * @param id the id of the requested EntityConcept. Null value is illegal.
     * @return the EntityConcept with the matching id
     */
    @NotNull
    Set<String> retrieveEntityConceptSynonyms(@NotNull String id) throws NoElementForIDException;

    /**
     * Retrieves all EntityConcepts whose main labels start with the given one. Ignores case.
     *
     * @return all EntityConcepts.
     */
    @NotNull
    Set<EntityConcept> retrieveEntityConceptsByPrefix(@NotNull String prefix);

    /**
     * Retrieves all EntityConcepts whose source URI equals with the given one. Ignores case.
     *
     * @return all EntityConcepts.
     */
    @NotNull
    Set<EntityConcept> retrieveEntityConceptsByURI(@NotNull String uri);

    /**
     * Retrieves all EntityConcepts whose main labels contain (or are equal to) the given one. Ignores case.
     *
     * @param mainLabel the main label to search for
     * @return a set of all matching EntityConcepts
     */
    @NotNull
    Set<EntityConcept> retrieveEntityConceptsContainingString(@NotNull String mainLabel);

    /**
     * Returns all EntityConcepts which are recursively referred to from the root EntityConcept via the specified RelationConcept.
     *
     * @param root            the starting EntityConcept for the search
     * @param relationConcept the RelationConcept which traversed Edges must have
     * @return all reachable EntityConcepts
     */
    @NotNull
    Set<EntityConcept> queryRelatedEntityConcepts(@NotNull EntityConcept root, @NotNull RelationConcept relationConcept);

    /**
     * This method creates the given EntityConcept in the graph.
     *
     * @param entityConcept the EntityConcept to create.
     * @return the created EntityConcept vertex
     */
    @NotNull
    EntityConcept createEntityConcept(@NotNull EntityConcept entityConcept);

    /**
     * Deletes the entity concept that the given id belongs to.
     *
     * @param ecID the id of the entity concept that is to be removed
     * @return returns True if the deletion was successful
     */
    boolean deleteEntityConcept(@NotNull String ecID);

    /**
     * Creates an edge between the two ECs identified by their IDs.
     *
     * @param ecIDfrom the EC the edge will originate from
     * @param ecIDto   the EC the edge will coincide with
     * @param edgeName the name the edge will carry
     * @return true iff the creation of the edge was successful
     */
    boolean createEdge(@NotNull String ecIDfrom, @NotNull String ecIDto, String edgeName);

    /**
     * Checks if there is a specific edge between the two ECs.
     *
     * @param ecIDfrom the first EC
     * @param ecIDto   the second EC
     * @param edgeName the name of the edge that is to be checked for
     * @return true iff there is an edge with given label between the two ECs
     */
    boolean hasEdge(@NotNull String ecIDfrom, @NotNull String ecIDto, String edgeName);

    /**
     * Checks for a path between the two ECs.
     *
     * @param ecIDfrom first EC
     * @param ecIDto   second EC
     * @return true if there is a path with arbitrary edge labels
     */
    boolean hasPath(@NotNull String ecIDfrom, @NotNull String ecIDto);

    /* ################################### Relation Concept ################################### */

    /**
     * Retrieves the RelationConcept for the given id.
     * If passed id doesn't exist, {@link NoElementForIDException} is thrown.
     *
     * @param id the id of the requested RelationConcept. Null value is illegal.
     * @return the RelationConcept with the matching id
     */
    @NotNull
    RelationConcept retrieveRelationConcept(@NotNull String id) throws NoElementForIDException;

    /**
     * Retrieves all RelationConcepts whose labels start with the given one. Ignores case.
     *
     * @return all RelationConcepts.
     */
    @NotNull
    Set<RelationConcept> retrieveRelationConceptsByPrefix(@NotNull String prefix);

    /**
     * Retrieves all RelationConcepts whose labels start with the given one. Ignores case.
     *
     * @return all RelationConcepts.
     */
    @NotNull
    Set<RelationConcept> retrieveRelationConceptsByURI(@NotNull String prefix);

    /**
     * Retrieves all RelationConcepts whose labels contain (or are equal to) the given one. Ignores case.
     *
     * @param string the label to search for
     * @return a set of all matching RelationConcepts
     */
    @NotNull
    Set<RelationConcept> retrieveRelationConceptsContainingString(@NotNull String string);

    /**
     * This method creates the given RelationConcept in the graph.
     *
     * @param relationConcept the RelationConcept to create.
     * @return the created RelationConcept vertex
     */
    @NotNull
    RelationConcept createRelationConcept(@NotNull RelationConcept relationConcept);

    /**
     * Delete the relation concept that the given id belongs to.
     *
     * @param rcID the id of the relation concept that is to be removed
     * @return True if the deletion was successful
     */
    boolean deleteRelationConcept(@NotNull String rcID);

    /* ################################### Entity Concept Relation ################################### */

    /**
     * This method creates the given EntityConceptRelation in the graph.
     * All elements with existent ids are reused, and new elements are created.
     *
     * @param entityConceptRelation the EntityConceptRelation to create.
     * @return the created EntityConceptRelation vertex
     */
    @NotNull
    EntityConceptRelation createEntityConceptRelation(@NotNull EntityConceptRelation entityConceptRelation);

    /**
     * Retrieves all EntityConceptRelations.
     *
     * @param useRelativeCalculation checks whether additional (costly) calculations for the relative usage of the ECR should be made
     *
     * @return all EntityConceptRelations.
     */
    @NotNull
    Set<EntityConceptRelation> retrieveEntityConceptRelations(boolean useRelativeCalculation);

    /**
     * Delete the entity concept relation that the given id belongs to.
     *
     * @param id the id of the entity concept relation that is to be removed
     *
     * @return True if the deletion was successful
     */
    boolean deleteEntityConceptRelation(@NotNull String id);


    int getNumberOfReferences(@NotNull String ecId);

    /**
     * Find EntityConceptRelation that the given EntityConcept is part of.
     *
     * @param root the EntityConcept the relations are sought after
     * @return a set of all EntityConceptRelation the given EntityConcept is part of
     */
    @NotNull
    Set<EntityConceptRelation> queryAllRelatedEntityConcepts(@NotNull EntityConcept root);

    /* ################################### Entity Type ################################### */

    /**
     * Retrieves the EntityType for the given id. This method creates the required sub-type and properly populates it as if the specific retrieval
     * method was invoked.
     * If passed id doesn't exist, {@link NoElementForIDException} is thrown.
     *
     * @param id the id of the requested EntityType. Null value is illegal.
     * @return the EntityType with the matching id
     */
    @NotNull
    EntityType retrieveEntityType(@NotNull String id) throws NoElementForIDException;

    /**
     * Searches for the related neighbours of the given EntityConcept, specified by the name of the EntityConceptRelation connecting them.
     *
     * @param entityConcept        the {@link EntityConcept} to find related neighbours for
     * @param relationConceptNames the names that the relations considered must have (i.e., isA)
     *                             If passed EntityConcept doesn't exist, {@link NoElementForIDException} is thrown.
     * @return a set of {@link EntityConcept} that are connected via given relations
     */
    @NotNull
    Set<EntityConcept> retrieveEntityConceptNeighbours(@NotNull EntityConcept entityConcept, String... relationConceptNames);

    @NotNull
    Set<EntityConcept> recommendEntityConcepts(@NotNull SemanticModel semanticModel);

    /* ################################### RDF ################################### */

    /**
     * Create a prefix repository for the given prefix map.
     *
     * @param prefixMap The map that maps prefixes to URLs
     *
     * @return True iff the creation was successful and not prefix repository was present before
     */
    boolean createRdfPrefixNode(Map<String, String> prefixMap);

    /**
     * Retrieve the prefix map of URLs from the RDF model that was imported.
     *
     * @return The prefix to URL map
     */
    @NotNull
    Map<String, String> getPrefixMap();

}