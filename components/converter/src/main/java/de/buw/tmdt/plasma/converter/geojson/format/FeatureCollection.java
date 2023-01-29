package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureCollection implements Serializable {

    private static final String NAME_PROPERTY = "name";
    private static final String FEATURES_PROPERTY = "features";
    private static final String TYPE_PROPERTY = "type";
    private static final String CRS_PROPERTY = "crs";
    private static final long serialVersionUID = -950019924340449056L;
    private final ArrayList<Feature> features;
    private String type;
    private String name;
    private Crs crs;

    @JsonCreator
    public FeatureCollection(
            @JsonProperty(NAME_PROPERTY) String name,
            @JsonProperty(FEATURES_PROPERTY) List<Feature> features,
            @JsonProperty(TYPE_PROPERTY) String type,
            @Nullable @JsonProperty(CRS_PROPERTY) Crs crs
    ) {
        this.name = name;
        this.features = new ArrayList<>(features);
        this.type = type;
        this.crs = crs;
    }

    @JsonProperty(NAME_PROPERTY)
    public String getName() {
        return name;
    }

    @JsonProperty(FEATURES_PROPERTY)
    public ArrayList<Feature> getFeatures() {
        return features;
    }

    @Nullable
    @JsonProperty(CRS_PROPERTY)
    public Crs getCrs() {
        return crs;
    }

    @JsonProperty(TYPE_PROPERTY)
    public String getType() {
        return type;
    }
}
