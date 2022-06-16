package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.datamodel.CombinedModelElement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Edge of the syntax model between two schema nodes.
 */
public class Edge extends CombinedModelElement {

    private static final long serialVersionUID = 40733795713697672L;

    public static final String FROM_PROPERTY = "from";
    public static final String TO_PROPERTY = "to";
    public static final String ANNOTATIONS_PROPERTY = "annotations";

    /**
     * An identifier of the origin node, references to a {@link SchemaNode#getUuid()}.
     */
    @NotNull
    private String fromId;

    /**
     * An identifier of the target node, references to a {@link SchemaNode#getUuid()}.
     */
    @NotNull
    private String toId;

    /**
     * Optional additional annotation / information for this edge.
     * Will, for example, contain the splitting pattern if used to link a composite node to its child nodes.
     */
    @Nullable
    private String annotation;

    @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION") // does not intent to override
    public Edge(@NotNull String fromId, @NotNull String toId) {
        this(UUID.randomUUID().toString(), "", fromId, toId, null);
    }

    public Edge(
            @NotNull String uuid,
            @NotNull String label,
            @NotNull String fromId,
            @NotNull String toId
    ) {
        this(uuid, label, fromId, toId, null);
    }

    @JsonCreator
    public Edge(
            @NotNull @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @NotNull @JsonProperty(FROM_PROPERTY) String fromId,
            @NotNull @JsonProperty(TO_PROPERTY) String toId,
            @Nullable @JsonProperty(ANNOTATIONS_PROPERTY) String annotation
    ) {
        super(label);
        setUuid(uuid);
        this.fromId = fromId;
        this.toId = toId;
        this.annotation = annotation;
    }

    @JsonProperty(FROM_PROPERTY)
    @NotNull
    public String getFromId() {
        return fromId;
    }

    public void setFromId(@NotNull String fromId) {
        this.fromId = fromId;
    }

    @JsonProperty(TO_PROPERTY)
    @NotNull
    public String getToId() {
        return toId;
    }

    public void setToId(@NotNull String toId) {
        this.toId = toId;
    }

    @Nullable
    @JsonProperty(ANNOTATIONS_PROPERTY)
    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(@Nullable String annotation) {
        this.annotation = annotation;
    }

    /**
     * Creates a deep clone of the current entity.
     *
     * @return The cloned entity
     */
    @Override
    public Edge copy() {
        return new Edge(getUuid(), getLabel(), getFromId(), getToId(), getAnnotation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        Edge edge = (Edge) o;
        return Objects.equals(getUuid(), edge.getUuid());
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"Edge\""
                + ", \"uuid\":" + getUuid()
                + ", \"from\":" + fromId
                + ", \"to\":" + toId
                + (annotation != null ? ", \"annotation\":" + annotation : "")
                + '}';
    }
}
