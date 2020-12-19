package de.buw.tmdt.plasma.services.kgs.shared.api;

import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping("/api/plasma-kgs/query")
public interface QueryApi {

    /* ********************************************** QUERY **************************************************/

    @GetMapping(value = "/byEntityConcept", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull Set<SemanticModelDTO> searchByEntityConcept(@NotNull @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm);

    @GetMapping(value = "/byEntityType", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull Set<SemanticModelDTO> searchByEntityType(@NotNull @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm);

    @GetMapping(value = "/bySynonym", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull Set<SemanticModelDTO> searchBySynonym(@NotNull @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm);

    @GetMapping(value = "/allByLabel", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull Set<SemanticModelDTO> searchAllByLabel(@NotNull @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm);

    @GetMapping(value = "/byEntityTypeOrEntityConceptContainingLabel", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull Set<SemanticModelDTO> searchByEntityTypeOrEntityConceptContainingLabel(
            @NotNull @RequestParam(value = "searchTerm", defaultValue = "", required = false) String searchTerm
    );
}
