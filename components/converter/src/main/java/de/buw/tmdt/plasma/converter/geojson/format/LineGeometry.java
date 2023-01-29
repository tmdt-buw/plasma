package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class LineGeometry extends Geometry {

    public static final String TYPE_PROPERTY = "type";
    private static final String COORDINATES_PROPERTY = "coordinates";
    private static final String LINE_PROPERTY = "line";
    private static final long serialVersionUID = 2262006280377799093L;
    private final ArrayList<Coordinate> line;

    public LineGeometry(
            @JsonProperty(TYPE_PROPERTY) String type,
            @JsonProperty(COORDINATES_PROPERTY) List<List<Double>> coordinates
    ) {
        super(type);
        ArrayList<Coordinate> lineList = new ArrayList<>();
        for (List<Double> element : coordinates) {
            if (element.size() == 2) {
                lineList.add(new Coordinate(element.get(0), element.get(1)));
            } else {
                lineList.add(new Coordinate3D(element.get(0), element.get(1), element.get(2)));
            }
        }
        this.line = lineList;
    }

    @JsonProperty(LINE_PROPERTY)
    public ArrayList<Coordinate> getLine() {
        return line;
    }
}
