package de.buw.tmdt.plasma.services.srs.model.datasource;

import de.buw.tmdt.plasma.services.srs.model.Traversable;
import de.buw.tmdt.plasma.services.srs.model.TraversableModelBase;
import de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel.EntityConcept;
import de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel.RelationConcept;
import de.buw.tmdt.plasma.services.srs.model.datasource.semanticmodel.SemanticModel;
import de.buw.tmdt.plasma.services.srs.model.datasource.syntaxmodel.Node;
import de.buw.tmdt.plasma.utilities.misc.ObjectUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class DataSourceSchema extends TraversableModelBase {

	private DataSourceModel owningNode;

	private Node syntaxModel;

	private SemanticModel semanticModel;

	private List<EntityConcept> entityConceptCache;

	private List<RelationConcept> relationConceptCache;

	private boolean finalized = false;

	//Hibernate constructor
	//creates invalid state if not properly initialized afterwards
	@Deprecated
	private DataSourceSchema() {

	}

	public DataSourceSchema(
			@NotNull DataSourceModel parent,
			@NotNull Node syntaxModel,
			@NotNull SemanticModel semanticModel,
			@NotNull List<? extends EntityConcept> entityConceptCache,
			@NotNull List<? extends RelationConcept> relationConceptCache
	) {
		this(parent, syntaxModel, semanticModel, entityConceptCache, relationConceptCache, Identity.random());
	}

	public DataSourceSchema(
			@NotNull DataSourceModel parent,
			@NotNull Node syntaxModel,
			@NotNull SemanticModel semanticModel,
			@NotNull List<? extends EntityConcept> entityConceptCache,
			@NotNull List<? extends RelationConcept> relationConceptCache,
			@NotNull Long id
	) {
		super(null, id);
		init(parent, syntaxModel, semanticModel, entityConceptCache, relationConceptCache);
	}

	private DataSourceSchema(
			@NotNull DataSourceModel parent,
			@NotNull Node syntaxModel,
			@NotNull SemanticModel semanticModel,
			@NotNull List<? extends EntityConcept> entityConceptCache,
			@NotNull List<? extends RelationConcept> relationConceptCache,
			@NotNull Traversable.Identity<?> identity
	) {
		super(null, identity);
		init(parent, syntaxModel, semanticModel, entityConceptCache, relationConceptCache);
	}

	private void init(
			@NotNull DataSourceModel parent,
			@NotNull Node syntaxModel,
			@NotNull SemanticModel semanticModel,
			@NotNull List<? extends EntityConcept> entityConceptCache,
			@NotNull List<? extends RelationConcept> relationConceptCache
	) {
		this.owningNode = parent;
		this.syntaxModel = syntaxModel;
		this.semanticModel = semanticModel;
		this.entityConceptCache = new ArrayList<>(entityConceptCache);
		this.relationConceptCache = new ArrayList<>(relationConceptCache);
	}

	@NotNull
	public Node getSyntaxModel() {
		return syntaxModel;
	}

	public void setSyntaxModel(@NotNull Node syntaxModel) {
		this.syntaxModel = syntaxModel;
	}

	@NotNull
	public SemanticModel getSemanticModel() {
		return semanticModel;
	}

	public void setSemanticModel(@NotNull SemanticModel semanticModel) {
		this.semanticModel = semanticModel;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"DataSourceSchema\""
		       + ", \"@super\":" + super.toString()
		       + ", \"syntaxModel\":" + syntaxModel
		       + ", \"semanticModel\":" + semanticModel
		       + '}';
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}

	@NotNull
	public DataSourceModel getOwningNode() {
		return owningNode;
	}

	public void setOwningNode(@NotNull DataSourceModel owningNode) {
		this.owningNode = owningNode;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<EntityConcept> getEntityConceptCache() {
		return entityConceptCache;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setEntityConceptCache(List<EntityConcept> entityConceptCache) {
		this.entityConceptCache = entityConceptCache;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public List<RelationConcept> getRelationConceptCache() {
		return relationConceptCache;
	}

	@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType - necessary for hibernate")
	public void setRelationConceptCache(List<RelationConcept> relationConceptCache) {
		this.relationConceptCache = relationConceptCache;
	}

	@Override
	public DataSourceSchema copy() {
		return copy(new HashMap<>());
	}

	@Override
	public DataSourceSchema copy(@NotNull Map<Identity<?>, Traversable> copyableLookup) {
		return ObjectUtilities.checkedReturn(
				copy(copyableLookup, () -> new DataSourceSchema(
						owningNode,
						this.syntaxModel.copy(copyableLookup),
						this.semanticModel.copy(copyableLookup),
						this.entityConceptCache,
						this.relationConceptCache,
						this.getIdentity()
				)), DataSourceSchema.class
		);
	}

	@Override
	public DataSourceSchema replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
		if (getIdentity().equals(identity)) {
			return ObjectUtilities.checkedReturn(replacement, this.getClass());
		}
		this.semanticModel = semanticModel.replace(identity, replacement);
		this.syntaxModel = syntaxModel.replace(identity, replacement);
		return this;
	}

	@Override
	public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Identity<?>> visited, @NotNull Deque<Identity<?>> collateralRemoveQueue) {
		visited.add(this.getIdentity());

		List<Traversable> subGraphs = Arrays.asList(syntaxModel, semanticModel);

		boolean result = Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, subGraphs, OWNED | COMPONENT);
		result &= Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, entityConceptCache, OWNED);
		result &= Traversable.removeFromChildren(identity, visited, collateralRemoveQueue, relationConceptCache, OWNED);

		return result;
	}

	@Override
	public void execute(Consumer<? super Traversable> consumer, Set<? super Identity<?>> visited) {
		super.execute(consumer, visited, getAllChildren());
	}

	@Override
	public Traversable find(@NotNull Traversable.Identity<?> identity) {
		if (this.getIdentity().equals(identity)) {
			return this;
		} else {
			Traversable result;
			for (Traversable child : getAllChildren()) {
				if (child != null && (result = child.find(identity)) != null) {
					return result;
				}
			}
		}
		return null;
	}

	@NotNull
	private ArrayList<Traversable> getAllChildren() {
		ArrayList<Traversable> children = new ArrayList<>(entityConceptCache.size() + relationConceptCache.size() + 2);
		children.addAll(entityConceptCache);
		children.addAll(relationConceptCache);
		children.add(syntaxModel);
		children.add(semanticModel);
		return children;
	}
}
