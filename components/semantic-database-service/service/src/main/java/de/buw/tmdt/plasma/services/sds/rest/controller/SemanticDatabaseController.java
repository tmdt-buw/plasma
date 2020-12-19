package de.buw.tmdt.plasma.services.sds.rest.controller;

import de.buw.tmdt.plasma.services.sds.shared.dto.Database;
import de.buw.tmdt.plasma.services.sds.shared.api.SemanticDatabaseApi;
import de.buw.tmdt.plasma.services.sds.core.strategies.HypernymSearch;
import de.buw.tmdt.plasma.services.sds.core.strategies.HyponymSearch;
import de.buw.tmdt.plasma.services.sds.core.strategies.SynonymSearch;
import de.buw.tmdt.plasma.services.sds.shared.dto.request.RequestDTO;
import de.buw.tmdt.plasma.services.sds.shared.dto.response.ResponseDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SemanticDatabaseController implements SemanticDatabaseApi {

    private final SynonymSearch synonymStrategy;
    private final HypernymSearch hypernymStrategy;
    private final HyponymSearch hyponymStrategy;

    @Autowired
    public SemanticDatabaseController(
            @NotNull SynonymSearch synonymStrategy,
            @NotNull HypernymSearch hypernymStrategy,
            @NotNull HyponymSearch hyponymStrategy
    ) {
        this.synonymStrategy = synonymStrategy;
        this.hypernymStrategy = hypernymStrategy;
        this.hyponymStrategy = hyponymStrategy;
    }

    @Override
    public List<String> databases() {
        return Arrays.stream(Database.values()).map(Enum::name).collect(Collectors.toList());
    }

    @Override
    public ResponseDTO resultSynonyms(RequestDTO requestDTO) {
        return synonymStrategy.runStrategy(requestDTO);
    }

    @Override
    public ResponseDTO resultHypernyms(RequestDTO requestDTO) {
        return hypernymStrategy.runStrategy(requestDTO);
    }

    @Override
    public ResponseDTO resultHyponyms(RequestDTO requestDTO) {
        return hyponymStrategy.runStrategy(requestDTO);
    }

    @Override
    public ResponseDTO resultProcessedSynonyms(RequestDTO requestDTO) {
        return synonymStrategy.getProcessedSynonyms(requestDTO);
    }
}