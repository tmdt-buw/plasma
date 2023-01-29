package de.buw.tmdt.plasma.converter.geojson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.converter.ConversionException;
import de.buw.tmdt.plasma.converter.geojson.format.Crs;
import de.buw.tmdt.plasma.converter.geojson.format.EnrichedFeature;
import de.buw.tmdt.plasma.converter.geojson.format.Feature;
import de.buw.tmdt.plasma.converter.geojson.format.FeatureCollection;

import java.util.ArrayList;
import java.util.List;

public class GeoJSONConverter {
    private static ObjectMapper mapper = new ObjectMapper();

    private GeoJSONConverter() {

    }

    public static String convert(String input) throws ConversionException {
        List<EnrichedFeature> features = new ArrayList<>();
        try {
            FeatureCollection fc = mapper.readValue(input, FeatureCollection.class);
            String collectionName = fc.getName();
            Crs crs = fc.getCrs();
            for (Feature f : fc.getFeatures()) {
                EnrichedFeature ef = new EnrichedFeature(f, collectionName, crs);

                features.add(ef);
            }
        } catch (JsonProcessingException e) {
            throw new ConversionException("Could not read GeoJson. Please verify that file is valid GeoJSON.", e);
        }
        try {
            return mapper.writeValueAsString(features);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Error writing final features.", e);
        }
    }
}
