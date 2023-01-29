package de.buw.tmdt.plasma.services.dps.conversion.rdf;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Literal;
import de.buw.tmdt.plasma.datamodel.semanticmodel.*;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.*;
import de.buw.tmdt.plasma.services.dps.conversion.ConversionException;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.stream.Collectors;

import static de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode.ARRAY_PATH_TOKEN;
import static de.buw.tmdt.plasma.datamodel.syntaxmodel.SchemaNode.PREDEFINED_LABELS;

/**
 * Converter to parse a {@link SemanticModel} with filled {@link Literal}s and convert it to RDF.
 */
public class RDFConverter {

    public static final Logger log = LoggerFactory.getLogger(RDFConverter.class);

    private static final int NODE_LABEL_LENGTH = 254;
    private static final String ROOT_ELEMENT_NAME = "";

    private static final String MISSING_DATA_LITERAL = "##ValueNotAvailable##";


    private final PrefixMapping prefixes;

    /* Runtime resources.
     * Those are reset for every datum to parse.
     */
    private CombinedModel combinedModel;
    private JsonNode datum;
    private Map<String, RDFNode> uuidtoResource = new HashMap<>();
    private Model m;
    private String smURI;
    private String plasmaURI;
    private Property hasValueProperty;
    private String semanticModelId;
    private Map<SemanticModelNode, List<RDFNode>> arrayExpansionMap = new HashMap<>();
    private Map<String, RDFNode> deepListMap = new HashMap<>();
    private int counter = 0;
    private boolean dirty = false;
    List<SemanticModelNode> unprocessedNodes;
    List<Relation> unprocessedEdges;
    Set<Relation> processed;

    /**
     * Initialize a new {@link RDFConverter}.
     */
    public RDFConverter(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

    public void reset() {
        uuidtoResource = new HashMap<>();
        arrayExpansionMap = new HashMap<>();
        semanticModelId = null;
        combinedModel = null;
        datum = null;
        dirty = false;
    }

    private void validate() {
        // TODO
    }

    private int getCounter() {
        return counter++;
    }

    public List<Model> convertToRDF(CombinedModel combinedModel, List<JsonNode> data) throws ConversionException {
        return convertToRDF(combinedModel, data, null);
    }

    public List<Model> convertToRDF(CombinedModel combinedModel, List<JsonNode> data, String relativePath) throws ConversionException {
        List<Model> results = new ArrayList<>(data.size());
        for (JsonNode datum : data) {
            results.add(convertToRDF(combinedModel, datum, relativePath));
        }
        return results;
    }

    public Model convertToRDF(CombinedModel combinedModel, JsonNode datum) throws ConversionException {
        return convertToRDF(combinedModel, datum, null);
    }

    public Model convertToRDF(CombinedModel combinedModel, JsonNode datum, String relativePath) throws ConversionException {
        SemanticModel semanticModel = combinedModel.getSemanticModel();
        if (dirty) {
            reset();
        }
        if (relativePath == null) {
            relativePath = "";
        }
        dirty = true;
        this.combinedModel = combinedModel;
        this.datum = datum;

        semanticModelId = semanticModel.getId().substring(0, 13);
        m = ModelFactory.createDefaultModel();
        m.setNsPrefixes(prefixes);
        smURI = prefixes.getNsPrefixURI("plsm");
        plasmaURI = prefixes.getNsPrefixURI("plasma");
        hasValueProperty = m.createProperty(plasmaURI, "hasValue");

        unprocessedNodes = new ArrayList<>(combinedModel.getSemanticModel().getNodes());
        unprocessedEdges = new ArrayList<>(combinedModel.getSemanticModel().getEdges());

        // process primitive arrays

        // process serialized arrays

        // identify array contexts
        List<SemanticModel> arrayContexts = new ArrayList<>();
        for (SemanticModelNode unprocessedNode : unprocessedNodes) {
            // check that this node is not part of any existing array context
            if (arrayContexts.stream()
                    .flatMap(ic -> ic.getNodes().stream())
                    .anyMatch(node -> node.getUuid().equals(unprocessedNode.getUuid()))) {
                continue;
            }
            SemanticModel arrayContext = combinedModel.getSemanticModel().getArrayContext(unprocessedNode, new ArrayList<>());
            if (arrayContext.getNodes().size() == 1) {
                // check if this node is actually mapped below an array
                SemanticModelNode semanticModelNode = arrayContext.getNodes().get(0);
                if (!semanticModelNode.isMapped()) {
                    continue;
                }
                MappableSemanticModelNode mappedNode = (MappableSemanticModelNode) semanticModelNode;
                SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(mappedNode.getMappedSyntaxNodeUuid());
                if (!schemaNode.getPath().contains(ARRAY_PATH_TOKEN)) {
                    continue;
                }
            }

            // heck homogeneity of each array context (same level)
            for (Relation r : arrayContext.getEdges()) {
                SemanticModelNode from = semanticModel.getNode(r.getFrom());
                SemanticModelNode to = semanticModel.getNode(r.getTo());
                int fromLevel = combinedModel.getArrayDepthOfNode(from);
                int toLevel = combinedModel.getArrayDepthOfNode(to);
                if (fromLevel > 0 && toLevel > 0 && fromLevel != toLevel) {
                    throw new ConversionException("Found array context spanning multiple array depths");
                }
                // reject all models with array context depth > 1 (remove when #68 is solved)
                if (fromLevel > 1 || toLevel > 1) {
                    throw new ConversionException("Array contexts of depth greater than 1 are not yet supported");
                }
            }
            arrayContexts.add(arrayContext);
        }
        arrayContexts = arrayContexts.stream().sorted(Comparator.comparing(ac ->
                                ac.getNodes().stream()
                                        .filter(SemanticModelNode::isMapped)
                                        .map(node -> (MappableSemanticModelNode) node)
                                        .map(node -> combinedModel.getSyntaxModel().getNode(node.getMappedSyntaxNodeUuid()))
                                        .map(snode -> snode.getPath().stream()
                                                .filter(token -> token.equals(ARRAY_PATH_TOKEN))
                                                .count())
                                        .findFirst().orElseThrow()
                        )
                )
                .collect(Collectors.toList());
        Collections.reverse(arrayContexts);

        for (SemanticModel arrayContext : arrayContexts) {
            MappableSemanticModelNode indexNode;
            if (arrayContext.getNodes().size() == 1) {
                indexNode = (MappableSemanticModelNode) arrayContext.getNodes().get(0);
            } else {
                // identify the index node
                List<MappableSemanticModelNode> indexNodes = arrayContext.getNodes().stream()
                        .filter(smn -> smn instanceof MappableSemanticModelNode)
                        .filter(SemanticModelNode::isMapped)
                        .map(smn -> (MappableSemanticModelNode) smn)
                        .filter(smn -> smn.getMappedSyntaxNodePath().endsWith(ARRAY_PATH_TOKEN))
                        .collect(Collectors.toList());
                if (indexNodes.size() != 1) {
                    throw new ConversionException(
                            "Could not identify unanimous index node for array context containing node " +
                                    arrayContext.getNodes().get(0).getUuid() +
                                    " (" + arrayContext.getNodes().get(0).getURI() + ")");
                }
                indexNode = indexNodes.get(0);
            }
            // identify the array json node
            SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(indexNode.getMappedSyntaxNodeUuid());
            JsonPointer tmpPointer = JsonPointer.compile(schemaNode.getPathAsJSONPointer());
            JsonPointer arrayPointer = null;
            JsonPointer tailPointer = tmpPointer;
            while (arrayPointer == null) {
                JsonNode jsonNode = datum.at(tmpPointer);
                if (jsonNode.isArray()) {
                    arrayPointer = tmpPointer;
                    continue;
                }
                if (tmpPointer.toString().equals("/")) {
                    throw new ConversionException(
                            "Could not identify array node for array context containing node " +
                                    arrayContext.getNodes().get(0).getUuid() +
                                    " (" + arrayContext.getNodes().get(0).getURI() + ")");
                }
                tailPointer = tmpPointer;
                tmpPointer = tmpPointer.head();
            }

            JsonNode arrayNode = datum.at(arrayPointer);
            if (!arrayNode.isArray()) {
                throw new ConversionException("Node " + arrayPointer + " is not an array!");
            }

            RDFList list = buildRDFListForArrayContext(indexNode, arrayContext);
            uuidtoResource.put(indexNode.getUuid(), list);
        }

        // process the remaining nodes and edges
        for (SemanticModelNode node : unprocessedNodes) {
            convertNodeToRDF(node, false);
        }
        for (Relation r : unprocessedEdges) {
            SemanticModelNode from = semanticModel.getNode(r.getFrom());
            SemanticModelNode to = semanticModel.getNode(r.getTo());
            RDFNode fromNode = uuidtoResource.get(from.getUuid());
            RDFNode toNode = uuidtoResource.get(to.getUuid());
            List<RDFNode> headNodes = List.of(toNode);
            List<RDFNode> tailNodes = List.of(fromNode);
            if (!(toNode instanceof RDFList) && arrayExpansionMap.containsKey(to)) {
                headNodes = arrayExpansionMap.get(to);
            }
            if (arrayExpansionMap.containsKey(from)) {
                tailNodes = arrayExpansionMap.get(from);
            }
            for (RDFNode tailNode : tailNodes) {
                for (RDFNode headNode : headNodes) {
                    tailNode.asResource().addProperty(m.createProperty(m.expandPrefix(r.getURI())), headNode);
                }
            }
        }
        return m;
    }

    private RDFNode convertNodeToRDF(SemanticModelNode node, boolean force) throws ConversionException {
        if (uuidtoResource.containsKey(node.getUuid()) && !force) {
            return null;
        }
        RDFNode rdfNode = null;
        if (node instanceof Class) {
            Class clazz = (Class) node;
            if (clazz.isMapped()) {
                String syntaxNodeUuid = clazz.getMappedSyntaxNodeUuid();
                RDFNode resultNode;

                SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(syntaxNodeUuid);
                List<String> nodePath = schemaNode.getPath();
                if (ARRAY_PATH_TOKEN.equals(nodePath.get(nodePath.size() - 1))) {
                    // this is a child node of an array, refer back to parent node to build list
                    JsonNode arrayNode = datum.at(JsonPointer.compile(schemaNode.getPathAsJSONPointer()).head());
                    if (!(arrayNode instanceof ArrayNode)) {
                        throw new ConversionException("Node " + JsonPointer.compile(schemaNode.getPathAsJSONPointer()).head() + " is not an array node!");
                    }
                    RDFList list = buildRDFListFromArray(arrayNode, clazz, JsonPointer.compile(schemaNode.getPathAsJSONPointer()).head().toString());
                    arrayExpansionMap.put(clazz, list.asJavaList());
                    resultNode = list;
                } else if (schemaNode instanceof SetNode) {
                    JsonNode arrayNode = datum.at(JsonPointer.compile(schemaNode.getPathAsJSONPointer()));
                    if (!(arrayNode instanceof ArrayNode)) {
                        throw new ConversionException("Node " + JsonPointer.compile(schemaNode.getPathAsJSONPointer()) + " is not an array node!");
                    }
                    String value = readJSONArray((ArrayNode) arrayNode);
                    resultNode = m.createLiteral(value);
                } else {
                    //String value = uuidValueMap.get(syntaxNodeUuid);
                    resultNode = convertClassNodeToRDF(clazz);
                }
                rdfNode = resultNode;
            } else {
                // not mapped
                Resource resource = convertClassNodeToRDF(clazz);
                rdfNode = resource;
            }
        } else if (node instanceof Literal) {
            Literal l = (Literal) node;
            rdfNode = convertLiteralNodeToRDF(l);
        } else if (node instanceof NamedEntity) {
            NamedEntity ne = (NamedEntity) node;
            // create a resource which describes the named entity
            // this will later be elevated to the ontology
            Resource neResource = m.createResource(m.expandPrefix(ne.getURI()));
            neResource.addProperty(RDF.type, PLCM.NamedEntity);

            Resource resource = m.createResource();
            resource.addProperty(RDF.type, neResource);
            // use the b-node as reference
            rdfNode = resource;
        }
        uuidtoResource.put(node.getUuid(), rdfNode);
        return rdfNode;
    }

    private RDFList buildRDFListForArrayContext(MappableSemanticModelNode indexNode, SemanticModel arrayContext) throws ConversionException {
        MappableSyntaxNode syntaxNode = findMappedSyntaxNode(indexNode);
        ArrayList<String> mappedSyntaxNodePath = new ArrayList<>(syntaxNode.getPath());

        PointerFactory pf = new PointerFactory(mappedSyntaxNodePath);
        processed = new HashSet<>();

        // for each data point in the array node, a new set of rdf entities has to be generated
        List<RDFList> list = new ArrayList<>();
        JsonPointer peekPointer = JsonPointer.compile(pf.composePath());
        JsonNode peekNode = datum.at(peekPointer);
        while (!peekNode.isMissingNode()) {
            list.add(iterateArray(pf, 0, indexNode, arrayContext));
            pf.increaseIndex(0);
            peekPointer = JsonPointer.compile(pf.composePath());
            peekNode = datum.at(peekPointer);
        }
        unprocessedEdges.removeAll(processed);
        return list.get(0);
    }

    private RDFList iterateArray(PointerFactory pf, int level, MappableSemanticModelNode indexNode, SemanticModel arrayContext) throws ConversionException {
        if (level >= pf.getDepth()) {
            // process
            String originPointer = pf.composePath();
            JsonPointer peekPointer = JsonPointer.compile(pf.composePath());
            JsonNode peekNode = datum.at(peekPointer);
            List<RDFNode> nodes = new ArrayList<>();

            while (!peekNode.isMissingNode()) {
                JsonNode entry = datum.at(JsonPointer.compile(pf.composePath()));
                RDFNode rdfIndexNode = processArrayContext(arrayContext, indexNode, pf.getOriginalPath(), pf.composePath(), entry);
                nodes.add(rdfIndexNode);
                pf.increaseIndex(level);
                peekPointer = JsonPointer.compile(pf.composePath());
                peekNode = datum.at(peekPointer);
            }
            RDFList list = m.createList(nodes.iterator());
            deepListMap.put(originPointer, list);
            return list;
        } else {
            return iterateArray(pf, level + 1, indexNode, arrayContext);
        }
    }


    private MappableSyntaxNode findMappedSyntaxNode(MappableSemanticModelNode node) {
        return combinedModel.getSyntaxModel().getNodes().stream()
                .filter(n -> n.getUuid().equals(node.getMappedSyntaxNodeUuid()))
                .map(n -> (MappableSyntaxNode) n)
                .findFirst().orElseThrow();
    }

    private RDFNode processArrayContext(SemanticModel arrayContext, MappableSemanticModelNode indexNode,
                                        String basePath, String currentPath, JsonNode entry) throws ConversionException {
        RDFNode rdfIndexNode = null;
        for (SemanticModelNode node : arrayContext.getNodes()) {
            RDFNode rdfNode = convertNodeInArrayContextToRDF(node, entry, basePath);
            if (node.equals(indexNode)) {
                rdfIndexNode = rdfNode;
            }
            if (!arrayExpansionMap.containsKey(node)) {
                arrayExpansionMap.put(node, new ArrayList<>());
            }
            arrayExpansionMap.get(node).add(rdfNode);
            unprocessedNodes.remove(node);
        }
        for (Relation r : arrayContext.getEdges()) {
            SemanticModelNode from = arrayContext.getNode(r.getFrom());
            SemanticModelNode to = arrayContext.getNode(r.getTo());
            RDFNode fromNode = uuidtoResource.get(from.getUuid());
            RDFNode toNode = uuidtoResource.get(to.getUuid());
            List<RDFNode> headNodes = List.of(toNode);
            List<RDFNode> tailNodes = List.of(fromNode);

            if (!arrayContext.getNodes().contains(to) && arrayExpansionMap.containsKey(to)) {
                headNodes = arrayExpansionMap.get(to);
            }
            if (!arrayContext.getNodes().contains(from) && arrayExpansionMap.containsKey(from)) {
                tailNodes = arrayExpansionMap.get(from);
            }
            for (RDFNode tailNode : tailNodes) {
                for (RDFNode headNode : headNodes) {
                    tailNode.asResource().addProperty(m.createProperty(m.expandPrefix(r.getURI())), headNode);
                }
            }
            unprocessedEdges.remove(r);
        }
        // search outside of array context

        for (Relation r : unprocessedEdges) {
            SemanticModelNode from = combinedModel.getSemanticModel().getNode(r.getFrom());
            SemanticModelNode to = combinedModel.getSemanticModel().getNode(r.getTo());
            RDFNode fromNode = uuidtoResource.get(from.getUuid());
            RDFNode toNode = uuidtoResource.get(to.getUuid());
            if (fromNode == null || toNode == null) {
                continue;
            }
            for (Map.Entry<String, RDFNode> listmapEntry : deepListMap.entrySet()) {
                if (listmapEntry.getKey().startsWith(currentPath)) {
                    RDFNode headNode = listmapEntry.getValue();
                    fromNode.asResource().addProperty(m.createProperty(m.expandPrefix(r.getURI())), headNode);
                    processed.add(r);
                }
            }
        }

        return rdfIndexNode;
    }

    private RDFNode convertNodeInArrayContextToRDF(SemanticModelNode node, JsonNode datum, String relativePath) throws ConversionException {
        RDFNode rdfNode = null;
        if (node instanceof Class) {
            Class clazz = (Class) node;
            if (clazz.isMapped()) {
                String syntaxNodeUuid = clazz.getMappedSyntaxNodeUuid();
                RDFNode resultNode;
                SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(syntaxNodeUuid);


                /*
                 List<String> nodePath = schemaNode.getPath();
                if (ARRAY_ELEMENT_NAME.equals(nodePath.get(nodePath.size() - 1))) {
                    // this is a child node of an array, refer back to parent node to build list
                    JsonNode arrayNode = datum.at(JsonPointer.compile(schemaNode.getPathAsJSONPointer()).head());
                    if (!(arrayNode instanceof ArrayNode)) {
                        throw new ConversionException("Node " + JsonPointer.compile(schemaNode.getPathAsJSONPointer()).head() + " is not an array node!");
                    }
                    RDFList list = buildRDFListFromArray(arrayNode, clazz, JsonPointer.compile(schemaNode.getPathAsJSONPointer()).head().toString());
                    arrayExpansionMap.put(clazz, list.asJavaList());
                    resultNode = list;
                } else
                 */
                if (schemaNode instanceof SetNode) {
                    String jsonPointerPath = schemaNode.getPathAsJSONPointer();
                    jsonPointerPath = jsonPointerPath.replaceFirst(relativePath, "");
                    JsonNode arrayNode = datum.at(JsonPointer.compile(jsonPointerPath));
                    if (!(arrayNode instanceof ArrayNode)) {
                        throw new ConversionException("Node " + JsonPointer.compile(schemaNode.getPathAsJSONPointer()) + " is not an array node!");
                    }
                    String value = readJSONArray((ArrayNode) arrayNode);
                    resultNode = m.createLiteral(value);
                } else if (schemaNode instanceof ObjectNode) {
                    resultNode = convertClassNodeToRDF(clazz, datum, relativePath);
                } else if (schemaNode instanceof PrimitiveNode) {
                    resultNode = convertClassNodeToRDF(clazz, datum, relativePath);
                } else {
                    log.warn("Encountered unknown node type for processing:" + schemaNode);
                    return null;
                }
                rdfNode = resultNode;
            } else {
                // not mapped
                Resource resource = convertClassNodeToRDF(clazz, datum, relativePath);
                rdfNode = resource;
            }
        } else if (node instanceof Literal) {
            Literal l = (Literal) node;
            rdfNode = convertLiteralNodeToRDF(l, datum, relativePath);

        } else if (node instanceof NamedEntity) {
            throw new ConversionException("Named Entity " + node.getURI() + " should not be part of an iteration context!");
        }
        uuidtoResource.put(node.getUuid(), rdfNode);
        return rdfNode;
    }


    @NotNull
    private Resource convertClassNodeToRDF(Class clazz) throws ConversionException {
        return convertClassNodeToRDF(clazz, datum, "");
    }

    public static String convertToInstanceId(String label) {
        String formatted = label.trim().toLowerCase().replace(" ", "_");
        formatted = HtmlUtils.htmlEscape(formatted);
        formatted = formatted.replaceAll("##", "_");
        formatted = formatted.replaceAll("#", "_");
        formatted = formatted.replaceAll("\\[", "_");
        formatted = formatted.replaceAll("\\]", "_");
        formatted = formatted.replaceAll("\\)", "_");
        formatted = formatted.replaceAll("\\(", "_");
        formatted = formatted.replaceAll("__", "_");
        return formatted;
    }

    private RDFNode convertLiteralNodeToRDF(Literal l) throws ConversionException {
        return convertLiteralNodeToRDF(l, datum, "");
    }

    private RDFNode convertLiteralNodeToRDF(Literal l, JsonNode datum, String relativePath) throws ConversionException {
        if (l.isMapped()) {
            String syntaxNodeUuid = l.getMappedSyntaxNodeUuid();
            SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(syntaxNodeUuid);
            String jsonPointerPath = schemaNode.getPathAsJSONPointer();
            jsonPointerPath = jsonPointerPath.replaceFirst(relativePath, "");
            JsonNode valueNode = datum.at(JsonPointer.compile(jsonPointerPath));
            if (!valueNode.isValueNode()) {
                throw new ConversionException("Node " + JsonPointer.compile(schemaNode.getPathAsJSONPointer()) + " is not a value node!");
            }
            if (valueNode.isMissingNode()) {
                // throw new ConversionException("Could not find value for syntaxId " + syntaxNodeUuid + " on model " + combinedModel.getId());
                return m.createLiteral(MISSING_DATA_LITERAL);
            } else {
                String value = valueNode.asText();
                return m.createLiteral(value);
            }
        } else {
            return m.createLiteral(l.getValue());
        }
    }

    /**
     * Parses the values of an array and converts them to an {@link RDFList}.
     *
     * @param arrayNode The JSON node containing the array data
     * @return The root element of the list
     */
    private RDFList buildRDFListFromArray(JsonNode arrayNode, Class clazz, String relativePath) throws ConversionException {
        /*
        SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(syntaxNodeUuid);
        if(!(schemaNode instanceof SetNode)){
            throw new ConversionException("Node " + syntaxNodeUuid + " is no SetNode");
        }
        SetNode node = (SetNode) schemaNode;
         */
        List<RDFNode> nodes = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            if (node.isValueNode()) {
                if (clazz != null) {
                    Resource classResource = convertClassNodeToRDF(clazz);
                    org.apache.jena.rdf.model.Literal valueLiteral = m.createLiteral(node.asText());
                    classResource.addProperty(hasValueProperty, valueLiteral);
                    nodes.add(classResource);
                } else {
                    nodes.add(m.createLiteral(node.asText()));
                }
            } else {
                // should not happen
                throw new ConversionException("There are no unprocessed complex arrays");
            }
        }
        return m.createList(nodes.iterator());
    }

    @NotNull
    private Resource convertClassNodeToRDF(Class clazz, JsonNode datum, String relativePath) throws ConversionException {
        Instance i = clazz.getInstance();
        Resource resource;
        String baseURI = smURI + semanticModelId + "-" + getCounter();

        resource = m.createResource(baseURI);
        if (i != null) {
            if (!PREDEFINED_LABELS.contains(i.getLabel())) {
                resource.addProperty(PLCM.label, m.createLiteral(i.getLabel()));
            }
            if (i.getDescription() != null && !i.getDescription().isBlank()) {
                resource.addProperty(PLCM.description, m.createLiteral(i.getDescription()));
            }
        }
        resource.addProperty(RDF.type, m.createResource(m.expandPrefix(clazz.getURI())));
        if (clazz.isMapped()) {
            SchemaNode schemaNode = combinedModel.getSyntaxModel().getNode(clazz.getMappedSyntaxNodeUuid());
            if (schemaNode instanceof ObjectNode) {
                // ignore the mapping, this is just for clarification
                return resource;
            }
            String jsonPointerPath = schemaNode.getPathAsJSONPointer();
            jsonPointerPath = jsonPointerPath.replaceFirst(relativePath, "");
            JsonNode valueNode = datum.at(JsonPointer.compile(jsonPointerPath));
            if (!valueNode.isValueNode()) {
                throw new ConversionException("Node " + JsonPointer.compile(schemaNode.getPathAsJSONPointer()) + " is not a value node!");
            }
            org.apache.jena.rdf.model.Literal valueLiteral;
            if (valueNode.isMissingNode()) {
                // throw new ConversionException("Could not find value for syntaxId " + syntaxNodeUuid + " on model " + combinedModel.getId());
                valueLiteral = m.createLiteral(MISSING_DATA_LITERAL);
            } else {
                String value = valueNode.asText();
                valueLiteral = m.createLiteral(value);
            }
            m.add(resource, hasValueProperty, valueLiteral);
        }
        return resource;
    }

    /**
     * Serializes a json array into a {@link String}.
     *
     * @param arrayNode The node containing the data
     * @return A string of all values divided by the ',' character
     */
    private String readJSONArray(ArrayNode arrayNode) {
        List<String> values = new ArrayList<>();
        arrayNode.elements().forEachRemaining(el -> {
            if (el instanceof ValueNode) {
                ValueNode v = (ValueNode) el;
                values.add(v.asText());
            } else if (el instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                com.fasterxml.jackson.databind.node.ObjectNode o = (com.fasterxml.jackson.databind.node.ObjectNode) el;
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String objectString = mapper.writeValueAsString(o);
                    values.add(objectString);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }

        });
        return values.stream()
                .map(v -> {
                    if (!v.startsWith("\"") || !v.endsWith("\"")) {
                        v = "\"" + v + "\"";
                    }
                    return v;
                })
                .collect(Collectors.joining(","));
    }
}
