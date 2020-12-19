package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("WildCardSelectorDTO")
public class WildCardSelectorDTO extends SelectorDTO {

	private static final long serialVersionUID = -7769301104325905677L;

	@JsonCreator
	public WildCardSelectorDTO() {
		super();
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"WildCardSelectorDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + '}';
	}
}
