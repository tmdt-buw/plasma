package de.buw.tmdt.plasma.services.kgs.core.rdf;

import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;
import org.jetbrains.annotations.NotNull;

public final class UpperOntologyDefiner {

    static final String URI_UO_PREFIX = "http://tmdtuo#";
    static final String URI_KGS_PREFIX = "http://tmdtkg#";
    static final String URI_SM_PREFIX = "http://tmdtsm#";
    static final String URI_PLASMA = "http://www.plasma.uni-wuppertal.de/schema#";
    static final String URI_NEOVOC_PREFIX = "neo4j://vocabulary#";
    static final String URI_RDFS_PREFIX = "http://www.w3.org/2000/01/rdf-schema#";
    static final String URI_OWL_PREFIX = "http://www.w3.org/2002/07/owl#";

    private UpperOntologyDefiner() {
        //Static access only
    }

    @NotNull
    public static OntModel getUpperOntologyModel() {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

        OntClass entityConceptOntClass = model.createClass(URI_UO_PREFIX + "EntityConcept");
        OntClass entityTypeOntClass = model.createClass(URI_UO_PREFIX + "EntityType");

        model.setNsPrefix("neovoc", URI_NEOVOC_PREFIX);
        model.setNsPrefix("rdfs", URI_RDFS_PREFIX);
        model.setNsPrefix("owl", URI_OWL_PREFIX);
        model.setNsPrefix("uo", URI_UO_PREFIX);
        model.setNsPrefix("kgs", URI_KGS_PREFIX);
        model.setNsPrefix("sm", URI_SM_PREFIX);

        ObjectProperty relationOntProperty = model.createObjectProperty(URI_UO_PREFIX + "instantiatedBy");
        relationOntProperty.addDomain(entityConceptOntClass);
        relationOntProperty.addRange(entityConceptOntClass);
        relationOntProperty.addDomain(entityTypeOntClass);
        relationOntProperty.addRange(entityTypeOntClass);

        DatatypeProperty identifierOntProperty = model.createDatatypeProperty(URI_UO_PREFIX + "id");
        identifierOntProperty.addDomain(entityConceptOntClass);
        identifierOntProperty.addDomain(entityTypeOntClass);
        identifierOntProperty.addRange(XSD.ID);

        DatatypeProperty descriptionOntProperty = model.createDatatypeProperty(URI_UO_PREFIX + "description");
        descriptionOntProperty.addDomain(entityConceptOntClass);
        descriptionOntProperty.addDomain(entityTypeOntClass);
        descriptionOntProperty.addRange(XSD.xstring);

        DatatypeProperty uriOntProperty = model.createDatatypeProperty(URI_UO_PREFIX + "uri");
        uriOntProperty.addDomain(entityConceptOntClass);
        uriOntProperty.addDomain(entityTypeOntClass);
        uriOntProperty.addRange(XSD.xstring);

        DatatypeProperty labelOntProperty = model.createDatatypeProperty(URI_UO_PREFIX + "label");
        labelOntProperty.addDomain(entityConceptOntClass);
        labelOntProperty.addDomain(entityTypeOntClass);
        labelOntProperty.addRange(XSD.xstring);

        entityConceptOntClass.addProperty(labelOntProperty, "EntityConcept");
        entityTypeOntClass.addProperty(labelOntProperty, "EntityType");

        return model;
    }
}
