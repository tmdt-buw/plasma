package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MultiPointGeometry extends Geometry {

    private static final String TYPE_PROPERTY = "type";
    private static final String COORDINATES_PROPERTY = "coordinates";
    private static final String POINTS_PROPERTY = "points";
    private static final long serialVersionUID = 8815479106247046291L;
    private final ArrayList<PointGeometry> points;

    @JsonCreator
    public MultiPointGeometry(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(COORDINATES_PROPERTY) List<List<Double>> coordinates
    ) {
        super(type);
        ArrayList<PointGeometry> points = new ArrayList<>();
        for (List<Double> elements : coordinates) {
            points.add(new PointGeometry("Point", elements));
        }
        this.points = points;
    }

    @JsonProperty(POINTS_PROPERTY)
    public ArrayList<PointGeometry> getPoints() {
        return points;
    }
}
