package de.buw.tmdt.plasma.services.sds.core.strategies;

import de.buw.tmdt.plasma.services.sds.shared.dto.Database;
import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;
import de.buw.tmdt.plasma.services.sds.shared.dto.request.RequestDTO;
import de.buw.tmdt.plasma.services.sds.shared.dto.response.ResponseDTO;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Search {
    private static final Logger logger = LoggerFactory.getLogger(Search.class);

    @NotNull
    public ResponseDTO runStrategy(@NotNull RequestDTO request) {
        Set<Database> databases = request.getDatabases();
        // Query all databases if no specific database was denoted
        if (databases == null || databases.isEmpty()) {
            databases = Arrays.stream(Database.values()).collect(Collectors.toSet());
        }

        // Extract concepts from data bases.
        logger.info("Extracting concepts from databases.");
        final Map<String, List<SDConceptDTO>> labelConceptsMap = extractConcepts(request.getLabels(), databases);
        return new ResponseDTO(labelConceptsMap);
    }

    @NotNull
    private Map<String, List<SDConceptDTO>> extractConcepts(@NotNull Set<String> labels, @NotNull Set<Database> databases) {
        final Map<String, List<SDConceptDTO>> labelMap = new HashMap<>();
        labels.forEach(label -> labelMap.put(label, new ArrayList<>()));

        if (databases.contains(Database.DATAMUSE)) {
            logger.info("Extracting concepts from DataMuse");
            for (Map.Entry<String, List<SDConceptDTO>> entry : queryDatamuse(labels).entrySet()) {
                labelMap.get(entry.getKey()).addAll(entry.getValue());
            }
        }

        if (databases.contains(Database.WORDNET)) {
            logger.info("Extracting concepts from WordNet");
            for (Map.Entry<String, List<SDConceptDTO>> entry : queryWordNet(labels).entrySet()) {
                labelMap.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        return labelMap;
    }

    @NotNull
    abstract Map<String, List<SDConceptDTO>> queryDatamuse(@NotNull Set<String> labels);

    @NotNull
    abstract Map<String, List<SDConceptDTO>> queryWordNet(@NotNull Set<String> labels);
}