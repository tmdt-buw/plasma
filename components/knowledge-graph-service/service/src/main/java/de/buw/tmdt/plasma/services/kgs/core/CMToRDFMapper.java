package de.buw.tmdt.plasma.services.kgs.core;

import de.buw.tmdt.plasma.datamodel.CombinedModelIntegrityException;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Literal;
import de.buw.tmdt.plasma.datamodel.semanticmodel.*;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.buw.tmdt.plasma.services.kgs.core.Ontologies.PREFIXES;

/**
 * Converts a {@link SemanticModel} from a {@link de.buw.tmdt.plasma.datamodel.CombinedModel} to
 * it's RDF representation and vice versa.
 * TODO remove usage of BNodes if possible
 */
public class CMToRDFMapper {

    private static final Logger log = LoggerFactory.getLogger(CMToRDFMapper.class);

    private CMToRDFMapper() {

    }

    public static Model convertToRDFModel(SemanticModel semanticModel) {
        if (semanticModel.getId() == null) {
            throw new IllegalArgumentException("Semantic model id must not be empty or null");
        }
        Map<String, RDFNode> uuidtoResource = new HashMap<>();
        String semanticModelId = semanticModel.getId().substring(0, 5);
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes(PREFIXES);
        String smPrefix = PREFIXES.getNsPrefixURI("plsm");
        Resource rdfModel = m.createResource(smPrefix + semanticModelId);
        rdfModel.addProperty(RDF.type, PLCM.SemanticModel);
        rdfModel.addProperty(PLCM.cmUuid, semanticModel.getId());
        semanticModel.getNodes().forEach(node -> {
            RDFNode rdfNode = null;
            if (node instanceof Class) {
                Class clazz = (Class) node;
                Instance i = clazz.getInstance();
                Resource resource;
                if (i != null) {
                    String originInstanceURI = smPrefix + semanticModelId + "-" + convertToInstanceId(i.getLabel());
                    int counter = 1;
                    String instanceURI = originInstanceURI + "_" + counter;

                    Resource res = ResourceFactory.createResource(instanceURI);
                    while (m.containsResource(res)) {
                        instanceURI = originInstanceURI + "_" + counter++;
                        res = ResourceFactory.createResource(instanceURI);
                    }
                    resource =
                            m.createResource(instanceURI); // will reuse if already existent
                    resource.addProperty(PLCM.label, m.createLiteral(i.getLabel()));
                    if (i.getDescription() != null && !i.getDescription().isBlank()) {
                        resource.addProperty(PLCM.description, m.createLiteral(i.getDescription()));
                    }
                } else {
                    resource = m.createResource();
                }
                if (clazz.getMappedSyntaxNodeUuid() != null) {
                    resource.addProperty(PLCM.syntaxNodeId, m.createLiteral(clazz.getMappedSyntaxNodeUuid()));
                }
                if (clazz.getMappedSyntaxNodeLabel() != null && !clazz.getMappedSyntaxNodeLabel().isBlank()) {
                    resource.addProperty(PLCM.originalLabel, m.createLiteral(clazz.getMappedSyntaxNodeLabel()));
                }

                resource.addProperty(RDF.type, m.createResource(m.expandPrefix(clazz.getURI())));

                if (clazz.getXCoordinate() != null) {
                    resource.addProperty(PLCM.xCoordinate, m.createLiteral(String.valueOf(clazz.getXCoordinate())));
                }
                if (clazz.getYCoordinate() != null) {
                    resource.addProperty(PLCM.yCoordinate, m.createLiteral(String.valueOf(clazz.getYCoordinate())));
                }
                resource.addProperty(PLCM.cmUuid, m.createLiteral(clazz.getUuid()));
                rdfNode = resource;
            } else if (node instanceof Literal) {
                Literal l = (Literal) node;
                rdfNode = m.createLiteral(l.getValue());
            } else if (node instanceof NamedEntity) {
                NamedEntity ne = (NamedEntity) node;
                // create a resource which describes the named entity
                // this will later be elevated to the ontology
                Resource neResource = m.createResource(m.expandPrefix(ne.getURI()));
                neResource.addProperty(RDF.type, PLCM.NamedEntity);
                neResource.addProperty(PLCM.label, m.createLiteral(ne.getLabel()));
                if (ne.getDescription() != null && !ne.getDescription().isBlank()) {
                    neResource.addProperty(PLCM.description, m.createLiteral(ne.getDescription()));
                }

                // now create an anonymous representation of the named entity in this model
                // this stores local information like the CM uuid and model position
                Resource resource = m.createResource();
                resource.addProperty(RDF.type, neResource);
                resource.addProperty(PLCM.cmUuid, ne.getUuid());
                if (ne.getXCoordinate() != null) {
                    resource.addProperty(PLCM.xCoordinate, m.createLiteral(String.valueOf(ne.getXCoordinate())));
                }
                if (ne.getYCoordinate() != null) {
                    resource.addProperty(PLCM.yCoordinate, m.createLiteral(String.valueOf(ne.getYCoordinate())));
                }
                // use the b-node as reference
                rdfNode = resource;
            }
            rdfModel.addProperty(PLCM.hasNode, rdfNode);
            uuidtoResource.put(node.getUuid(), rdfNode);

        });
        semanticModel.getEdges().forEach(r -> {
            SemanticModelNode from = semanticModel.getNode(r.getFrom());
            SemanticModelNode to = semanticModel.getNode(r.getTo());
            RDFNode fromNode = uuidtoResource.get(from.getUuid());
            RDFNode toNode = uuidtoResource.get(to.getUuid());
            fromNode.asResource().addProperty(ResourceFactory.createProperty(m.expandPrefix(r.getURI())), toNode);
        });
        return m;
    }


    public static SemanticModel convertToSemanticModel(Model model, Function<String, SemanticModelNode> nodeRetrieverByURI,
                                                       Function<String, Relation> relationRetrieverByURI) {
        String modelId = null;
        List<SemanticModelNode> nodes = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();
        Map<Resource, SemanticModelNode> mappedNodes = new HashMap<>();

        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.nextStatement();
            Resource s = stmt.getSubject();
            Property p = stmt.getPredicate();
            RDFNode o = stmt.getObject();
            if (s.hasProperty(RDF.type, PLCM.SemanticModel)) {
                // get the model id
                modelId = s.getProperty(PLCM.cmUuid).getString();
                continue;
            }

            // handle subject
            SemanticModelNode convertedSubject = mappedNodes.get(s);

            if (convertedSubject == null) {
                // we have to create a new one
                Resource superclass = s.getRequiredProperty(RDF.type).getObject().asResource();
                SemanticModelNode supernode = nodeRetrieverByURI.apply(superclass.getURI());
                convertedSubject = createSemanticModelNode(s, supernode);
                nodes.add(convertedSubject);
                mappedNodes.put(s, convertedSubject);
            }

            // check if we can handle the object or if we want to drop this line all together
            if (Objects.equals(p.getNameSpace(), PLCM.getURI()) // this is a system statement made by PLASMA
                    || Objects.equals(p, RDF.type)) { // this is a RDF type relation
                continue;
            }

            // handle object before predicate
            SemanticModelNode convertedObject;
            if (o.isResource()) {
                Resource object = o.asResource();
                // check if this already exist
                convertedObject = mappedNodes.get(object);
                if (convertedObject == null) {
                    // we have to create a new one
                    Resource superclass = object.getRequiredProperty(RDF.type).getObject().asResource();
                    SemanticModelNode supernode = nodeRetrieverByURI.apply(superclass.getURI());
                    convertedObject = createSemanticModelNode(object, supernode);
                    nodes.add(convertedObject);
                    mappedNodes.put(object, convertedObject);
                }

            } else if (o.isLiteral()) {
                // create a literal
                convertedObject = createLiteral(o.asLiteral());
                nodes.add(convertedObject);
            } else {
                throw new CombinedModelIntegrityException("Could not determine type for object " + o);
            }

            // handle properties
            if (o.isResource()) {
                // this is an ObjectProperty
                Relation ontologyInstance = relationRetrieverByURI.apply(stmt.getPredicate().getURI());
                Relation r = new ObjectProperty(convertedSubject.getUuid(), convertedObject.getUuid(), PREFIXES.shortForm(stmt.getPredicate().getURI()));
                if (ontologyInstance != null) {
                    r.setLabel(ontologyInstance.getLabel());
                }
                relations.add(r);
            } else if (o.isLiteral()) {
                // this is a DataProperty
                Relation ontologyInstance = relationRetrieverByURI.apply(stmt.getPredicate().getURI());
                Relation r = new DataProperty(convertedSubject.getUuid(), convertedObject.getUuid(), PREFIXES.shortForm(stmt.getPredicate().getURI()));
                if (ontologyInstance != null) {
                    r.setLabel(ontologyInstance.getLabel());
                }
                relations.add(r);
            }
        }
        if (modelId == null) {
            // there is no model for that id in the model
            return null;
        }
        SemanticModel semanticModel = new SemanticModel(modelId, nodes, relations);
        return semanticModel;

    }

    private static SemanticModelNode createSemanticModelNode(Resource res, SemanticModelNode entityFromOntology) {
        if (entityFromOntology instanceof NamedEntity) {
            try {
                return createNamedEntity(res, (NamedEntity) entityFromOntology);
            } catch (PropertyNotFoundException pnfe) {
                log.error("Error occurred building named entity from {}", res.getURI(), pnfe);
            }

        } else if (entityFromOntology instanceof Class) {
            try {
                return createClass(res, (Class) entityFromOntology);
            } catch (PropertyNotFoundException pnfe) {
                log.error("Error occurred building class from {}", res.getURI(), pnfe);
            }
        }
        throw new CombinedModelIntegrityException("Error occurred building object from " + res.getURI());
    }

    private static Class createClass(Resource res, Class superclass) {

        String uuid = res.getRequiredProperty(PLCM.cmUuid).getString();
        Double xCoordinate = getPropertyAsDoubleOrNull(res, PLCM.xCoordinate);
        Double yCoordinate = getPropertyAsDoubleOrNull(res, PLCM.yCoordinate);
        String syntaxNodeId = getPropertyOrNull(res, PLCM.syntaxNodeId);
        String originalLabel = getPropertyOrNull(res, PLCM.originalLabel);
        String syntaxNodePath = getPropertyOrNull(res, PLCM.syntaxNodePath);
        Class sClass = new Class(uuid, PREFIXES.shortForm(superclass.getURI()), superclass.getLabel(),
                superclass.getDescription(), xCoordinate, yCoordinate, syntaxNodeId, originalLabel, syntaxNodePath, null, false);

        if (!res.isAnon()) {
            // this is an actual instance
            String instanceURI = res.getURI();
            String label = res.getRequiredProperty(PLCM.label).getString();
            String description = getPropertyOrNull(res, PLCM.description);
            Instance i = new Instance(uuid, PREFIXES.shortForm(instanceURI), label, description);
            sClass.setInstance(i);
        }
        return sClass;
    }

    private static NamedEntity createNamedEntity(Resource res, NamedEntity namedEntity) {
        Double xCoordinate = getPropertyAsDoubleOrNull(res, PLCM.xCoordinate);
        Double yCoordinate = getPropertyAsDoubleOrNull(res, PLCM.yCoordinate);
        NamedEntity ne = new NamedEntity(PREFIXES.shortForm(namedEntity.getURI()), namedEntity.getLabel(),
                namedEntity.getDescription(), xCoordinate, yCoordinate);
        return ne;
    }

    private static Literal createLiteral(org.apache.jena.rdf.model.Literal res) {
        // TODO manage literal positions
        //Double xCoordinate = getPropertyAsDoubleOrNull(res, PLCM.xCoordinate);
        //Double yCoordinate = getPropertyAsDoubleOrNull(res, PLCM.yCoordinate);
        Literal l = new Literal(res.getString());
        return l;
    }

    public static List<SemanticModelNode> convertOntologyResourcesToSemanticModelNodes(List<Resource> resources) {
        return resources.stream()
                .map(CMToRDFMapper::convertOntologyResourceToSemanticModelNode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static SemanticModelNode convertOntologyResourceToSemanticModelNode(Resource resource) {
        try {
            String label = resource.getRequiredProperty(RDFS.label).getString();
            String description = getPropertyOrNull(resource, RDFS.comment);
            if (resource.hasProperty(RDF.type, OWL.Class)) {
                return new Class(
                        PREFIXES.shortForm(resource.getURI()),
                        label,
                        description);
            } else if (resource.hasProperty(RDF.type, PLCM.NamedEntity)) {
                return new NamedEntity(
                        PREFIXES.shortForm(resource.getURI()),
                        label,
                        description);
            }
        } catch (PropertyNotFoundException pnfe) {
            log.error("Error occurred building class from {}", resource.getURI(), pnfe);

        }
        return null;
    }

    private static String getPropertyOrNull(Resource r, Property property) {
        return r.hasProperty(property) ? r.getProperty(property).getString() : null;
    }

    private static Double getPropertyAsDoubleOrNull(Resource r, Property property) {
        return r.hasProperty(property) ? r.getProperty(property).getDouble() : null;
    }

    /**
     * Convert a label to an instance id usable in a URI.
     * Jena does not offer proper support for IRIs yet, thus many special symbols are converted to '_' characters.
     *
     * @param label The input label
     * @return A cleansed label
     */
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

    public static List<Relation> convertOntologyRelationsToRelations(List<Resource> resources) {
        return resources.stream()
                .map(resource -> {
                    try {
                        String label = resource.getRequiredProperty(RDFS.label).getString();
                        String description = getPropertyOrNull(resource, RDFS.comment);
                        if (resource.hasProperty(RDF.type, OWL.DatatypeProperty)) {
                            return new DataProperty(
                                    null,
                                    null,
                                    PREFIXES.shortForm(resource.getURI()),
                                    label,
                                    description);
                        } else if (resource.hasProperty(RDF.type, PLCM.NamedEntity)) {
                            return new ObjectProperty(
                                    null,
                                    null,
                                    PREFIXES.shortForm(resource.getURI()),
                                    label,
                                    description);
                        }
                    } catch (PropertyNotFoundException pnfe) {
                        log.error("Error occurred building relation from {}", resource.getURI(), pnfe);

                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static Relation convertOntologyPropertyToRelation(OntProperty property) {
        if (property == null) {
            return null;
        }
        if (property.isDatatypeProperty()) {
            return new DataProperty("", "", PREFIXES.shortForm(property.getURI()), PREFIXES.shortForm(property.getLabel(null) == null ? property.getURI() : property.getLabel(null)), property.getComment(null));
        } else if (property.isObjectProperty()) {
            return new ObjectProperty("", "", PREFIXES.shortForm(property.getURI()), PREFIXES.shortForm(property.getLabel(null) == null ? property.getURI() : property.getLabel(null)), property.getComment(null));
        }
        return null;
    }

    public static Class convertOntologyClassToClass(OntClass ontClass) {
        return new Class(PREFIXES.shortForm(ontClass.getURI()), PREFIXES.shortForm(ontClass.getLabel(null) == null ? ontClass.getURI() : ontClass.getLabel(null)), ontClass.getComment(null));
    }

}
