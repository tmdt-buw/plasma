package de.buw.tmdt.plasma.services.dms.core.model.datasource.syntaxmodel;

import de.buw.tmdt.plasma.services.dms.core.model.Position;
import de.buw.tmdt.plasma.services.dms.core.model.Traversable;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.*;
import java.util.function.Consumer;

@Entity
@DynamicUpdate
@Table(name = "collision_nodes")
public class CollisionNode extends Node {
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private PrimitiveNode primitiveNode;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private ObjectNode objectNode;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private SetNode setNode;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	private CollisionNode() {
		primitiveNode = null;
		objectNode = null;
		setNode = null;
	}

	public CollisionNode(
			@Nullable PrimitiveNode primitiveNode,
			@Nullable ObjectNode objectNode,
			@Nullable SetNode setNode
	) {
		this(primitiveNode, objectNode, setNode, null, Identity.random());
	}

	public CollisionNode(
			@Nullable PrimitiveNode primitiveNode,
			@Nullable ObjectNode objectNode,
			@Nullable SetNode setNode,
			@Nullable Position position,
			@NotNull UUID uuid
	) {
		super(position, uuid);
		init(primitiveNode, objectNode, setNode);
	}

	private CollisionNode(
			@Nullable PrimitiveNode primitiveNode,
			@Nullable ObjectNode objectNode,
			@Nullable SetNode setNode,
			@Nullable Position position,
			@NotNull Identity<?> identity
	) {
		super(position, identity);
		init(primitiveNode, objectNode, setNode);
	}

	private void init(@Nullable PrimitiveNode primitiveNode, @Nullable ObjectNode objectNode, @Nullable SetNode setNode) {
		int counter = 0;
		counter += primitiveNode != null ? 1 : 0;
		counter += objectNode != null ? 1 : 0;
		counter += setNode != null ? 1 : 0;
		if (counter < 2) {
			throw new IllegalArgumentException("At least two parameters must not be null.");
		}
		this.primitiveNode = primitiveNode;
		this.objectNode = objectNode;
		this.setNode = setNode;
	}

	public PrimitiveNode getPrimitiveNode() {
		return primitiveNode;
	}

	public ObjectNode getObjectNode() {
		return objectNode;
	}

	public SetNode getSetNode() {
		return setNode;
	}

	protected List<Node> getChildren() {
		return Arrays.asList(getPrimitiveNode(), getObjectNode(), getSetNode());
	}

	@NotNull
	@Override
	public CollisionNode copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new CollisionNode(
						this.primitiveNode.copy(copyableLookup),
						this.objectNode.copy(copyableLookup),
						this.setNode.copy(copyableLookup),
						this.getPosition(),
						this.getIdentity()
				)), CollisionNode.class
		);
	}

	@Override
	public Node replace(@NotNull Identity<?> identity, @NotNull Traversable replacement) {
		if (this.getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, Node.class);
		}

		if (this.primitiveNode != null) {
			this.primitiveNode = ObjectUtilities.checkedReturn(this.primitiveNode.replace(identity, replacement), PrimitiveNode.class);
		}
		if (this.objectNode != null) {
			this.objectNode = ObjectUtilities.checkedReturn(this.objectNode.replace(identity, replacement), ObjectNode.class);
		}
		if (this.setNode != null) {
			this.setNode = ObjectUtilities.checkedReturn(setNode.replace(identity, replacement), SetNode.class);
		}
		return this;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Traversable.Identity<?>> visited) {
		super.execute(consumer, visited, primitiveNode, setNode, objectNode);
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());
		int counter = 0;
		if (this.primitiveNode != null) {
			if (identity.equals(this.primitiveNode.getIdentity()) || this.primitiveNode.remove(identity, visited, collateralRemoveQueue)) {
				this.primitiveNode = null;
			} else {
				counter++;
			}
		}
		if (this.setNode != null) {
			if (identity.equals(this.setNode.getIdentity()) || this.setNode.remove(identity, visited, collateralRemoveQueue)) {
				this.setNode = null;
			} else {
				counter++;
			}
		}
		if (this.objectNode != null) {
			if (identity.equals(this.objectNode.getIdentity()) || this.objectNode.remove(identity, visited, collateralRemoveQueue)) {
				this.objectNode = null;
			} else {
				counter++;
			}
		}
		return counter != 0;
	}

	@Override
	public Traversable find(@NotNull Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			Traversable result;
			for (Node child : Arrays.asList(primitiveNode, setNode, objectNode)) {
				if (child != null && (result = child.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("NestedConditionalExpression - yes I'm going to hell for this")
	@Override
	protected Set<Fault> hasCollision(Set<Fault> faults) {
		faults.add(new Fault(String.format(
				"Node contains collisions of type %s%s%s.",
				this.primitiveNode != null ? "Primitive and " : "",
				this.objectNode != null ? "Object" + (this.setNode != null ? " and " : "") : "",
				this.setNode != null ? "Set" : ""
		)));
		return faults;
	}

	@Override
	protected Set<Fault> idGloballyDisjoint(Set<UUID> collectedIds, Set<Fault> faults) {
		for (Node node : this.getChildren()) {
			faults = node.idGloballyDisjoint(collectedIds, faults);
		}
		return super.idGloballyDisjoint(collectedIds, faults);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"CollisionNode\""
		       + ", \"@super\":" + super.toString()
		       + ", \"primitiveNode\":" + primitiveNode
		       + ", \"objectNode\":" + objectNode
		       + ", \"setNode\":" + setNode
		       + '}';
	}
}
