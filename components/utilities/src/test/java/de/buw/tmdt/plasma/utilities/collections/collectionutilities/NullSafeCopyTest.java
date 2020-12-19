package de.buw.tmdt.plasma.utilities.collections.collectionutilities;

import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(org.junit.runners.Parameterized.class)
public class NullSafeCopyTest<T> {

	private final @NotNull Supplier<Collection<? super T>> targetSupplier;
	private final @Nullable Collection<? extends T> source;
	private final @NotNull Collection<? extends T> expectedResult;

	public NullSafeCopyTest(String ignoredName, Parameter<T> parameter) {
		this.targetSupplier = parameter.targetSupplier;
		this.source = parameter.source;
		this.expectedResult = parameter.expectedResult;
	}

	@Parameterized.Parameters(name = "{index}: {0}")
	public static Collection<Object[]> parameters() {
		return Arrays.asList(
				new Object[]{
						"HashSet to empty HashSet",
						new Parameter<>(
								HashSet::new,
								new HashSet<>(Arrays.asList("foo", "bar")),
								new HashSet<>(Arrays.asList("foo", "bar"))
						)
				},
				new Object[]{
						"HashSet to empty ArrayList",
						new Parameter<>(
								ArrayList::new,
								new HashSet<>(Arrays.asList("foo", "bar")),
								new ArrayList<>(Arrays.asList("foo", "bar"))
						)
				},
				new Object[]{
						"ArrayList to empty HashSet (duplicates)",
						new Parameter<>(
								HashSet::new,
								new ArrayList<>(Arrays.asList("foo", "bar", "foo")),
								new HashSet<>(Arrays.asList("foo", "bar"))
						)
				},
				new Object[]{
						"ArrayList to empty HashSet (no duplicates)",
						new Parameter<>(
								HashSet::new,
								new ArrayList<>(Arrays.asList("foo", "bar")),
								new HashSet<>(Arrays.asList("foo", "bar"))
						)
				},
				new Object[]{
						"ArrayList to empty ArrayList",
						new Parameter<>(
								ArrayList::new,
								new ArrayList<>(Arrays.asList("foo", "bar")),
								new ArrayList<>(Arrays.asList("foo", "bar"))
						)
				},
				new Object[]{
						"null to empty HashSet",
						new Parameter<>(
								HashSet::new,
								null,
								Collections.emptyList()
						)
				},
				new Object[]{
						"null to empty ArrayList",
						new Parameter<>(
								ArrayList::new,
								null,
								new ArrayList<>()
						)
				},
				new Object[]{
						"HashSet to pre-filled HashSet",
						new Parameter<>(
								() -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
								new HashSet<>(Arrays.asList("foo", "bar")),
								new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
						)
				},
				new Object[]{
						"HashSet to pre-filled ArrayList",
						new Parameter<>(
								() -> new ArrayList<>(Arrays.asList("some", "pre set", "values")),
								new HashSet<>(Arrays.asList("foo", "bar")),
								new ArrayList<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
						)
				},
				new Object[]{
						"ArrayList to pre-filled HashSet (duplicates)",
						new Parameter<>(
								() -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
								new ArrayList<>(Arrays.asList("foo", "bar", "foo")),
								new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
						)
				},
				new Object[]{
						"ArrayList to pre-filled HashSet (no duplicates)",
						new Parameter<>(
								() -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
								new ArrayList<>(Arrays.asList("foo", "bar")),
								new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
						)
				},
				new Object[]{
						"ArrayList to pre-filled ArrayList",
						new Parameter<>(
								() -> new ArrayList<>(Arrays.asList("some", "pre set", "values")),
								new ArrayList<>(Arrays.asList("foo", "bar")),
								new HashSet<>(Arrays.asList("some", "pre set", "values", "foo", "bar"))
						)
				},
				new Object[]{
						"null to pre-filled HashSet",
						new Parameter<>(
								() -> new HashSet<>(Arrays.asList("some", "pre set", "values")),
								null,
								new HashSet<>(Arrays.asList("some", "pre set", "values"))
						)
				},
				new Object[]{
						"null to pre-filled ArrayList",
						new Parameter<>(
								() -> new ArrayList<>(Arrays.asList("some", "pre set", "values")),
								null,
								new ArrayList<>(Arrays.asList("some", "pre set", "values"))
						)
				}
		);
	}

	@Test
	public void testNullSafeCopy() {
		final Collection<? super T> result = CollectionUtilities.nullSafeCopy(targetSupplier, source);
		assertEquals(result.getClass(), targetSupplier.get().getClass());
		if (source == null) {
			assertEquals(result, targetSupplier.get());
		} else {
			//equals with ignore order
			assertTrue(result.containsAll(expectedResult));
			assertTrue(expectedResult.containsAll(result));
		}
	}

	private static class Parameter<T> {
		public final @NotNull Supplier<Collection<? super T>> targetSupplier;
		public final @Nullable Collection<? extends T> source;
		public final @NotNull Collection<? extends T> expectedResult;

		public Parameter(@NotNull Supplier<Collection<? super T>> targetSupplier, @Nullable Collection<? extends T> source, @NotNull Collection<? extends T> expectedResult) {
			this.targetSupplier = targetSupplier;
			this.source = source;
			this.expectedResult = expectedResult;
		}
	}
}
