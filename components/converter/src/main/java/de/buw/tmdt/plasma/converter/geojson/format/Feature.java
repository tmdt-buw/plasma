package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;

public class Feature implements Serializable {

    private static final String ID_PROPERTY = "id";
    private static final String TYPE_PROPERTY = "type";
    private static final String GEOMETRY_PROPERTY = "geometry";
    private static final String PROPERTIES_PROPERTY = "properties";
    private static final long serialVersionUID = -254604510094717558L;

    @JsonProperty(ID_PROPERTY)
    @JsonIgnore
    private String id;
    private final String type;
    private final Geometry geometry;
    private final Map<String, String> properties;

    @JsonCreator
    public Feature(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(GEOMETRY_PROPERTY) Geometry geometry,
            @JsonProperty(PROPERTIES_PROPERTY) Map<String, String> properties
    ) {
        this.type = type;
        this.geometry = geometry;
        this.properties = properties;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    @JsonIgnore
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty(TYPE_PROPERTY)
    public String getType() {
        return type;
    }

    @JsonProperty(GEOMETRY_PROPERTY)
    public Geometry getGeometry() {
        return geometry;
    }

    @JsonProperty(PROPERTIES_PROPERTY)
    public Map<String, String> getProperties() {
        return properties;
    }
}
