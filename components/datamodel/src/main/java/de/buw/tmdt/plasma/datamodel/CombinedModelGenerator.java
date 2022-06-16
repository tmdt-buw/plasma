package de.buw.tmdt.plasma.datamodel;

import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import de.buw.tmdt.plasma.datamodel.semanticmodel.Class;
import de.buw.tmdt.plasma.datamodel.semanticmodel.*;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Generates a basic {@link CombinedModel}.
 * Can be used for testing purposes.
 */
public class CombinedModelGenerator {

    public static final String NAMESPACE = "http://plasma.uni-wuppertal.de/ontology#";

    private CombinedModelGenerator() {

    }

    public static String generateResourceURI(String label) {
        return NAMESPACE + label;
    }

    public static CombinedModel getCombinedModel() {
        return getCombinedModel(NAMESPACE);
    }

    public static CombinedModel getCombinedModel(String namespace) {
        SchemaNode root = new ObjectNode("ROOT", true);
        List<String> examples = Arrays.asList("10-11;suffix1", "20-21;suffix2", "test-test;suffix3");
        PrimitiveNode identifierNode = new PrimitiveNode("ident", true, DataType.String, examples, null);
        PrimitiveNode timeNode = new PrimitiveNode("time", true, DataType.String, null, null);
        ObjectNode companyNode = new ObjectNode("carrier",  true);
        PrimitiveNode nameNode = new PrimitiveNode("name", true, DataType.String, null, null);

        SetNode codesNode = new SetNode( true);
        PrimitiveNode codeNode = new PrimitiveNode("codes1", true, DataType.String, null, null);

        List<SchemaNode> nodes = Arrays.asList(root, identifierNode, timeNode, companyNode, nameNode, codesNode, codeNode);

        Edge rootIdentifierEdge = new Edge(root.getUuid(), identifierNode.getUuid());
        Edge rootTimeEdge = new Edge(root.getUuid(), timeNode.getUuid());
        Edge rootCompanyEdge = new Edge(root.getUuid(), companyNode.getUuid());
        Edge companyNameEdge = new Edge(companyNode.getUuid(), nameNode.getUuid());
        Edge companyCodesEdge = new Edge(companyNode.getUuid(), codeNode.getUuid());
        Edge codesCodeEdge = new Edge(codesNode.getUuid(), codeNode.getUuid());

        List<Edge> edges = Arrays.asList(rootIdentifierEdge, rootTimeEdge, rootCompanyEdge, companyNameEdge, companyCodesEdge, codesCodeEdge);

        SyntaxModel syntaxModel = new SyntaxModel(root.getUuid(), nodes, edges);

        // generate semantic model

        Class flightClass = new Class(namespace + "flight", "flight", "A scheduled flight of an airplane");

        Literal flightTypeLiteral = new Literal("domestic");

        Class identifierClass = new Class(namespace + "identifier", "identifier", "Identifies a certain object");
        Instance identifierInstance = new Instance(identifierClass.getLabel(), null);
        identifierClass.setMappedSyntaxNodeLabel(identifierNode.getLabel());
        identifierClass.setMappedSyntaxNodeUuid(identifierNode.getUuid());
        identifierClass.setInstance(identifierInstance);

        Class airlineClass = new Class(namespace + "airline", "airline", "A company that provides air travel services");

        Class nameClass = new Class(namespace + "name", "name", "A commonly known but not unique identifier of something");
        Instance nameInstance = new Instance(nameClass.getLabel(), null);
        nameClass.setMappedSyntaxNodeLabel(nameNode.getLabel());
        nameClass.setMappedSyntaxNodeUuid(nameNode.getUuid());
        nameClass.setInstance(nameInstance);

        Class iataCodeClass = new Class(namespace + "iata_code", "IATA code", "A unique identifier issued by the IATA");
        Instance iataCodeInstance = new Instance(iataCodeClass.getLabel(), null);
        iataCodeClass.setMappedSyntaxNodeLabel(codeNode.getLabel());
        iataCodeClass.setMappedSyntaxNodeUuid(codeNode.getUuid());
        iataCodeClass.setInstance(iataCodeInstance);

        NamedEntity iataClass = new NamedEntity(namespace + "IATA", "IATA", "The International Air Transport Association");

        List<SemanticModelNode> elements = Arrays.asList(identifierClass, flightClass, flightTypeLiteral, airlineClass, nameClass, iataCodeClass, iataClass);

        Relation flightIdentifierRelation = new ObjectProperty(flightClass.getUuid(), identifierClass.getUuid(), namespace + "has");
        Relation airlineNameRelation = new ObjectProperty(airlineClass.getUuid(), nameClass.getUuid(), namespace + "has");
        Relation airlineIATACodeRelation = new ObjectProperty(airlineClass.getUuid(), iataCodeClass.getUuid(), namespace + "has");
        Relation iataCodeIATARelation = new ObjectProperty(iataCodeClass.getUuid(), iataClass.getUuid(), namespace + "issuedBy");
        Relation flightTypeRelation = new DataProperty(flightClass.getUuid(), flightTypeLiteral.getUuid(), namespace + "flightType");
        Relation flightAirlineRelation = new ObjectProperty(airlineClass.getUuid(), flightClass.getUuid(), namespace + "operates");

        List<Relation> objectProperties = Arrays.asList(flightIdentifierRelation, flightAirlineRelation, airlineNameRelation, airlineIATACodeRelation, iataCodeIATARelation, flightTypeRelation);

        SemanticModel semanticModel = new SemanticModel(null, elements, objectProperties);
        CombinedModel combinedModel = new CombinedModel(UUID.randomUUID().toString(), syntaxModel, semanticModel, new ArrayList<>(), false);

        combinedModel.validate();

        return combinedModel;
    }
}

