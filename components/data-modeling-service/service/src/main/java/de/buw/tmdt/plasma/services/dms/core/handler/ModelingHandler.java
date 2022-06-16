package de.buw.tmdt.plasma.services.dms.core.handler;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import de.buw.tmdt.plasma.datamodel.CombinedModelIntegrityException;
import de.buw.tmdt.plasma.datamodel.PositionedCombinedModelElement;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import de.buw.tmdt.plasma.datamodel.modification.operation.SyntacticOperationDTO;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.*;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode;
import de.buw.tmdt.plasma.services.config.ConfigServiceClient;
import de.buw.tmdt.plasma.services.dms.core.converter.SchemaOperationDTOConverter;
import de.buw.tmdt.plasma.services.dms.core.model.Modeling;
import de.buw.tmdt.plasma.services.dms.core.operations.Operation;
import de.buw.tmdt.plasma.services.dms.core.operations.OperationLookUp;
import de.buw.tmdt.plasma.services.dms.core.operations.exceptions.ParameterParsingException;
import de.buw.tmdt.plasma.services.dms.core.repository.ModelingRepository;
import de.buw.tmdt.plasma.services.kgs.shared.feignclient.OntologyApiClient;
import de.buw.tmdt.plasma.services.kgs.shared.feignclient.SemanticModelApiClient;
import de.buw.tmdt.plasma.services.srs.feignclient.SemanticRecommendationApiClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ModelingHandler {

    private static final Logger log = LoggerFactory.getLogger(ModelingHandler.class);

    private final SemanticModelApiClient semanticModelApiClient;
    private final OntologyApiClient ontologyApiClient;
    private final ModelingRepository modelingRepository;
    private final SemanticRecommendationApiClient semanticRecommendationApiClient;
    private final SchemaOperationDTOConverter schemaOperationDTOConverter;
    private final OperationLookUp operationLookUp;
    private HashMap<SchemaNode, Set<Operation.Handle>> parameterDefinitionLookup;
    private final ConfigServiceClient configServiceClient;

    @Value("${plasma.dms.feature.syntacticoperations.enabled:false}")
    private Boolean syntacticOperationsEnabled;

    @Value("${plasma.dms.feature.recommendations.enabled:false}")
    private Boolean recommendationsEnabled;

    @Autowired
    public ModelingHandler(
            @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
            @NotNull SemanticModelApiClient semanticModelApiClient,
            @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
            @NotNull OntologyApiClient ontologyApiClient,
            @NotNull ModelingRepository modelingRepository,
            @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"})
            @NotNull SemanticRecommendationApiClient semanticRecommendationApiClient,
            @NotNull SchemaOperationDTOConverter schemaOperationDTOConverter,
            @NotNull OperationLookUp operationLookUp, ConfigServiceClient configServiceClient) {
        this.semanticModelApiClient = semanticModelApiClient;
        this.ontologyApiClient = ontologyApiClient;
        this.modelingRepository = modelingRepository;
        this.semanticRecommendationApiClient = semanticRecommendationApiClient;
        this.schemaOperationDTOConverter = schemaOperationDTOConverter;
        this.operationLookUp = operationLookUp;
        this.configServiceClient = configServiceClient;
    }

    public Modeling initializeModeling(String modelId, CombinedModel combinedModel, String name, String description, String dataId) {
        Optional<Modeling> modelingResult = modelingRepository.findById(modelId);

        if (modelingResult.isPresent()) {
            log.info("Data source already exists");
            return modelingResult.get();
        }
        ZonedDateTime created = ZonedDateTime.now();
        Modeling modeling = new Modeling(modelId, name, description, created, dataId);
        // save initial version of submitted model
        modeling.pushCombinedModel(combinedModel);
        modeling = persistModeling(modeling);


        return modeling;
    }

    public CombinedModel performSemanticLabeling(String modelId) throws ResponseStatusException {
        if (!recommendationsEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Recommendations are currently disabled.");
        }
        Modeling modeling = findModelingOrThrow(modelId);
        try {
            CombinedModel labeledCombinedModel;
            labeledCombinedModel = semanticRecommendationApiClient.performSemanticLabeling(
                    modelId, null, null, modeling.getCurrentModel());
            modeling.pushCombinedModel(labeledCombinedModel);
            // store the model after recommendations have been added
            modeling = persistModeling(modeling);
        } catch (Exception e) {
            log.warn("Semantic labeling did not succeed: ", e);
            if (e instanceof FeignException.NotImplemented) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No labeling services are defined to process this request.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during labeling: " + e.getMessage());
        }
        return modeling.getCurrentModel();
    }

    public CombinedModel performSemanticModeling(String modelId) throws ResponseStatusException {
        if (!recommendationsEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Recommendations are currently disabled.");
        }
        Modeling modeling = findModelingOrThrow(modelId);
        try {
            CombinedModel modeledCombinedModel;
            modeledCombinedModel = semanticRecommendationApiClient.performSemanticModeling(
                    modelId, null, null, modeling.getCurrentModel());
            modeling.pushCombinedModel(modeledCombinedModel);
            // store the model after recommendations have been added
            modeling = persistModeling(modeling);
        } catch (FeignException e) {
            log.warn("Semantic modeling did not succeed: ", e);
            if (e instanceof FeignException.NotImplemented) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No modeling services are defined to process this request.");
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during modeling: " + e.getMessage());
        }
        return modeling.getCurrentModel();
    }

    /**
     * Copies the semantic model from the source model into the given model.
     * All uuids are generated new, all mappings or modifications are removed.
     *
     * @param modelId       The target model id
     * @param sourceModelId The source model id
     * @return The updated model
     */
    // TODO handle proper positioning of new nodes
    public CombinedModel copySemanticModelFromOtherModel(@NotNull String modelId, @NotNull String sourceModelId) {
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel model = getCombinedModel(modelId, true);
        CombinedModel sourceCM = getCombinedModel(sourceModelId);

        SemanticModel sourceSM = sourceCM.getSemanticModel();
        DeltaModification delta = new DeltaModification("clone-" + sourceModelId);
        // sanitize and clone all elements
        Map<String, String> uuidMapping = new HashMap<>(); // keep track of exchanged uuids to adjust relations later on
        sourceSM.getNodes().stream().map(node -> {
                    String newUuid = UUID.randomUUID().toString();
                    uuidMapping.put(node.getUuid(), newUuid);
                    if (node instanceof Class) {
                        Class source = (Class) node;
                        return new Class(source.getURI(), source.getLabel(),
                                source.getDescription(), source.getXCoordinate(), source.getYCoordinate(), newUuid);
                    } else if (node instanceof NamedEntity) {
                        NamedEntity source = (NamedEntity) node;
                        return new NamedEntity(source.getURI(), source.getLabel(),
                                source.getDescription(), source.getXCoordinate(), source.getYCoordinate(), newUuid);
                    } else if (node instanceof Literal) {
                        Literal source = (Literal) node;
                        return new Literal(newUuid, source.getValue(), source.getXCoordinate(), source.getYCoordinate());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(node -> delta.getEntities().add(node));
        sourceSM.getEdges().stream().map(rel -> {
                    String newUuid = UUID.randomUUID().toString();
                    uuidMapping.put(rel.getUuid(), newUuid);
                    if (rel instanceof ObjectProperty) {
                        return new ObjectProperty(newUuid, uuidMapping.get(rel.getFrom()), uuidMapping.get(rel.getTo()),
                                rel.getURI(), rel.getLabel(), rel.getDescription(), null);
                    } else if (rel instanceof DataProperty) {
                        return new DataProperty(newUuid, uuidMapping.get(rel.getFrom()), uuidMapping.get(rel.getTo()),
                                rel.getURI(), rel.getLabel(), rel.getDescription(), null);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .forEach(rel -> delta.getRelations().add(rel));

        model.apply(delta);
        modeling.pushCombinedModel(model);
        persistModeling(modeling);
        return model;
    }

    @SuppressWarnings("SameParameterValue") // this might change
    @NotNull
    private CombinedModel getCombinedModel(@NotNull String modelId, boolean modelWillBeModified) {
        Optional<Modeling> modeling = modelingRepository.findById(modelId);
        if (modeling.isPresent()) {
            try {
                return getCombinedModel(modeling.get(), modelWillBeModified);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving combined model for modeling");
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Modeling was not found");
    }

    @NotNull
    private CombinedModel getCombinedModel(@NotNull Modeling modeling, boolean modelWillBeModified) {
        CombinedModel currentModel = modeling.getCurrentModel();

        if (modelWillBeModified && currentModel.isFinalized()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Finalized models are not allowed to be modified");
        }
        return currentModel;
    }

    public @NotNull CombinedModel getCombinedModel(@NotNull String modelId) {

        return getCombinedModel(modelId, false);
    }

    /**
     * Retrieves all available {@link SemanticModelNode}s, from the KGS as well as the current model.
     *
     * @param modelId The modeling uuid
     * @param prefix  A search prefix
     * @param infix   A search infix
     * @return The resulting list of matching {@link SemanticModelNode}s
     */
    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") // stream collector does not return null
    @NotNull
    public List<SemanticModelNode> getElements(@NotNull String modelId, @Nullable String prefix, @Nullable String infix) {
        Modeling modeling = findModelingOrThrow(modelId);
        List<SemanticModelNode> availableElements;
        try {
            availableElements =
                    ontologyApiClient.getElements(modeling.getSelectedOntologies(), prefix, infix, null);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ontology service currently unavailable.");
        }

        // Retrieve the concepts that were created within this semantic model...
        CombinedModel combinedModel = getCombinedModel(findModelingOrThrow(modelId), false);
        combinedModel.getProvisionalElements().stream()
                .filter(element -> {
                    if (prefix != null && !(prefix.isBlank())) {
                        return StringUtils.startsWithIgnoreCase(element.getLabel(), prefix);
                    }
                    if (infix != null && !(infix.isBlank())) {
                        return StringUtils.containsIgnoreCase(element.getLabel(), infix);
                    }
                    return true;
                })
                // ... and add them to the ones from the KGS while ensuring that there are no conflicts
                .filter(element -> availableElements.stream().noneMatch(ae -> ae.sameAs(element)))
                .peek(element -> element.setProvisional(true))
                .forEach(availableElements::add);

        return availableElements.stream()
                .sorted(Comparator.comparing(SemanticModelNode::getLabel))
                .collect(Collectors.toList());
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") // stream collector does not return null
    @NotNull
    public List<Relation> getRelations(@NotNull String modelId, @Nullable String prefix, @Nullable String infix) {
        Modeling modeling = findModelingOrThrow(modelId);

        List<Relation> availableRelations;
        try {
            availableRelations =
                    ontologyApiClient.getRelations(modeling.getSelectedOntologies(), prefix, infix, null);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ontology service currently unavailable.");
        }

        // Retrieve the concepts that were created within this semantic model...
        CombinedModel combinedModel = getCombinedModel(findModelingOrThrow(modelId), false);
        combinedModel.getProvisionalRelations().stream()
                .filter(relation -> {
                    if (prefix != null && !(prefix.isBlank())) {
                        return StringUtils.startsWithIgnoreCase(relation.getLabel(), prefix);
                    }
                    if (infix != null && !(infix.isBlank())) {
                        return StringUtils.containsIgnoreCase(relation.getLabel(), infix);
                    }
                    return true;
                })
                // ... and add them to the ones from the KGS while ensuring that there are no conflicts
                .filter(relationConcept -> availableRelations.stream().noneMatch(ar -> ar.sameAs(relationConcept)))
                .peek(element -> element.setProvisional(true))
                .forEach(availableRelations::add);

        return availableRelations.stream()
                .sorted(Comparator.comparing(Relation::getLabel))
                .collect(Collectors.toList());
    }

    /* SYNTAX NODE OPERATIONS */

    /**
     * Updates the position of a {@link SchemaNode} in the {@link CombinedModel}.
     *
     * @param modelId        The modeling uuid identifying the {@link CombinedModel}
     * @param schemaNode     The {@link SchemaNode} to place
     * @param schemaNodeUuid The reference uuid of the existing {@link SchemaNode} to update
     * @deprecated Legacy function, use {@link #applyModification(String, DeltaModification)} instead
     */
    @Deprecated
    public void placeSchemaNode(@NotNull String modelId, @NotNull UUID schemaNodeUuid, @NotNull SchemaNode schemaNode) {
        if (!schemaNodeUuid.toString().equals(schemaNode.getUuid())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passed uuid in syntax model node is inconsistent to resource to update. "
                            + "Resource: " + schemaNodeUuid + " Payload: " + schemaNode.getUuid()
            );
        }
        if (schemaNode.getXCoordinate() == null || schemaNode.getYCoordinate() == null) {
            StringJoiner stringJoiner = new StringJoiner(" and ", "Missing ", " coordinate in SchemaNode.");
            if (schemaNode.getXCoordinate() == null) {
                stringJoiner.add("X");
            }
            if (schemaNode.getYCoordinate() == null) {
                stringJoiner.add("Y");
            }
            //noinspection HardcodedFileSeparator
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    stringJoiner.toString()
            );
        }

        updatePositions(modelId, Collections.singletonList(schemaNode));
    }

    /**
     * Apply a syntactic operation to the model.
     *
     * @param modelId               The modeling to modify
     * @param syntacticOperationDTO The operation to invoke
     * @return The updated model
     * @deprecated Legacy function, use {@link #applyModification(String, DeltaModification)} instead
     */
    @Deprecated
    public @NotNull CombinedModel modifySyntaxModel(@NotNull String modelId, @NotNull SyntacticOperationDTO syntacticOperationDTO) {
        //load referenced modeling
        Modeling modeling = findModelingOrThrow(modelId);

        CombinedModel combinedModel = getCombinedModel(modeling, true);

        //deserialize syntactic operation
        Operation.Handle handle = schemaOperationDTOConverter.fromDTO(syntacticOperationDTO);

        final CombinedModel modifiedModel;
        try {
            modifiedModel = handle.invoke(combinedModel);
        } catch (ParameterParsingException e) {
            log.warn("Failed to execute operation: {}", syntacticOperationDTO, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parametrization for operation " + syntacticOperationDTO.getName() + " was invalid.");
        }

        persistCombinedModel(modeling, modifiedModel);
        return modifiedModel;
    }

    public void prepareForFrontend(CombinedModel combinedModel) {
        if (syntacticOperationsEnabled) {
            // add syntactic operations
            parameterDefinitionLookup = operationLookUp.getOperationHandles(combinedModel);
            combinedModel.getSyntaxModel().getNodes().forEach(schemaNode -> {
                schemaNode.getOperations().clear();
                schemaNode.getOperations().addAll(getOperationDTOs(schemaNode, parameterDefinitionLookup));
            });
        }
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") // .toList() never returns null, SB cant validate
    @NotNull
    private List<SyntacticOperationDTO> getOperationDTOs(@NotNull SchemaNode node, @NotNull HashMap<SchemaNode, Set<Operation.Handle>> parameterDefinitionLookup) {
        return parameterDefinitionLookup.getOrDefault(node, new HashSet<>()).stream()
                .map(handle -> new SyntacticOperationDTO(
                        handle.getOperation().getName(),
                        handle.getOperation().getLabel(),
                        handle.getOperation().getDescription(),
                        schemaOperationDTOConverter.toDTO(handle.getParameterDefinition())
                )).collect(Collectors.toList());
    }

    /**
     * Applies all modifications stored in the modification object to the {@link CombinedModel} for the modeling
     * identified by modelId.
     *
     * @param modelId      The modeling uuid identifying the model
     * @param modification The modifications. All modifications are applied in one step
     * @return The updated {@link CombinedModel}
     */
    @NotNull
    public CombinedModel applyModification(@NotNull String modelId, @NotNull DeltaModification modification) {

        //load schema
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel combinedModel = getCombinedModel(modeling, true);

        CombinedModel clone = combinedModel.copy();
        try {
            clone.apply(modification);
        } catch (Exception e) {
            // something went wrong
            // TODO add more fine grained exceptions
            log.warn("Exception during application of modification: " + modification + ":");
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during application of modification: " + e.getMessage());
        }
        persistCombinedModel(modeling, clone);

        return clone;
    }

    /**
     * This function updates the positions of the given movedElements.
     * All other changes are ignored, only the position information is updated.
     *
     * @param modelId       The modeling uuid identifying the {@link CombinedModel} to update
     * @param movedElements A list of elements with updated positions, can be syntactic and semantic elements
     */
    public void updatePositions(@NotNull String modelId, @NotNull List<PositionedCombinedModelElement> movedElements) {
        //load referenced modeling
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel combinedModel = getCombinedModel(modeling, true);

        // create a map for quick lookups
        final Map<String, PositionedCombinedModelElement> elementLookup =
                combinedModel.getSyntaxModel().getNodes().stream()
                        .map(schemaNode -> (PositionedCombinedModelElement) schemaNode)
                        .collect(Collectors.toMap(CombinedModelElement::getUuid, Function.identity()));

        combinedModel.getSemanticModel().getNodes().stream()
                .map(semanticNode -> (PositionedCombinedModelElement) semanticNode)
                .forEach(node -> elementLookup.put(node.getUuid(), node));

        for (PositionedCombinedModelElement movedElement : movedElements) {
            PositionedCombinedModelElement current = elementLookup.get(movedElement.getUuid());
            if (current == null) {
                log.warn("Could not update position for non-existing element in CombinedModel: {}", movedElement.getUuid());
                continue;
            }
            current.setXCoordinate(movedElement.getXCoordinate());
            current.setYCoordinate(movedElement.getYCoordinate());
        }
        persistModeling(modeling);
    }


    @NotNull
    public SemanticModelNode cacheNode(@NotNull String modelId, @NotNull SemanticModelNode element) {

        element.validate();

        //load model - DON'T COPY since caching of transient concepts is not "undoable"
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel combinedModel = getCombinedModel(modeling, true);

        if (element instanceof Class || element instanceof NamedEntity) {
            final SemanticModelNode usedElement = getAndUpdateCache(
                    element,
                    SemanticModelNode::getURI,
                    combinedModel.getProvisionalElements());

            persistModeling(modeling);
            return usedElement;
        } else {
            log.info("Received uncachable type " + element);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This type cannot be cached");
        }
    }

    public void uncacheElement(@NotNull String modelId, String uri) {
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel combinedModel = getCombinedModel(modeling, true);
        boolean inUse = combinedModel.getSemanticModel().getNodes().stream().anyMatch(node -> node.getURI().equals(uri));
        if (inUse) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Element with URI " + uri + " is in use in this model. Please remove all instances before deleting.");
        }

        boolean change = combinedModel.getProvisionalElements().removeIf(o -> o.getURI().equals(uri));
        if (change) {
            persistModeling(modeling);
        }
    }

    @NotNull
    public Relation cacheRelation(@NotNull String modelId, @NotNull Relation relation) {

        relation.convertToTemplate();
        relation.validate();

        //load schema - DON'T COPY since caching of transient concepts is not "undoable"
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel combinedModel = getCombinedModel(modeling, true);
        //compute entity concept
        final Relation usedRelation = getAndUpdateCache(
                relation,
                Relation::getUuid,
                combinedModel.getProvisionalRelations()
        );
        persistModeling(modeling);

        return usedRelation;

    }

    public void uncacheRelation(@NotNull String modelId, String uri) {
        Modeling modeling = findModelingOrThrow(modelId);
        CombinedModel combinedModel = getCombinedModel(modeling, true);

        boolean inUse = combinedModel.getSemanticModel().getEdges().stream().anyMatch(rel -> rel.getURI().equals(uri));
        if (inUse) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Element with URI " + uri + " is in use in this model. Please remove all instances before deleting.");
        }

        boolean change = combinedModel.getProvisionalRelations().removeIf(o -> o.getURI().equals(uri));
        if (change) {
            persistModeling(modeling);
        }
    }

    /**
     * NOTE: this has side effects on the passed list
     * Finds the entry in the cache which is equivalent to {@code element} concerning it's identity function and returns it. If there is none it uses the
     * passed element and adds it to the cache. <i>Equivalence</i> of two elements {@code x} and {@code y} here means:
     * {@code identityFunction.apply(x).equals(identityFunction.apply(y)}
     *
     * @param element          the entry which is looked for
     * @param identityFunction the function which maps an element to it's {@link UUID} (usually just a method reference to T::getUuid)
     * @param elementCache     the cache of known entries
     * @return an entity concept which is equivalent to {@code entityConcept}
     */
    @NotNull
    private <T extends CombinedModelElement> T getAndUpdateCache(
            @NotNull T element,
            @NotNull Function<T, String> identityFunction,
            @NotNull List<T> elementCache
    ) {
        final T result;
        String uuid = identityFunction.apply(element);
        //if uuid exists see if one exists in the cache all-ready
        if (uuid != null) {
            Optional<T> cachedEntityConcept = elementCache.stream()
                    .filter(ec -> identityFunction.apply(ec) != null && uuid.equals(identityFunction.apply(ec)))
                    .findAny();
            result = cachedEntityConcept.orElse(element);
            if (cachedEntityConcept.isEmpty()) {
                elementCache.add(result);
            }
        } else {
            result = element;
            elementCache.add(result);
        }

        return result;
    }

    /* UNDO / REDO Functions */

    public @NotNull CombinedModel undoModelModification(@NotNull String modelId) {
        //Get the modeling from the repository
        Modeling modeling = findModelingOrThrow(modelId);
        try {
            modeling.popCombinedModel();
        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
        persistModeling(modeling);
        return modeling.getCurrentModel();
    }

    public @NotNull CombinedModel redoModelModification(@NotNull String modelId) {
        //Get the modeling from the repository
        Modeling modeling = findModelingOrThrow(modelId);
        try {
            modeling.restoreCombinedModel();
        } catch (UnsupportedOperationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }

        persistModeling(modeling);
        return modeling.getCurrentModel();
    }

    /* Finish operations */

    /**
     * Finishes the modeling of the given modeling.
     * The semantic model is persisted in the KGS.
     * Provisional concepts are dropped and recommendations removed.
     * After calling this function, the {@link CombinedModel} of the modeling cannot be changed as it is set to finalized.
     * The given {@link CombinedModel} is cloned first, so the change is reversible as a new instance is returned.
     *
     * @param modelId The id of the modeling to finalize
     * @return An updated model instance with the new persistence ids (entityIds) included
     */
    @NotNull
    @Transactional
    public CombinedModel finalizeModeling(@NotNull String modelId) {
        //Load the modeling from the repository.
        Modeling modeling = modelingRepository.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Data Source does not exists"
                ));

        CombinedModel combinedModel = getCombinedModel(modeling, false);

        combinedModel.validate();

        try {
            combinedModel = createSemanticModel(combinedModel, modelId);
        } catch (CombinedModelIntegrityException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred during conversion.");
        }
        // clear provisional elements and recommendations
        combinedModel.getRecommendations().clear();
        combinedModel.getProvisionalRelations().clear();
        combinedModel.getProvisionalElements().clear();
        combinedModel.getSyntaxModel().getNodes().forEach(schemaNode -> schemaNode.getOperations().clear());

        // prevent any further modifications
        combinedModel.finalizeModel();

        modeling.pushCombinedModel(combinedModel);
        Modeling finalDataSource = modelingRepository.save(modeling);
        return finalDataSource.getCurrentModel();
    }

    /**
     * Sends the {@link SemanticModel} of the given {@link CombinedModel} to the KGS to persist it.
     * ID mappings (the KGS does not use our UUIDs) is done in this function, including the reverse mapping once
     * new KGS ids are generated. Those are then stored in the entityId field.
     * Equal concepts used by multiple entities or relations are merged during this process to not clutter the KG.
     * The given {@link CombinedModel} is cloned first, so the change is reversible as a new instance is returned.
     *
     * @param combinedModel The combined model containing the {@link SemanticModel}
     * @param modelId       The modeling uuid
     * @return The updated {@link CombinedModel}
     * @throws CombinedModelIntegrityException If the integrity of the {@link CombinedModel} is violated during the concept merge
     */
    @NotNull
    private CombinedModel createSemanticModel(@NotNull CombinedModel combinedModel, @NotNull String modelId) throws CombinedModelIntegrityException {

        combinedModel = combinedModel.copy();
        SemanticModel semanticModel = combinedModel.getSemanticModel();

        //Ensure that all instances have a unique name
        // TODO remove once not necessary any more (currently is for export in platform)
        Map<String, SemanticModelNode> duplicateCheck = new HashMap<>();
        for (SemanticModelNode node : semanticModel.getNodes()) {
            if (node instanceof Class) {
                Instance instance = ((Class) node).getInstance();
                if (instance != null) {
                    if (duplicateCheck.containsKey(instance.getLabel())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Instance labels must be unique");
                    }
                    duplicateCheck.put(node.getLabel(), node);
                }
            }
        }

        //send request
        final SemanticModel response;
        try {
            log.info("Creating Semantic Model");
            if (semanticModel.getId() == null) {
                // just use the one from the combined model
                semanticModel.setId(modelId);
            }
            response = semanticModelApiClient.createSemanticModel(semanticModel);
            log.info("Created Semantic Model");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error when creating semantic model in the KGS", e);
        }

        combinedModel = new CombinedModel(modelId, combinedModel.getSyntaxModel(), response, null, true);

        return combinedModel;
    }

    @Transactional
    public void deleteModel(@NotNull String modelId) {
        Optional<Modeling> possibleSchema = modelingRepository.findById(modelId);
        if (possibleSchema.isPresent()) {
            modelingRepository.delete(possibleSchema.get());
        } else {
            log.warn("Model with id {} was not available", modelId);
        }
    }

    @SuppressWarnings("unused")
    @Transactional
    void persistCombinedModel(@NotNull String modelId, @NotNull CombinedModel combinedModel) {
        Modeling modeling = findModelingOrThrow(modelId);
        persistCombinedModel(modeling, combinedModel);
    }

    @Transactional
    void persistCombinedModel(@NotNull Modeling modeling, @NotNull CombinedModel combinedModel) {
        modeling.pushCombinedModel(combinedModel);
        modelingRepository.save(modeling);
    }

    @Transactional
    Modeling persistModeling(@NotNull Modeling modeling) {
        return modelingRepository.save(modeling);
    }

    @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION") // throws Exception if null
    @NotNull
    private Modeling findModelingOrThrow(@NotNull String modelId) {
        return modelingRepository.findById(modelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid modeling id: " + modelId));
    }

    @NotNull
    public CombinedModel requestRecommendations(@NotNull String modelId, @NotNull CombinedModel combinedModel) {
        if (recommendationsEnabled) {
            try {
                return semanticRecommendationApiClient.performSemanticRefinement(modelId, null, null, combinedModel);
            } catch (Exception e) {
                log.warn("Could not reach SRS for recommendations");
            }
        }
        return combinedModel;
    }

    public List<String> getSelectedOntologies(@NotNull String modelId) {
        Modeling modeling = findModelingOrThrow(modelId);
        return modeling.getSelectedOntologies();
    }

    public void updateSelectedOntologies(@NotNull String modelId, @NotNull List<String> selectedOntologies) {
        Modeling modeling = findModelingOrThrow(modelId);
        modeling.getSelectedOntologies().clear();
        modeling.getSelectedOntologies().addAll(selectedOntologies);
        persistModeling(modeling);
    }

}
