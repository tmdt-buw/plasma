package de.buw.tmdt.plasma.services.sds.core.api;

import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SemanticDatabase {

	@NotNull
	List<SDConceptDTO> findSynonyms(@NotNull String word);

	@NotNull
	List<SDConceptDTO> findHyponyms(@NotNull String word);

	@NotNull
	List<SDConceptDTO> findHypernyms(@NotNull String word);

}