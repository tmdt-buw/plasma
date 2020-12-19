package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class SplitterDTO implements Serializable {
	private static final long serialVersionUID = -5015349436916583098L;

	private static final String PATTERN_PROPERTY = "pattern";

	@JsonProperty(PATTERN_PROPERTY)
	private String pattern;

	@JsonCreator
	public SplitterDTO(@NotNull @JsonProperty(PATTERN_PROPERTY) String pattern) {
		this.pattern = pattern;
	}

	@JsonProperty(PATTERN_PROPERTY)
	public String getPattern() {
		return pattern;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SplitterDTO\""
		       + ", \"pattern\":" + pattern
		       + '}';
	}
}
