package de.buw.tmdt.plasma.services.dms.shared.feignclient;

import de.buw.tmdt.plasma.services.dms.shared.api.ModelingAPI;
import org.springframework.cloud.openfeign.FeignClient;

@SuppressWarnings("HardcodedFileSeparator - uris")
@FeignClient(
        name = "plasma-dms",
        contextId = "plasma-services",
        configuration = DataModelingFeignConfiguration.class
)
public interface DataModelingApiClient extends ModelingAPI {

}