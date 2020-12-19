package de.buw.tmdt.plasma.utilities.collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ListUtilities {
	private ListUtilities() {
		//don't instantiate this class
	}

	/**
	 * Returns the last element of a list or null if list is empty.
	 *
	 * @param list the list from which to retrieve the last element
	 * @param <T>  the element type
	 *
	 * @return the last element or null
	 */
	@Nullable
	public static <T> T getLastElement(@NotNull List<T> list) {
		return !list.isEmpty() ? list.get(list.size() - 1) : null;
	}

	/**
	 * Returns a view of the passed list which does not contain the first element. This still uses the passed list as a backing data structure which causes it
	 * to reflect it's modifications.
	 * @param list the list for which the reduced view is created
	 * @param <T> the element type
	 * @return a view on the passed list without the first element
	 */
	@NotNull
	public static <T> List<T> truncateFirstElement(@NotNull List<T> list) {
		return list.isEmpty() ? list : list.subList(1, list.size());
	}

	/**
	 * Returns a view of the passed list which does not contain the last element. This still uses the passed list as a backing data structure which causes it
	 * to reflect it's modifications.
	 * @param list the list for which the reduced view is created
	 * @param <T> the element type
	 * @return a view on the passed list without the last element
	 */
	@NotNull
	public static <T> List<T> truncateLastElement(@NotNull List<T> list) {
		return list.isEmpty() ? list : list.subList(0, list.size() - 1);
	}

	/**
	 * Creates a shallow copy of the given List or an empty List if the parameter was null.
	 *
	 * @param list the list to copy
	 * @param <T>  the type parameter of the list
	 *
	 * @return a shallow copy of the given List
	 */
	@NotNull
	public static <T> List<T> nullSafeCopy(@Nullable List<T> list) {
		return list != null ? new ArrayList<>(list) : new ArrayList<>();
	}
}