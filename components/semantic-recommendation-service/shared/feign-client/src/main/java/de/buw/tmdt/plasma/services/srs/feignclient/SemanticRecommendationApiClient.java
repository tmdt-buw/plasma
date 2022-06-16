package de.buw.tmdt.plasma.services.srs.feignclient;

import de.buw.tmdt.plasma.services.srs.api.SemanticRecommendationAPI;
import org.springframework.cloud.openfeign.FeignClient;

@SuppressWarnings("HardcodedFileSeparator - uris")
@FeignClient(
		name = "plasma-srs",
		contextId = "plasma-services",
		configuration = SemanticRecommendationFeignConfiguration.class
)
public interface SemanticRecommendationApiClient extends SemanticRecommendationAPI {

}