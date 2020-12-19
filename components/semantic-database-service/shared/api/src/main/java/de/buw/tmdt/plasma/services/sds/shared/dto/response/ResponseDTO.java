package de.buw.tmdt.plasma.services.sds.shared.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseDTO {

	private static final String PROPERTY_RESULT = "result";

	private final Map<String, List<SDConceptDTO>> result;

	@JsonCreator
	public ResponseDTO(@JsonProperty(PROPERTY_RESULT) Map<String, List<SDConceptDTO>> result) {
		this.result = new HashMap<>(result);
	}

	@JsonProperty(PROPERTY_RESULT)
	public Map<String, List<SDConceptDTO>> getResultMap() {
		return Collections.unmodifiableMap(result);
	}
}