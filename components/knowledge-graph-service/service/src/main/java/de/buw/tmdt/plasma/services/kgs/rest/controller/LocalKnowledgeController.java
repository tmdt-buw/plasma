package de.buw.tmdt.plasma.services.kgs.rest.controller;

import de.buw.tmdt.plasma.services.kgs.shared.api.LocalKnowledgeApi;
import de.buw.tmdt.plasma.services.kgs.core.knowledge.local.LocalKnowledgeGraphHandler;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelCreationResponse;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.List;

@RestController
public class LocalKnowledgeController implements LocalKnowledgeApi {

    private final LocalKnowledgeGraphHandler localKnowledgeGraphHandler;

    @Autowired
    public LocalKnowledgeController(@NotNull LocalKnowledgeGraphHandler localKnowledgeGraphHandler) {
        this.localKnowledgeGraphHandler = localKnowledgeGraphHandler;
    }

    @Override
    @Operation(description = "Creates a new semantic model")
    public @NotNull SemanticModelCreationResponse createSemanticModel(@NotNull SemanticModelDTO request) {
        return localKnowledgeGraphHandler.createSemanticModel(request);
    }

    @Override
    @Operation(description = "Load Semantic Model")
    public @NotNull SemanticModelDTO getSemanticModel(@NotNull String id) {
        return localKnowledgeGraphHandler.getSemanticModel(id);
    }

    @Override
    @Operation(description = "Deletes a semantic model")
    public void deleteSemanticModel(@NotNull String id) {
        localKnowledgeGraphHandler.deleteSemanticModel(id);
    }

    @Override
    @Operation(description = "Retrieve Entity Types with the given ids.")
    public List<EntityTypeDTO> getEntityTypes(@NotNull List<String> ids) {
        ids.removeIf(id -> id == null || id.isEmpty());
        return localKnowledgeGraphHandler.getEntityTypes(ids);
    }

    @Override
    @Operation(description = "Load Semantic Model by its DataSourceId")
    public SemanticModelDTO getSemanticModelByDataSourceId(@NotNull String dataSourceId) {
        return localKnowledgeGraphHandler.getSemanticModelByDataSourceId(dataSourceId);
    }

    @Override
    @Operation(description = "Delete Semantic Model by its DataSourceId")
    public void deleteSemanticModelByDataSourceId(@NotNull String dataSourceId) {
        localKnowledgeGraphHandler.deleteSemanticModelByDataSourceId(dataSourceId);
    }

    @Override
    @Operation(description = "Returns the Turtle or RDF XML format of a semantic model identified by its id")
    public @NotNull ResponseEntity<String> convertSemanticModel(@NotNull String id, @NotNull String format, boolean flat) throws MalformedURLException {
        return localKnowledgeGraphHandler.convertSemanticModel(id, format, flat);
    }
}