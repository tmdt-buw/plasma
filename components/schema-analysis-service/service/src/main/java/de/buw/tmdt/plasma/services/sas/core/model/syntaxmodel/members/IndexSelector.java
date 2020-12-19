package de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.members;

import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DynamicUpdate
@Table(name = "index_selectors")
public class IndexSelector extends Selector {

	private static final String PARSING_PREFIX = "i:";

	static {
		registerParser(PARSING_PREFIX, IndexSelector::parse);
	}

	@Column
	private int elementIndex;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	private IndexSelector() {
		elementIndex = -1;
	}

	public IndexSelector(int elementIndex) {
		if (elementIndex < 0) {
			throw new IllegalArgumentException("Index must be greater or equal zero but was " + elementIndex);
		}
		this.elementIndex = elementIndex;
	}

	public static IndexSelector parse(@NotNull String string) {
		if (!string.startsWith(PARSING_PREFIX)) {
			throw new IllegalArgumentException("String to parse did not start with `" + PARSING_PREFIX + "`: " + string);
		}
		return new IndexSelector(Integer.parseInt(string.substring(PARSING_PREFIX.length())));
	}

	@NotNull
	@Override
	public String serialize() {
		return PARSING_PREFIX + Integer.toString(elementIndex);
	}

	@NotNull
	@Override
	public Selector copy() {
		return new IndexSelector(this.elementIndex);
	}

	public int getElementIndex() {
		return elementIndex;
	}

	public void setElementIndex(int elementIndex) {
		this.elementIndex = elementIndex;
	}

	@Override
	public Type getType() {
		return Type.INDEX;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"IndexSelector\""
		       + ", \"@super\":" + super.toString()
		       + ", \"elementIndex\":\"" + elementIndex + '"'
		       + '}';
	}
}
