package de.buw.tmdt.plasma.services.dps.api;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequestMapping(value = "/api/plasma-dps")
public interface DataProcessingApi {

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    SampleDTO uploadFile(@Nullable @RequestParam(value = "dataId", required = false) String dataId, @NotNull @RequestParam("file") MultipartFile file);

    @GetMapping(value = "/files/{dataId}", produces = MediaType.APPLICATION_JSON_VALUE)
    List<String> listFiles(@NotNull @PathVariable("dataId") String dataId);

    @GetMapping(value = "/available", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> isAvailable();

    @PostMapping(value = "/convert")
    String convertFile(@RequestBody CombinedModel template,
                       @RequestParam(value = "dataId") String dataId,
                       @RequestParam(value = "fileId", required = false) String fileId,
                       @RequestParam(value = "format", defaultValue = "turtle") String format);

}
