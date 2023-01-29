package de.buw.tmdt.plasma.services.kgs.rest.controller;

import de.buw.tmdt.plasma.datamodel.semanticmodel.NodeTemplate;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import de.buw.tmdt.plasma.services.kgs.core.Ontologies;
import de.buw.tmdt.plasma.services.kgs.core.OntologyHandler;
import de.buw.tmdt.plasma.services.kgs.shared.api.OntologyApi;
import de.buw.tmdt.plasma.services.kgs.shared.model.ExportFormat;
import de.buw.tmdt.plasma.services.kgs.shared.model.OntologyInfo;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIs;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class OntologyController implements OntologyApi {

    private static final Logger log = LoggerFactory.getLogger(OntologyController.class);
    private final OntologyHandler ontologyHandler;
    private final Ontologies ontologies;

    @Value("${plasma.kgs.localontology.label:LOCAL}")
    private String localOntologyLabel;

    @Autowired
    public OntologyController(@NotNull OntologyHandler ontologyHandler,
                              @NotNull Ontologies ontologies) {
        this.ontologyHandler = ontologyHandler;
        this.ontologies = ontologies;
    }

    @Override
    public @NotNull List<SemanticModelNode> getElements(List<String> ontologyLabels, String prefix, String infix, String uri) {
        List<SemanticModelNode> elements = ontologyHandler.getElements(ontologyLabels, prefix, infix, uri);
        elements.stream()
                .filter(Objects::nonNull)
                .forEach(element -> ((NodeTemplate) element).convertToTemplate());
        return elements.stream().limit(50).collect(Collectors.toList());
    }

    @Override
    public @NotNull List<Relation> getRelations(List<String> ontologyLabels, String prefix, String infix, String uri) {
        List<Relation> relations = ontologyHandler.getRelations(ontologyLabels, prefix, infix, uri);
        return relations.stream()
                .filter(Objects::nonNull)
                .limit(50)
                .map(Relation::convertToTemplate)
                .collect(Collectors.toList());
    }

    @Override
    public void addOntology(String label, String prefix, String uri, MultipartFile ontologyFile) {
        ontologies.addOntology(label, prefix, uri, ontologyFile);
    }

    @Override
    public void deleteOntology(String label) {
        ontologies.deleteOntology(label);
    }

    @Override
    public List<OntologyInfo> listOntologies() {
        return ontologies.listOntologies();
    }

    @Override
    public ResponseEntity<String> downloadLocalOntology(@NotNull ExportFormat format) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(localOntologyLabel + ".ttl")
                .build();
        headers.setContentDisposition(contentDisposition);
        headers.set("filename", localOntologyLabel + ".ttl");
        headers.setAccessControlExposeHeaders(List.of(HttpHeaders.CONTENT_DISPOSITION, "filename"));

        return new ResponseEntity<>(ontologyHandler.downloadLocalOntology(format), headers, HttpStatus.OK);
    }

    @Override
    public Map<String, String> getNamespaces() {
        return Ontologies.PREFIXES.getNsPrefixMap();
    }

    @Override
    public void validateURI(String uri) {
        try {
            IRIs.checkEx(uri);
        } catch (IRIException ie) {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "The URI provided is not valid according to RFC3987");
        }

        if (!getElements(List.of(localOntologyLabel), null, null, uri).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The URI provided is already in use in the local ontology");
        }

        if (!getRelations(List.of(localOntologyLabel), null, null, uri).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The URI provided is already in use in the local ontology");
        }
    }
}