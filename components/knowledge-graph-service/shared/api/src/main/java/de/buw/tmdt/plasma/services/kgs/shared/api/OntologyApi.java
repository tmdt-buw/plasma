package de.buw.tmdt.plasma.services.kgs.shared.api;

import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import de.buw.tmdt.plasma.services.kgs.shared.model.ExportFormat;
import de.buw.tmdt.plasma.services.kgs.shared.model.OntologyInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping("/api/plasma-kgs/ontologies")
public interface OntologyApi {

    /**
     * Find {@link SemanticModelNode}s filtered by a given prefix, infix or uri.
     * If a URI is specified, this takes precedence over the prefix and infix, thus, only an exact matching concept is returned or an empty list.
     *
     * @param ontologyLabels A list of ontology labels to indicate which ontologies to search
     * @param prefix The prefix to search for, can be a partial match but is only evaluated at the start of the label
     * @param infix  The infix/substring to search for
     * @param uri    The uri to match. Has to be an exact match to the specified URI of the concept.
     * @return A list of matching {@link SemanticModelNode}s
     */
    @GetMapping(value = "/nodes", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull List<SemanticModelNode> getElements(
            @RequestParam(value = "ontologyLabels", defaultValue = "") List<String> ontologyLabels,
            @RequestParam(value = "prefix",  required = false) String prefix,
            @RequestParam(value = "infix",  required = false) String infix,
            @RequestParam(value = "uri",  required = false) String uri);

    /**
     * Find {@link Relation}s filtered by a given prefix, infix or uri.
     * If a URI is specified, this takes precedence over the prefix and infix, thus, only an exact matching concept is returned or an empty list.
     *
     * @param ontologyLabels A list of ontology labels to indicate which ontologies to search
     * @param prefix The prefix to search for, can be a partial match but is only evaluated at the start of the label
     * @param infix  The infix/substring to search for
     * @param uri    The uri to match. Has to be an exact match to the specified URI of the concept
     * @return A list of matching {@link Relation}s
     */
    @GetMapping(value = "/relations", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull List<Relation> getRelations(
            @RequestParam(value = "ontologyLabels", defaultValue = "") List<String> ontologyLabels,
            @RequestParam(value = "prefix", required = false) String prefix,
            @RequestParam(value = "infix", required = false) String infix,
            @RequestParam(value = "uri", required = false) String uri);

    /**
     * Add ontology.
     */
    @PostMapping(value = "/ontologies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void addOntology(@RequestParam(value = "label") String label,
                     @RequestParam(value = "prefix") String prefix,
                     @RequestParam(value = "uri") String uri,
                     @RequestParam("file") MultipartFile ontologyFile);

    /**
     * Delete an ontology.
     */
    @DeleteMapping(value = "/ontologies/{label}")
    void deleteOntology(@PathVariable(value = "label") String label);

    /**
     * Get a list of all available ontologies.
     * This call returns ontologies to be provided to the GUI.
     */
    @GetMapping(value = "/ontologies", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OntologyInfo> listOntologies();


    /**
     * Downloads the current local ontology.
     */
    @GetMapping(value = "/ontologies/local", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> downloadLocalOntology(@NotNull @RequestParam(name = "format", defaultValue = "TURTLE") ExportFormat format);


    /**
     * Get prefix map.
     * This endpoint returns a mapping of all prefixes to their namespaces currently known to the service.
     */
    @GetMapping(value = "/prefixes", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> getNamespaces();

    /**
     * Validate the given URI.
     * Checks if the given URI is valid according to RFC3987 and 3986.
     * Also checks if this URI is already taken in the local ontology.
     * @param uri The uri to check
     * @throws org.springframework.web.server.ResponseStatusException 406 if the URI is not valid
     * @throws org.springframework.web.server.ResponseStatusException 409 if an element with that URI already exists
     */
    @GetMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    void validateURI(@RequestParam(name = "uri") String uri);
}