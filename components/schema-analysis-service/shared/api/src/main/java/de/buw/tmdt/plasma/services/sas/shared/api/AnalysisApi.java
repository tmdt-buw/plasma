package de.buw.tmdt.plasma.services.sas.shared.api;

import de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel;
import de.buw.tmdt.plasma.services.sas.shared.dto.SchemaAnalysisDataProvisionDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provides endpoints for schema analysis operations.
 * Expected usage pattern:
 * - initAnalysis
 * - addDataPoint (multiple times)
 * - finish
 * - ready (until it returns true)
 * - getResult
 * - delete
 */
@RequestMapping("/api/plasma-sas/analysis")
@SuppressWarnings("HardcodedFileSeparator - uris")
public interface AnalysisApi {

	@PostMapping(path = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
	void initAnalysis(@PathVariable("id") String id);

	@PostMapping(path = "/{id}/add", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	void addDataPoint(@PathVariable("id") String id, @RequestBody SchemaAnalysisDataProvisionDTO schemaAnalysisDataProvisionDTO);

	@PostMapping(path = "/{id}/add/multiple", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	void addDataPoints(@PathVariable("id") String id, @RequestBody List<SchemaAnalysisDataProvisionDTO> schemaAnalysisDataProvisionDTOs);

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull SyntaxModel getResult(@PathVariable("id") String id, @RequestParam(value = "exampleLimit", defaultValue = "100") int exampleLimit);

	@GetMapping(path = "/{id}/ready", produces = MediaType.APPLICATION_JSON_VALUE)
	boolean isReady(@PathVariable("id") String id);

	@PostMapping(path = "/{id}/finish", produces = MediaType.APPLICATION_JSON_VALUE)
	void finish(@PathVariable("id") String id);

	@GetMapping(path = "/{id}/existing", produces = MediaType.APPLICATION_JSON_VALUE)
	boolean existing(@PathVariable("id") String id);

	@DeleteMapping(path = "/{id}")
	void delete(@PathVariable("id") String id);
}
