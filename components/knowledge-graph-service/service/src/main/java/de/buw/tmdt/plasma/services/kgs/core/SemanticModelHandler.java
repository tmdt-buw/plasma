package de.buw.tmdt.plasma.services.kgs.core;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.*;
import de.buw.tmdt.plasma.services.kgs.shared.model.ExportFormat;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticModelHandler {

    private static final Logger log = LoggerFactory.getLogger(SemanticModelHandler.class);

    private final SemanticModelManagement ops;
    private final LocalOntologyManagement onto;
    private final OntologyHandler ontologyHandler;

    private final DateTimeFormatter exportTimestampFormatter = DateTimeFormatter.ofPattern("yyMMdd-HHmm");

    @Autowired
    public SemanticModelHandler(
            @NotNull SemanticModelManagement ops,
            @NotNull LocalOntologyManagement onto, OntologyHandler ontologyHandler) {
        this.ops = ops;
        this.onto = onto;
        this.ontologyHandler = ontologyHandler;
    }


    @NotNull
    public SemanticModel createSemanticModel(@NotNull SemanticModel semanticModel) {
        log.info("Creating Semantic Model");
        Model cmModel = CMToRDFMapper.convertToRDFModel(semanticModel);
        log.info(asTurtle(cmModel));

        ops.persistModel(cmModel);

        List<SemanticModelNode> resources = semanticModel.getNodes().stream()
                .filter(smn -> smn instanceof NamedEntity || smn instanceof Class)
                .collect(Collectors.toList());
        onto.addResourcesToOntology(resources);

        List<Relation> relations = semanticModel.getEdges();
        onto.addRelationsToOntology(relations);

        SemanticModel sm = getSemanticModel(semanticModel.getId());

        return sm;
    }

    @NotNull
    public SemanticModel getSemanticModel(@NotNull String id) {
        Model retrievedModel = ops.queryModel(id);
        log.info("retrieved model\n {}", asTurtle(retrievedModel));

        if (retrievedModel.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No model available for id " + id);
        }
        SemanticModel restoredSM = CMToRDFMapper.convertToSemanticModel(retrievedModel, this::lookupNodeByURI, this::lookupRelationByURI);
        return restoredSM;
    }

    private SemanticModelNode lookupNodeByURI(String uri) {
        return ontologyHandler.getElements(Collections.emptyList(), null, null, uri).stream()
                .findFirst()
                .orElse(null);
    }

    private Relation lookupRelationByURI(String uri) {
        return ontologyHandler.getRelations(Collections.emptyList(), null, null, uri).stream()
                .findFirst()
                .orElse(null);
    }

    public void deleteSemanticModel(@NotNull String id) {
        ops.deleteModel(id);
    }

    public static String asTurtle(Model model) {
        StringWriter out = new StringWriter();
        model.write(out, "TURTLE");
        return out.toString();
    }

    public @NotNull ResponseEntity<String> exportSemanticModel(@NotNull String id, @NotNull ExportFormat format, boolean includeMappings) {
        Model retrievedModel = ops.queryModel(id);

        if (retrievedModel.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No model available for id " + id);
        }
        String timestamp = LocalDateTime.now().format(exportTimestampFormatter);

        HttpHeaders headers = generateFilenameHeader(timestamp + "-" + id + ".ttl");
        return new ResponseEntity<>(prepareForExport(retrievedModel, format, includeMappings), headers, HttpStatus.OK);
    }

    public @NotNull ResponseEntity<String> convertSemanticModel(@NotNull CombinedModel combinedModel, @NotNull ExportFormat format, boolean includeMappings) {
        if (combinedModel.getSemanticModel().getId() == null) {
            combinedModel.getSemanticModel().setId(UUID.randomUUID().toString().substring(0, 5));
        }
        Model cmModel = CMToRDFMapper.convertToRDFModel(combinedModel.getSemanticModel());

        String modelId = combinedModel.getSemanticModel().getId();

        if (modelId == null) {
            modelId = "temp";
        }
        String timestamp = LocalDateTime.now().format(exportTimestampFormatter);

        HttpHeaders headers = generateFilenameHeader(timestamp + "-" + modelId + ".ttl");
        return new ResponseEntity<>(prepareForExport(cmModel, format, includeMappings), headers, HttpStatus.OK);
    }

    private String prepareForExport(Model model, ExportFormat format, boolean includeMappings) {
        // remove all relations that are from the PLCM namespace
        Model cleansedModel = ModelFactory.createDefaultModel();
        cleansedModel.setNsPrefixes(model.getNsPrefixMap());

        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.nextStatement();
            Property predicate = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            if (predicate.getNameSpace().equals(PLCM.getURI())) {
                if (includeMappings && predicate.equals(PLCM.originalLabel)) {
                    // exception if we want to include the mapping information
                } else {
                    continue;
                }
            }
            if (object.isResource() && Objects.equals(object.asResource(), PLCM.SemanticModel)) {
                continue;
            }
            cleansedModel.add(stmt);

        }
        StringWriter out = new StringWriter();
        cleansedModel.write(out, format.getFormatString());
        String result = out.toString();
        log.info(result);
        return result;
    }

    private HttpHeaders generateFilenameHeader(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename)
                .build();
        headers.setContentDisposition(contentDisposition);
        headers.set("filename", filename);
        headers.setAccessControlExposeHeaders(List.of(HttpHeaders.CONTENT_DISPOSITION, "filename"));
        return headers;
    }

    @NotNull
    public Set<SemanticModel> searchByElementLabel(@NotNull String searchTerm) {
        List<String> ids = ops.findByElementLabel(searchTerm);
        return ids.stream()
                .map(this::getSemanticModel)
                .collect(Collectors.toSet());
    }
}
