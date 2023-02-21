package de.buw.tmdt.plasma.ars.labeling.lm.shared.api;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping(value = "/api/plasma-ars-l-lm")
public interface LabelMatchingAPI {

    @NotNull
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel performLabeling(@NotNull @RequestParam("uuid") String uuid,
                                  @RequestParam(value = "configId", required = false) String configId,
                                  @RequestParam(value = "configToken", required = false) String configToken,
                                  @NotNull @RequestBody CombinedModel model);

}
