package de.buw.tmdt.plasma.utilities.statemachine;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class Transition<I extends Serializable & Comparable<I>> implements Serializable, Comparable<Transition<I>> {

	private static final long serialVersionUID = 5255293498996318795L;
	private final I from;
	private final I to;

	public Transition(@NotNull I from, @NotNull I to) {
		this.from = from;
		this.to = to;
	}

	@NotNull
	public I getTo() {
		return to;
	}

	@NotNull
	public I getFrom() {
		return from;
	}

	@SuppressWarnings("MagicCharacter")
	@Override
	public String toString() {
		return "Transition{" +
			   "from=" + from +
			   ", to=" + to +
			   '}';
	}

	@Override
	public int compareTo(@NotNull Transition<I> o) {
		int result;
		if ((result = this.getFrom().compareTo(o.getFrom())) != 0) {
			return result;
		}
		return this.getTo().compareTo(o.getTo());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Transition<?> that = (Transition<?>) o;
		return Objects.equals(from, that.from) &&
				Objects.equals(to, that.to);
	}

	@Override
	public int hashCode() {

		return Objects.hash(from, to);
	}
}
