package de.buw.tmdt.plasma.services.kgs.shared.api;

import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptRelationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping("/api/plasma-kgs/universalknowledge")
public interface UniversalKnowledgeApi {

    /* ********************************************** ENTITY CONCEPT **************************************************/

    @PostMapping(value = "/entityconcept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull EntityConceptDTO createEntityConcept(@NotNull @RequestBody EntityConceptDTO request);

    @GetMapping(value = "/entityconcept", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull List<EntityConceptDTO> getEntityConcepts(@NotNull @RequestParam(value = "prefix", defaultValue = "", required = false) String prefix);

    @DeleteMapping(value = "/deleteEntityConcept/{id}")
    boolean deleteEntityConcept(@NotNull @PathVariable("id") String id);

    @GetMapping(value = "/entityconcept/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull EntityConceptDTO getEntityConceptById(@NotNull @PathVariable(value = "id") String id);

    /* ********************************************** RELATION CONCEPT **************************************************/

    @PostMapping(value = "/relationconcept", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull RelationConceptDTO createRelationConcept(@NotNull @RequestBody RelationConceptDTO request);

    @GetMapping(value = "/relationconcept", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull List<RelationConceptDTO> getRelationConcepts(@NotNull @RequestParam(value = "prefix", defaultValue = "", required = false) String prefix);

    @DeleteMapping(value = "/deleteRelationConcept/{id}")
    boolean deleteRelationConcept(@NotNull @PathVariable("id") String id);

    @GetMapping(value = "/relationconcept/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull RelationConceptDTO getRelationConceptById(@NotNull @PathVariable(value = "id") String id);

    /* ********************************************** ENTITY CONCEPT RELATION **************************************************/

    @PostMapping(value = "/entityconceptrelation", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull EntityConceptRelationDTO createEntityConceptRelation(@NotNull @RequestBody EntityConceptRelationDTO request);

    @GetMapping(value = "/entityconceptrelation", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull List<EntityConceptRelationDTO>
    getEntityConceptRelations(@RequestParam(name = "relativeUsageCalculation", defaultValue = "true") boolean useRelativeCalculation);

    @DeleteMapping(value = "/deleteEntityConceptRelation/{id}")
    boolean deleteEntityConceptRelation(@NotNull @PathVariable("id") String id);

    @GetMapping(value = "/referenceCount/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    int getNumberOfReferences(@NotNull @PathVariable(value = "id") String id);

    /* ********************************************** RDF **************************************************/

    /**
     * Imports the rdf model into the knowledge graph by translating each triple into.
     * EC -> ECR(->RC) -> EC
     *
     * @param body The model in Turtle or RDF/XML format to be imported.
     *
     * @return A set of {@link EntityConceptRelationDTO} that correspond to the model triples.
     */
    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull Set<EntityConceptRelationDTO> importRdf(@RequestBody String body);

    /**
     * Converts the underlying knowledge graph into a specified format.
     *
     * @param format The format of the output (Turtle or RDF/XML)
     * @param flat   A flag that indicates whether entity types should be mapped to entity concepts in the RDF output.
     *
     * @return The knowledge graph in RDF Turtle format.
     */
    @GetMapping(value = "/export", produces = MediaType.TEXT_PLAIN_VALUE)
    @NotNull String convertGraphToRdf(
            @NotNull @RequestParam(name = "format", defaultValue = "turtle") String format,
            @RequestParam(defaultValue = "false", name = "flat") boolean flat
    ) throws MalformedURLException;

    /**
     * Getter for the upper ontology of the KGS.
     *
     * @return A RDF Turtle formatted plain text that contains the upper ontology.
     */
    @GetMapping(value = "/upperOntology", produces = MediaType.TEXT_PLAIN_VALUE)
    @NotNull String getUpperOntology(@NotNull @RequestParam(name = "format", defaultValue = "turtle") String format) throws MalformedURLException;
}