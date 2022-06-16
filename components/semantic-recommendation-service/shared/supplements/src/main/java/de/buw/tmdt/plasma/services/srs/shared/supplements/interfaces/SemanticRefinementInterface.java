package de.buw.tmdt.plasma.services.srs.shared.supplements.interfaces;

import de.buw.tmdt.plasma.datamodel.CombinedModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

public interface SemanticRefinementInterface {

    @NotNull
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CombinedModel performRefinement(@NotNull @RequestParam("uuid") String uuid,
                                    @RequestParam("configId") String configId,
                                    @NotNull @RequestBody CombinedModel model);
}
