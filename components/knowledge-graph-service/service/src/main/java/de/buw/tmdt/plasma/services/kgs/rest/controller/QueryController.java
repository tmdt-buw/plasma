package de.buw.tmdt.plasma.services.kgs.rest.controller;

import de.buw.tmdt.plasma.services.kgs.shared.api.QueryApi;
import de.buw.tmdt.plasma.services.kgs.core.query.QueryHandler;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class QueryController implements QueryApi {

    private final QueryHandler queryHandler;

    @Autowired
    public QueryController(@NotNull QueryHandler knowledgeGraphHandler
    ) {
        this.queryHandler = knowledgeGraphHandler;
    }

    /* ********************************************** QUERY **************************************************/

    @Override
    @Operation(description = "Returns Semantic Models if the search term matches a referenced entity concept")
    public @NotNull Set<SemanticModelDTO> searchByEntityConcept(@NotNull String searchTerm) {
        return queryHandler.searchByEntityConcept(searchTerm);
    }

    @Override
    @Operation(description = "Returns Semantic Models if the searchTerm matches a referenced entity type")
    public @NotNull Set<SemanticModelDTO> searchByEntityType(@NotNull String searchTerm) {
        return queryHandler.searchByEntityType(searchTerm);
    }

    @Override
    @Operation(description = "Returns Semantic Models if the searchTerm matches a referenced entity concept with additionally considering synonyms")
    public @NotNull Set<SemanticModelDTO> searchBySynonym(@NotNull String searchTerm) {
        return queryHandler.searchBySynonym(searchTerm);
    }

    @Override
    @Operation(description = "Performs an EntityConcept search including synonyms, hypernyms and hyponyms")
    public @NotNull Set<SemanticModelDTO> searchAllByLabel(@NotNull String searchTerm) {
        return queryHandler.searchAllByLabel(searchTerm);
    }

    @Override
    @Operation(description = "Returns Semantic Models if the searchTerm is contained in Entity Concept or Entity Type")
    public @NotNull Set<SemanticModelDTO> searchByEntityTypeOrEntityConceptContainingLabel(@NotNull String searchTerm) {
        return queryHandler.searchByEntityTypeOrEntityConceptContainingLabel(searchTerm);
    }
}