package de.buw.tmdt.plasma.services.dps.rest.controller;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import de.buw.tmdt.plasma.services.dps.api.DataProcessingApi;
import de.buw.tmdt.plasma.services.dps.api.SampleDTO;
import de.buw.tmdt.plasma.services.dps.core.DataProcessingHandler;
import de.buw.tmdt.plasma.services.dps.core.DataStorageHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController("Data Processing Controller")
public class DataProcessingController implements DataProcessingApi {

	private final DataProcessingHandler dataProcessingHandler;
	private final DataStorageHandler dataStorageHandler;


	public DataProcessingController(DataProcessingHandler dataProcessingHandler, DataStorageHandler dataStorageHandler) {
		this.dataProcessingHandler = dataProcessingHandler;
		this.dataStorageHandler = dataStorageHandler;
	}

	@Override
	@Operation(description = "Initializes a new modeling.")
	public SampleDTO uploadFile(@Nullable String dataId, @NotNull MultipartFile file) {
		return dataStorageHandler.storeFile(dataId, file);
	}

	@Override
	@Operation(description = "List all files of a dataId.")
	public List<String> listFiles(@NotNull String dataId) {
		return dataStorageHandler.listFiles(dataId).stream().map(File::getName).sorted().collect(Collectors.toList());
	}

	@Override
	@Operation(description = "Check if service available.")
	public ResponseEntity<String> isAvailable() {
		return new ResponseEntity<>("OK", HttpStatus.OK);
	}

	@Override
	public String convertFile(CombinedModel template, String dataId, String fileId, String format) {
		// ignore format parameter for now
		return dataProcessingHandler.processFile(template,dataId, fileId);
	}
}
