package de.buw.tmdt.plasma.services.config;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;

public interface ConfigServiceClient {

    /**
     * Retrieves a config for a given configId.
     *
     * @param configId The configuration id to retrieve
     * @param token    Authentication token used to retrieve config values
     * @return A saved map for the given config id
     */
    Map<String, String> getConfig(String configId, String token) throws NoSuchElementException;

    /**
     * Updates a configuration for a given configId.
     *
     * @param configId The configuration id to identify the config
     * @param config   A map for the given config id containing the config values
     * @param token    Authentication token used to override config values. Only needed if override is true.
     */
    void updateConfig(String configId, Map<String, Serializable> config, String token);

    /**
     * Stores a configuration for a given configId.
     * The method will throw an error if a config for the given key is already present.
     *
     * @param configId The configuration id to identify the config
     * @param config   A map for the given config id containing the config values
     * @return The generated token to access or update the data
     */
    String storeConfig(String configId, Map<String, Serializable> config);

    /**
     * Checks if a config for a given identifier exists.
     *
     * @param configId The configuration id to identify the config
     * @return true if a config exists, false otherwise
     */
    boolean hasConfig(String configId);

    /**
     * Removes a stored config from the service.
     *
     * @param configId The configuration id to identify the config
     * @param token    Authentication token used to delete config values
     */
    void removeConfig(String configId, String token);

    /**
     * Formats the given configuration in the same way it is saved.
     *
     * @param config The config to format
     * @return A formatted string representing the config.
     */
    String printFormattedConfig(Map<String, Serializable> config);
}
