package de.buw.tmdt.plasma.services.kgs.shared.feignclient;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "plasma-kgs",
        contextId = "query-kgs-client",
        configuration = KnowledgeGraphFeignConfiguration.class
)
public interface QueryApiClient {

}