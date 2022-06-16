package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.regex.Pattern;

public class Splitting implements Serializable {
	private static final long serialVersionUID = -5015349436916583098L;

	private static final String PATTERN_PROPERTY = "pattern";

	@JsonProperty(PATTERN_PROPERTY)
	private String pattern;

	@JsonIgnore
	private Pattern compiledPattern;

	@JsonCreator
	public Splitting(@NotNull @JsonProperty(PATTERN_PROPERTY) String pattern) {

		this.pattern = pattern;
		this.compiledPattern = Pattern.compile(pattern);
	}

	@JsonProperty(PATTERN_PROPERTY)
	public String getPattern() {
		return pattern;
	}

	public Splitting copy() {
		return new Splitting(getPattern());
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"" + this.getClass().getSimpleName() + "\""
				+ ", \"pattern\":" + pattern
				+ '}';
	}

	@NotNull
	public Pair<String, String> apply(@NotNull String s) {
		String[] tokens = compiledPattern.split(s, 2);
		if (tokens.length != 1) {
			return new Pair<>(tokens[0], tokens[1]);
		} else {
			return new Pair<>(tokens[0], "");
		}
	}
}
