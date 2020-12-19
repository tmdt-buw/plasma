package de.buw.tmdt.plasma.services.sds.shared.api;

import de.buw.tmdt.plasma.services.sds.shared.dto.request.RequestDTO;
import de.buw.tmdt.plasma.services.sds.shared.dto.response.ResponseDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping("/api/plasma-sds/semantic")
public interface SemanticDatabaseApi {

	@GetMapping(path = "/databases", produces = MediaType.APPLICATION_JSON_VALUE)
	List<String> databases();

	@PostMapping(path = "/synonyms", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseDTO resultSynonyms(@RequestBody RequestDTO requestDTO);

	@PostMapping(path = "/processedSynonyms", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseDTO resultProcessedSynonyms(@RequestBody RequestDTO requestDTO);

	@PostMapping(path = "/hypernyms", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseDTO resultHypernyms(@RequestBody RequestDTO requestDTO);

	@PostMapping(path = "/hyponyms", produces = MediaType.APPLICATION_JSON_VALUE)
	ResponseDTO resultHyponyms(@RequestBody RequestDTO requestDTO);
}