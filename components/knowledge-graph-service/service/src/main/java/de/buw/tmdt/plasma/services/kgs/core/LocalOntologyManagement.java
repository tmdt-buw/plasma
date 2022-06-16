package de.buw.tmdt.plasma.services.kgs.core;

import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.NamedEntity;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Relation;
import de.buw.tmdt.plasma.datamodel.semanticmodel.SemanticModelNode;
import org.apache.jena.arq.querybuilder.DescribeBuilder;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import static de.buw.tmdt.plasma.services.kgs.core.Ontologies.PREFIXES;

/**
 * Handles the connection to the ontology build by manually adding objects and relations.
 */
@Service
public class LocalOntologyManagement {

    private final Ontologies ontologies;

    @Value("${plasma.kgs.localontology.prefix:local}")
    private String localOntologyPrefix;
    @Value("${plasma.kgs.localontology.uri:https://local.ontology#}")
    private String localOntologyUri;
    @Value("${plasma.kgs.localontology.label:LOCAL}")
    private String localOntologyLabel;
    @Value("${plasma.kgs.localontology.description:A local ontology maintained by this instance.}")
    private String localOntologyDescription;


    public LocalOntologyManagement(Ontologies ontologies) {
        this.ontologies = ontologies;
    }

    @PostConstruct
    public void initialize() {
        getInternalOntology();
    }

    public void addResourcesToOntology(List<SemanticModelNode> resources) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes(PREFIXES);
        for (SemanticModelNode resource : resources) {
            /* Enable this to only store plasma resources
            if(!clazz.getSourceURI().startsWith("plasma") && !clazz.getSourceURI().startsWith(PREFIXES.getNsPrefixURI("plasma"))){
                // this is not a prefix that belongs here
                continue;
            }
             */
            Resource r = m.createResource(m.expandPrefix(resource.getURI()));
            if (resource instanceof NamedEntity) {
                NamedEntity ne = (NamedEntity) resource;
                m.add(new StatementImpl(r, RDF.type, PLCM.NamedEntity));
                if (!resource.getLabel().isBlank()) {
                    m.add(new StatementImpl(r, RDFS.label, m.createLiteral(resource.getLabel())));
                }
                if (ne.getDescription() != null && !ne.getDescription().isBlank()) {
                    m.add(new StatementImpl(r, RDFS.comment, m.createLiteral(ne.getDescription())));
                }
            } else if (resource instanceof Class) {
                Class sClass = (Class) resource;
                m.add(new StatementImpl(r, RDF.type, OWL.Class));
                if (!resource.getLabel().isBlank()) {
                    m.add(new StatementImpl(r, RDFS.label, m.createLiteral(resource.getLabel())));
                }
                if (sClass.getDescription() != null && !sClass.getDescription().isBlank()) {
                    m.add(new StatementImpl(r, RDFS.comment, m.createLiteral(sClass.getDescription())));
                }
            }
        }
        putModel(m);
    }

    public void addRelationsToOntology(List<Relation> relations) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes(PREFIXES);
        for (Relation relation : relations) {
            /* Enable this to only store plasma relations
            if(!relation.getSourceURI().startsWith("plasma") && !relation.getSourceURI().startsWith(PREFIXES.getNsPrefixURI("plasma"))){
                // this is not a prefix that belongs here
                continue;
            }
             */
            Resource r = m.createResource(m.expandPrefix(relation.getURI()));
            m.add(new StatementImpl(r, RDF.type, OWL.ObjectProperty));
            if (relation.getDescription() != null && !relation.getDescription().isBlank()) {
                m.add(new StatementImpl(r, RDFS.comment, m.createLiteral(relation.getDescription())));
            }
            if (!relation.getLabel().isBlank()) {
                m.add(new StatementImpl(r, RDFS.label, m.createLiteral(relation.getLabel())));
            }

        }
        putModel(m);
    }

    public OntModel getInternalOntology() {
        Optional<OntModel> localModel = ontologies.getOntologyModelByLabel(localOntologyLabel);
        if (localModel.isEmpty()) {
            // create a new local ontology
            return ontologies.createLocalOntology(localOntologyLabel, localOntologyPrefix, localOntologyUri, localOntologyDescription);
        }

        return localModel.get();
    }

    public SemanticModelNode findElementsByURI(String uri) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?class")
                .addWhere("?class", RDF.type, "?type")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.Class, PLCM.NamedEntity))
                .addFilter(describeBuilder.getExprFactory().eq("?class", ResourceFactory.createResource(PREFIXES.expandPrefix(uri))))
        //.addFilter(describeBuilder.getExprFactory().eq("?class", uri))
        ;
        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<SemanticModelNode> elements = CMToRDFMapper.convertOntologyResourcesToSemanticModelNodes(model1.listSubjects().toList());
        return elements.stream().findFirst().orElse(null);
    }


    public List<SemanticModelNode> findElementsByLabelPrefix(String prefix) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?class")
                .addWhere("?class", RDF.type, "?type")
                .addWhere("?class", RDFS.label, "?o")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.Class, PLCM.NamedEntity))
                .addFilter(describeBuilder.getExprFactory().strstarts("?o", prefix));

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();

        List<SemanticModelNode> elements = CMToRDFMapper.convertOntologyResourcesToSemanticModelNodes(model1.listSubjects().toList());
        return elements;
    }

    public List<SemanticModelNode> findElementsByLabelInfix(String infix) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?class")
                .addWhere("?class", RDF.type, "?type")
                .addWhere("?class", RDFS.label, "?o")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.Class, PLCM.NamedEntity))
                .addFilter(describeBuilder.getExprFactory().regex("?o", infix, "i"));

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<SemanticModelNode> elements = CMToRDFMapper.convertOntologyResourcesToSemanticModelNodes(model1.listSubjects().toList());
        return elements;
    }

    public List<SemanticModelNode> findElementsByNamespace(String namespace) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?class")
                .addWhere("?class", RDF.type, "?type")
                .addWhere("?class", RDFS.label, "?o")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.Class, PLCM.NamedEntity))
                .addFilter(describeBuilder.getExprFactory().strstarts("?class", namespace));

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<SemanticModelNode> elements = CMToRDFMapper.convertOntologyResourcesToSemanticModelNodes(model1.listSubjects().toList());
        return elements;
    }

    public Relation findRelationByURI(String uri) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?relation")
                .addWhere("?relation", RDF.type, "?type")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.ObjectProperty, OWL.DatatypeProperty))
                .addFilter(describeBuilder.getExprFactory().eq("?relation", ResourceFactory.createProperty(uri)))
        ;

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<Relation> elements = CMToRDFMapper.convertOntologyRelationsToRelations(model1.listSubjects().toList());
        return elements.stream().findFirst().orElse(null);
    }


    public List<Relation> findRelationsByLabelPrefix(String prefix) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?relation")
                .addWhere("?relation", RDFS.label, "?o")
                .addWhere("?relation", RDF.type, "?type")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.ObjectProperty, OWL.DatatypeProperty))
                .addFilter(describeBuilder.getExprFactory().strstarts("?o", prefix));

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<Relation> elements = CMToRDFMapper.convertOntologyRelationsToRelations(model1.listSubjects().toList());
        return elements;
    }

    public List<Relation> findRelationsByLabelInfix(String infix) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?relation")
                .addWhere("?relation", RDFS.label, "?o")
                .addWhere("?relation", RDF.type, "?type")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.ObjectProperty, OWL.DatatypeProperty))
                .addFilter(describeBuilder.getExprFactory().strstarts("?o", infix));

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<Relation> elements = CMToRDFMapper.convertOntologyRelationsToRelations(model1.listSubjects().toList());
        return elements;
    }

    public List<Relation> findRelationsByNamespace(String namespace) {
        DescribeBuilder describeBuilder = new DescribeBuilder();
        describeBuilder.addPrefixes(PREFIXES)
                .addVar("?relation")
                .addWhere("?relation", RDFS.label, "?o")
                .addWhere("?relation", RDF.type, "?type")
                .addFilter(describeBuilder.getExprFactory().in("?type", OWL.ObjectProperty, OWL.DatatypeProperty))
                .addFilter(describeBuilder.getExprFactory().strstarts("?relation", namespace));

        QueryExecution query = QueryExecutionFactory.create(describeBuilder.build(), getInternalOntology());
        Model model1 = query.execDescribe();
        List<Relation> elements = CMToRDFMapper.convertOntologyRelationsToRelations(model1.listSubjects().toList());
        return elements;
    }

    private void putModel(Model model) {
        OntModel internalOntology = getInternalOntology();
        internalOntology.add(model);
        ontologies.updateOntology(localOntologyLabel, internalOntology);
    }
}
