package de.buw.tmdt.plasma.services.dps.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.buw.tmdt.plasma.converter.csv.CSVConverter;
import de.buw.tmdt.plasma.converter.esri.ESRIConverter;
import de.buw.tmdt.plasma.converter.geojson.GeoJSONConverter;
import de.buw.tmdt.plasma.services.dps.api.SampleDTO;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressFBWarnings({"PATH_TRAVERSAL_IN", "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
public class DataStorageHandler {

    @Value("${plasma.dps.storage.directory:./storage}")
    String storageDirectoryString;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(DataStorageHandler.class);

    @PostConstruct
    public void initStorage() {
        log.info("Storage directory: {}", storageDirectoryString);
        File validationPath = new File(storageDirectoryString);
        if (!validationPath.exists()) {
            if (!validationPath.mkdirs()) {
                throw new RuntimeException("Could not init folder for data");
            }
        }
    }

    /**
     * Processes a file and returns the converted data.
     *
     * @param dataId data id, can be null
     * @param file   file to process
     * @return sample dto consisting of data id and sample
     */
    public SampleDTO storeFile(@Nullable String dataId, @NotNull MultipartFile file) {

        ZonedDateTime currentTime = ZonedDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        String dateString = dateTimeFormatter.format(currentTime);

        // Generate data id if not provided
        if (dataId == null) {
            dataId = UUID.randomUUID().toString();
        }

        File folder = Paths.get(storageDirectoryString, dataId).toFile();

        // Create folder if not existing
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                log.warn("Could not create directory for id " + dataId);
            }
        }

        // Check how many files are already existing to increase version
        int numberOfFiles = folder.list() == null ? 0 : Objects.requireNonNull(folder.list()).length;

        Path newFilePath = Paths.get(storageDirectoryString, dataId, dateString + "_upload_" + (numberOfFiles + 1) + ".json"); // nur json speichern -> volume

        String fileEnding = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
        String result = "";


        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if ("geojson".equals(fileEnding)) {
                result = GeoJSONConverter.convert(content);
            } else if ("zip".equalsIgnoreCase(fileEnding)) {
                // Convert ESRI to JSON
                byte[] bytes = file.getBytes();
                // Create temp file
                Path newFilePathTemp = Paths.get(storageDirectoryString, dataId, dateString + "_upload_" + (numberOfFiles + 1) + "_temp"); //
                Files.write(newFilePathTemp, bytes);
                // extract contents from temp file
                String geoJSON = ESRIConverter.convertToGeoJSON(newFilePathTemp);
                result = GeoJSONConverter.convert(geoJSON);
                Files.deleteIfExists(newFilePathTemp);

            } else if ("csv".equalsIgnoreCase(fileEnding)) {
                // Convert CSV to JSON
                result = CSVConverter.convert(content, 1);
            } else if ("json".equalsIgnoreCase(fileEnding)) {
                result = content;
            }
        } catch (IOException | de.buw.tmdt.plasma.converter.ConversionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not convert file"
            );
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(newFilePath.toFile());
            byte[] strToBytes = result.getBytes(StandardCharsets.UTF_8);
            outputStream.write(strToBytes);
            outputStream.close();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store file"
            );
        }

        List<String> jsonSampleList = new ArrayList<>();
        try {
            JsonNode obj = objectMapper.readTree(result);
            if (obj.isArray()) {
                int counter = 0;

                for (JsonNode element : obj) {
                    counter++;
                    jsonSampleList.add(element.toString());
                    if (counter >= 10) {
                        break;
                    }
                }
            } else {
                jsonSampleList.add(obj.toString());
            }
        } catch (JsonProcessingException e) {
            if (!newFilePath.toFile().delete()) {
                log.error("Could not properly delete file");
            }
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Formatting error in JSON file"
            );
        }

        return new SampleDTO(dataId, newFilePath.getFileName().toString(), jsonSampleList);
    }

    /**
     * Get all files for a given dataId.
     *
     * @param dataId data id to fetch data for
     * @return sorted list of files
     */
    public List<File> listFiles(String dataId) {
        File folder = Paths.get(storageDirectoryString, dataId).toFile();
        List<File> fileList = new ArrayList<>();
        if (folder.exists()) {
            Collections.addAll(fileList, Objects.requireNonNull(folder.listFiles()));
        }
        return fileList.stream().sorted().collect(Collectors.toList());
    }

}
