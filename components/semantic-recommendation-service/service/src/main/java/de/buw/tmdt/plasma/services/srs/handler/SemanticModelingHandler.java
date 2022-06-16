package de.buw.tmdt.plasma.services.srs.handler;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SemanticModelingHandler {

    private static final Logger log = LoggerFactory.getLogger(SemanticModelingHandler.class);

    @Value("${plasma.ars.modeling.url:}")
    private List<String> modelingServiceURLs;

    public SemanticModelingHandler() {
    }

    /**
     * Calls one or more ARS-M services.
     *
     * @param uuid          The data source uuid which services use to associate results
     * @param combinedModel The current state of the model
     * @param configId      A config id to provide additional information
     * @param configToken   A token to access or update the config
     * @return The updated model or the original one if no services were called
     */
    @NotNull
    public CombinedModel performModeling(@NotNull String uuid, String configId, String configToken, @NotNull CombinedModel combinedModel) {
        modelingServiceURLs = modelingServiceURLs.stream()
                .filter(string -> !string.isBlank())
                .collect(Collectors.toList());
        if (modelingServiceURLs.isEmpty()) {
            log.debug("No modeling services defined");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No modeling services are defined to process your request.");
        }
        // we only get the first for now
        String url = modelingServiceURLs.get(0);
        try {
            // call ARS-M services here
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
            CombinedModel withModeling = combinedModelMono.block();
            log.info("Performed modeling for {} using service: {}", uuid, url);
            if (withModeling == null) {
                log.warn("Result was NULL. Fallback to input model.");
                return combinedModel;
            }
            return withModeling;
        } catch (Exception ex) {
            log.warn("Could not perform semantic modeling using service {}", url, ex);
        }
        return combinedModel;
    }


}
