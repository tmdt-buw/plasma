package de.buw.tmdt.plasma.converter.csv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.buw.tmdt.plasma.converter.ConversionException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class CSVConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CSVConverter() {

    }

    public static String convert(String input, int headerHeight) throws ConversionException {
        return convert(input, ',', headerHeight);
    }

    public static String convert(String input, char delimiter, int headerHeight) throws ConversionException {
        Reader dataReader;
        dataReader = new StringReader(input);
        com.opencsv.CSVReader reader = new com.opencsv.CSVReader(dataReader, delimiter);
        //Generate the tabular processor
        TabularProcessor tabularProcessor = new TabularProcessor(headerHeight);
        List<ObjectNode> nodes = new ArrayList<>();
        try {
            String[] line;
            while ((line = reader.readNext()) != null) {
                ObjectNode jsonNode = tabularProcessor.processRow(line);
                if (jsonNode != null) {
                    nodes.add(jsonNode);
                }
            }
            reader.close();
        } catch (IOException e) {
            throw new ConversionException("Couldn't read the input for CSV conversion.", e);
        }
        try {
            return objectMapper.writeValueAsString(nodes);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Error writing content to combined JSON.", e);
        }
    }
}
