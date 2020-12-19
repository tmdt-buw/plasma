package de.buw.tmdt.plasma.services.sds.shared.feignclient;

import de.buw.tmdt.plasma.services.sds.shared.api.SemanticDatabaseApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "plasma-sds",
        contextId = "sds-client",
        configuration = SemanticDatabaseFeignConfiguration.class
)
public interface SemanticDatabaseClient extends SemanticDatabaseApi {
}
