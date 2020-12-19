package de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel;

import de.buw.tmdt.plasma.services.sas.core.model.Traversable;
import de.buw.tmdt.plasma.services.sas.core.model.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityConcept;
import de.buw.tmdt.plasma.services.sas.core.model.semanticmodel.EntityType;
import de.buw.tmdt.plasma.services.sas.core.model.Position;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.members.EntityConceptSuggestion;
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
@Table(name = "primitive_nodes")
public class PrimitiveNode extends Node implements RawDataContainer {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private EntityType entityType;
    @Column
    private DataType dataType;
    @Column
    private final String cleansingPattern;
    @ElementCollection
    private final List<String> examples;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<EntityConceptSuggestion> entityConceptSuggestions;
    //Hibernate constructor

    //creates invalid state if not properly initialized afterwards
    protected PrimitiveNode() {
        entityType = null;
        dataType = null;
        cleansingPattern = null;
        examples = null;
        entityConceptSuggestions = null;
    }

    public PrimitiveNode(@Nullable EntityType entityType, @NotNull DataType dataType, @Nullable List<String> examples) {
        this(entityType, dataType, examples, null, "");
    }

    private PrimitiveNode(
            @Nullable EntityType entityType,
            @NotNull DataType dataType,
            @Nullable List<String> examples,
            @Nullable List<EntityConceptSuggestion> entityConceptSuggestions,
            @Nullable String cleansingPattern
    ) {
        this(entityType, dataType, examples, entityConceptSuggestions, cleansingPattern, null, Traversable.Identity.random());
    }

    private PrimitiveNode(
            @Nullable EntityType entityType,
            @NotNull DataType dataType,
            @Nullable List<String> examples,
            @Nullable List<EntityConceptSuggestion> entityConceptSuggestions,
            @Nullable String cleansingPattern,
            @Nullable Position position,
            @NotNull Traversable.Identity<?> identity
    ) {
        super(position, identity);
        this.setUuid(UUID.randomUUID());
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
        predictDataType();
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

    private void addExample(String example) {
        examples.add(example);
    }

    @NotNull
    @Override
    public PrimitiveNode copy(@NotNull Map<Traversable.Identity<?>, Traversable> copyableLookup) {
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
    public Node replace(@NotNull Traversable.Identity<?> identity, @NotNull Traversable replacement) {
        if (this.getIdentity().equals(identity)) {
            return ObjectUtilities.checkedReturn(replacement, Node.class);
        }
        if (this.entityType != null) {
            this.entityType = entityType.replace(identity, replacement);
        }
        return this;
    }

    @Override
    public void execute(Consumer<? super Traversable> consumer, Set<? super Traversable.Identity<?>> visited) {
        super.execute(consumer, visited, entityType);
    }

    @Override
    public boolean remove(@NotNull Traversable.Identity<?> identity, @NotNull Set<Traversable.Identity<?>> visited, @NotNull Deque<Traversable.Identity<?>> collateralRemoveQueue) {
        visited.add(this.getIdentity());

        if (this.entityType != null && (this.entityType.getIdentity().equals(identity) || !this.entityType.remove(identity, visited, collateralRemoveQueue))) {
            this.entityType = null;
        }

        return true;
    }

    @Override
    public Traversable find(@NotNull Traversable.Identity<?> identity) {
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

    @Override
    public Node merge(Node other) throws SchemaAnalysisException {
        if (other instanceof CollisionNode) {
            return other.merge(this);
        } else if (other instanceof PrimitiveNode) {
            return this.merge((PrimitiveNode) other);
        } else if (other instanceof ObjectNode) {
            return new CollisionNode(this, (ObjectNode) other, null);
        } else if (other instanceof SetNode) {
            return new CollisionNode(this, null, (SetNode) other);
        } else if (other == null) {
            return this;
        } else {
            throw new SchemaAnalysisException("Unknown type"); // needs to get correct exception
        }
    }

    public PrimitiveNode merge(PrimitiveNode other) throws SchemaAnalysisException {
        if (other == null) {
            return this;
        }
        if (this.getUuid().equals(other.getUuid()) && this.equals(other)) {
            throw new SchemaAnalysisException("Tried to merge Schema with itself");
        }
        other.examples.forEach(this::addExample);
        return this;
    }

    public enum DataType {
        UNKNOWN("Unknown"),
        STRING("String"),
        BOOLEAN("Boolean"),
        NUMBER("Number"),
        BINARY("Binary");

        public final String identifier;

        DataType(String identifier) {
            this.identifier = identifier;
        }

        public static DataType fromIdentifier(String string) {
            for (DataType dataType : DataType.values()) {
                if (dataType.identifier.equals(string)) {
                    return dataType;
                }
            }
            throw new IllegalArgumentException("Unknown identifier for DataType `" + string + "`.");
        }
    }

    private void predictDataType() {
        if (examples.size() > 0) {
            if (isBoolean(examples)) {
                this.dataType = DataType.BOOLEAN;
            } else if (isNumber(examples)) {
                this.dataType = DataType.NUMBER;
            } else {
                this.dataType = DataType.STRING;
            }
        } else {
            this.dataType = DataType.UNKNOWN;
        }
    }

    private boolean isBoolean(List<String> examples) {
        for (String s : examples) {
            if (!isBoolean(s)) {
                return false;
            }
        }
        return true;
    }

    private boolean isBoolean(String value) {
        return value != null && Arrays.stream(new String[]{"true", "false", "1", "0"})
                .anyMatch(b -> b.equalsIgnoreCase(value));
    }

    private boolean isNumber(List<String> examples) {
        for (String s : examples) {
            try {
                Double r = Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }


}
