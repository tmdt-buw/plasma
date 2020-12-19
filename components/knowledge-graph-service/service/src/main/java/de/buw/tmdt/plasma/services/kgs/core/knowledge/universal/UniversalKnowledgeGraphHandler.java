package de.buw.tmdt.plasma.services.kgs.core.knowledge.universal;

import de.buw.tmdt.plasma.services.kgs.core.rdf.KgsToRdfConverter;
import de.buw.tmdt.plasma.services.kgs.core.rdf.RdfImporter;
import de.buw.tmdt.plasma.services.kgs.core.rdf.UpperOntologyDefiner;
import de.buw.tmdt.plasma.services.kgs.shared.converter.ModelDTOConverter;
import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriverFactory;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.ConceptNameAlreadyExistsException;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.GraphDBException;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.NoElementForIDException;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.EntityConceptRelationDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.knowledgegraph.RelationConceptDTO;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.RDFReaderF;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UniversalKnowledgeGraphHandler {

    private static final Logger logger = LoggerFactory.getLogger(UniversalKnowledgeGraphHandler.class);

    private final GraphDBDriverFactory graphDBDriverFactory;
    private final ModelDTOConverter modelDTOConverter;
    private static final String XML_VARIABLE = "xml";
    private static final String TURTLE_VARIABLE = "turtle";
    private final KgsToRdfConverter kgsToRdfConverter;
    private final RdfImporter rdfImporter;

    @Autowired
    public UniversalKnowledgeGraphHandler(
            @NotNull GraphDBDriverFactory graphDBDriverFactory,
            @NotNull ModelDTOConverter modelDTOConverter,
            @NotNull KgsToRdfConverter kgsToRdfConverter,
            @NotNull RdfImporter rdfImporter
    ) {
        this.graphDBDriverFactory = graphDBDriverFactory;
        this.modelDTOConverter = modelDTOConverter;
        this.kgsToRdfConverter = kgsToRdfConverter;
        this.rdfImporter = rdfImporter;
    }


    /* ********************************************** ENTITY CONCEPT **************************************************/

    @NotNull
    public EntityConceptDTO createEntityConcept(@NotNull EntityConceptDTO request) {
        logger.debug("Creating EntityConcept with name: {}, description: {}", request.getMainLabel(), request.getDescription());

        if (!request.getId().trim().isEmpty()) {
            logger.warn("Ignoring provided ID {} for the Entity Concept on creation ", request.getId());
        }

        if (request.getMainLabel().isEmpty()) {
            logger.warn("EntityConcept name must not be empty {}", request.getMainLabel());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EntityConcept name must not be empty!");
        }

        EntityConcept result;
        try {
            result = graphDBDriverFactory.getGraphDBDriver().createEntityConcept(new EntityConcept(
                    "",
                    request.getMainLabel(),
                    request.getDescription(),
                    null
            ));
        } catch (ConceptNameAlreadyExistsException e) {
            logger.warn("EntityConcept already exists: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EntityConcept already exists!");
        }

        logger.debug("Created EntityConcept: id: {}, name: {}, description: {}", result.getId(), result.getMainLabel(), result.getDescription());
        return modelDTOConverter.mapEntityConcept(result);
    }

    @NotNull
    public List<EntityConceptDTO> getEntityConcepts(@NotNull String prefix) {
        Set<EntityConcept> result = graphDBDriverFactory.getGraphDBDriver().retrieveEntityConceptsByPrefix(prefix);
        return result.stream().map(modelDTOConverter::mapEntityConcept).collect(Collectors.toList());
    }

    public boolean deleteEntityConcept(@NotNull String ecID) {
        return graphDBDriverFactory.getGraphDBDriver().deleteEntityConcept(ecID);
    }

    @NotNull
    public EntityConceptDTO getEntityConceptById(@NotNull String id) {
        try {
            return modelDTOConverter.mapEntityConcept(graphDBDriverFactory.getGraphDBDriver().retrieveEntityConcept(id));
        } catch (NoElementForIDException e) {
            logger.warn("EntityConcept with ID {} was not found", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "EntityConcept not found");
        }
    }

    /* ********************************************** RELATION CONCEPT **************************************************/

    @NotNull
    public RelationConceptDTO createRelationConcept(@NotNull RelationConceptDTO request) {
        logger.debug(
                "Creating RelationConcept with name: {}, description: {}, properties: {}",
                request.getLabel(),
                request.getDescription(),
                request.getProperties()
        );

        if (!request.getId().trim().isEmpty()) {
            logger.warn("Ignoring provided ID {} for the Entity Concept on creation ", request.getId());
        }

        if (request.getLabel().isEmpty()) {
            logger.warn("RelationConcept name must not be empty {}", request.getLabel());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RelationConcept name must not be empty!");
        }

        RelationConcept result;
        try {
            Set<RelationConcept.Property> relConceptProperties = new HashSet<>();
            request.getProperties().forEach(property -> relConceptProperties.add(RelationConcept.Property.byClassName(property)));
            RelationConcept relationConcept = new RelationConcept("", request.getDescription(), request.getLabel(), relConceptProperties);
            result = graphDBDriverFactory.getGraphDBDriver().createRelationConcept(relationConcept);
        } catch (ConceptNameAlreadyExistsException e) {
            logger.warn("RelationConcept already exists: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RelationConcept already exists!");
        }

        logger.debug("Created Relation Concept with Name: {} Description: {} Properties: {}", result.getLabel(), result.getDescription(),
                result.getProperties()
        );

        return modelDTOConverter.mapRelationConcept(result);
    }

    @NotNull
    public List<RelationConceptDTO> getRelationConcepts(@NotNull String prefix) {
        Set<RelationConcept> result = graphDBDriverFactory.getGraphDBDriver().retrieveRelationConceptsByPrefix(prefix);
        return result.stream().map(modelDTOConverter::mapRelationConcept).collect(Collectors.toList());
    }

    public boolean deleteRelationConcept(@NotNull String rcID) {
        return graphDBDriverFactory.getGraphDBDriver().deleteRelationConcept(rcID);
    }

    @NotNull
    public RelationConceptDTO getRelationConceptById(@NotNull String id) {
        try {
            return modelDTOConverter.mapRelationConcept(graphDBDriverFactory.getGraphDBDriver().retrieveRelationConcept(id));
        } catch (NoElementForIDException e) {
            logger.warn("RelationConcept with ID {} was not found", id, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "RelationConcept not found");
        }
    }

    /* ********************************************** ENTITY CONCEPT RELATION **************************************************/

    @NotNull
    public EntityConceptRelationDTO createEntityConceptRelation(@NotNull EntityConceptRelationDTO request) {
        if (request.getRelationConceptID().isEmpty() || request.getHeadEntityConceptID().isEmpty() || request.getTailEntityConceptID().isEmpty()) {
            logger.warn("RelationConcept and EntityConcept IDs must not be empty");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "RelationConcept and EntityConcept IDs must not be empty!");
        }

        GraphDBDriver graphDBDriver = graphDBDriverFactory.getGraphDBDriver();

        RelationConcept relationConcept = graphDBDriver.retrieveRelationConcept(request.getRelationConceptID());
        EntityConcept headEntityConcept = graphDBDriver.retrieveEntityConcept(request.getHeadEntityConceptID());
        EntityConcept tailEntityConcept = graphDBDriver.retrieveEntityConcept(request.getTailEntityConceptID());

        EntityConceptRelation entityConceptRelation = new EntityConceptRelation("", tailEntityConcept, headEntityConcept, relationConcept);

        EntityConceptRelation result;
        try {
            result = graphDBDriver.createEntityConceptRelation(entityConceptRelation);
        } catch (ConceptNameAlreadyExistsException e) {
            logger.warn("EntityConceptRelation already exists: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EntityConceptRelation already exists!");
        }
        return modelDTOConverter.mapEntityConceptRelation(result);
    }

    @NotNull
    public List<EntityConceptRelationDTO> getEntityConceptRelations(boolean useRelativeCalculation) {
        GraphDBDriver graphDBDriver = graphDBDriverFactory.getGraphDBDriver();
        Set<EntityConceptRelation> result;
        try {
            result = graphDBDriver.retrieveEntityConceptRelations(useRelativeCalculation);
        } catch (GraphDBException e) {
            logger.warn("Could not query all entity concept relations: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not query all entity concept relations!");
        }
        return result.stream().map(modelDTOConverter::mapEntityConceptRelation).collect(Collectors.toList());
    }

    public boolean deleteEntityConceptRelation(@NotNull String id) {
        return graphDBDriverFactory.getGraphDBDriver().deleteEntityConceptRelation(id);
    }

    public int getNumberOfReferences(@NotNull String ecId) {
        GraphDBDriver driver = graphDBDriverFactory.getGraphDBDriver();
        return driver.getNumberOfReferences(ecId);
    }

    /* ********************************************** RDF **************************************************/

    public @NotNull String convertGraphToRdf(@NotNull String format, boolean flat) throws MalformedURLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model model = kgsToRdfConverter.convertKgsToRdf(flat);
        if (format.equals(XML_VARIABLE)) {
            RDFDataMgr.write(baos, model, Lang.RDFXML);
        } else if (format.equals(TURTLE_VARIABLE)) {
            RDFDataMgr.write(baos, model, Lang.TURTLE);
        } else {
            throw new MalformedURLException("Path variable \"format\" must either specify \"xml\" or \"format\" but states " + format);
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    public @NotNull String getUpperOntology(@NotNull String format) throws MalformedURLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model model = UpperOntologyDefiner.getUpperOntologyModel();
        if (format.equals(XML_VARIABLE)) {
            RDFDataMgr.write(baos, model, Lang.RDFXML);
        } else if (format.equals(TURTLE_VARIABLE)) {
            RDFDataMgr.write(baos, model, Lang.TURTLE);
        } else {
            throw new MalformedURLException("Path variable \"format\" must either specify \"xml\" or \"format\" but states " + format);
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    public @NotNull Set<EntityConceptRelationDTO> importRdf(@NotNull String input) {
        RDFReaderF factory = new RDFReaderFImpl();
        RDFReader reader;
        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        OntModel rdfModel = ModelFactory.createOntologyModel();
        if (!Strings.isEmpty(input)) {
            if (input.startsWith("<?xml") || input.startsWith("<rdf")) {
                reader = factory.getReader(Lang.RDFXML.getName());
            } else {
                reader = factory.getReader(Lang.TURTLE.getName());
            }
        } else {
            logger.info("Input for rdf import is null");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty body");
        }
        reader.read(rdfModel, is, null);

        return rdfImporter.convertRdfToKgs(rdfModel).stream().map(modelDTOConverter::mapEntityConceptRelation).collect(Collectors.toSet());
    }
}