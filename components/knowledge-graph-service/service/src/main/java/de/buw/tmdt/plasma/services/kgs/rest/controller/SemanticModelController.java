package de.buw.tmdt.plasma.services.kgs.rest.controller;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModel;
import de.buw.tmdt.plasma.services.kgs.core.SemanticModelHandler;
import de.buw.tmdt.plasma.services.kgs.shared.api.SemanticModelApi;
import de.buw.tmdt.plasma.services.kgs.shared.model.ExportFormat;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class SemanticModelController implements SemanticModelApi {

    private final SemanticModelHandler semanticModelHandler;

    @Autowired
    public SemanticModelController(@NotNull SemanticModelHandler semanticModelHandler) {
        this.semanticModelHandler = semanticModelHandler;
    }

    @Override
    @Operation(description = "Creates a new semantic model")
    public @NotNull SemanticModel createSemanticModel(@NotNull SemanticModel request) {
        return semanticModelHandler.createSemanticModel(request);
    }

    @Override
    @Operation(description = "Load Semantic Model")
    public @NotNull SemanticModel getSemanticModel(@NotNull String id) {
        return semanticModelHandler.getSemanticModel(id);
    }

    @Override
    @Operation(description = "Deletes a semantic model")
    public void deleteSemanticModel(@NotNull String id) {
        semanticModelHandler.deleteSemanticModel(id);
    }

    @Operation(description = "Returns a stored semantic model in a specified format.")
    public @NotNull ResponseEntity<String> exportStoredSemanticModel(@NotNull String id, @NotNull ExportFormat format, boolean includeMappings) {
        return semanticModelHandler.exportSemanticModel(id, format, includeMappings);
    }

    public @NotNull ResponseEntity<String> exportStoredSemanticModel(@NotNull String id, @NotNull ExportFormat format) {
        return semanticModelHandler.exportSemanticModel(id, format, false);
    }

    @Operation(description = "Returns the given semantic model in a specified format.")
    public @NotNull ResponseEntity<String> convertSemanticModel(@NotNull CombinedModel combinedModel, @NotNull ExportFormat format, boolean includeMappings) {
        return semanticModelHandler.convertSemanticModel(combinedModel, format, includeMappings);
    }

    public @NotNull ResponseEntity<String> convertSemanticModel(@NotNull CombinedModel combinedModel, @NotNull ExportFormat format) {
        return semanticModelHandler.convertSemanticModel(combinedModel, format, false);
    }

    @Override
    @Operation(description = "Returns Semantic Models if the search term matches a contained element")
    public @NotNull Set<SemanticModel> searchByElementLabel(@NotNull String searchTerm) {
        return semanticModelHandler.searchByElementLabel(searchTerm);
    }

    @Override
    @Operation(description = "Returns Semantic Models if the searchTerm matches a search string with additionally considering synonyms")
    public @NotNull Set<SemanticModel> searchBySynonym(@NotNull String searchTerm) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    @Operation(description = "Performs a search including synonyms, hypernyms and hyponyms")
    public @NotNull Set<SemanticModel> searchAllByLabel(@NotNull String searchTerm) {
        throw new UnsupportedOperationException("NYI");
    }
}
