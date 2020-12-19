package de.buw.tmdt.plasma.services.kgs.core.rdf;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriverFactory;
import de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph.EntityConceptRelation;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static de.buw.tmdt.plasma.services.kgs.core.rdf.UpperOntologyDefiner.*;

@Component
public class KgsToRdfConverter {

    private static final Logger logger = LoggerFactory.getLogger(KgsToRdfConverter.class);
    private GraphDBDriverFactory driverFactory;

    @Autowired
    public KgsToRdfConverter(@NotNull GraphDBDriverFactory driverFactory) {
        this.driverFactory = driverFactory;
    }

    @NotNull
    public OntModel convertKgsToRdf(boolean flat) {

        logger.info("Converting KG to rdf");

        Set<EntityConceptRelation> entityConceptRelations = driverFactory.getGraphDBDriver().retrieveEntityConceptRelations(false);

        OntModel model = getUpperOntologyModel();
        DatatypeProperty descriptionOntProperty = model.getDatatypeProperty(URI_UO_PREFIX + "description");
        DatatypeProperty uriOntProperty = model.getDatatypeProperty(URI_UO_PREFIX + "uri");
        DatatypeProperty labelOntProperty = model.getDatatypeProperty(URI_UO_PREFIX + "label");
        OntClass entityConceptOntClass = model.getOntClass(URI_UO_PREFIX + "EntityConcept");

        model.clearNsPrefixMap();
        model.setNsPrefixes(driverFactory.getGraphDBDriver().getPrefixMap());

        //TODO: Remove explicit plasma namespace from export and implement method that allows adding pairs for the prefix map
        model.setNsPrefix("plasma", URI_PLASMA);
        if (!flat) {
            model.setNsPrefix("tmdtkg", URI_KGS_PREFIX);
            model.setNsPrefix("tmdtuo", URI_UO_PREFIX);
        }

        for (EntityConceptRelation ecr : entityConceptRelations) {
            Resource from;
            Resource to;
            String fromUri;
            String toUri;
            String relUri;

            if (!ecr.getTailNode().getSourceURI().contains("://")) { //URI is only with prefix
                fromUri = model.getNsPrefixURI(ecr.getTailNode().getSourceURI().replaceAll(":.*", "")) +
                          ecr.getTailNode().getSourceURI().replaceAll(".*:", "");
            } else {
                fromUri = ecr.getTailNode().getSourceURI();
            }

            if (!ecr.getHeadNode().getSourceURI().contains("://")) { //URI is only with prefix
                toUri = model.getNsPrefixURI(ecr.getHeadNode().getSourceURI().replaceAll(":.*", "")) +
                        ecr.getHeadNode().getSourceURI().replaceAll(".*:", "");
            } else {
                toUri = ecr.getHeadNode().getSourceURI();
            }

            if (!ecr.getRelationConcept().getSourceURI().contains("://")) { //URI is only with prefix
                relUri = model.getNsPrefixURI(ecr.getRelationConcept().getSourceURI().replaceAll(":.*", "")) +
                         ecr.getRelationConcept().getSourceURI().replaceAll(".*:", "");
            } else {
                relUri = ecr.getRelationConcept().getSourceURI();
            }

            if (flat) {
                from = model.getResource(fromUri);

                to = model.getResource(toUri);

                if (from == null) {
                    from = model.createResource(fromUri);
                }
                if (to == null) {
                    to = model.createResource(toUri);
                }

                from.addProperty(
                        new PropertyImpl(relUri),
                        to
                );
            } else {

                StmtIterator fromIter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement statement) {
                        return statement.getObject().toString().equals(ecr.getTailNode().getId());
                    }
                });

                StmtIterator toIter = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement statement) {
                        return statement.getObject().toString().equals(ecr.getHeadNode().getId());
                    }
                });

                if (!fromIter.hasNext()) {
                    from = model.createIndividual(fromUri, entityConceptOntClass)
                            .addProperty(labelOntProperty, ecr.getTailNode().getMainLabel())
                            .addProperty(uriOntProperty, ecr.getTailNode().getSourceURI())
                            .addProperty(descriptionOntProperty, ecr.getTailNode().getDescription());
                } else {
                    from = fromIter.next().getSubject();
                }

                if (!toIter.hasNext()) {
                    to = model.createIndividual(toUri, entityConceptOntClass)
                            .addProperty(labelOntProperty, ecr.getHeadNode().getMainLabel())
                            .addProperty(uriOntProperty, ecr.getHeadNode().getSourceURI())
                            .addProperty(descriptionOntProperty, ecr.getHeadNode().getDescription());
                } else {
                    to = toIter.next().getSubject();
                }

                from.addProperty(
                        new PropertyImpl(relUri),
                        to
                );

                model.remove(model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement statement) {
                        return statement.getObject().toString().equals(ecr.getHeadNode().getId());
                    }
                }));
            }
        }

        model.remove(getUpperOntologyModel());

        logger.info("Converted KG to RDF successfully");

        return model;
    }
}
