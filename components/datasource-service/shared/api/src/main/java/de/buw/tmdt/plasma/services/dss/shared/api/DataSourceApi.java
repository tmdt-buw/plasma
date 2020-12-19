package de.buw.tmdt.plasma.services.dss.shared.api;

import de.buw.tmdt.plasma.services.dss.shared.dto.DataSourceDTO;
import de.buw.tmdt.plasma.services.dss.shared.dto.CreateDataSourceDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("HardcodedFileSeparator - uris")
@RequestMapping("/api/plasma-dss/datasource")
public interface DataSourceApi {

	@PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull DataSourceDTO createDataSource(@NotNull @RequestBody CreateDataSourceDTO dataSourceDTO);

	@GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull DataSourceDTO getDataSource(@NotNull @PathVariable("uuid") String uuid);

	@GetMapping(value = "/listAll", produces = MediaType.APPLICATION_JSON_VALUE)
	@NotNull List<DataSourceDTO> getAllDataSources();

	@DeleteMapping(value = "/{uuid}")
	void deleteDataSource(@NotNull @PathVariable("uuid") String uuid);
}