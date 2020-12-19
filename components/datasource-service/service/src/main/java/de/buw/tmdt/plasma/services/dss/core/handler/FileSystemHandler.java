package de.buw.tmdt.plasma.services.dss.core.handler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileSystemHandler {

	private static final Logger logger = LoggerFactory.getLogger(FileSystemHandler.class);

	@NotNull
	@SuppressFBWarnings("PATH_TRAVERSAL_IN")
	public String uploadFile(@NotNull MultipartFile file) {
		String filename = UUID.randomUUID().toString() + FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			logger.info("Uploading File");
			byte[] bytes = file.getBytes();
			Files.write(Paths.get(filename), bytes);
			logger.info("Uploaded File");
			return filename;
		} catch (IOException ignore) {
			logger.error("Error while uploading file");
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"File with the name"
					+ filename +
					"could not be written to the file system."
			);
		}
	}
}