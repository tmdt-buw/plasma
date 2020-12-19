package de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel.members;

import de.buw.tmdt.plasma.services.srs.model.ModelBase;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Selector extends ModelBase {

	private static final Map<String, Function<? super String, ? extends Selector>> PARSER_LOOKUP = new HashMap<>();

	public static Selector parse(@NotNull String serializedSelector) {
		for (Map.Entry<String, Function<? super String, ? extends Selector>> entry : PARSER_LOOKUP.entrySet()) {
			if (serializedSelector.startsWith(entry.getKey())) {
				return entry.getValue().apply(serializedSelector);
			}
		}
		throw new IllegalArgumentException("Did not find parser for string: " + serializedSelector);
	}

	protected static void registerParser(@NotNull String prefix, @NotNull Function<? super String, ? extends Selector> parser) {
		if (PARSER_LOOKUP.containsKey(prefix)) {
			throw new IllegalArgumentException("Parser for prefix `" + prefix + "` was registered already.");
		}

		PARSER_LOOKUP.put(prefix, parser);
	}

	public abstract Type getType();

	@NotNull
	public abstract String serialize();

	@NotNull
	public abstract Selector copy();

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Selector\""
		       + ", \"@super\":" + super.toString()
		       + '}';
	}

	public enum Type {
		WILDCARD,
		INDEX,
		PATTERN
	}
}
