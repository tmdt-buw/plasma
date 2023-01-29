package de.buw.tmdt.plasma.services.dps.conversion.rdf;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.services.dps.CombinedModelGenerator;
import de.buw.tmdt.plasma.services.dps.conversion.ConversionException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static de.buw.tmdt.plasma.services.dps.CombinedModelGenerator.asTurtle;

public class DataConverterTest {

    private CombinedModelGenerator combinedModelGenerator;

    @BeforeEach
    public void setup() {
        combinedModelGenerator = new CombinedModelGenerator();
    }


    @Test
    public void testConversionWithoutMappedList() throws IOException, ConversionException {
        CombinedModel combinedModel = combinedModelGenerator.getFlightModel();

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DataConverterTest.class.getResourceAsStream("/flight.json");
        JsonNode jsonNode = mapper.readTree(is);

        PrefixMapping prefixMapping = new PrefixMappingImpl();
        prefixMapping.setNsPrefix("local", CombinedModelGenerator.namespace);
        prefixMapping.setNsPrefix("plcm", PLCM.getURI());
        prefixMapping.setNsPrefix("plsm", "http://plasma.uni-wuppertal.de/sm/");
        prefixMapping.setNsPrefix("plasma", "http://plasma.uni-wuppertal.de/ontology#");

        RDFConverter converter = new RDFConverter(prefixMapping);

        jsonNode = jsonNode.get(0);
        Model converted = converter.convertToRDF(combinedModel, jsonNode);

        String output = asTurtle(converted);

        System.out.println(output);
    }

    @Test
    public void testConversionWithMappedPrimitiveList() throws IOException, ConversionException {
        combinedModelGenerator.setMapIdentificationNumbers(true);
        CombinedModel combinedModel = combinedModelGenerator.getFlightModel();

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DataConverterTest.class.getResourceAsStream("/flight.json");
        JsonNode jsonNode = mapper.readTree(is);

        PrefixMapping prefixMapping = new PrefixMappingImpl();
        prefixMapping.setNsPrefix("local", CombinedModelGenerator.namespace);
        prefixMapping.setNsPrefix("plcm", PLCM.getURI());
        prefixMapping.setNsPrefix("plsm", "http://plasma.uni-wuppertal.de/sm/");
        prefixMapping.setNsPrefix("plasma", "http://plasma.uni-wuppertal.de/ontology#");

        RDFConverter converter = new RDFConverter(prefixMapping);

        jsonNode = jsonNode.get(0);
        Model converted = converter.convertToRDF(combinedModel, jsonNode);

        String output = asTurtle(converted);

        System.out.println(output);
    }

    @Test
    public void testConversionWithMappedPrimitiveListInstance() throws IOException, ConversionException {
        combinedModelGenerator.setMapIdentificationNumberInstance(true);
        CombinedModel combinedModel = combinedModelGenerator.getFlightModel();

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DataConverterTest.class.getResourceAsStream("/flight.json");
        JsonNode jsonNode = mapper.readTree(is);

        PrefixMapping prefixMapping = new PrefixMappingImpl().withDefaultMappings(PrefixMapping.Standard);
        prefixMapping.setNsPrefix("local", CombinedModelGenerator.namespace);
        prefixMapping.setNsPrefix("plcm", PLCM.getURI());
        prefixMapping.setNsPrefix("plsm", "http://plasma.uni-wuppertal.de/sm/");
        prefixMapping.setNsPrefix("plasma", "http://plasma.uni-wuppertal.de/ontology#");


        RDFConverter converter = new RDFConverter(prefixMapping);

        jsonNode = jsonNode.get(0);
        Model converted = converter.convertToRDF(combinedModel, jsonNode);

        String output = asTurtle(converted);

        System.out.println(output);
    }

    @Test
    public void testConversionWithMappedStaffInstance() throws IOException, ConversionException {
        combinedModelGenerator.setMapVesselInformation(false);
        combinedModelGenerator.setMapStaffInstance(true);

        CombinedModel combinedModel = combinedModelGenerator.getFlightModel();

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DataConverterTest.class.getResourceAsStream("/flight.json");
        JsonNode jsonNode = mapper.readTree(is);

        PrefixMapping prefixMapping = new PrefixMappingImpl().withDefaultMappings(PrefixMapping.Standard);
        prefixMapping.setNsPrefix("local", CombinedModelGenerator.namespace);
        prefixMapping.setNsPrefix("plcm", PLCM.getURI());
        prefixMapping.setNsPrefix("plsm", "http://plasma.uni-wuppertal.de/sm/");
        prefixMapping.setNsPrefix("plasma", "http://plasma.uni-wuppertal.de/ontology#");


        RDFConverter converter = new RDFConverter(prefixMapping);

        jsonNode = jsonNode.get(0);
        Model converted = converter.convertToRDF(combinedModel, jsonNode);

        String output = asTurtle(converted);

        System.out.println(output);
    }

    @Test
    @Disabled
    public void testConversionWith2ndLayerArray() throws IOException, ConversionException {
        combinedModelGenerator.setMapVesselInformation(false);
        combinedModelGenerator.setMapStaffInstance(true);
        combinedModelGenerator.setMapTagInstance(true);

        CombinedModel combinedModel = combinedModelGenerator.getFlightModel();

        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DataConverterTest.class.getResourceAsStream("/flight.json");
        JsonNode jsonNode = mapper.readTree(is);

        PrefixMapping prefixMapping = new PrefixMappingImpl().withDefaultMappings(PrefixMapping.Standard);
        prefixMapping.setNsPrefix("local", CombinedModelGenerator.namespace);
        prefixMapping.setNsPrefix("plcm", PLCM.getURI());
        prefixMapping.setNsPrefix("plsm", "http://plasma.uni-wuppertal.de/sm/");
        prefixMapping.setNsPrefix("plasma", "http://plasma.uni-wuppertal.de/ontology#");


        RDFConverter converter = new RDFConverter(prefixMapping);

        jsonNode = jsonNode.get(0);
        Model converted = converter.convertToRDF(combinedModel, jsonNode);

        String output = asTurtle(converted);

        System.out.println(output);
    }

    public Model cleanse(Model model) {
        Model newModel = ModelFactory.createDefaultModel();
        newModel.setNsPrefixes(model.getNsPrefixMap());
        StmtIterator stmtIterator = model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement stmt = stmtIterator.nextStatement();
            if (stmt.getPredicate().getURI().contains("cmUuid")) {
                continue;
            }
            if (stmt.getPredicate().getURI().contains("http://plasma.uni-wuppertal.de/cm#syntaxNodeId")) {
                continue;
            }
            newModel.add(stmt);
        }
        return newModel;
    }


}
