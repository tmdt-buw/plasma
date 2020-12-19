package de.buw.tmdt.plasma.services.dss.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.services.dss.shared.api.FileSystemApi;
import de.buw.tmdt.plasma.services.dss.core.handler.FileSystemHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("HardcodedFileSeparator")
@RestController("FileSystem Controller")
public class FileSystemController implements FileSystemApi {

	private final FileSystemHandler fileSystemHandler;

	@Autowired
	public FileSystemController(@NotNull FileSystemHandler fileSystemHandler) {
		this.fileSystemHandler = fileSystemHandler;
	}

	@Override
	@Operation(description = "Uploads a file to the local storage of the service")
	public String uploadFile(@NotNull MultipartFile file) {
		try {
			return new ObjectMapper().writeValueAsString(fileSystemHandler.uploadFile(file));
		} catch (JsonProcessingException ignore) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not convert return type");
		}
	}
}