package de.buw.tmdt.plasma.converter.geojson;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.converter.ConversionException;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class GeoJSONConverterTest extends TestCase {

    ObjectMapper mapper = new ObjectMapper();

    public void testGeoJson() throws IOException, ConversionException {
        String prefix = "geojson/";
        InputStream is = GeoJSONConverter.class.getClassLoader().getResourceAsStream(prefix + "test.json");
        assertNotNull(is);
        String input = IOUtils.toString(is, StandardCharsets.UTF_8);
        String resultString = GeoJSONConverter.convert(input);
        is = GeoJSONConverter.class.getClassLoader().getResourceAsStream(prefix + "result.json");
        assertNotNull(is);
        String expectedString = IOUtils.toString(is, StandardCharsets.UTF_8).trim()
                .replace("\n", "")
                .replace("\r", "")
                .replace(" ", "")
                .replace("\t", "");
        assertEquals(mapper.readTree(expectedString), mapper.readTree(resultString));
    }
}
