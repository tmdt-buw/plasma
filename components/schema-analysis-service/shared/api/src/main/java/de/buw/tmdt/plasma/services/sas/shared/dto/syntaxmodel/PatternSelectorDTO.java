package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;

import java.util.List;

@JsonTypeName("PatternSelectorDTO")
public class PatternSelectorDTO extends SelectorDTO{
	private static final long serialVersionUID = 2618339272865879181L;
	private static final String SELECTIONS_PROPERTY = "selections";
	private static final String OFFSET_PROPERTY = "offset";
	private static final String MODULUS_PROPERTY = "modulus";

	@JsonProperty(SELECTIONS_PROPERTY)
	private final List<Integer> selections;

	@JsonProperty(OFFSET_PROPERTY)
	private final int offset;

	@JsonProperty(MODULUS_PROPERTY)
	private final int modulus;

	@JsonCreator
	public PatternSelectorDTO(@JsonProperty(SELECTIONS_PROPERTY) List<Integer> selections,
	                          @JsonProperty(OFFSET_PROPERTY) int offset,
	                          @JsonProperty(MODULUS_PROPERTY) int modulus) {
		super();
		this.selections = selections;
		this.offset = offset;
		this.modulus = modulus;
	}

	@JsonProperty(SELECTIONS_PROPERTY)
	public List<Integer> getSelections() {
		return selections;
	}

	@JsonProperty(OFFSET_PROPERTY)
	public int getOffset() {
		return offset;
	}

	@JsonProperty(MODULUS_PROPERTY)
	public int getModulus() {
		return modulus;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"PatternSelectorDTO\""
		       + ", \"@super\":" + super.toString()
		       + ", \"selections\":" + StringUtilities.listToJson(selections)
		       + ", \"offset\":" + offset
		       + ", \"modulus\":" + modulus
		       + '}';
	}
}
