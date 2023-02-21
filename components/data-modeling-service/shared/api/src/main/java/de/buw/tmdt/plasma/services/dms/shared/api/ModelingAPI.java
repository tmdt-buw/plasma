package de.buw.tmdt.plasma.services.dms.shared.api;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import de.buw.tmdt.plasma.datamodel.ModelMapping;
import de.buw.tmdt.plasma.datamodel.PositionedCombinedModelElement;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping(value = "/api/plasma-dms/modelings")
public interface ModelingAPI {

    //############################################### Schema Definition ###############################################

    @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ModelingInfo createNewModeling(
            @Nullable @RequestParam(value = "modelId", required = false) String modelId,
            @NotNull @RequestBody CombinedModel combinedModel,
            @RequestParam(defaultValue = "false") boolean performInitialModeling,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String dataId);

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ModelingInfo> listModelings();

    @PatchMapping(value = "/{modelId}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    ModelingInfo updateModeling(@NotNull @PathVariable("modelId") String modelId,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String dataId);

    @NotNull
    @GetMapping(value = "/{modelId}/import", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel copySemanticModelFromOtherModel(@NotNull @PathVariable("modelId") String modelId, @RequestParam(value = "sourceModelId") @NotNull String sourceModelId);

    @NotNull
    @GetMapping(value = "/{modelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel getCombinedModel(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @GetMapping(value = "/{modelId}/arraycontexts", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Set<String>> getArrayContexts(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @GetMapping(value = "/{modelId}/mappings", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ModelMapping> getModelMappings(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @GetMapping(value = "/{modelId}/labeling", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel performSemanticLabeling(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @GetMapping(value = "/{modelId}/modeling", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel performSemanticModeling(@NotNull @PathVariable("modelId") String modelId);

    @PatchMapping(value = "/{modelId}/positions", consumes = MediaType.APPLICATION_JSON_VALUE)
    void updatePositions(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody List<PositionedCombinedModelElement> elements);

    @NotNull
    @GetMapping(value = "/{modelId}/elements", produces = MediaType.APPLICATION_JSON_VALUE)
    List<SemanticModelNode> getElements(@NotNull @PathVariable("modelId") String modelId,
                                        @Nullable @RequestParam(value = "prefix", required = false) String prefix,
                                        @Nullable @RequestParam(value = "infix", required = false) String infix);

    @NotNull
    @GetMapping(value = "/{modelId}/relations", produces = MediaType.APPLICATION_JSON_VALUE)
    List<Relation> getRelations(@NotNull @PathVariable("modelId") String modelId,
                                @Nullable @RequestParam(value = "prefix", required = false) String prefix,
                                @Nullable @RequestParam(value = "infix", required = false) String infix);

    /* Cache operations */

    @NotNull
    @PostMapping(value = "/{modelId}/cache/nodes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModelElement cacheElement(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody SemanticModelNode node);

    @NotNull
    @PatchMapping(value = "/{modelId}/cache/nodes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModelElement updateProvisionalNode(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody SemanticModelNode node, @RequestParam(value = "oldURI", required = false) String oldURI);


    @DeleteMapping(value = "/{modelId}/cache/nodes")
    void deleteCachedElement(@NotNull @PathVariable("modelId") String modelId, @RequestParam(value = "uri") @NotNull String uri);


    @NotNull
    @PostMapping(value = "/{modelId}/cache/relations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModelElement cacheRelation(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody Relation relation);

    @NotNull
    @PatchMapping(value = "/{modelId}/cache/relations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModelElement updateProvisionalRelation(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody Relation relation, @RequestParam(value = "oldURI", required = false) String oldURI);
    
    @DeleteMapping(value = "/{modelId}/cache/relations")
    void deleteCachedRelation(@NotNull @PathVariable("modelId") String modelId, @RequestParam(value = "uri") @NotNull String uri);

    @NotNull
    @PatchMapping(value = "/{modelId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel modifyModel(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody DeltaModification delta);

    /* Recommendation */

    @NotNull
    @PostMapping(value = "/{modelId}/recommendations/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel acceptRecommendation(@NotNull @PathVariable("modelId") String modelId, @NotNull @RequestBody DeltaModification recommendation);

    /* Undo / Redo / finish */

    @NotNull
    @PostMapping(value = "/{modelId}/undo", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel undo(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @PostMapping(value = "/{modelId}/redo", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel redo(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @PostMapping(value = "/{modelId}/finalize", produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel finalizeModeling(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @PostMapping(value = "/{modelId}/clone", produces = MediaType.APPLICATION_JSON_VALUE)
    ModelingInfo cloneModeling(@NotNull @PathVariable("modelId") String modelId);

    @NotNull
    @GetMapping(value = "/finalizeAvailable", produces = MediaType.APPLICATION_JSON_VALUE)
    Boolean finalizeModelingAvailable();

    @DeleteMapping(value = "/{modelId}")
    void deleteModel(@NotNull @PathVariable("modelId") String modelId);

    @GetMapping(value = "/{modelId}/selectedOntologies", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> getSelectedOntologies(@NotNull @PathVariable("modelId") String modelId);

    @PutMapping(value = "/{modelId}/selectedOntologies")
    void updateSelectedOntologies(@NotNull @PathVariable("modelId") String modelId,
                                  @NotNull @RequestParam(value = "labels", defaultValue = "") List<String> selectedOntologyLabels);
}