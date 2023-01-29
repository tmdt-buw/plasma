package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Coordinate implements Serializable {

    private static final String LATITUDE_PROPERTY = "latitude";
    private static final String LONGITUDE_PROPERTY = "longitude";
    private static final long serialVersionUID = 7484923584947401858L;
    private final double latitude;
    private final double longitude;

    @JsonCreator
    public Coordinate(
            @JsonProperty(LONGITUDE_PROPERTY) double longitude,
            @JsonProperty(LATITUDE_PROPERTY) double latitude

    ) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
