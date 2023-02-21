package de.buw.tmdt.plasma.services.srs.handler;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.datamodel.modification.DeltaModification;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConfigurationProperties(prefix = "plasma.ars.refinement")
public class SemanticRefinementHandler {

    private static final Logger log = LoggerFactory.getLogger(SemanticModelingHandler.class);

    @Value("${plasma.ars.refinement.prefix:ARS-R-}")
    private String prefix;

    private List<String> urls = new ArrayList<>();

    public SemanticRefinementHandler() {
    }

    /**
     * Calls one or more ARS-R services.
     *
     * @param uuid          The data source uuid which services use to associate results
     * @param combinedModel The current state of the model
     * @param configId      A config id to provide additional information
     * @param configToken   A token to access or update the config
     * @return The updated model or the original one if no services were called
     */
    @NotNull
    public CombinedModel performRefinement(@NotNull String uuid, String configId, String configToken, @NotNull CombinedModel combinedModel) {
        urls = urls.stream()
                .filter(string -> !string.isBlank())
                .collect(Collectors.toList());
        if (urls.isEmpty()) {
            log.debug("No refinement services defined");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No refinement services are defined to process your request.");
        }
        combinedModel.getRecommendations().removeIf(dm -> dm.getReference().startsWith(prefix));
        List<DeltaModification> modifications = new ArrayList<>();
        for (String url : urls) {
            try {
                log.debug("Performing refinement for {} using service: {}", uuid, url);
                WebClient webClient = WebClient.builder()
                        .baseUrl(url)
                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .build();

                Mono<CombinedModel> combinedModelMono = webClient.post()
                        .uri(uriBuilder -> uriBuilder
                                .queryParam("uuid", uuid)
                                .queryParam("configId", configId)
                                .queryParam("configToken", configToken)
                                .build()
                        )

                        .bodyValue(combinedModel)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(CombinedModel.class);
                CombinedModel withRefinement = combinedModelMono.block();

                if (withRefinement == null) {
                    log.debug("Result was NULL.");
                    continue;
                }
                modifications.addAll(withRefinement.getRecommendations());
            } catch (Exception ex) {
                log.warn("Could not perform semantic refinement using service {}", url, ex);
            }
        }
        modifications.removeIf(dm -> !dm.getReference().startsWith(prefix));
        combinedModel.getRecommendations().addAll(modifications);

        return combinedModel;
    }

    public List<String> getUrls() {
        return urls;
    }
}
