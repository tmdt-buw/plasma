package de.buw.tmdt.plasma.services.kgs.shared.feignclient;

import de.buw.tmdt.plasma.services.kgs.shared.api.OntologyApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "plasma-kgs",
        contextId = "universal-graph-kgs-client",
        configuration = KnowledgeGraphFeignConfiguration.class
)
public interface OntologyApiClient extends OntologyApi {

}