package de.buw.tmdt.plasma.services.sds.core.strategies;

import de.buw.tmdt.plasma.services.sds.core.api.SemanticDatabase;
import de.buw.tmdt.plasma.services.sds.core.util.HeuristicApplier;
import de.buw.tmdt.plasma.services.sds.shared.dto.Database;
import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;
import de.buw.tmdt.plasma.services.sds.shared.dto.request.RequestDTO;
import de.buw.tmdt.plasma.services.sds.shared.dto.response.ResponseDTO;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SynonymSearch extends Search {

    private final SemanticDatabase datamuseAPI;
    private final SemanticDatabase wordNetAPI;
    private final HeuristicApplier heuristicApplier;

    @Autowired
    public SynonymSearch(
            @NotNull @Qualifier("DatamuseDatabase") SemanticDatabase datamuseAPI,
            @NotNull @Qualifier("WordNetDatabase") SemanticDatabase wordNetAPI,
            @NotNull HeuristicApplier heuristicApplier
    ) {
        this.datamuseAPI = datamuseAPI;
        this.wordNetAPI = wordNetAPI;
        this.heuristicApplier = heuristicApplier;
    }

    @NotNull
    @Override
    protected Map<String, List<SDConceptDTO>> queryDatamuse(@NotNull Set<String> labels) {
        return generalQuery(labels, datamuseAPI);
    }

    @NotNull
    @Override
    protected Map<String, List<SDConceptDTO>> queryWordNet(@NotNull Set<String> labels) {
        return generalQuery(labels, wordNetAPI);
    }

    @NotNull
    private Map<String, List<SDConceptDTO>> generalQuery(@NotNull Set<String> labels, @NotNull SemanticDatabase semanticDatabaseAPI) {
        Map<String, List<SDConceptDTO>> result = new HashMap<>();
        labels.forEach(label -> {
            // Extract & convert the concept itself plus its related concepts.
            final List<SDConceptDTO> concepts = semanticDatabaseAPI.findSynonyms(label);
            List<SDConceptDTO> conceptsForThisLabel = new ArrayList<>(concepts);
            result.put(label, conceptsForThisLabel);
        });
        return result;
    }

    @NotNull
    public ResponseDTO getProcessedSynonyms(RequestDTO request) {

        Set<String> labels = request.getLabels();
        final Map<String, List<SDConceptDTO>> labelMap = request.getDatabases().stream()
                .map(db -> processedSynonymRetrieval(labels, db))
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (list1, list2) -> {
                            list1.addAll(list2);
                            return list1;
                        }
                ));

        return new ResponseDTO(labelMap);
    }

    //Lookup synonyms in the database and applies developed heuristics to find 'good' synonyms
    @NotNull
    private Map<String, List<SDConceptDTO>> processedSynonymRetrieval(
            @NotNull Set<String> labels,
            Database database
    ) {

        Map<String, List<SDConceptDTO>> result = new HashMap<>();

        //Initial query that finds concepts related (on a synonym bias) to the given labels
        //It is important to mention that BabelNet and WordNet return concepts that share the same main label
        //as the search word and holding the (sought after) synonyms as sets of strings and thus giving the need to lookup own concepts for these strings
        Map<String, List<SDConceptDTO>> initialQuery = queryWithSynStrat(labels, database);

        for (Map.Entry<String, List<SDConceptDTO>> query1Entry : initialQuery.entrySet()) {

            String currentLabel = query1Entry.getKey();

            List<SDConceptDTO> resultListForLabel = new LinkedList<>();
            List<SDConceptDTO> synConceptsForLabel = new LinkedList<>();
            //Datamuse synonym strings sorted after their scores
            List<String> datamuseStringsForLabel = queryWithSynStrat(
                    Collections.singleton(currentLabel), Database.DATAMUSE)
                    .values().stream()
                    .flatMap(List::stream)
                    .sorted(Comparator.comparingDouble(o -> -o.getScore()))
                    .map(SDConceptDTO::getLabel)
                    .collect(Collectors.toList());

            //Create a set of all notated synonym strings also intersect with the datamuse query
            Set<String> synonymStrings = query1Entry.getValue().stream()
                    .map(SDConceptDTO::getSynonyms)
                    .flatMap(Collection::stream)
                    .map(heuristicApplier::applyHeuristics)
                    .filter(datamuseStringsForLabel::contains)
                    .collect(Collectors.toSet());

            //in case datamuse is empty, use a default list
            if (datamuseStringsForLabel.isEmpty()) {
                datamuseStringsForLabel = new ArrayList<>(synonymStrings);
            }

            //A new query that looks up concepts for the corresponding extracted synonym strings
            Map<String, List<SDConceptDTO>> synonymConceptQuery = queryWithSynStrat(synonymStrings, database);

            for (String probableSyn : synonymStrings) {
                List<SDConceptDTO> probableSynConcepts = synonymConceptQuery.get(probableSyn);
                for (SDConceptDTO probSynConcept : probableSynConcepts) {
                    if (//only take concepts that differ in their main label (to avoid redundancy), check wether the concept contains the current label
                        //as synonym (reflexivity) and exclude concepts with special characters
                            !probSynConcept.getLabel().equals(currentLabel)
                                    && probSynConcept.getSynonyms().contains(query1Entry.getKey())
                                    && !probSynConcept.getLabel().matches("(.*)\\(.*?\\)(.*)")) {
                        synConceptsForLabel.add(probSynConcept);
                    }
                }
            }

            //Filter the reflexivity proofed concepts and sort them according to
            //the largest intersection of their synonyms (that are found inside the concept) with all found synonyms in general
            synConceptsForLabel = synConceptsForLabel.stream()
                    .map(concept -> Pair.of(
                            concept,
                            concept.getSynonyms().stream()
                                    .map(heuristicApplier::applyHeuristics)
                                    .filter(synonymStrings::contains)
                                    .count() //cardinality of the intersection
                    ))
                    .sorted(Comparator.comparing(Pair::getRight))
                    .map(Pair::getLeft)
                    .collect(Collectors.toList());

            //Because the datamuse strings are sorted by their highest scores, iterating through them and finding the first
            //matches with the synonym concepts allows to finally filter the concepts based on the datamuse score
            Set<String> stringFilter = new HashSet<>();
            for (String datamuseEntry : datamuseStringsForLabel) {
                for (SDConceptDTO synConcept : synConceptsForLabel) {
                    String probMainLabel = heuristicApplier.applyHeuristics(synConcept.getLabel());
                    if (probMainLabel.equals(datamuseEntry)
                            && !stringFilter.contains(probMainLabel)) {

                        stringFilter.add(probMainLabel);
                        resultListForLabel.add(synConcept);
                    }
                }
            }

            //restrict to 5 concepts for returns
            result.put(query1Entry.getKey(), resultListForLabel.subList(0, Math.min(resultListForLabel.size(), 5)));
        }

        return result;
    }

    //A general request is performed in order to run this method inside a loop with changing databases
    @NotNull
    private Map<String, List<SDConceptDTO>> queryWithSynStrat(Set<String> labels, Database database) {
        ResponseDTO responseForSynonyms = runStrategy(
                new RequestDTO(labels, Collections.singleton(database))
        );

        if (responseForSynonyms.getResultMap().values().size() == 0 ||
                responseForSynonyms.getResultMap().values().isEmpty()) {
            return Collections.emptyMap();
        }

        return responseForSynonyms.getResultMap();
    }
}