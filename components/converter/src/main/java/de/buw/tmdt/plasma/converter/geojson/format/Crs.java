package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class Crs implements Serializable {

    private static final String TYPE_PROPERTY = "type";
    private static final String PROPERTIES_PROPERTY = "properties";
    private static final long serialVersionUID = 8915015082989732091L;

    private final String type;
    private final Map<String, String> properties;

    @JsonCreator
    public Crs(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(PROPERTIES_PROPERTY) Map<String, String> properties
    ) {
        this.type = type;
        this.properties = properties;
    }

    @JsonProperty(TYPE_PROPERTY)
    public String getType() {
        return type;
    }

    @JsonProperty(PROPERTIES_PROPERTY)
    public Map<String, String> getProperties() {
        return properties;
    }
}
