package de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel;

import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.members.Splitter;
import de.buw.tmdt.plasma.services.sas.core.model.Traversable;
import de.buw.tmdt.plasma.services.sas.core.model.Position;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Entity
@DynamicUpdate
@Table(name = "composite_nodes")
public final class CompositeNode extends Node implements RawDataContainer {

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderColumn
	private List<PrimitiveNode> components;
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderColumn
	private List<Splitter> splitter;
	@ElementCollection
	private List<String> examples;
	@Column
	private String cleansingPattern;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	protected CompositeNode() {
		components = null;
		splitter = null;
		cleansingPattern = null;
		examples = null;
	}

	private CompositeNode(
			@NotNull Collection<PrimitiveNode> components,
			@NotNull Collection<Splitter> splitter,
			@Nullable List<String> examples,
			@Nullable String cleansingPattern,
			@Nullable Position position,
			@NotNull Identity<?> identity
	) {
		super(position, identity);
		this.setUuid(UUID.randomUUID());
		if (CollectionUtilities.collectionContains(components, null)) {
			throw new IllegalArgumentException("Components must not contain null.");
		}
		if (CollectionUtilities.collectionContains(splitter, null)) {
			throw new IllegalArgumentException("Splitters must not contain null.");
		}
		this.components = new ArrayList<>(components);
		this.splitter = new ArrayList<>(splitter);
		if (examples == null) {
			this.examples = new ArrayList<>();
		}
		else {
			this.examples = new ArrayList<>(examples);
		}
		this.cleansingPattern = cleansingPattern != null ? cleansingPattern : "";
	}

	@Override
	public Traversable find(@NotNull Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			for (Node component : components) {
				Traversable result;
				if (component != null && (result = component.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, this.getComponents().toArray(new PrimitiveNode[0]));
	}

	@NotNull
	@Override
	public CompositeNode copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new CompositeNode(
						this.components.stream().map(primitiveNode -> primitiveNode.copy(copyableLookup)).collect(Collectors.toList()),
						this.splitter.stream().map(Splitter::copy).collect(Collectors.toList()),
						new ArrayList<>(this.getExamples()),
						this.cleansingPattern,
						this.getPosition(),
						this.getIdentity()
				)), CompositeNode.class
		);
	}

	@SuppressWarnings("ReturnOfThis")
	@Override
	public Node replace(@NotNull Identity<?> identity, @NotNull Traversable replacement) {
		if (this.getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, Node.class);
		}
		this.components.replaceAll(
				primitiveNode -> ObjectUtilities.checkedReturn(primitiveNode.replace(identity, replacement), PrimitiveNode.class)
		);
		return this;
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());

		return Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, this.getComponents(), OWNED | COMPONENT);
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<PrimitiveNode> getComponents() {
		return components;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setComponents(List<PrimitiveNode> components) {
		this.components = components;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<Splitter> getSplitter() {
		return splitter;
	}

	public void setCleansingPattern(String cleansingPattern) {
		this.cleansingPattern = cleansingPattern;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setSplitter(List<Splitter> splitter) {
		this.splitter = splitter;
	}

	@Override
	@NotNull
	public String getCleansingPattern() {
		return this.cleansingPattern;
	}

	@Override
	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<String> getExamples() {
		return this.examples;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setExamples(List<String> examples) {
		this.examples = examples;
	}

	@Override
	protected Set<Fault> idGloballyDisjoint(Set<UUID> collectedIds, Set<Fault> faults) {
		for (PrimitiveNode node : components) {
			faults = node.idGloballyDisjoint(collectedIds, faults);
		}
		return super.idGloballyDisjoint(collectedIds, faults);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"CompositeNode\""
		       + ", \"@super\":" + super.toString()
		       + ", \"components\":" + StringUtilities.listToJson(components)
		       + ", \"splitter\":" + StringUtilities.listToJson(splitter)
		       + ", \"cleansingPattern\":\"" + cleansingPattern + '"'
		       + '}';
	}
}
