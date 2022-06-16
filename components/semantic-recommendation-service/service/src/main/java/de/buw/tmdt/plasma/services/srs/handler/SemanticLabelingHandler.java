package de.buw.tmdt.plasma.services.srs.handler;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemanticLabelingHandler {

    private static final Logger log = LoggerFactory.getLogger(SemanticLabelingHandler.class);

    @Value("${plasma.ars.labeling.url:}")
    private List<String> labelingServiceURLs;

    public SemanticLabelingHandler() {
    }

    @NotNull
    public CombinedModel performLabeling(@NotNull String uuid, String configId, String configToken, @NotNull CombinedModel combinedModel) {
        labelingServiceURLs = labelingServiceURLs.stream()
                .filter(string -> !string.isBlank())
                .collect(Collectors.toList());
        if (labelingServiceURLs.isEmpty()) {
            log.debug("No labeling services defined");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "No labeling services are defined to process your request.");
        }
        // we only get the first for now
        String url = labelingServiceURLs.get(0);
        try {
            // call ARS-L services here
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
            CombinedModel withLabeling = combinedModelMono.block();
            log.info("Performed labeling for {} using service: {}", uuid, url);
            if (withLabeling == null) {
                log.warn("Result was NULL. Fallback to input model.");
                return combinedModel;
            }
            return withLabeling;
        } catch (Exception ex) {
            log.warn("Could not perform semantic labeling using service {}", url, ex);
        }

        return combinedModel;
    }
}
