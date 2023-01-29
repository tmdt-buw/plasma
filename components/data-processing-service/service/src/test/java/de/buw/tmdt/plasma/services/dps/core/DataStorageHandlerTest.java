package de.buw.tmdt.plasma.services.dps.core;

import de.buw.tmdt.plasma.services.dps.api.SampleDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class DataStorageHandlerTest {

    static DataStorageHandler storageHandler;

    @BeforeAll
    public static void setup() {
        storageHandler = new DataStorageHandler();
    }

    @Test
    @Order(1)
    void uploadFile() throws IOException {
        InputStream resourceAsStream = DataStorageHandlerTest.class.getClassLoader().getResourceAsStream("flight.json");
        assertNotNull(resourceAsStream);
        MultipartFile mpf = new MockMultipartFile("flight.json", "flight.json", "application/json", resourceAsStream);

        SampleDTO sample = storageHandler.storeFile(null, mpf);
        String dataId = sample.getDataId();
        assertNotNull(dataId);
    }

    @Test
    @Order(2)
    void listFiles() throws IOException {
        InputStream resourceAsStream = DataStorageHandlerTest.class.getClassLoader().getResourceAsStream("flight.json");
        assertNotNull(resourceAsStream);
        MultipartFile mpf = new MockMultipartFile("flight.json", "flight.json", "application/json", resourceAsStream);

        SampleDTO sample = storageHandler.storeFile(null, mpf);
        String dataId = sample.getDataId();
        assertNotNull(dataId);
        List<File> files = storageHandler.listFiles(dataId);
        assertTrue(files.size() > 0);
    }
}