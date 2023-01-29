package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinate3D extends Coordinate {

    private static final String ALTITUDE_PROPERTY = "altitude";
    private static final long serialVersionUID = 655098815639071633L;
    private final double altitude;

    public Coordinate3D(double longitude, double latitude, double altitude) {
        super(longitude, latitude);
        this.altitude = altitude;
    }

    @JsonProperty(ALTITUDE_PROPERTY)
    public double getAltitude() {
        return altitude;
    }
}
