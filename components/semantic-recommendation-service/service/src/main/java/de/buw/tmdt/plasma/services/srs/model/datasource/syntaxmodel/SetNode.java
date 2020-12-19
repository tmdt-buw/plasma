package de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel;

import de.buw.tmdt.plasma.services.srs.model.Position;
import de.buw.tmdt.plasma.services.srs.model.Traversable;
import de.buw.tmdt.plasma.services.srs.model.TraversableModelBase;
import de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel.members.PatternSelector;
import de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel.members.Selector;
import de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel.members.WildcardSelector;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SetNode extends Node {
	private final List<Child> children;

	private SetNode() {
		this.children = null;
	}

	public SetNode(@NotNull Set<Node> children) {
		this(
				children.stream().map(node -> new Child(new WildcardSelector(), node)).collect(Collectors.toList()),
				null,
				Identity.random()
		);
	}

	public SetNode(@NotNull List<Child> children, @Nullable Position position, @NotNull UUID uuid) {
		super(position, uuid);
		if (CollectionUtilities.containsNull(children)) {
			throw new IllegalArgumentException("Unmapped children must not contain null.");
		}
		this.children = new ArrayList<>(children);
	}

	private SetNode(@NotNull List<Child> children, @Nullable Position position, @NotNull Traversable.Identity<?> identity) {
		super(position, identity);
		if (CollectionUtilities.containsNull(children)) {
			throw new IllegalArgumentException("Unmapped children must not contain null.");
		}
		this.children = new ArrayList<>(children);
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
	public List<Child> getChildren() {
		return children;
	}

	@Override
	@NotNull
	public SetNode copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new SetNode(
						children.stream().map(child -> child.copy(copyableLookup)).collect(Collectors.toList()),
						this.getPosition(),
						this.getIdentity()
				)), SetNode.class
		);
	}

	@Override
	public Node replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
		if (this.getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, Node.class);
		}
		this.children.forEach(e -> e.setNode(e.getNode().replace(identity, replacement)));

		this.children.replaceAll(child -> ObjectUtilities.checkedReturn(child.replace(identity, replacement), Child.class));
		return this;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, this.children.stream().map(Child::getNode).collect(Collectors.toList()));
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());

		return Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, this.children, Traversable.OWNED);
	}

	@Override
	public Traversable find(@NotNull Traversable.Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			for (Child child : children) {
				Traversable result;
				if (child != null && (result = child.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public boolean isValid() {
		if (!validatePrimitiveSetSelectors()) {
			return false;
		}
		for (Child child : children) {
			if (!child.getNode().isValid()) {
				return false;
			}
		}
		return true;
	}

	private boolean validateObjectSet() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	protected Set<Fault> hasCollision(Set<Fault> faults) {
		for (Child child : children) {
			faults = child.getNode().hasCollision(faults);
		}
		return faults;
	}

	@Override
	protected Set<Fault> idGloballyDisjoint(Set<UUID> collectedIds, Set<Fault> faults) {
		for (Child child : children) {
			faults = child.getNode().idGloballyDisjoint(collectedIds, faults);
		}
		return super.idGloballyDisjoint(collectedIds, faults);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SetNode\""
		       + ", \"@super\":" + super.toString()
		       + ", \"children\":" + StringUtilities.listToJson(children)
		       + '}';
	}

	private boolean validatePrimitiveSetSelectors() {
		if (children.isEmpty()) {
			return true;
		}
		int modulus = -1;
		int offset = -1;
		Set<Integer> indexes = new HashSet<>();
		for (Child child : children) {
			Selector selector = child.getSelector();
			switch (selector.getType()) {
				case WILDCARD: //wildcard must be the only selector
					return children.size() == 1;
				case INDEX:
					continue;
				case PATTERN: {
					PatternSelector patternSelector = (PatternSelector) selector;
					if (modulus == -1 && offset == -1) {
						modulus = patternSelector.getModulus();
						offset = patternSelector.getOffset();
						continue;
					}
					if (modulus != patternSelector.getModulus() || offset != patternSelector.getOffset()) {
						return false;
					}
					if (Collections.disjoint(indexes, patternSelector.getSelections())) {
						return false;
					}
					indexes.addAll(patternSelector.getSelections());
				}
			}
		}
		return true;
	}

	public static class Child extends TraversableModelBase {
		private Selector selector;

		private Node node;

		private Child() {

		}

		public Child(@NotNull Selector selector, @NotNull Node node) {
			super(null, Identity.random());
			this.selector = selector;
			this.node = node;
		}

		public Child(@NotNull Selector selector, @NotNull Node node, @NotNull Long id) {
			super(null, id);
			this.selector = selector;
			this.node = node;
		}

		private Child(@NotNull Selector selector, @NotNull Node node, @NotNull Identity<?> identity) {
			super(null, identity);
			this.selector = selector;
			this.node = node;
		}

		public Selector getSelector() {
			return selector;
		}

		@NotNull
		public Node getNode() {
			return node;
		}

		public void setNode(@NotNull Node node) {
			this.node = node;
		}

		@Override
		public Child copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
			return ObjectUtilities.checkedReturn(
					copy(copyableLookup, () -> new Child(
							this.getSelector().copy(),
							this.getNode().copy(copyableLookup),
							this.getIdentity()
					)), Child.class
			);
		}

		@Override
		public Child replace(@NotNull Identity<?> identity, @NotNull Traversable replacement) {
			if (this.getIdentity().equals(identity)) {
				return ObjectUtilities.checkedReturn(replacement, Child.class);
			}
			this.node = this.node.replace(identity, replacement);
			return this;
		}

		@Override
		public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
			super.execute(consumer, visited, this.node);
		}

		@Override
		public Traversable find(@NotNull Identity<?> identity) {
			if (this.getIdentity().equals(identity)) {
				return this;
			}
			return this.node.find(identity);
		}

		@Override
		@SuppressWarnings("MagicCharacter")
		public String toString() {
			return "{\"@class\":\"Child\""
			       + ", \"@super\":" + super.toString()
			       + ", \"selector\":" + selector
			       + ", \"node\":" + node
			       + '}';
		}

		@Override
		public boolean remove(
				@NotNull Traversable.Identity<?> identity,
				@NotNull Set<Identity<?>> visited,
				@NotNull Deque<Identity<?>> collateralRemoveQueue
		) {
			visited.add(this.getIdentity());
			return Traversable.removeFromChildren(
					identity,
					visited,
					collateralRemoveQueue,
					new ArrayList<>(Collections.singleton(this.node)),
					OWNED | COMPONENT
			);
		}
	}
}