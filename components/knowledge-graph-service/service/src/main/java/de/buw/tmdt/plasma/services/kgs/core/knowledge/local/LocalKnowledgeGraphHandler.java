package de.buw.tmdt.plasma.services.kgs.core.knowledge.local;

import de.buw.tmdt.plasma.services.kgs.core.rdf.ModelToRdfConverter;
import de.buw.tmdt.plasma.services.kgs.shared.converter.ModelDTOConverter;
import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriverFactory;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.GraphDBException;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.NoElementForIDException;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelCreationResponse;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LocalKnowledgeGraphHandler {

    private static final Logger logger = LoggerFactory.getLogger(LocalKnowledgeGraphHandler.class);

    private final GraphDBDriverFactory graphDBDriverFactory;
    private final ModelDTOConverter modelDTOConverter;
    private static final String XML_VARIABLE = "xml";
    private static final String TURTLE_VARIABLE = "turtle";
    private final ModelToRdfConverter modelToRdfConverter;

    @Autowired
    public LocalKnowledgeGraphHandler(
            @NotNull GraphDBDriverFactory graphDBDriverFactory,
            @NotNull ModelDTOConverter modelDTOConverter,
            @NotNull ModelToRdfConverter modelToRdfConverter
    ) {
        this.graphDBDriverFactory = graphDBDriverFactory;
        this.modelDTOConverter = modelDTOConverter;
        this.modelToRdfConverter = modelToRdfConverter;
    }


    @NotNull
    public SemanticModelCreationResponse createSemanticModel(@NotNull SemanticModelDTO request) {
        try {
            logger.info("Creating Semantic Model");
            SemanticModel semanticModel = modelDTOConverter.mapSemanticModelDTO(request);

            Map<String, String> oldToNewIdLookup;
            try {
                oldToNewIdLookup = graphDBDriverFactory.getGraphDBDriver().createSemanticModelExposeMapping(semanticModel).entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getId()
                        ));
            } catch (GraphDBException e) {
                logger.error("Could not create Semantic Model", e);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not create semantic model");
            }

            //Retrieve the created semantic model from the database
            final SemanticModelDTO semanticModelDTO = getSemanticModel(oldToNewIdLookup.get(request.getId()));

            logger.info("Created Semantic Model");
            return new SemanticModelCreationResponse(oldToNewIdLookup, semanticModelDTO);
        } catch (RuntimeException e) {
            logger.warn("An unknown error occured", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error occured");
        }
    }

    @NotNull
    public SemanticModelDTO getSemanticModel(@NotNull String id) {
        SemanticModel model = graphDBDriverFactory.getGraphDBDriver().retrieveSemanticModel(id);
        return modelDTOConverter.mapSemanticModel(model);
    }

    public void deleteSemanticModel(@NotNull String id) {
        if (!graphDBDriverFactory.getGraphDBDriver().removeSemanticModel(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not delete SemanticModel!");
        }
    }

    @NotNull
    public SemanticModelDTO getSemanticModelByDataSourceId(@NotNull String dataSourceId) {
        SemanticModel model = graphDBDriverFactory.getGraphDBDriver().retrieveSemanticModelByDataSourceId(dataSourceId);
        return modelDTOConverter.mapSemanticModel(model);
    }

    public void deleteSemanticModelByDataSourceId(@NotNull String dataSourceId) {
        if (!graphDBDriverFactory.getGraphDBDriver().removeSemanticModelByDataSourceId(dataSourceId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not delete SemanticModel!");
        }
    }

    @NotNull
    public List<EntityTypeDTO> getEntityTypes(@NotNull List<String> ids) {
        List<EntityTypeDTO> result = new ArrayList<>(ids.size());
        for (String id : ids) {
            try {
                final EntityType entityType = graphDBDriverFactory.getGraphDBDriver().retrieveEntityType(id);
                final EntityTypeDTO entityTypeDTO = modelDTOConverter.mapEntityType(entityType);
                result.add(entityTypeDTO);
            } catch (NoElementForIDException e) {
                //silent "failure" to deliver a partial result instead of none at all.
                // This allows the caller to react dependent on its own requirements.
                logger.warn("EntityType for invalid id `{}` was requested.", id);
            }
        }
        return result;
    }

    public @NotNull ResponseEntity<String> convertSemanticModel(@NotNull String id, @NotNull String format, boolean flat) throws MalformedURLException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Model model = modelToRdfConverter.convertSemanticModelToRdf(id, flat);
        if (format.equals(XML_VARIABLE)) {
            RDFDataMgr.write(baos, model, Lang.RDFXML);
        } else if (format.equals(TURTLE_VARIABLE)) {
            RDFDataMgr.write(baos, model, Lang.TURTLE);
        } else {
            throw new MalformedURLException("Path variable \"format\" must either specify \"xml\" or \"format\" but states " + format);
        }
        String modelString = baos.toString(StandardCharsets.UTF_8);

        return new ResponseEntity<>(modelString, HttpStatus.OK);
    }
}