package de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel;

import de.buw.tmdt.plasma.services.sas.core.model.Traversable;
import de.buw.tmdt.plasma.services.sas.core.model.Position;
import de.buw.tmdt.plasma.services.sas.core.model.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.utilities.collections.MapUtilities;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;

@Entity
@DynamicUpdate
@Table(name = "object_nodes")
public class ObjectNode extends Node {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@MapKeyColumn
	private Map<String, Node> children;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	protected ObjectNode() {
		children = null;
	}

	public ObjectNode(@NotNull Map<String, Node> children, @Nullable Position position) {
		this(children, position, Traversable.Identity.random());
	}

	private ObjectNode(@NotNull Map<String, Node> children, @Nullable Position position, @NotNull Identity<?> identity) {
		super(position, identity);
		this.setUuid(UUID.randomUUID());
		if (MapUtilities.mapContains(children, null)) {
			throw new IllegalArgumentException("Children must neither contain null as a key nor as a value.");
		}
		this.children = new HashMap<>(children);
	}

	@Override
	public Traversable find(@NotNull Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			for (Node child : children.values()) {
				Traversable result;
				if (child != null && (result = child.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	@NotNull
	@Override
	public ObjectNode copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> {
					Map<String, Node> childCopy = new HashMap<>(children.size());
					children.forEach((String key, Node value) -> childCopy.put(key, value.copy(copyableLookup)));
					return new ObjectNode(childCopy, this.getPosition(), this.getIdentity());
				}), ObjectNode.class
		);
	}

	@Override
	public Node replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
		if (this.getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, Node.class);
		}

		this.children.entrySet().forEach(
				childEntry -> childEntry.setValue(childEntry.getValue().replace(identity, replacement))
		);

		return this;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, this.children.values());
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		ArrayList<Node> children = new ArrayList<>(this.children.values());

		boolean result = Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, children, OWNED);

		this.children.values().retainAll(children);

		return result;
	}

	@Override
	protected Set<Fault> hasCollision(Set<Fault> faults) {
		for (Node node : children.values()) {
			faults = node.hasCollision(faults);
		}
		return faults;
	}

	@Override
	protected Set<Fault> idGloballyDisjoint(Set<UUID> collectedIds, Set<Fault> faults) {
		for (Node node : children.values()) {
			faults = node.idGloballyDisjoint(collectedIds, faults);
		}
		return super.idGloballyDisjoint(collectedIds, faults);
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	@NotNull
	public Map<String, Node> getChildren() {
		return children;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setChildren(@NotNull Map<String, Node> children) {
		this.children = children;
	}

	public void mergeChild(String key, Node newChild) {
		if (this.children.containsKey(key)) {
			mergeChild(key, children.get(key), newChild);
		}
		else {
			children.put(key, newChild);
		}
	}

	private void mergeChild(String key, Node oldChild, Node newChild) throws SchemaAnalysisException{
		try {
			this.children.put(key, oldChild.merge(newChild));
		} catch (Exception e) {
			throw new SchemaAnalysisException("Could not merge chilren: " + oldChild + " and " + newChild);
		}
	}

	@Override
	public Node merge(Node other) throws SchemaAnalysisException {
		if (other instanceof CollisionNode) {
			return other.merge(this);
		} else if (other instanceof PrimitiveNode) {
			return new CollisionNode((PrimitiveNode) other, this, null);
		} else if (other instanceof ObjectNode) {
			return this.merge((ObjectNode) other);
		} else if (other instanceof SetNode) {
			return new CollisionNode(null, this, (SetNode) other);
		} else if (other == null) {
			return this;
		} else {
			throw new SchemaAnalysisException("Unknown type"); // needs to get correct exception
		}
	}

	public ObjectNode merge(ObjectNode other) throws SchemaAnalysisException {
		if (other == null) {
			return this;
		}
		if (this.uuid.equals(other.uuid) && this.equals(other)) {
			throw new SchemaAnalysisException("Tried to merge object with itself: " + this);
		}
		MapUtilities.innerMerge(this.children, other.children, this::mergeChild);
		return this;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"ObjectNode\""
		       + ", \"@super\":" + super.toString()
		       + ", \"children\":" + StringUtilities.mapToJson(children)
		       + '}';
	}
}
