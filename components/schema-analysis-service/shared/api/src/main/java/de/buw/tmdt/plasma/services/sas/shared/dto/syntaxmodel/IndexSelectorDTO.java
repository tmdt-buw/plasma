package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("IndexSelectorDTO")
public class IndexSelectorDTO extends SelectorDTO {
	private static final long serialVersionUID = 7161514810881655797L;
	private static final String ELEMENT_INDEX_PROPERTY = "elementIndex";

	@JsonProperty(ELEMENT_INDEX_PROPERTY)
	private int elementIndex;

	@JsonCreator
	public IndexSelectorDTO(@JsonProperty(ELEMENT_INDEX_PROPERTY) int elementIndex) {
		this.elementIndex = elementIndex;
	}

	@JsonProperty(ELEMENT_INDEX_PROPERTY)
	public int getElementIndex() {
		return elementIndex;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"IndexSelectorDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + ", \"elementIndex\":" + elementIndex
		       + '}';
	}
}
