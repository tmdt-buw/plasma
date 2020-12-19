package de.buw.tmdt.plasma.utilities.misc;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Replacement object for Apache ImmutablePair, used for easy deserialization.
 */
public class Pair<L, R> implements Serializable, Map.Entry<L, R> {
	private static final long serialVersionUID = 6819455313436787544L;

	/**
	 * Left object.
	 */
	private final L left;
	/**
	 * Right object.
	 */
	private final R right;

	/**
	 * Create a new pair instance.
	 *
	 * @param left  the left value, may be null
	 * @param right the right value, may be null
	 */
	public Pair(final L left, final R right) {
		super();
		this.left = left;
		this.right = right;
	}

	/**
	 * <p>Obtains an immutable pair of from two objects inferring the generic types.</p>
	 *
	 * <p>This factory allows the pair to be created using inference to obtain the generic types.</p>
	 *
	 * @param <L>   the left element type
	 * @param <R>   the right element type
	 * @param left  the left element, may be null
	 * @param right the right element, may be null
	 *
	 * @return a pair formed from the two parameters, not null
	 */
	public static <L, R> Pair<L, R> of(final L left, final R right) {
		return new Pair<>(left, right);
	}


	/**
	 * Creates a pair equivalent to the given map entry.
	 *
	 * @param entry the map entry to create a pair from
	 * @param <L>   the type of left
	 * @param <R>   the type of right
	 * @return a pair of map.getKey() and map.getValue()
	 */
	public static <L, R> Pair<L, R> of(final Map.Entry<? extends L, ? extends R> entry) {
		return new Pair<>(entry.getKey(), entry.getValue());
	}

	/**
	 * Gets the left entity of the pair.
	 *
	 * @return The left part of the pair
	 */
	public L getLeft() {
		return left;
	}

	/**
	 * Gets the right entity of the pair.
	 *
	 * @return The right part of the pair
	 */
	public R getRight() {
		return right;
	}

	/**
	 * Maps the Pair&lt;L,R&gt; to a Pair&lt;LNew,RNew&gt; by applying the mapping functions {@code leftMapper} and {@code rightMapper} accordingly.
	 * @param leftMapper the function to convert the left value
	 * @param rightMapper the function to convert the right value
	 * @param <LNew> the type of the left element of the new pair
	 * @param <RNew>the type of the right element of the new pair
	 * @return a new Pair where both values are mapped
	 */
	public <LNew, RNew> Pair<LNew, RNew> map(Function<? super L, ? extends LNew> leftMapper, Function<? super R, ? extends RNew> rightMapper) {
		return new Pair<>(leftMapper.apply(left), rightMapper.apply(right));
	}

	/**
	 * Alias for {@code Pair#map(leftMapper, Function.identity())}.
	 * @param leftMapper the function to convert the left value
	 * @param <LNew> the type of the left element of the new pair
	 * @return a new Pair where the left value was mapped
	 * @see Pair#map(Function, Function)
	 */
	public <LNew> Pair<LNew, R> mapLeft(Function<? super L, ? extends LNew> leftMapper) {
		return map(leftMapper, Function.identity());
	}

	/**
	 * Alias for {@code Pair#map(Function.identity(), rightMapper)}.
	 * @param rightMapper the function to convert the right value
	 * @param <RNew> the type of the right element of the new pair
	 * @return a new Pair where the right value was mapped
	 * @see Pair#map(Function, Function)
	 */
	public <RNew> Pair<L, RNew> mapRight(Function<? super R, ? extends RNew> rightMapper) {
		return map(Function.identity(), rightMapper);
	}

	@Override
	public int hashCode() {
		int result = left != null ? left.hashCode() : 0;
		result = 31 * result + (right != null ? right.hashCode() : 0);
		return result;
	}

	@Override
	public L getKey() {
		return getLeft();
	}

	@Override
	public R getValue() {
		return getRight();
	}

	@Override
	public R setValue(R value) {
		throw new UnsupportedOperationException(Pair.class+" is immutable.");
	}

	@SuppressWarnings("SimplifiableIfStatement - improves readability")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Pair<?, ?> pair = (Pair<?, ?>) o;

		if (!Objects.equals(left, pair.left)) {
			return false;
		}
		return Objects.equals(right, pair.right);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"Pair\""
				+ ", \"left\": " + left
				+ ", \"right\": " + right
				+ '}';
	}

}
