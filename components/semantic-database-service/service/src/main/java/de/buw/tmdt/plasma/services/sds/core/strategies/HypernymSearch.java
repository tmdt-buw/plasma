package de.buw.tmdt.plasma.services.sds.core.strategies;

import de.buw.tmdt.plasma.services.sds.core.api.SemanticDatabase;
import de.buw.tmdt.plasma.services.sds.shared.dto.concept.SDConceptDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HypernymSearch extends Search {

	private final SemanticDatabase datamuseAPI;
	private final SemanticDatabase wordNetAPI;

	@Autowired
	public HypernymSearch(
			@NotNull @Qualifier("DatamuseDatabase") SemanticDatabase datamuseAPI,
			@NotNull @Qualifier("WordNetDatabase") SemanticDatabase wordNetAPI
	) {
		this.datamuseAPI = datamuseAPI;
		this.wordNetAPI = wordNetAPI;
	}

	@NotNull
	@Override
	protected Map<String, List<SDConceptDTO>> queryDatamuse(@NotNull Set<String> labels) {
		return generalQuery(labels, datamuseAPI);
	}

	@NotNull
	@Override
	protected Map<String, List<SDConceptDTO>> queryWordNet(@NotNull Set<String> labels) {
		return generalQuery(labels, wordNetAPI);
	}

	@NotNull
	private Map<String, List<SDConceptDTO>> generalQuery(@NotNull Set<String> labels, @NotNull SemanticDatabase semanticDatabaseAPI){
		Map<String, List<SDConceptDTO>> result = new HashMap<>();
		labels.forEach(label -> {
			// Extract & convert the concept itself plus its related concepts.
			final List<SDConceptDTO> concepts = semanticDatabaseAPI.findHypernyms(label);
			List<SDConceptDTO> conceptsForThisLabel = new ArrayList<>(concepts);
			result.put(label, conceptsForThisLabel);
		});
		return result;
	}
}