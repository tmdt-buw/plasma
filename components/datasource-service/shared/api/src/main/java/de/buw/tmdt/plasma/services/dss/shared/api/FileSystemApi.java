package de.buw.tmdt.plasma.services.dss.shared.api;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings("HardcodedFileSeparator - uris")
@RequestMapping(value = "/api/filesystem")
public interface FileSystemApi {

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	String uploadFile(@NotNull @RequestParam("file") MultipartFile file);
}