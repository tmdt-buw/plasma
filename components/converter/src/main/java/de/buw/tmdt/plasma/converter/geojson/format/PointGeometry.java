package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PointGeometry extends Geometry {

    public static final String TYPE_PROPERTY = "type";
    private static final String COORDINATES_PROPERTY = "coordinates";
    private static final String COORDINATES_EXPORT_PROPERTY = "coordinate";
    private static final long serialVersionUID = 8312519184054799076L;
    private final Coordinate coordinate;

    @JsonCreator
    public PointGeometry(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(COORDINATES_PROPERTY) List<Double> coordinates
    ) {
        super(type);
        if (coordinates.size() == 2) {
            this.coordinate = new Coordinate(coordinates.get(0), coordinates.get(1));
        } else {
            this.coordinate = new Coordinate3D(coordinates.get(0), coordinates.get(1), coordinates.get(2));
        }
    }

    @JsonProperty(COORDINATES_EXPORT_PROPERTY)
    public Coordinate getCoordinate() {
        return coordinate;
    }
}
