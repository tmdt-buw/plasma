package de.buw.tmdt.plasma.services.dps.shared.feignclient;

import de.buw.tmdt.plasma.services.dps.api.DataProcessingApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
		name = "plasma-dps",
		contextId = "data-processing-client",
		configuration = DataProcessingFeignConfiguration.class
)
public interface DataProcessingApiClient extends DataProcessingApi {
}
