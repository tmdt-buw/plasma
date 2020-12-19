package de.buw.tmdt.plasma.ars.refinement.gr.shared.feignclient;

import de.buw.tmdt.plasma.ars.refinement.gr.shared.api.GeneralizationRecommendationAPI;
import org.springframework.cloud.openfeign.FeignClient;

@SuppressWarnings("HardcodedFileSeparator - uris")
@FeignClient(
        name = "plasma-ars-r-gr",
        contextId = "plasma-ars",
        configuration = LabelMatchingFeignConfiguration.class
)
public interface GeneralizationRecommendationApiClient extends GeneralizationRecommendationAPI {

}