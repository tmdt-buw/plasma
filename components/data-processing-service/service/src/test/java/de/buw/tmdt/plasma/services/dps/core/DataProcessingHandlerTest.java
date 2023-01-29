package de.buw.tmdt.plasma.services.dps.core;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.services.dps.CombinedModelGenerator;
import de.buw.tmdt.plasma.services.dps.api.SampleDTO;
import de.buw.tmdt.plasma.services.kgs.shared.feignclient.OntologyApiClient;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DataProcessingHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(DataProcessingHandlerTest.class);

    private DataProcessingHandler processingHandler;
    private DataStorageHandler storageHandler;

    @BeforeEach
    public void setup(@Mock OntologyApiClient ontologyApiClient) {
        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("local", CombinedModelGenerator.namespace);
        prefixes.put("plsm", "http://plasma.uni-wuppertal.de/sm/");
        prefixes.put("plcm", "http://plasma.uni-wuppertal.de/cm#");
        prefixes.put("plasma", "http://plasma.uni-wuppertal.de/ontology#");
        Mockito.lenient().when(ontologyApiClient.getNamespaces()).thenReturn(prefixes);
        storageHandler = new DataStorageHandler();
        processingHandler = new DataProcessingHandler(ontologyApiClient, storageHandler);
    }

    @Test
    @Disabled("not curated")
    void processFile() throws IOException {
        InputStream resourceAsStream = DataProcessingHandlerTest.class.getClassLoader().getResourceAsStream("flight.json");
        assertNotNull(resourceAsStream);
        MultipartFile mpf = new MockMultipartFile("flight.json", "flight.json", "application/json", resourceAsStream);

        SampleDTO sample = storageHandler.storeFile(null, mpf);
        String dataId = sample.getDataId();
        assertNotNull(dataId);
        log.info("DataId: {}", dataId);
        CombinedModelGenerator combinedModelGenerator = new CombinedModelGenerator();
        CombinedModel flightModel = combinedModelGenerator.getFlightModel();
        String result = processingHandler.processFile(flightModel, dataId, null);
        InputStream convertedInputStream = DataProcessingHandlerTest.class.getClassLoader().getResourceAsStream("flight_converted.ttl");
        assert convertedInputStream != null;
        Model expected = ModelFactory.createDefaultModel();
        expected.read(convertedInputStream, CombinedModelGenerator.namespace, "TTL");
        Model actual = ModelFactory.createDefaultModel();
        actual.read(IOUtils.toInputStream(result, "UTF-8"), CombinedModelGenerator.namespace, "TTL");
        assertEquals(expected.size(), actual.size(), "Expected result for dataId " + dataId + " did not match");
        assertEquals(expected.listObjects().toList().size(), actual.listObjects().toList().size(), "Expected result for dataId " + dataId + " did not match");
        assertEquals(expected.listSubjects().toList().size(), actual.listSubjects().toList().size(), "Expected result for dataId " + dataId + " did not match");
        assertEquals(expected.listNameSpaces().toList().size(), actual.listNameSpaces().toList().size(), "Expected result for dataId " + dataId + " did not match");

    }
}