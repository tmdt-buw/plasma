package de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel;

import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.DataType;
import de.buw.tmdt.plasma.services.srs.model.Position;
import de.buw.tmdt.plasma.services.srs.model.Traversable;
import de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel.members.EntityConceptSuggestion;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PrimitiveNode extends Node implements RawDataContainer {
	private EntityType entityType;
	private DataType dataType;
	private final String cleansingPattern;
	private final List<String> examples;
	private List<EntityConceptSuggestion> entityConceptSuggestions;
	//Hibernate constructor

	//creates invalid state if not properly initialized afterwards
	private PrimitiveNode() {
		entityType = null;
		dataType = null;
		cleansingPattern = null;
		examples = null;
		entityConceptSuggestions = null;
	}

	public PrimitiveNode(@Nullable EntityType entityType, @NotNull DataType dataType) {
		this(entityType, dataType, null);
	}

	public PrimitiveNode(@Nullable EntityType entityType, @NotNull DataType dataType, @Nullable List<String> examples) {
		this(entityType, dataType, examples, null, "");
	}

	public PrimitiveNode(
			@Nullable EntityType entityType,
			@NotNull DataType dataType,
			@Nullable List<String> examples,
			@Nullable List<EntityConceptSuggestion> entityConceptSuggestions,
			@Nullable String cleansingPattern
	) {
		this(entityType, dataType, examples, entityConceptSuggestions, cleansingPattern, null, Identity.random());
	}

	public PrimitiveNode(
			@Nullable EntityType entityType,
			@NotNull DataType dataType,
			@Nullable String cleansingPattern,
			@Nullable List<EntityConceptSuggestion> entityConceptSuggestions,
			@Nullable Position position,
			@Nullable List<String> examples,
			@NotNull UUID uuid
	) {
		super(position, uuid);
		this.entityType = entityType;
		this.dataType = dataType;
		this.cleansingPattern = cleansingPattern != null ? cleansingPattern : "";
		if (CollectionUtilities.containsNull(examples)) {
			throw new IllegalArgumentException("Examples must not contain null.");
		}
		this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
		if (CollectionUtilities.containsNull(entityConceptSuggestions)) {
			throw new IllegalArgumentException("EntityConcept suggestions must not contain null.");
		}
		this.entityConceptSuggestions = entityConceptSuggestions != null ? new ArrayList<>(entityConceptSuggestions) : new ArrayList<>();
	}

	private PrimitiveNode(
			@Nullable EntityType entityType,
			@NotNull DataType dataType,
			@Nullable List<String> examples,
			@Nullable List<EntityConceptSuggestion> entityConceptSuggestions,
			@Nullable String cleansingPattern,
			@Nullable Position position,
			@NotNull Identity<?> identity
	) {
		super(position, identity);
		this.entityType = entityType;
		this.dataType = dataType;
		this.cleansingPattern = cleansingPattern != null ? cleansingPattern : "";
		if (CollectionUtilities.containsNull(examples)) {
			throw new IllegalArgumentException("Examples must not contain null.");
		}
		this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
		if (CollectionUtilities.containsNull(entityConceptSuggestions)) {
			throw new IllegalArgumentException("EntityConcept suggestions must not contain null.");
		}
		this.entityConceptSuggestions = entityConceptSuggestions != null ? new ArrayList<>(entityConceptSuggestions) : new ArrayList<>();
	}

	@Nullable
	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(@Nullable EntityType entityType) {
		this.entityType = entityType;
	}

	@NotNull
	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(@NotNull DataType dataType) {
		this.dataType = dataType;
	}

	@NotNull
	@Override
	public String getCleansingPattern() {
		return cleansingPattern;
	}

	@Override
	public List<String> getExamples() {
		return Collections.unmodifiableList(examples);
	}

	public boolean addExample(String example) {
		return examples.add(example);
	}

	public void setEntityConceptSuggestions(List<EntityConceptSuggestion> entityConceptSuggestions) {
		this.entityConceptSuggestions = entityConceptSuggestions;
	}

	@NotNull
	@Override
	public PrimitiveNode copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new PrimitiveNode(
						this.entityType != null ? this.entityType.copy(copyableLookup) : null,
						this.dataType,
						new ArrayList<>(this.examples),
						this.entityConceptSuggestions.stream().map(EntityConceptSuggestion::copy).collect(Collectors.toList()),
						this.cleansingPattern,
						this.getPosition(),
						this.getIdentity()
				)), PrimitiveNode.class
		);
	}

	@Override
	public Node replace(@NotNull Identity<?> identity, @NotNull Traversable replacement) {
		if (this.getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, Node.class);
		}
		if (this.entityType != null) {
			this.entityType = entityType.replace(identity, replacement);
		}
		return this;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, entityType);
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());

		if (this.entityType != null && (this.entityType.getIdentity().equals(identity) || !this.entityType.remove(identity, visited, collateralRemoveQueue))) {
			this.entityType = null;
		}

		return true;
	}

	@Override
	public Traversable find(@NotNull Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		}
		return this.entityType != null ? this.entityType.find(identity) : null;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<EntityConceptSuggestion> getEntityConceptSuggestions() {
		return entityConceptSuggestions;
	}

	@Override
	protected Set<Fault> otherFaults(Set<Fault> faults) {
		if (this.dataType == DataType.UNKNOWN && this.entityType != null) {
			faults.add(new Fault(String.format(
					"Data carrying node is mapped to entity type %s(%s) but has data type %s.",
					this.entityType.getLabel(),
					entityType.getUuid(),
					DataType.UNKNOWN
			)));
		}
		return faults;
	}

	@Override
	public int hashCode() {
		return Objects.hash(cleansingPattern, examples, entityConceptSuggestions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		PrimitiveNode primitive = (PrimitiveNode) o;
		return Objects.equals(cleansingPattern, primitive.cleansingPattern) &&
		       Objects.equals(examples, primitive.examples) &&
		       Objects.equals(entityConceptSuggestions, primitive.entityConceptSuggestions);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"PrimitiveNode\""
		       + ", \"@super\":" + super.toString()
		       + ", \"entityType\":" + entityType
		       + ", \"dataType\":\"" + dataType + '"'
		       + ", \"cleansingPattern\":\"" + cleansingPattern + '"'
		       + ", \"examples\":" + StringUtilities.listToJson(examples)
		       + ", \"entityConceptSuggestions\":" + StringUtilities.listToJson(entityConceptSuggestions)
		       + '}';
	}
}
