package de.buw.tmdt.plasma.converter.esri;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.converter.ConversionException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ESRIConverterTest {

    @SuppressWarnings("HardcodedFileSeparator")
    private static final String INPUT_SHAPE_FILE_ZIP = "esri/test.zip";
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String EXPECTED_OUTPUT_FILE = "esri/result.json";
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        URL resource = ESRIConverter.class.getClassLoader().getResource(INPUT_SHAPE_FILE_ZIP);
        assertNotNull(resource);
        URL resource2 = ESRIConverter.class.getClassLoader().getResource(EXPECTED_OUTPUT_FILE);
        assertNotNull(resource2);
    }


    @Test
    public void testConvertToJSON() throws ConversionException, IOException, URISyntaxException {
        URL resource = ESRIConverter.class.getClassLoader().getResource(INPUT_SHAPE_FILE_ZIP);
        assertNotNull(resource);
        Path path = Path.of(resource.toURI());

        String result = ESRIConverter.convertToJSON(path);
        InputStream inputStream = ESRIConverter.class.getClassLoader().getResourceAsStream(EXPECTED_OUTPUT_FILE);
        assertNotNull(inputStream);
        String expected = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

        JsonNode jsonFileExpected = mapper.readTree(expected);
        JsonNode jsonFileActual = mapper.readTree(result);

        assertEquals(jsonFileExpected, jsonFileActual);
    }


}