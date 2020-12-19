package de.buw.tmdt.plasma.services.kgs.shared.api;

import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.EntityTypeDTO;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelCreationResponse;
import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;

@SuppressWarnings("HardcodedFileSeparator - API URLs are OK")
@RequestMapping("/api/plasma-kgs/localknowledge")
public interface LocalKnowledgeApi {

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull SemanticModelCreationResponse createSemanticModel(@NotNull @RequestBody SemanticModelDTO request);

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @NotNull SemanticModelDTO getSemanticModel(@NotNull @PathVariable("id") String id);

    @DeleteMapping(value = "/{id}")
    void deleteSemanticModel(@NotNull @PathVariable("id") String id);

    /**
     * Converts a semantic model into a specified format.
     *
     * @param id     The id of the semantic model in the knowledge graph.
     * @param format The format of the output (Turtle or RDF/XML)
     * @param flat   A flag that indicates whether entity types should be mapped to entity concepts in the RDF output.
     *
     * @return A semantic model in RDF Turtle format
     */
    @GetMapping(value = "/{id}/export", produces = MediaType.TEXT_PLAIN_VALUE)
    @NotNull ResponseEntity<String> convertSemanticModel(
            @NotNull @PathVariable("id") String id,
            @NotNull @RequestParam(name = "format", defaultValue = "turtle") String format,
            @RequestParam(defaultValue = "false", name = "flat") boolean flat
    ) throws MalformedURLException;

    @GetMapping(value = "/entityType", produces = MediaType.APPLICATION_JSON_VALUE)
    List<EntityTypeDTO> getEntityTypes(@NotNull @RequestParam("ids") List<String> ids);

    @GetMapping(value = "/byDataSourceId/{dataSourceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    SemanticModelDTO getSemanticModelByDataSourceId(@NotNull String dataSourceId);

    @DeleteMapping(value = "/byDataSourceId/{dataSourceId}")
    void deleteSemanticModelByDataSourceId(@NotNull String dataSourceId);
}