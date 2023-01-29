package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class MultiLineGeometry extends Geometry {

    public static final String TYPE_PROPERTY = "type";
    public static final String LINES_PROPERTY = "lines";
    public static final String COORDINATES_PROPERTY = "coordinates";
    private static final long serialVersionUID = 509292125754456347L;
    private final ArrayList<LineGeometry> lines;

    public MultiLineGeometry(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(COORDINATES_PROPERTY) List<List<List<Double>>> coordinates
    ) {
        super(type);
        ArrayList<LineGeometry> lines = new ArrayList<>();
        for (List<List<Double>> sublines : coordinates) {
            lines.add(new LineGeometry("LineString", sublines));
        }
        this.lines = lines;
    }

    @JsonProperty(LINES_PROPERTY)
    public ArrayList<LineGeometry> getLines() {
        return lines;
    }
}
