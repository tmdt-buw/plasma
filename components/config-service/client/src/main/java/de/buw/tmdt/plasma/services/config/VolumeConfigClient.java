package de.buw.tmdt.plasma.services.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This is a client to write and read configs from a local storage mount.
 */
@Service
@ConditionalOnProperty(value = "plasma.config.local.path")
public class VolumeConfigClient implements ConfigServiceClient {

    private static final Logger log = LoggerFactory.getLogger(VolumeConfigClient.class);

    private final ObjectMapper mapper;

    @Value("${plasma.config.local.path}")
    protected String storageLocation;

    public VolumeConfigClient() {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Map<String, String> getConfig(String configId, String token) throws NoSuchElementException {
        if (configId == null) {
            return new HashMap<>();
        }
        Path filePath = getConfigFilePath(configId);
        log.info("Reading config from " + filePath);
        try {
            JavaType type = mapper.getTypeFactory().constructParametricType(HashMap.class, String.class, String.class);
            Map<String, String> map = mapper.readValue(filePath.toFile(), type);
            if (map == null) {
                throw new NoSuchElementException("Could not find file or valid config: " + filePath);
            }
            return map;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new NoSuchElementException("Could not find or properly read requested file " + filePath + ":\n" + e.getMessage());
        }
    }

    private Path getConfigFilePath(String configId) {
        if (storageLocation == null || storageLocation.isEmpty()) {
            throw new RuntimeException("Cannot initialize ConfigService with empty or null path.");
        }
        return Path.of(storageLocation, configId + ".cfg");
    }

    @Override
    public void updateConfig(String configId, Map<String, Serializable> config, String token) {
        if (configId == null) {
            return;
        }
        if (config == null) {
            config = new HashMap<>();
        }
        Path filePath = getConfigFilePath(configId);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            storeConfig(configId, config);
        } catch (IOException e) {
            log.error("Could not write to requested file " + filePath, e);
            throw new NoSuchElementException("Could not write to requested file " + filePath + ": " + e.getMessage());
        }
    }

    @Override
    public String storeConfig(String configId, Map<String, Serializable> config) {
        if (configId == null) {
            throw new IllegalArgumentException("A configId needs to be specified");
        }
        Map<String, String> serializedConfig;
        if (config == null) {
            serializedConfig = new HashMap<>();
        } else {
            serializedConfig = serializeValues(config);
        }
        Path filePath = getConfigFilePath(configId);
        try {
            if (Files.exists(filePath)) {
                throw new UnsupportedOperationException("Cannot write to " + filePath + ". Config alredy exists");
            }
            mapper.writeValue(filePath.toFile(), serializedConfig);
        } catch (IOException e) {
            log.error("Could not write to requested file " + filePath, e);
            throw new NoSuchElementException("Could not write to requested file " + filePath + ": " + e.getMessage());
        }
        return "noToken";
    }

    @Override
    public boolean hasConfig(String configId) {
        Path filePath = getConfigFilePath(configId);
        return Files.exists(filePath);
    }

    @Override
    public void removeConfig(String configId, String token) {
        Path filePath = getConfigFilePath(configId);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            log.error("Could not remove requested file " + filePath, e);
            throw new NoSuchElementException("Could not remove requested file " + filePath + ": " + e.getMessage());
        }
    }

    @Override
    public String printFormattedConfig(Map<String, Serializable> config) {
        try {
            return mapper.writeValueAsString(serializeValues(config));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return config.toString();
        }
    }

    private Map<String, String> serializeValues(Map<String, Serializable> config) {
        Map<String, String> serializedConfig = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : config.entrySet()) {
            Serializable value = entry.getValue();
            String key = entry.getKey();
            if (!(value instanceof String)) {
                try {
                    serializedConfig.put(key, mapper.writeValueAsString(value));
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Error converting non-string value '" + key + "' to string: " + e.getMessage());
                }
            } else {
                serializedConfig.put(key, (String) value);
            }
        }
        return serializedConfig;
    }
}
