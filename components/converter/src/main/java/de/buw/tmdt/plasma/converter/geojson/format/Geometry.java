package de.buw.tmdt.plasma.converter.geojson.format;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PointGeometry.class, name = "Point"),
        @JsonSubTypes.Type(value = LineGeometry.class, name = "LineString"),
        @JsonSubTypes.Type(value = PolygonGeometry.class, name = "Polygon"),
        @JsonSubTypes.Type(value = MultiPointGeometry.class, name = "MultiPoint"),
        @JsonSubTypes.Type(value = MultiPolygonGeometry.class, name = "MultiPolygon"),
        @JsonSubTypes.Type(value = MultiLineGeometry.class, name = "MultiLineString")
})
public abstract class Geometry implements Serializable {

    public static final String TYPE_PROPERTY = "type";
    private static final long serialVersionUID = -267993307983003571L;
    private String type;

    @JsonCreator
    public Geometry(
            @JsonProperty(TYPE_PROPERTY) String type
    ) {
        this.type = type;
    }
}
