package de.buw.tmdt.plasma.services.kgs.shared.feignclient;

import de.buw.tmdt.plasma.services.kgs.shared.api.QueryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
		name = "plasma-kgs",
		contextId = "query-kgs-client",
		configuration = KnowledgeGraphFeignConfiguration.class
)
public interface QueryApiClient extends QueryApi {

}