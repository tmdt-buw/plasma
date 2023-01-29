package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EnrichedFeature extends Feature {

    private static final long serialVersionUID = -6673814941163675256L;
    private static final String COLLECTION_NAME_PROPERTY = "collectionName";
    private static final String CRS_PROPERTY = "crs";

    private final String collectionName;
    private final Crs crs;

    public EnrichedFeature(Feature feature, String collectionName, Crs crs) {
        super(feature.getType(), feature.getGeometry(), feature.getProperties());
        this.collectionName = collectionName;
        this.crs = crs;
    }

    @JsonProperty(COLLECTION_NAME_PROPERTY)
    public String getCollectionName() {
        return collectionName;
    }

    @JsonProperty(CRS_PROPERTY)
    public Crs getCrs() {
        return crs;
    }
}

