package de.buw.tmdt.plasma.services.dms.rest.controller;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import de.buw.tmdt.plasma.datamodel.ModelMapping;
import de.buw.tmdt.plasma.datamodel.PositionedCombinedModelElement;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import de.buw.tmdt.plasma.services.dms.core.handler.ModelingHandler;
import de.buw.tmdt.plasma.services.dms.core.model.Modeling;
import de.buw.tmdt.plasma.services.dms.core.repository.ModelingRepository;
import de.buw.tmdt.plasma.services.dms.shared.api.ModelingAPI;
import de.buw.tmdt.plasma.services.dms.shared.api.ModelingInfo;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController("Modeling Controller")
public class ModelingController implements ModelingAPI {

    private final Logger log = LoggerFactory.getLogger(ModelingController.class);

    private final ModelingHandler modelingHandler;
    private final ModelingRepository modelingRepository;

    @Value("${plasma.dms.feature.finalizemodels.enabled:true}")
    private Boolean finalizeModelsEnabled;

    @Autowired
    public ModelingController(
            @NotNull ModelingHandler modelingHandler, ModelingRepository modelingRepository) {
        this.modelingHandler = modelingHandler;
        this.modelingRepository = modelingRepository;
    }

    @Override
    @Operation(description = "Initializes a new modeling.")
    public ModelingInfo createNewModeling(@Nullable String modelId,
                                          @NotNull CombinedModel combinedModel,
                                          boolean performInitialModeling,
                                          String name,
                                          String description,
                                          String dataId) {
        if (modelId == null) {
            modelId = UUID.randomUUID().toString();
        }
        Modeling modeling = modelingHandler.initializeModeling(modelId, combinedModel, name, description, dataId);
        if (performInitialModeling) {
            try {
                modelingHandler.performSemanticLabeling(modelId);
                modelingHandler.performSemanticModeling(modelId);
            } catch (ResponseStatusException ignore) {
                // do nothing here as we still want to return the model
            }
        }
        return new ModelingInfo(
                modeling.getId(),
                modeling.getName(),
                modeling.getDescription(),
                modeling.getCreated(),
                modeling.getDataId(),
                false);
    }

    @Override
    @Operation(description = "Get all modelings.")
    public List<ModelingInfo> listModelings() {
        return modelingRepository.findAll().stream()
                .map(modeling -> new ModelingInfo(
                        modeling.getId(),
                        modeling.getName(),
                        modeling.getDescription(),
                        modeling.getCreated(),
                        modeling.getDataId(),
                        modeling.isFinalized()))
                .sorted(Comparator.comparing(ModelingInfo::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Operation(description = "Update secondary information of a modeling.")
    public ModelingInfo updateModeling(@NotNull String modelId, @Nullable String name, @Nullable String description, @Nullable String dataId) {
        Modeling updated = modelingHandler.updateModeling(modelId, name, description, dataId);
        return new ModelingInfo(
                updated.getId(),
                updated.getName(),
                updated.getDescription(),
                updated.getCreated(),
                updated.getDataId(),
                updated.getCurrentModel().isFinalized());
    }

    @Override
    public @NotNull CombinedModel performSemanticLabeling(@NotNull String modelId) {
        return modelingHandler.performSemanticLabeling(modelId);
    }

    @Override
    public @NotNull CombinedModel performSemanticModeling(@NotNull String modelId) {
        return modelingHandler.performSemanticModeling(modelId);
    }

    @Override
    @Operation(description = "Copy the semantic model from source model to target model")
    public @NotNull CombinedModel copySemanticModelFromOtherModel(@NotNull String modelId, @NotNull String sourceModelId) {
        return modelingHandler.copySemanticModelFromOtherModel(modelId, sourceModelId);
    }

    @NotNull
    @Override
    @Operation(description = "Returns the current combined model of the specified modeling.")
    public CombinedModel getCombinedModel(@NotNull String modelId) {
        CombinedModel combinedModel = modelingHandler.getCombinedModel(modelId);
        if (!combinedModel.isFinalized()) {
            combinedModel = modelingHandler.requestRecommendations(modelId, combinedModel);
        }
        modelingHandler.prepareForFrontend(combinedModel);
        return combinedModel;
    }

    @NotNull
    @Override
    @Operation(description = "Requests all array context represented by element ids (nodes and edges) for the given model")
    public List<Set<String>> getArrayContexts(@NotNull String modelId) {
        return modelingHandler.getArrayContexts(modelId);
    }

    @NotNull
    @Override
    @Operation(description = "Requests all model mappings for the given model")
    public List<ModelMapping> getModelMappings(@NotNull String modelId) {
        return modelingHandler.getModelMappings(modelId);
    }

    @Override
    @Operation(description = "Stores the position of all provided elements.")
    public void updatePositions(@NotNull String modelId, @NotNull List<PositionedCombinedModelElement> elements) {
        modelingHandler.updatePositions(modelId, elements);
    }

    @NotNull
    @Override
    @Operation(description = "Returns all elements either persistent in the knowledge base or cached for the referenced model sorted alphabetically.")
    public List<SemanticModelNode> getElements(@NotNull String modelId, @Nullable String prefix, @Nullable String infix) {
        return modelingHandler.getElements(modelId, prefix, infix);
    }

    @NotNull
    @Override
    @Operation(description = "Returns all relations either persistent in the knowledge base or cached for the referenced model sorted alphabetically.")
    public List<Relation> getRelations(@NotNull String modelId, @Nullable String prefix, @Nullable String infix) {
        return modelingHandler.getRelations(modelId, prefix, infix);
    }

    @NotNull
    @Override
    @Operation(description = "Adds a new provisional node to the cache of the modeling.")
    public CombinedModelElement cacheElement(@NotNull String modelingId, @NotNull SemanticModelNode node) {
        return modelingHandler.cacheNode(modelingId, node);
    }

    @NotNull
    @Override
    @Operation(description = "Updates provisional node in the cache of the modeling.")
    public CombinedModelElement updateProvisionalNode(@NotNull String modelingId, @NotNull SemanticModelNode node, @Nullable String oldURI) {
        return modelingHandler.updateCachedNode(modelingId, node, oldURI);
    }

    @Override
    @Operation(description = "Removes a cached element identified by the given URI")
    public void deleteCachedElement(@NotNull String modelId, @NotNull String uri) {
        modelingHandler.uncacheNode(modelId, uri);
    }


    @NotNull
    @Override
    @Operation(description = "Adds a new provisional relation to the cache of the modeling.")
    public CombinedModelElement cacheRelation(@NotNull String modelingId, @NotNull Relation relation) {
        return modelingHandler.cacheRelation(modelingId, relation);
    }


    @NotNull
    @Override
    @Operation(description = "Updates provisional relation in the cache of the modeling.")
    public CombinedModelElement updateProvisionalRelation(@NotNull String modelingId, @NotNull Relation relation, @Nullable String oldURI) {
        return modelingHandler.updateCachedRelation(modelingId, relation, oldURI);
    }


    @Override
    @Operation(description = "Removes a cached relation identified by the given URI")
    public void deleteCachedRelation(@NotNull String modelId, @NotNull String uri) {
        modelingHandler.uncacheRelation(modelId, uri);
    }

    @NotNull
    @Operation(description = "Accepts a recommendation and adds the contents to the semantic model.")
    public CombinedModel acceptRecommendation(@NotNull String modelId, @NotNull DeltaModification recommendation) {
        @NotNull CombinedModel model = modelingHandler.applyModification(modelId, recommendation);

        modelingHandler.prepareForFrontend(model);
        return model;
    }

    @NotNull
    @Operation(description = "Performs a modification of the CombinedModel.")
    public CombinedModel modifyModel(@NotNull String modelId, @NotNull DeltaModification recommendation) {
        @NotNull CombinedModel model = modelingHandler.applyModification(modelId, recommendation);

        modelingHandler.prepareForFrontend(model);
        return model;
    }

    @NotNull
    @Override
    @Operation(description = "pop last element from model stack")
    public CombinedModel undo(@NotNull String uuid) {
        CombinedModel combinedModel = modelingHandler.undoModelModification(uuid);
        modelingHandler.prepareForFrontend(combinedModel);
        return combinedModel;
    }

    @NotNull
    @Override
    @Operation(description = "push last element from model stack")
    public CombinedModel redo(@NotNull String uuid) {
        CombinedModel combinedModel = modelingHandler.redoModelModification(uuid);
        modelingHandler.prepareForFrontend(combinedModel);
        return combinedModel;
    }

    @NotNull
    @Override
    @Operation(description = "End modelling process and finalize model.")
    public CombinedModel finalizeModeling(@NotNull String modelId) {
        return modelingHandler.finalizeModeling(modelId);
    }

    @NotNull
    @Override
    @Operation(description = "Clone an existing modeling.")
    public ModelingInfo cloneModeling(@NotNull String modelId) {
        Modeling modeling = modelingHandler.cloneModeling(modelId);
        return new ModelingInfo(
                modeling.getId(),
                modeling.getName(),
                modeling.getDescription(),
                modeling.getCreated(),
                modeling.getDataId(),
                false);
    }

    @NotNull
    @Override
    @Operation(description = "Check if a model may be finalized. This can be disabled in the DMS config.")
    public Boolean finalizeModelingAvailable() {
        return finalizeModelsEnabled;
    }

    @Override
    @Operation(description = "Deletes an existing modeling")
    public void deleteModel(@NotNull String modelId) {
        modelingHandler.deleteModel(modelId);
    }

    @Override
    public List<String> getSelectedOntologies(@NotNull String modelId) {
        return modelingHandler.getSelectedOntologies(modelId);
    }

    @Override
    public void updateSelectedOntologies(@NotNull String modelId, @NotNull List<String> selectedOntologyLabels) {
        modelingHandler.updateSelectedOntologies(modelId, selectedOntologyLabels);
    }
}