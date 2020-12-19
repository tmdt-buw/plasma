package de.buw.tmdt.plasma.services.kgs.shared.api;

import de.buw.tmdt.plasma.services.kgs.shared.dto.semanticmodel.SemanticModelDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping("/api/plasma-kgs/rdfconverter")
public interface RdfConversionApi {

	/**
	 * Converts a semantic model into RDF Turtle format.
	 *
	 * @param semanticModelDTO The semantic model to be converted
	 *
	 * @return A semantic model in RDF Turtle format
	 */
	@GetMapping(value = "/semanticModelToRdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	@NotNull ResponseEntity<String> convertSemanticModel(@NotNull @RequestBody SemanticModelDTO semanticModelDTO);

	/**
	 * Getter for the upper ontology of the KGS.
	 *
	 * @return A RDF Turtle formatted plain text that contains the upper ontology
	 */
	@GetMapping(value = "/upperOntology", produces = MediaType.TEXT_PLAIN_VALUE)
	@NotNull ResponseEntity<String> getUpperOntology();
}
