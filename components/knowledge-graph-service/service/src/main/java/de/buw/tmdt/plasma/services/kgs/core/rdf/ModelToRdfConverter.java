package de.buw.tmdt.plasma.services.kgs.core.rdf;

import de.buw.tmdt.plasma.services.kgs.database.api.GraphDBDriverFactory;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.Relation;
import de.buw.tmdt.plasma.services.kgs.shared.model.semanticmodel.SemanticModel;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static de.buw.tmdt.plasma.services.kgs.core.rdf.UpperOntologyDefiner.*;

@SuppressWarnings("HardcodedFileSeparator")
@Component
public class ModelToRdfConverter {

    private static final Logger logger = LoggerFactory.getLogger(ModelToRdfConverter.class);
    private GraphDBDriverFactory driverFactory;

    @Autowired
    public ModelToRdfConverter(@NotNull GraphDBDriverFactory driverFactory) {
        this.driverFactory = driverFactory;
    }

    @NotNull
    public OntModel convertSemanticModelToRdf(@NotNull String id, boolean flat) {

        SemanticModel semanticModel = driverFactory.getGraphDBDriver().retrieveSemanticModel(id);

        Set<Relation> relationSet = semanticModel.getRelations();
        Set<EntityType> entityTypes = semanticModel.getEntityTypes();

        OntModel model = UpperOntologyDefiner.getUpperOntologyModel();
        ObjectProperty relationOntProperty = model.getObjectProperty(URI_UO_PREFIX + "instantiatedBy");
        DatatypeProperty descriptionOntProperty = model.getDatatypeProperty(URI_UO_PREFIX + "description");
        DatatypeProperty uriOntProperty = model.getDatatypeProperty(URI_UO_PREFIX + "uri");
        DatatypeProperty labelOntProperty = model.getDatatypeProperty(URI_UO_PREFIX + "label");
        OntClass entityConceptOntClass = model.getOntClass(URI_UO_PREFIX + "EntityConcept");
        OntClass entityTypeOntClass = model.getOntClass(URI_UO_PREFIX + "EntityType");

        model.setNsPrefixes(driverFactory.getGraphDBDriver().getPrefixMap());

        model.setNsPrefix("plasma", URI_KGS_PREFIX);

        if (!flat) {
            model.setNsPrefix("tmdtkg", URI_KGS_PREFIX);
            model.setNsPrefix("tmdtuo", URI_UO_PREFIX);
            model.setNsPrefix("tmdtsm", URI_SM_PREFIX);
        }

        for (EntityType et : entityTypes) {
            String ecUri;
            if (!et.getEntityConcept().getSourceURI().contains("://")) { //URI is only with prefix
                ecUri = model.getNsPrefixURI(et.getEntityConcept().getSourceURI().replaceAll(":.*", "")) +
                        et.getEntityConcept().getSourceURI().replaceAll(".*:", "");
            } else {
                ecUri = et.getEntityConcept().getSourceURI();
            }
            if (flat) {
                model.createResource(ecUri);
            } else {
                Resource entityConceptIndividual = model.createIndividual(ecUri, entityConceptOntClass)
                        .addProperty(labelOntProperty, et.getEntityConcept().getMainLabel())
                        .addProperty(uriOntProperty, et.getEntityConcept().getSourceURI())
                        .addProperty(descriptionOntProperty, et.getEntityConcept().getDescription());

                model.createIndividual(URI_SM_PREFIX + et.getLabel().replaceAll(" ", "_").replaceAll("#", ""), entityTypeOntClass)
                        .addProperty(labelOntProperty, et.getLabel())
                        .addProperty(descriptionOntProperty, et.getDescription())
                        .addProperty(relationOntProperty, entityConceptIndividual);
            }
        }

        for (Relation rel : relationSet) {
            String fromUri;
            String toUri;
            String relUri;
            if (!rel.getTailNode().getEntityConcept().getSourceURI().contains("://")) { //URI is only with prefix
                fromUri = model.getNsPrefixURI(rel.getTailNode().getEntityConcept().getSourceURI().replaceAll(":.*", "")) +
                          rel.getTailNode().getEntityConcept().getSourceURI().replaceAll(".*:", "");
            } else {
                fromUri = rel.getTailNode().getEntityConcept().getSourceURI();
            }
            if (!rel.getHeadNode().getEntityConcept().getSourceURI().contains("://")) { //URI is only with prefix
                toUri = model.getNsPrefixURI(rel.getHeadNode().getEntityConcept().getSourceURI().replaceAll(":.*", "")) +
                        rel.getHeadNode().getEntityConcept().getSourceURI().replaceAll(".*:", "");
            } else {
                toUri = rel.getHeadNode().getEntityConcept().getSourceURI();
            }
            if (!rel.getRelationConcept().getSourceURI().contains("://")) { //URI is only with prefix
                relUri = model.getNsPrefixURI(rel.getRelationConcept().getSourceURI().replaceAll(":.*", "")) +
                         rel.getRelationConcept().getSourceURI().replaceAll(".*:", "");
            } else {
                relUri = rel.getRelationConcept().getSourceURI();
            }
            if (flat) {
                Resource from = model.getResource(fromUri);

                Resource to = model.getResource(toUri);

                from.addProperty(
                        new PropertyImpl(relUri),
                        to
                );
            } else {

                Resource from = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement statement) {
                        return statement.getSubject().getClass().isInstance(entityTypeOntClass) && statement.getObject().toString()
                                .equals(rel.getTailNode().getLabel());
                    }
                }).nextStatement().getSubject();

                Resource to = model.listStatements(new SimpleSelector(null, null, (RDFNode) null) {
                    @Override
                    public boolean selects(Statement statement) {
                        return statement.getSubject().getClass().isInstance(entityTypeOntClass) && statement.getObject().toString()
                                .equals(rel.getHeadNode().getLabel());
                    }
                }).nextStatement().getSubject();

                from.addProperty(
                        new PropertyImpl(relUri),
                        to
                );
            }
        }

        model.remove(UpperOntologyDefiner.getUpperOntologyModel());
        return model;
    }

    private String getPrefixForUrl(Map<String, String> prefixMap, String url) {
        for (Map.Entry<String, String> entry : prefixMap.entrySet()) {
            if (entry.getValue().equals(url)) {
                return entry.getKey();
            }
        }
        logger.info("No prefix found for url: {}", url);
        return "";
    }
}
