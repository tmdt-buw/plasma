package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PolygonGeometry extends Geometry {

    public static final String TYPE_PROPERTY = "type";
    private static final String OUTER_POLYGON_PROPERTY = "outerPolygon";
    private static final String INNER_POLYGON_PROPERTY = "innerPolygon";
    private static final String COORDINATES_PROPERTY = "coordinates";
    private static final long serialVersionUID = -5584204278552301091L;
    private final ArrayList<Coordinate> outerPolygon;
    private final ArrayList<Coordinate> innerPolygon;

    @JsonCreator
    public PolygonGeometry(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(COORDINATES_PROPERTY) List<List<List<Double>>> coordinates
    ) {
        super(type);
        ArrayList<Coordinate> outer = new ArrayList<>();
        for (List<Double> element : coordinates.get(0)) {
            if (element.size() == 2) {
                outer.add(new Coordinate(element.get(0), element.get(1)));
            } else {
                outer.add(new Coordinate3D(element.get(0), element.get(1), element.get(2)));
            }
        }
        outerPolygon = outer;

        if (coordinates.size() > 1) {
            ArrayList<Coordinate> inner = new ArrayList<>();
            for (List<Double> element : coordinates.get(1)) {
                if (element.size() == 2) {
                    inner.add(new Coordinate(element.get(0), element.get(1)));
                } else {
                    inner.add(new Coordinate3D(element.get(0), element.get(1), element.get(2)));
                }
            }
            innerPolygon = inner;
        } else {
            innerPolygon = null;
        }
    }

    @JsonProperty(OUTER_POLYGON_PROPERTY)
    public ArrayList<Coordinate> getOuterPolygon() {
        return outerPolygon;
    }

    @JsonProperty(INNER_POLYGON_PROPERTY)
    public ArrayList<Coordinate> getInnerPolygon() {
        return innerPolygon;
    }
}
