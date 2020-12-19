package de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel.members;

import de.buw.tmdt.plasma.services.dms.core.model.ModelBase;
import de.buw.tmdt.plasma.utilities.misc.CachedSupplier;
import de.buw.tmdt.plasma.utilities.misc.Pair;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Entity
@DynamicUpdate
@Table(name = "splitters")
public class Splitter extends ModelBase implements Function<String, Pair<String, String>> {
	@Column
	private String pattern;
	@Transient
	private transient Supplier<Pattern> compiledPatternSupplier;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	protected Splitter() {
		pattern = null;
	}

	public Splitter(String pattern) {
		this.pattern = pattern;
		this.compiledPatternSupplier = new CachedSupplier<>(() -> Pattern.compile(pattern));
	}

	@NotNull
	public String getPattern() {
		return pattern;
	}

	public void setPattern(@NotNull String pattern) {
		this.pattern = pattern;
	}

	@NotNull
	public Pattern getCompiledPattern() {
		return compiledPatternSupplier.get();
	}

	@NotNull
	@Override
	public Pair<String, String> apply(@NotNull String s) {
		String[] tokens = compiledPatternSupplier.get().split(s, 2);
		if (tokens.length != 1) {
			return new Pair<>(tokens[0], tokens[1]);
		} else {
			return new Pair<>(tokens[0], "");
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(pattern);
	}

	@NotNull
	public Splitter copy() {
		return new Splitter(this.pattern);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		Splitter splitter = (Splitter) o;
		return Objects.equals(pattern, splitter.pattern);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Splitter\""
		       + ", \"@super\":" + super.toString()
		       + ", \"pattern\":\"" + pattern + '"'
		       + '}';
	}
}
