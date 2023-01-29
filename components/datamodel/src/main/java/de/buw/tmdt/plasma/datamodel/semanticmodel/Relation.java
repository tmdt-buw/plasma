package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import de.buw.tmdt.plasma.datamodel.CombinedModelIntegrityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ObjectProperty.class, name = "ObjectProperty"),
        @JsonSubTypes.Type(value = DataProperty.class, name = "DataProperty")
})
public abstract class Relation extends CombinedModelElement implements Serializable, RelationTemplate {
    private static final long serialVersionUID = -2964347710102077197L;

    public static final String FROM_PROPERTY = "from";
    public static final String TO_PROPERTY = "to";
    public static final String URI_PROPERTY = "uri";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String PROPERTIES_PROPERTY = "properties";
    public static final String PROVISIONAL_PROPERTY = "provisional";
    public static final String ARRAY_CONTEXT_PROPERTY = "arraycontext";


    /**
     * The uuid of the origin element, identified by CombinedModelElement#uuid.
     */
    private String from;

    /**
     * The uuid of the destination element, identified by CombinedModelElement#uuid.
     */
    private String to;

    @NotNull
    private String uri;
    private String description;
    private final Set<String> properties;
    private boolean provisional;
    private boolean arrayContext;

    public Relation(
            String from,
            String to,
            @NotNull String uri
    ) {
        this(null, from, to, uri, null, null, null, false, false);
    }

    public Relation(
            String from,
            String to,
            @NotNull String uri,
            @NotNull String label
    ) {
        this(null, from, to, uri, label, null, null, false, false);
    }

    public Relation(
            String from,
            String to,
            @NotNull String uri,
            @NotNull String label,
            @Nullable String description
    ) {
        this(null, from, to, uri, label, description, null, false, false);
    }

    @JsonCreator
    public Relation(
            @Nullable @JsonProperty(UUID_PROPERTY) String uuid,
            @Nullable @JsonProperty(FROM_PROPERTY) String from,
            @Nullable @JsonProperty(TO_PROPERTY) String to,
            @NotNull @JsonProperty(URI_PROPERTY) String uri,
            @Nullable @JsonProperty(LABEL_PROPERTY) String label,
            @Nullable @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @Nullable @JsonProperty(PROPERTIES_PROPERTY) Set<String> properties,
            @JsonProperty(ARRAY_CONTEXT_PROPERTY) boolean arrayContext,
            @JsonProperty(PROVISIONAL_PROPERTY) boolean provisional
    ) {
        super(label == null ? "" : label);
        this.arrayContext = arrayContext;
        setUuid(uuid);
        this.from = from;
        this.to = to;
        this.description = description;
        this.uri = uri;
        this.properties = properties == null ? new HashSet<>() : new HashSet<>(properties);
        this.setProvisional(provisional);
    }

    @JsonProperty(FROM_PROPERTY)
    public String getFrom() {
        return from;
    }

    public void setFrom(@NotNull String from) {
        this.from = from;
    }

    @JsonProperty(TO_PROPERTY)
    public String getTo() {
        return to;
    }

    public void setTo(@NotNull String to) {
        this.to = to;
    }

    @JsonProperty(DESCRIPTION_PROPERTY)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty(URI_PROPERTY)
    public String getURI() {
        return uri;
    }

    public void setUri(@NotNull String uri) {
        this.uri = uri;
    }

    @JsonProperty(PROPERTIES_PROPERTY)
    public Set<String> getProperties() {
        return Collections.unmodifiableSet(this.properties);
    }

    @JsonProperty(PROVISIONAL_PROPERTY)
    public boolean isProvisional() {
        return provisional;
    }

    public void setProvisional(boolean provisional) {
        this.provisional = provisional;
    }

    @JsonProperty(ARRAY_CONTEXT_PROPERTY)
    public boolean isArrayContext() {
        return arrayContext;
    }

    public void setArrayContext(boolean arrayContext) {
        this.arrayContext = arrayContext;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        Relation that = (Relation) o;
        return Objects.equals(this.getUuid(), that.getUuid()) &&
                sameAs(that);
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        if (isTemplate()) {
            return "Footer -- " + getURI() + " -> Header";
        }
        return getUuid().substring(0, 8) + ": " + getFrom().substring(0, 8) + " -- " + getURI() + " -> " + getTo().substring(0, 8);
    }

    public abstract Relation copy();

    public abstract Relation instanciate(String fromId, String toId);

    /**
     * Removes all ids from this relation.
     * Used ONLY when used as a template, e.g. in serialization to frontend.
     * For usage within a Java environment, use {@link #instanciate(String, String)} to create a new entity with set ids
     */
    @SuppressWarnings("ConstantConditions") // this is only to be invoked before serialization
    public Relation convertToTemplate() {
        removeUuid();
        this.from = null;
        this.to = null;
        return this;
    }

    @Override
    public boolean isTemplate() {
        return getUuid() == null;
    }

    public void validate() {
        if (getURI() == null || getURI().isBlank()) {
            throw new CombinedModelIntegrityException("Relation " + getUuid() + " has no proper URL assigned");
        }
        if (!isTemplate()) {
            if (getFrom() == null || getFrom().isBlank()) {
                throw new CombinedModelIntegrityException("Relation " + getUuid() + " has no proper fromId");
            }
            if (getTo() == null || getTo().isBlank()) {
                throw new CombinedModelIntegrityException("Relation " + getUuid() + " has no proper toId");
            }
        }
    }

    /**
     * Identifies if two {@link Relation}s represent the same entity.
     * Needed as there might exist multiple {@link Relation}s instances that actually represent the same relation
     * or class but have different uuids.
     *
     * @param relation The {@link Relation} to check
     * @return true of the {@code uri}s of both instances match
     */
    public boolean sameAs(Relation relation) {
        return Objects.equals(getURI(), relation.getURI());
    }


}
