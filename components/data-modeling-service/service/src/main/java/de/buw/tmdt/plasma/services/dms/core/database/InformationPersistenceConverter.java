package de.buw.tmdt.plasma.services.dms.core.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class InformationPersistenceConverter implements AttributeConverter<Map<String, String>, String> {

    private static final Logger log = LoggerFactory.getLogger(InformationPersistenceConverter.class);

    private final ObjectMapper objectMapper = getObjectMapper();

    public static ObjectMapper getObjectMapper() {

        ObjectMapper mapper = new Jackson2ObjectMapperBuilder()
                .dateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault())))
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .featuresToEnable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
        return mapper;
    }

    @Override
    public String convertToDatabaseColumn(Map<String, String> map) {
        // clean up all the operations as they are not needed here
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            log.error("Unexpected IOEx encoding json to database: ", ex);
            return null;
            // or throw an error
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        TypeReference<HashMap<String, String>> typeRef
                = new TypeReference<>() {
        };
        try {
            return objectMapper.readValue(dbData, typeRef);
        } catch (IOException ex) {
            log.error("Unexpected IOEx decoding json from database: " + dbData, ex);
            return null;
        }
    }
}