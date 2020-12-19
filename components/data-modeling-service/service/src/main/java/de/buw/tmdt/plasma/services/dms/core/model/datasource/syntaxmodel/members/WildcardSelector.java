package de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members;

import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DynamicUpdate
@Table(name = "wildcard_selectors")
public class WildcardSelector extends Selector {
	private static final String PARSING_PREFIX = "w:";

	static {
		Selector.registerParser(PARSING_PREFIX, WildcardSelector::parse);
	}

	public static WildcardSelector parse(@NotNull String s) {
		if (!s.startsWith(PARSING_PREFIX)) {
			throw new IllegalArgumentException("String to parse did not start with `" + PARSING_PREFIX + "`: " + s);
		}
		return new WildcardSelector();
	}

	@NotNull
	@Override
	public String serialize() {
		return PARSING_PREFIX;
	}

	@Override
	public @NotNull Selector copy() {
		return new WildcardSelector();
	}

	@Override
	public Type getType() {
		return Type.WILDCARD;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"WildcardSelector\""
		       + ", \"@super\":" + super.toString()
		       + '}';
	}
}
