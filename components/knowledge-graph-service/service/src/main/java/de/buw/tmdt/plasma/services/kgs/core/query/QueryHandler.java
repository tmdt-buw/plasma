package de.buw.tmdt.plasma.services.kgs.core.query;

import de.buw.tmdt.plasma.services.kgs.shared.converter.ModelDTOConverter;
import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriver;
import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriverFactory;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QueryHandler {

    private final GraphDBDriverFactory graphDBDriverFactory;
    private final ModelDTOConverter modelDTOConverter;

    @Autowired
    public QueryHandler(
            @NotNull GraphDBDriverFactory graphDBDriverFactory,
            @NotNull ModelDTOConverter modelDTOConverter
    ) {
        this.graphDBDriverFactory = graphDBDriverFactory;
        this.modelDTOConverter = modelDTOConverter;
    }

    @NotNull
    public Set<SemanticModelDTO> searchByEntityConcept(@NotNull String searchTerm) {
        return graphDBDriverFactory.getGraphDBDriver().searchSemanticModelsForEntityConceptMainLabel(searchTerm)
                .stream()
                .map(modelDTOConverter::mapSemanticModel)
                .collect(Collectors.toSet());
    }

    @NotNull
    public Set<SemanticModelDTO> searchByEntityType(@NotNull String searchTerm) {
        return graphDBDriverFactory.getGraphDBDriver().searchSemanticModelsForEntityType(searchTerm)
                .stream()
                .map(modelDTOConverter::mapSemanticModel)
                .collect(Collectors.toSet());
    }

    @NotNull
    public Set<SemanticModelDTO> searchBySynonym(@NotNull String searchTerm) {
        return graphDBDriverFactory.getGraphDBDriver().searchSemanticModelsForEntityConceptSynonymLabel(searchTerm)
                .stream()
                .map(modelDTOConverter::mapSemanticModel)
                .collect(Collectors.toSet());
    }

    @NotNull
    public Set<SemanticModelDTO> searchAllByLabel(@NotNull String searchTerm) {
        GraphDBDriver graphDBDriver = graphDBDriverFactory.getGraphDBDriver();
        Set<SemanticModel> allModels = new HashSet<>(graphDBDriver.searchSemanticModelsForEntityConceptAnyLabel(searchTerm));
        return allModels.stream().map(modelDTOConverter::mapSemanticModel).collect(Collectors.toSet());
    }

    @NotNull
    public Set<SemanticModelDTO> searchByEntityTypeOrEntityConceptContainingLabel(@NotNull String searchTerm) {
        return graphDBDriverFactory.getGraphDBDriver().searchSemanticModelByEntityTypeOrEntityConceptContainingLabel(searchTerm)
                .stream()
                .map(modelDTOConverter::mapSemanticModel)
                .collect(Collectors.toSet());
    }
}