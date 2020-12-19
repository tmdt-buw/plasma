package de.buw.tmdt.plasma.services.kgs.core.rdf;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriverFactory;
import de.buw.tmdt.plasma.services.kgs.database.api.exception.NoElementForIDException;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConcept;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.RelationConcept;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static de.buw.tmdt.plasma.services.kgs.core.rdf.UpperOntologyDefiner.URI_UO_PREFIX;

@SuppressWarnings("HardcodedFileSeparator")
@Component
public class RdfImporter {

    private static final Logger logger = LoggerFactory.getLogger(RdfImporter.class);
    private static final String NEW_ID = "newId";
    private final DatatypeProperty labelProp = UpperOntologyDefiner.getUpperOntologyModel().getDatatypeProperty(URI_UO_PREFIX + "label");
    // Used for isA relations in super class traversal of rdf classes
    private final RelationConcept isA = new RelationConcept(NEW_ID, "isA", "", "", Collections.emptySet());
    private GraphDBDriverFactory driverFactory;

    @Autowired
    public RdfImporter(GraphDBDriverFactory driverFactory) {
        this.driverFactory = driverFactory;
    }

    public Set<EntityConceptRelation> convertRdfToKgs(OntModel model) {
        driverFactory.getGraphDBDriver().createRdfPrefixNode(model.getNsPrefixMap());
        Pair<Set<EntityConceptRelation>, OntModel> cachedRdfConversionAfterOntProperties = removeAndCreateOntProperties(model);
        Pair<Set<EntityConceptRelation>, OntModel> cachedRdfConversionAfterTriples = removeRegularTriples(cachedRdfConversionAfterOntProperties.getRight());

        logger.info("Imported RDF into KG");

        return new HashSet<>(cachedRdfConversionAfterTriples.getLeft());
    }

    //Regular triple iteration (i.e. import of a flattened KG)
    private Pair<Set<EntityConceptRelation>, OntModel> removeRegularTriples(OntModel model) {
        Set<EntityConceptRelation> partialResult = new HashSet<>();

        StmtIterator stmtIterator = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
            @Override
            public boolean selects(Statement statement) {
                return !statement.getSubject().asNode().isBlank() &&
                       !(statement.getSubject().toString().contains("rdf-syntax") ||
                         statement.getSubject().toString().contains("rdf-schema") ||
                         statement.getObject().toString().contains("tmdt"));
            }
        });

        List<Statement> list = stmtIterator.toList();

        for (Statement currStatement : list) {

            EntityConcept from = rdfNodeToEntityConcept(currStatement.getSubject());
            RelationConcept rel = rdfPredicateToRelationConcept(currStatement.getPredicate());

            if (currStatement.getObject().asNode().isBlank()) {
                StmtIterator toIter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement statement) {
                        return statement.getSubject().asNode().isBlank() &&
                               Objects.equals(statement.getSubject().getId().getBlankNodeId(), currStatement.getObject().asNode().getBlankNodeId());
                    }
                });

                for (Statement toStatement : toIter.toList()) {
                    EntityConcept to = rdfNodeToEntityConcept(toStatement.getObject());

                    partialResult.add(driverFactory.getGraphDBDriver().createEntityConceptRelation(
                            new EntityConceptRelation(NEW_ID, from, to, rel)
                    ));
                }
            } else {
                EntityConcept to = rdfNodeToEntityConcept(currStatement.getObject());
                partialResult.add(driverFactory.getGraphDBDriver().createEntityConceptRelation(
                        new EntityConceptRelation(NEW_ID, from, to, rel)
                ));
            }
        }

        return new Pair<>(partialResult, model);
    }

    private Pair<Set<EntityConceptRelation>, OntModel> removeAndCreateOntProperties(OntModel model) {
        Set<EntityConceptRelation> partialResult = new HashSet<>();

        Map<String, String> uriToKgID = new HashMap<>();

        { // Create all classes as entity concepts
            ExtendedIterator<OntClass> classIterator = model.listNamedClasses();
            while (classIterator.hasNext()) {
                OntClass curr = classIterator.next();
                EntityConcept currAsEc = driverFactory.getGraphDBDriver()
                        .createEntityConcept(ontResourceToEntityConcept(model.getOntResource(curr), uriToKgID));

                Set<EntityConcept> superClasses = curr.listSuperClasses().toList()
                        .stream()
                        .filter(cl -> !cl.asNode().isBlank()) // No blank nodes (e.g. restrictive super classes)
                        .map(cl -> driverFactory.getGraphDBDriver().createEntityConcept(ontResourceToEntityConcept(model.getOntResource(cl), uriToKgID)))
                        .collect(Collectors.toSet());

                for (EntityConcept superEc : superClasses) { // Create an isA relation between class and super class
                    partialResult.add(driverFactory.getGraphDBDriver().createEntityConceptRelation(new EntityConceptRelation(NEW_ID, currAsEc, superEc, isA)));
                }
            }
        }

        // Iterate through all datatype properties
        ExtendedIterator<DatatypeProperty> dataTypeIterator = model.listDatatypeProperties();
        for (DatatypeProperty dtp : dataTypeIterator.toList()) {
            if (dtp.toString().contains("rdf-syntax") || dtp.toString().contains("rdf-schema")) {
                continue;
            }
            // Create an EC for the relation which is connected via an 'is' or 'has' relation
            EntityConcept dataTypeEc = ontResourceToEntityConcept(dtp, uriToKgID);

            if (!dtp.listRange().hasNext() || !dtp.listDomain().hasNext()) {
                driverFactory.getGraphDBDriver().createEntityConcept(dataTypeEc);
                continue;
            }

            String namePrefix = model.getOntResource(dtp.getRange()).getLocalName().contains("boolean") ? "is" : "has";

            RelationConcept datatypeRelation = new RelationConcept(
                    NEW_ID,
                    namePrefix + dtp.getLocalName().substring(0, 1).toUpperCase() + dtp.getLocalName().substring(1),
                    "",
                    dtp.getURI(),
                    Collections.emptySet()
            );

            Set<EntityConcept> from = new HashSet<>();

            // Iterate through the domains and build an is/has relation to the datatype
            dtp.listDomain().forEachRemaining(domain -> {
                if (domain.asClass().isUnionClass()) {

                    StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                        @Override
                        public boolean selects(Statement statement) {
                            return statement.getSubject().asNode().isBlank() && Objects.equals(statement.getSubject().getId(), domain.getId());
                        }
                    });

                    iter.forEachRemaining(stmt -> from.add(ontResourceToEntityConcept(model.getOntResource(stmt.getObject().asResource()), uriToKgID)));
                } else if (domain.canAs(RDFNode.class)) {
                    from.add(ontResourceToEntityConcept(model.getOntResource(domain), uriToKgID));
                }
            });

            for (EntityConcept ecFrom : from) {
                partialResult.add(driverFactory.getGraphDBDriver().createEntityConceptRelation(
                        new EntityConceptRelation(NEW_ID, ecFrom, dataTypeEc, datatypeRelation)
                ));
            }
        }

        //Iterate through all ontology properties
        ExtendedIterator<OntProperty> ontPropIterator = model.listOntProperties();

        for (OntProperty prop : ontPropIterator.toList()) {
            // Regular case for other properties
            if (prop.toString().contains("rdf-syntax") || prop.toString().contains("rdf-schema")) {
                continue;
            }

            RelationConcept rel;
            if (uriToKgID.containsKey(prop.getURI())) {
                rel = driverFactory.getGraphDBDriver().retrieveRelationConcept(uriToKgID.get(prop.getURI()));
            } else {
                rel = driverFactory.getGraphDBDriver().createRelationConcept(
                        new RelationConcept(
                                prop.getLocalName(),
                                prop.getLocalName(),
                                prop.getComment("EN"),
                                prop.getURI(),
                                Collections.emptySet()
                        ));
                uriToKgID.put(prop.getURI(), rel.getId());
            }

            Set<EntityConcept> from = new HashSet<>();
            Set<EntityConcept> to = new HashSet<>();

            // Iterate through the domains
            prop.listDomain().forEachRemaining(domain -> {
                if (domain.asClass().isUnionClass()) {

                    StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                        @Override
                        public boolean selects(Statement statement) {
                            return statement.getSubject().asNode().isBlank() && Objects.equals(statement.getSubject().getId(), domain.getId());
                        }
                    });

                    iter.forEachRemaining(stmt -> from.add(ontResourceToEntityConcept(model.getOntResource(stmt.getObject().asResource()), uriToKgID)));
                } else if (domain.canAs(RDFNode.class)) {
                    from.add(ontResourceToEntityConcept(model.getOntResource(domain), uriToKgID));
                }
            });

            // Iterate through the ranges
            prop.listRange().forEachRemaining(range -> {
                if (range.asClass().isUnionClass()) {
                    StmtIterator iter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                        @Override
                        public boolean selects(Statement statement) {
                            return statement.getSubject().asNode().isBlank() && statement.getSubject().getId() == range.getId();
                        }
                    });

                    iter.forEachRemaining(stmt -> to.add(ontResourceToEntityConcept(model.getOntResource(stmt.getObject().asResource()), uriToKgID)));
                } else if (range.canAs(RDFNode.class)) {
                    to.add(ontResourceToEntityConcept(model.getOntResource(range), uriToKgID));
                }
            });

            // Create the KGS concepts
            if (!from.isEmpty() && !to.isEmpty()) { // If domain/range is empty only create the relation concept
                for (EntityConcept ecFrom : from) {
                    for (EntityConcept ecTo : to) {
                        partialResult.add(driverFactory.getGraphDBDriver().createEntityConceptRelation(
                                new EntityConceptRelation(NEW_ID, ecFrom, ecTo, rel)
                        ));
                    }
                }
            }

            // Remove the property (jena only removes the statements of this property, not all other references towards this property)
            prop.remove();
        }
        return new Pair<>(partialResult, model);
    }

    /**
     * Maps a statement list onto a list where each entry is a list with only identical statement subjects.
     *
     * @param statements The list to be mapped.
     *
     * @return A list with subset where the subjects in each subset are the same.
     */
    private List<List<Statement>> divideIntoSubjectList(@NotNull List<Statement> statements) {
        List<List<Statement>> res = new ArrayList<>();

        for (Statement s : statements) {
            List<Statement> subjectList = filterSubjectList(s, statements);
            if (!res.contains(subjectList)) {
                res.add(subjectList);
            }
        }

        return res;
    }

    private List<Statement> filterSubjectList(Statement statement, List<Statement> toFilter) {
        return toFilter.stream().filter(s -> s.getSubject().equals(statement.getSubject())).collect(Collectors.toList());
    }

    private EntityConcept ontResourceToEntityConcept(OntResource resource, Map<String, String> cache) {
        if (cache.containsKey(resource.getURI())) {
            return driverFactory.getGraphDBDriver().retrieveEntityConcept(cache.get(resource.getURI()));
        } else {
            String ecUri;
            String ecName;
            String description;
            if (resource.isResource()) {
                ecUri = resource.asResource().getURI();
                ecName = resource.hasProperty(labelProp) ? resource.asResource().getPropertyResourceValue(labelProp).getLocalName() :
                        resource.asResource().getLocalName();
                description = resource.getComment("EN");
            } else if (resource.isLiteral()) {
                ecUri = resource.asLiteral().getDatatypeURI() + "^^" + resource.asLiteral().getString();
                ecName = resource.asLiteral().getString();
                description = "";
            } else {
                ecUri = resource.asNode().getBlankNodeId().getLabelString();
                ecName = resource.asNode().getBlankNodeId().getLabelString();
                description = resource.asNode().getBlankNodeId().getLabelString();
            }
            EntityConcept res = driverFactory.getGraphDBDriver().createEntityConcept(new EntityConcept(NEW_ID, ecName, description, ecUri));
            cache.put(resource.getURI(), res.getId());
            return res;
        }
    }

    private EntityConcept rdfNodeToEntityConcept(RDFNode node) {
        String ecUri;
        String ecName;
        if (node.isResource()) {
            ecUri = node.asResource().getURI();
            ecName = node.asResource().getLocalName();
        } else if (node.isLiteral()) {
            ecUri = node.asLiteral().getDatatypeURI() + "^^" + node.asLiteral().getString();
            ecName = node.asLiteral().getString();
        } else {
            ecUri = node.asNode().getBlankNodeId().getLabelString();
            ecName = node.asNode().getBlankNodeId().getLabelString();
        }
        return driverFactory.getGraphDBDriver()
                .retrieveEntityConceptsByPrefix(ecName).stream()
                .filter(ec -> ec.getMainLabel().equals(ecName))
                .findFirst().orElse(
                        new EntityConcept(ecName, ecName, "", ecUri)
                );
    }

    private RelationConcept rdfPredicateToRelationConcept(Property property) {
        String predicateUri = property.getURI();
        String predicateName = property.getLocalName();
        return driverFactory.getGraphDBDriver()
                .retrieveRelationConceptsByPrefix(predicateName).stream()
                .filter(rc -> rc.getLabel().equals(predicateName))
                .findFirst().orElse(
                        new RelationConcept(predicateName, predicateName, "", predicateUri, Collections.emptySet())
                );
    }

    private boolean isListStatement(Statement statement) {
        return statement.getPredicate().toString().endsWith("first")
               || statement.getPredicate().toString().endsWith("rest")
               || statement.getPredicate().toString().endsWith("nil");
    }

    private EntityConcept findOrCreateEc(EntityConcept concept) {
        try {
            return driverFactory.getGraphDBDriver().retrieveEntityConcept(concept.getId());
        } catch (NoElementForIDException e) {
            return driverFactory.getGraphDBDriver().createEntityConcept(concept);
        }
    }
}
