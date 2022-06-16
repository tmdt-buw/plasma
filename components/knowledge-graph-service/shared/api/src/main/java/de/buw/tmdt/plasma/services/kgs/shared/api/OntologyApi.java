package de.buw.tmdt.plasma.services.kgs.shared.api;

import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import de.buw.tmdt.plasma.services.kgs.shared.model.OntologyInfo;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @PostMapping(value = "/ontologies",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void addOntology(@RequestParam(value = "label") String label,
                     @RequestParam(value = "prefix") String prefix,
                     @RequestParam(value = "uri") String uri,
                     @RequestParam("file") MultipartFile ontologyFile);

    @GetMapping(value = "/ontologies", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OntologyInfo> listOntologies();
}