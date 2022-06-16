package de.buw.tmdt.plasma.services.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApplication.class)
@TestPropertySource(properties = {"plasma.config.local.path = ./temp"})
class VolumeConfigClientTest {

    @Value("${plasma.config.local.path}")
    protected String storageLocation;
    @Autowired
    VolumeConfigClient client;
    String configId = "testconfigId";
    private static File storageLocationFileRef;
    ObjectMapper mapper;

    @AfterAll
    static void cleanup() {
        FileSystemUtils.deleteRecursively(storageLocationFileRef);
    }

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    @BeforeEach
    public void setUp() throws IOException {
        mapper = new ObjectMapper();
        storageLocationFileRef = new File(storageLocation);
        //noinspection ResultOfMethodCallIgnored
        storageLocationFileRef.mkdir();
        try {
            Files.delete(Path.of(storageLocation, configId + ".cfg"));
        } catch (NoSuchFileException ignore) {

        }
    }

    @Test
    void testGetConfig() throws JsonProcessingException {
        Map<String, Serializable> map = new HashMap<>();
        map.put("testvalue1", "testvalue1");
        ArrayList<String> list = new ArrayList<>(Arrays.asList("Test", " Test"));
        map.put("testvalue2", list);

        ArrayList<AComplexObject> complexList = new ArrayList<>();
        Map<String, String> anotherMap = new HashMap<>();
        anotherMap.put("test", "test");
        anotherMap.put("test2", "test2");
        anotherMap.put("test3", null);
        complexList.add(new AComplexObject("complexObject", anotherMap));

        map.put("thecomplexList", complexList);

        client.storeConfig(configId, map);

        Map<String, String> config = client.getConfig(configId, null);
        assertEquals("testvalue1", config.get("testvalue1"));

        assertEquals(list, mapper.readValue(config.get("testvalue2"), mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class)));

        assertEquals(complexList, mapper.readValue(config.get("thecomplexList"), mapper.getTypeFactory().constructCollectionType(ArrayList.class, AComplexObject.class)));

    }

    @Test
    void testStoreConfig() {

        Map<String, Serializable> map = new HashMap<>();
        map.put("testvalue1", "testvalue1");
        map.put("testvalue2", new ArrayList<>(Arrays.asList("Test", " Test")));

        client.storeConfig(configId, map);
    }

    @Test
    void testStoreConfigWithOverride() {

        Map<String, Serializable> map = new HashMap<>();
        map.put("testvalue1", "testvalue1");
        map.put("testvalue2", new ArrayList<>(Arrays.asList("Test", " Test")));

        client.storeConfig(configId, map);

        client.updateConfig(configId, map, null);
    }

    @Test
    void testStoreConfigWithoutOverride() {

        Map<String, Serializable> map = new HashMap<>();
        map.put("testvalue1", "testvalue1");
        map.put("testvalue2", new ArrayList<>(Arrays.asList("Test", " Test")));

        client.storeConfig(configId, map);

        assertThrows(UnsupportedOperationException.class, () -> client.storeConfig(configId, map));
    }

    @Test
    void testHasConfig() {
        assertFalse(client.hasConfig(configId));
        Map<String, Serializable> map = new HashMap<>();
        map.put("testvalue1", "testvalue1");
        map.put("testvalue2", new ArrayList<>(Arrays.asList("Test", " Test")));

        client.storeConfig(configId, map);
        assertTrue(client.hasConfig(configId));
    }

    private static class AComplexObject {
        public Map<String, String> map;
        public String identifier;

        public AComplexObject() {

        }

        public AComplexObject(String identifier, Map<String, String> map) {
            this.map = map;
            this.identifier = identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            AComplexObject that = (AComplexObject) o;

            if (!Objects.equals(map, that.map)) {
                return false;
            }
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            int result = map != null ? map.hashCode() : 0;
            result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
            return result;
        }
    }
}