package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
		@JsonSubTypes.Type(value = WildCardSelectorDTO.class, name = "WildCardSelectorDTO"),
		@JsonSubTypes.Type(value = IndexSelectorDTO.class, name = "IndexSelectorDTO"),
		@JsonSubTypes.Type(value = PatternSelectorDTO.class, name = "PatternSelectorDTO")
})
public abstract class SelectorDTO implements Serializable {

	private static final long serialVersionUID = -1942954119618039295L;

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SelectorDTO\""
		       + '}';
	}

}
