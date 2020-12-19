package de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members;

import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
@DynamicUpdate
@Table(name = "pattern_selectors")
public class PatternSelector extends Selector {
	private static final String PARSING_PREFIX = "p:";

	static {
		Selector.registerParser(PARSING_PREFIX, PatternSelector::parse);
	}

	private static final String MODULUS_SYMBOL = "%";
	private static final String OFFSET_SYMBOL = "+";
	private static final String INDEX_SEPARATOR_SYMBOL = ",";
	private static final String INDEX_LIST_START_SYMBOL = "{";
	private static final String INDEX_LIST_END_SYMBOL = "}";
	@ElementCollection
	private final List<Integer> selections;
	@Column
	private final int offset;
	@Column
	private final int modulus;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	public PatternSelector() {
		selections = null;
		offset = modulus = -1;
	}

	public PatternSelector(@NotNull List<Integer> selections, int offset, int modulus) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset must be positive but was " + offset);
		}
		if (modulus < 1) {
			throw new IllegalArgumentException("Modulus must be greater 0 but was " + modulus);
		}
		if (selections.isEmpty()) {
			throw new IllegalArgumentException("Selections list must not be empty but was.");
		}
		if (CollectionUtilities.collectionContains(selections, null)) {
			throw new IllegalArgumentException("Selections must not contain null.");
		}
		this.selections = new ArrayList<>(selections);
		this.offset = offset;
		this.modulus = modulus;
	}

	@Override
	public Type getType() {
		return Type.PATTERN;
	}

	public List<Integer> getSelections() {
		return Collections.unmodifiableList(selections);
	}

	public int getOffset() {
		return offset;
	}

	public int getModulus() {
		return modulus;
	}

	public static PatternSelector parse(@NotNull final String string) {
		final int modulus;
		final int offset;
		final List<Integer> indexes;

		if (!string.startsWith(PARSING_PREFIX)) {
			throw new IllegalArgumentException("String to parse did not start with `" + PARSING_PREFIX + "`: " + string);
		}

		String patternString = string.substring(PARSING_PREFIX.length());

		String[] tokens = patternString.split(Pattern.quote(MODULUS_SYMBOL));
		if (tokens.length != 2) {
			throw new IllegalArgumentException(
					"Expected exactly one " + MODULUS_SYMBOL + " but found " + (tokens.length - 1) + " in `" + patternString + "`."
			);
		}
		String indexList = tokens[0];
		tokens = tokens[1].split(Pattern.quote(OFFSET_SYMBOL));
		if (tokens.length != 2) {
			throw new IllegalArgumentException("Expected exactly one " + OFFSET_SYMBOL + " but found " + (tokens.length - 1) + " in `" + patternString + "`.");
		}
		if (!indexList.startsWith(INDEX_LIST_START_SYMBOL)) {
			throw new IllegalArgumentException("Index list does not start with " + INDEX_LIST_START_SYMBOL + " in `" + patternString + "`.");
		}
		if (!indexList.endsWith(INDEX_LIST_END_SYMBOL)) {
			throw new IllegalArgumentException("Index list does not end with " + INDEX_LIST_END_SYMBOL + " in `" + patternString + "`.");
		}
		try {
			modulus = Integer.parseInt(tokens[0]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Failed to parse modulus token `" + tokens[0] + "` as integer in `" + patternString + "`.", e);
		}

		try {
			offset = Integer.parseInt(tokens[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Failed to parse modulus token `" + tokens[1] + "` as integer in `" + patternString + "`.", e);
		}
		indexes = Arrays.stream(indexList.substring(1, indexList.length() - 1).split(Pattern.quote(INDEX_SEPARATOR_SYMBOL)))
				.map(indexString -> {
					try {
						return Integer.parseInt(indexString);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Failed to parse index token `" + indexString + "` as integer in `" + patternString + "`.", e);
					}
				}).collect(Collectors.toList());

		return new PatternSelector(indexes, offset, modulus);
	}

	@NotNull
	@Override
	public String serialize() {
		StringJoiner stringJoiner = new StringJoiner(INDEX_SEPARATOR_SYMBOL, PARSING_PREFIX + INDEX_LIST_START_SYMBOL, INDEX_LIST_END_SYMBOL);
		selections.forEach(index -> stringJoiner.add(index.toString()));
		return stringJoiner.toString() + MODULUS_SYMBOL + modulus + OFFSET_SYMBOL + offset;
	}

	@Override
	@NotNull
	public Selector copy() {
		return new PatternSelector(new ArrayList<>(this.selections), this.offset, this.modulus);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"PatternSelector\""
		       + ", \"@super\":" + super.toString()
		       + ", \"selections\":" + StringUtilities.listToJson(selections)
		       + ", \"offset\":\"" + offset + '"'
		       + ", \"modulus\":\"" + modulus + '"'
		       + '}';
	}
}
