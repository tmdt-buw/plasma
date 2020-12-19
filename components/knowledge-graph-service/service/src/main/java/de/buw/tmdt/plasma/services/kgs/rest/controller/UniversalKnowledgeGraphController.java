package de.buw.tmdt.plasma.services.kgs.rest.controller;

import de.buw.tmdt.plasma.services.kgs.shared.api.UniversalKnowledgeApi;
import de.buw.tmdt.plasma.services.kgs.core.knowledge.universal.UniversalKnowledgeGraphHandler;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptRelationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

@RestController
public class UniversalKnowledgeGraphController implements UniversalKnowledgeApi {

    private final UniversalKnowledgeGraphHandler universalKnowledgeGraphHandler;

    @Autowired
    public UniversalKnowledgeGraphController(@NotNull UniversalKnowledgeGraphHandler universalKnowledgeGraphHandler) {
        this.universalKnowledgeGraphHandler = universalKnowledgeGraphHandler;
    }

    /* ********************************************** ENTITY CONCEPT **************************************************/

    @Override
    @Operation(description = "Creates a new entity concept.")
    public @NotNull EntityConceptDTO createEntityConcept(@NotNull EntityConceptDTO request) {
        return universalKnowledgeGraphHandler.createEntityConcept(request);
    }

    @Override
    @Operation(description = "Returns an entity concept for the given id")
    public @NotNull EntityConceptDTO getEntityConceptById(@NotNull String id) {
        return universalKnowledgeGraphHandler.getEntityConceptById(id);
    }

    @Override
    @Operation(description = "Returns a list of entity concepts matching a prefix. If prefix is omitted, all entity concepts are returned.")
    public @NotNull List<EntityConceptDTO> getEntityConcepts(@NotNull String prefix) {
        return universalKnowledgeGraphHandler.getEntityConcepts(prefix);
    }

    @Override
    @Operation(description = "Deletes the entity concept with the given id")
    public boolean deleteEntityConcept(@NotNull String id) {
        return universalKnowledgeGraphHandler.deleteEntityConcept(id);
    }

    /* ********************************************** RELATION CONCEPT **************************************************/

    @Override
    @Operation(description = "Creates a new relation concept.")
    public @NotNull RelationConceptDTO createRelationConcept(@NotNull RelationConceptDTO request) {
        return universalKnowledgeGraphHandler.createRelationConcept(request);
    }

    @Override
    @Operation(description = "Returns a relation concept for the given id")
    public @NotNull RelationConceptDTO getRelationConceptById(@NotNull String id) {
        return universalKnowledgeGraphHandler.getRelationConceptById(id);
    }

    @Override
    @Operation(description = "Returns a list of all relation concepts matching a prefix. If prefix is omitted, all relation concepts are returned.")
    public @NotNull List<RelationConceptDTO> getRelationConcepts(@NotNull String prefix) {
        return universalKnowledgeGraphHandler.getRelationConcepts(prefix);
    }

    @Override
    @Operation(description = "Deletes the relation concept with the given id")
    public boolean deleteRelationConcept(@NotNull String id) {
        return universalKnowledgeGraphHandler.deleteRelationConcept(id);
    }

    /* ********************************************** ENTITY CONCEPT RELATION **************************************************/

    @Override
    @Operation(description = "Creates a new EntityConceptRelation.")
    public @NotNull EntityConceptRelationDTO createEntityConceptRelation(@NotNull EntityConceptRelationDTO request) {
        return universalKnowledgeGraphHandler.createEntityConceptRelation(request);
    }

    @Override
    @Operation(description = "Returns a list of all EntityConceptRelations")
    public @NotNull List<EntityConceptRelationDTO> getEntityConceptRelations(boolean useRelativeCalculation) {
        return universalKnowledgeGraphHandler.getEntityConceptRelations(useRelativeCalculation);
    }

    @Override
    @Operation(description = "Deletes the entity concept relation with the given id")
    public boolean deleteEntityConceptRelation(@NotNull String id) {
        return universalKnowledgeGraphHandler.deleteEntityConceptRelation(id);
    }

    @Override
    @Operation(description = "Returns the number of ECR references of an entity concept")
    public int getNumberOfReferences(@NotNull String id) {
        return universalKnowledgeGraphHandler.getNumberOfReferences(id);
    }

    /* ********************************************** RDF **************************************************/

    @Override
    @Operation(description = "Returns the created ECR by importing RDF triples in either Turtle or RDF XML format")
    public @NotNull Set<EntityConceptRelationDTO> importRdf(@NotNull String body) {
        return universalKnowledgeGraphHandler.importRdf(body);
    }

    @Override
    @Operation(description = "Returns the knowledge graph in Turtle or RDF XML")
    public @NotNull String convertGraphToRdf(@NotNull String format, boolean flat) throws MalformedURLException {
        return universalKnowledgeGraphHandler.convertGraphToRdf(format, flat);
    }

    @Override
    @Operation(description = "Returns the upper ontology for the knowledge graph rdf format")
    public @NotNull String getUpperOntology(@NotNull String format) throws MalformedURLException {
        return universalKnowledgeGraphHandler.getUpperOntology(format);
    }
}