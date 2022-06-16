package de.buw.tmdt.plasma.services.sas.core.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.buw.tmdt.plasma.services.sas.core.model.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Traversable {

	//invoking removeFromChildren with this flag has no other side effects on the parent object but the element not being in the passed list anymore
	@SuppressWarnings("FieldNamingConvention")
	int NONE = 0;
	//invoking removeFromChildren with this flag implies any removed or inconsistent elements must be globally removed
	int OWNED = 1;
	//invoking removeFromChildren with this flag implies the parent becomes inconsistent when one of the children passed become inconsistent
	int COMPONENT = 1 << 1;

	static boolean removeFromChildren(
			@NotNull Traversable.Identity<?> identity,
			@NotNull Set<Identity<?>> visited,
			@NotNull Deque<Identity<?>> collateralRemoveQueue,
			@NotNull List<? extends Traversable> children,
			int flags
	) {
		boolean stillConsistent = true;
		ListIterator<? extends Traversable> childrenIterator = children.listIterator();
		while (childrenIterator.hasNext()) {
			Traversable child = childrenIterator.next();
			if (child.getIdentity().equals(identity) || !child.remove(identity, visited, collateralRemoveQueue)) {
				childrenIterator.remove();
				if ((flags & COMPONENT) != 0) {
					stillConsistent = false;
				}
				if ((flags & OWNED) != 0) {
					collateralRemoveQueue.add(child.getIdentity());
				}
			}
		}
		return stillConsistent;
	}

	/**
	 * Convenience method.
	 *
	 * @return the <i>root</i> of the copy
	 *
	 * @see Traversable#copy(Map)
	 */
	default Traversable copy() {
		return copy(new HashMap<>());
	}

	/**
	 * Creates a copy of the traversable graph (reachable from the node this was invoked on) while preserving the {@code Identity} of the vertices. This means
	 * {@code traversable.copy().getIdentity().equals(traversable.getIdentity()) == true} is invariant. The {@code copyableLookup} contains all nodes which
	 * were copied already and must not be revisited to ensure termination on cyclic graphs.
	 *
	 * @param copyableLookup the map of nodes which were copied already
	 *
	 * @return the <i>root</i> of the copy
	 */
	Traversable copy(@NotNull Map<Identity<?>, Traversable> copyableLookup);

	default Traversable copy(@NotNull Map<Identity<?>, Traversable> copyableLookup, @NotNull Supplier<? extends Traversable> elementSupplier) {
		if (copyableLookup.containsKey(this.getIdentity())) {
			return copyableLookup.get(this.getIdentity());
		}
		Traversable result = elementSupplier.get();
		copyableLookup.put(this.getIdentity(), result);
		return result;
	}

	/**
	 * Replace all occurrences of members with an Identity equal to {@code identity} by the given {@code traversable}. If any child elements are traversable
	 * invoke it recursively. The method returns either the {@code replacement} if {@code this.getIdentity().equals(identity)} is {@code true} or {@code
	 * this} otherwise. This allows to replace a child traversable by the return value of this method without knowledge of the exact side effect caused on
	 * the child.
	 *
	 * @param identity    the identity of the element to replace
	 * @param replacement the replacement object
	 *
	 * @return {@code this} or {@code replacement}
	 */
	Traversable replace(@NotNull Identity<?> identity, @NotNull Traversable replacement);

	/**
	 * Removes all occurrences of {@code Traversable}s with the given {@code Identity} and all {@code Traversable}s which have to be removed as a
	 * consequence in the reachable graph. Returning false indicates that removing the {@code Traversable}s caused {@code this} to become inconsistent and
	 * should be removed as a consequence.
	 *
	 * @param identity the identity token of the {@code Traversable} to remove.
	 *
	 * @return {@code false} if the method caused inconsistency in the object - {@code true} otherwise
	 */
	default boolean remove(@NotNull Identity<?> identity) {
		Set<Identity<?>> removedNodes = new HashSet<>();
		Deque<Identity<?>> collateralRemoveQueue = new ArrayDeque<>();
		collateralRemoveQueue.addLast(identity);
		while (!collateralRemoveQueue.isEmpty()) {
			Identity<?> head = collateralRemoveQueue.removeFirst();
			//don't traverse for elements which were removed already
			if (removedNodes.contains(head)) {
				continue;
			}
			//if removing an element destroys the state of `this` stop traversing and just return false since the complete reachable component becomes
			// inconsistent
			if (!remove(head, new HashSet<>(), collateralRemoveQueue)) {
				return false;
			}
			removedNodes.add(head);
		}
		return true;
	}

	/**
	 * Removes all occurrences of {@code Traversable}s with the given {@code Identity} and adds all {@code Traversable}s which have to be removed as a
	 * consequence to the {@code collateralRemoveQueue}. The method does not visit {@code Traversable}s which are in {@code visited} and adds all {@code
	 * Traversable}s which it visits. Returning false indicates that removing the {@code Traversable}s caused {@code this} to become inconsistent and
	 * should be removed as a consequence but the the caller has to decide if he respects that or tries to repair the state.
	 *
	 * @param identity              the identity token of the {@code Traversable} to remove.
	 * @param visited               the set of nodes which must not be visited again
	 * @param collateralRemoveQueue the queue of elements which have to be removed as a consequence
	 *
	 * @return {@code false} if the method caused inconsistency in the object - {@code true} otherwise
	 */
	boolean remove(@NotNull Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue);

	/**
	 * Convenience method which delegates to {@link #execute(Consumer, Set)} with an empty set.
	 *
	 * @param consumer the consumer which is invoked on every traversable node
	 */
	default void execute(Consumer<? super Traversable> consumer) {
		this.execute(consumer, new HashSet<>());
	}

	/**
	 * Invokes the {@code consumer} on itself and then recursively invokes {@code} execute(Consumer, Set)} on all child {@code Traversable}s which were not
	 * visited already.
	 *
	 * @param consumer the consumer which is invoked for every traversable
	 * @param visited  the set of traversables which were visited already
	 */
	default void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		if (visited.add(this.getIdentity())) {
			consumer.accept(this);
		}
	}

	/**
	 * Convenience method if the children are not present in an iterable collection.
	 *
	 * @see Traversable#execute(Consumer, Set, Iterable)
	 */
	default void execute(
			@NotNull final Consumer<? super Traversable> consumer,
			@NotNull final Set<? super Identity<?>> visited,
			@NotNull final Traversable... children
	) {
		execute(consumer, visited, Arrays.asList(children));
	}

	/**
	 * Invokes the {@code consumer} on all {@code children} and their successors except for those who are in {@code visited}.
	 * If the TraversableModelBase the method is invoked on was not visited yet:
	 * <ol>
	 * <li>{@code this} is marked as visited</li>
	 * <li>{@code consumer.accept(this)} is invoked</li>
	 * <li>{@link Traversable#execute(Consumer, Set)} is invoked for each child</li>
	 * </ol>
	 *
	 * @param consumer the consumer which accepts the TraversableModelBase instance
	 * @param visited  the set of visited Identities
	 * @param children the array of children which should execute the consumer
	 */
	default void execute(
			@NotNull final Consumer<? super Traversable> consumer,
			@NotNull final Set<? super Identity<?>> visited,
			@NotNull final Iterable<? extends Traversable> children
	) {
		if (visited.add(this.getIdentity())) {
			consumer.accept(this);
			for (Traversable child : children) {
				if (child != null) {
					child.execute(consumer, visited);
				}
			}
		}
	}

	Traversable find(@NotNull Identity<?> identity);

	@NotNull
	Identity<?> getIdentity();

	@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
	class Identity<@NotNull I> implements Serializable {
		private static final long serialVersionUID = 6948250907532940116L;
		protected final I identityToken;

		public Identity(@NotNull I identityToken) {
			this.identityToken = identityToken;
		}

		public static @NotNull Identity<?> nullSafe(Object identifier) {
			if (identifier != null) {
				return new Identity<>(identifier);
			}
			return random();
		}

		public static @NotNull Identity<UUID> random() {
			return new Identity<>(UUID.randomUUID());
		}

		@Override
		public int hashCode() {
			return identityToken.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || !getClass().equals(o.getClass())) {
				return false;
			}
			Identity<?> identity = (Identity<?>) o;
			return Objects.equals(identityToken, identity.identityToken);
		}

		@Override
		@SuppressWarnings("MagicCharacter")
		public String toString() {
			return "{\"@class\":\"Identity\""
			       + ", \"identityToken\":" + identityToken
			       + '}';
		}
	}

	default Node merge(Node traversable) throws SchemaAnalysisException {
		return traversable;
	}
}
