package de.buw.tmdt.plasma.services.kgs.shared.feignclient;

import de.buw.tmdt.plasma.services.kgs.shared.api.RdfConversionApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
		name = "plasma-kgs",
		contextId = "rdf-conversion-client",
		configuration = KnowledgeGraphFeignConfiguration.class
)
public interface RdfConversionApiClient extends RdfConversionApi {
}
