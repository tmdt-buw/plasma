package de.buw.tmdt.plasma.ars.labeling.lm.shared.feignclient;

import de.buw.tmdt.plasma.ars.labeling.lm.shared.api.LabelMatchingAPI;
import org.springframework.cloud.openfeign.FeignClient;

@SuppressWarnings("HardcodedFileSeparator - uris")
@FeignClient(
        name = "plasma-ars-l-lm",
        contextId = "plasma-ars",
        configuration = LabelMatchingFeignConfiguration.class
)
public interface LabelMatchingApiClient extends LabelMatchingAPI {

}