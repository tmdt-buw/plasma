package de.buw.tmdt.plasma.services.dss.rest.controller;

import de.buw.tmdt.plasma.services.dss.shared.api.DataSourceApi;
import de.buw.tmdt.plasma.services.dss.core.handler.DataSourceHandler;
import de.buw.tmdt.plasma.services.dss.shared.dto.CreateDataSourceDTO;
import de.buw.tmdt.plasma.services.dss.shared.dto.DataSourceDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("Data Source Controller")
public class DataSourceController implements DataSourceApi {

	private final DataSourceHandler dataSourceHandler;

	public DataSourceController(@NotNull DataSourceHandler dataSourceHandler) {
		this.dataSourceHandler = dataSourceHandler;
	}

	@Override
	@Operation(description = "Creates a new data source")
	public @NotNull DataSourceDTO createDataSource(@NotNull CreateDataSourceDTO dataSourceDTO) {
		return dataSourceHandler.createDataSource(dataSourceDTO);
	}

	@Override
	@Operation(description = "Retrieves a data source")
	public @NotNull DataSourceDTO getDataSource(@NotNull String uuid) {
		return dataSourceHandler.getDataSource(uuid);
	}

	@Override
	@Operation(description = "Retrieves all data sources")
	public @NotNull List<DataSourceDTO> getAllDataSources() {
		return dataSourceHandler.getAllDataSources();
	}


	@Override
	@Operation(description = "Deletes a data source")
	public void deleteDataSource(@NotNull String uuid) {
		dataSourceHandler.deleteDataSource(uuid);
	}
}
