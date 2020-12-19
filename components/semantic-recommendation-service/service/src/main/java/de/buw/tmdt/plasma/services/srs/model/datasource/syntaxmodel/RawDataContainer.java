package de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel;

import java.util.List;

public interface RawDataContainer {
	String getCleansingPattern();

	List<String> getExamples();
}
