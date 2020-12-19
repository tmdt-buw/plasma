package de.buw.tmdt.plasma.services.sas.shared.feignclient;

import de.buw.tmdt.plasma.services.sas.shared.api.AnalysisApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
		name = "plasma-sas",
		contextId = "schema-analysis-client",
		configuration = SchemaAnalysisFeignConfiguration.class
)
public interface SchemaAnalysisApiClient extends AnalysisApi {
}
