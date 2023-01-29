package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MultiPolygonGeometry extends Geometry {

    public static final String TYPE_PROPERTY = "type";
    public static final String POLYGONS_PROPERTY = "polygons";
    private static final String COORDINATES_PROPERTY = "coordinates";
    private static final long serialVersionUID = 5663492479313838450L;
    private final ArrayList<PolygonGeometry> polygons;

    @JsonCreator
    public MultiPolygonGeometry(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(COORDINATES_PROPERTY) List<List<List<List<Double>>>> coordinates
    ) {

        super(type);
        ArrayList<PolygonGeometry> polygons = new ArrayList<>();
        for (List<List<List<Double>>> subpolygons : coordinates) {
            polygons.add(new PolygonGeometry("Polygon", subpolygons));
        }
        this.polygons = polygons;
    }

    @JsonProperty(POLYGONS_PROPERTY)
    public ArrayList<PolygonGeometry> getPolygons() {
        return polygons;
    }
}
