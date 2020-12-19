package de.buw.tmdt.plasma.services.sas.shared.api;

import de.buw.tmdt.plasma.services.sas.shared.dto.SchemaAnalysisDataProvisionDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.StandardDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.StandardProvisionDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.NodeDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/plasma-sas/analysis")
@SuppressWarnings("HardcodedFileSeparator - uris")
public interface AnalysisApi {

	@PostMapping(path = "/{id}/start", produces = MediaType.APPLICATION_JSON_VALUE)
	void initAnalysis(@PathVariable("id") String id);

	@PostMapping(path = "/{id}/add", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	void addDataPoint(@PathVariable("id") String id, @RequestBody SchemaAnalysisDataProvisionDTO schemaAnalysisDataProvisionDTO);

	@GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull NodeDTO getResult(@PathVariable("id") String id, @RequestParam(value = "exampleLimit", defaultValue = "100") int exampleLimit);

	@GetMapping(path = "/{id}/ready", produces = MediaType.APPLICATION_JSON_VALUE)
	boolean isReady(@PathVariable("id") String id);

	@PostMapping(path = "/{id}/finish", produces = MediaType.APPLICATION_JSON_VALUE)
	void finish(@PathVariable("id") String id);

	@GetMapping(path = "/{id}/existing", produces = MediaType.APPLICATION_JSON_VALUE)
	boolean existing(@PathVariable("id") String id);

	@DeleteMapping(path = "/{id}")
	void delete(@PathVariable("id") String id);
}
