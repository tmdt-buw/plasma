package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.buw.tmdt.plasma.datamodel.PositionedCombinedModelElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_class")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrimitiveNode.class, name = "PrimitiveNode"),
        @JsonSubTypes.Type(value = ObjectNode.class, name = "ObjectNode"),
        @JsonSubTypes.Type(value = SetNode.class, name = "SetNode"),
        @JsonSubTypes.Type(value = CollisionSchema.class, name = "CollisionSchema")
})
public abstract class SchemaNode extends PositionedCombinedModelElement {

    private static final long serialVersionUID = 8398288916936841021L;

    public static final String ARRAY_PATH_TOKEN = "0";
    public static final String ROOT_PATH_TOKEN = "";
    public static final String ARRAY_LABEL = "##array";
    public static final String OBJECT_LABEL = "##object";
    public static final String VALUE_LABEL = "##value";
    public static final List<String> PREDEFINED_LABELS = List.of(ARRAY_LABEL, OBJECT_LABEL, VALUE_LABEL);

    public static final String VALID_PROPERTY = "valid";
    public static final String PATH_PROPERTY = "path";
    public static final String VISIBLE_PROPERTY = "visible";
    public static final String DISABLED_PROPERTY = "disabled";

    private boolean visible = true;
    private boolean disabled = false;

    private List<String> path;

    public SchemaNode(
            @NotNull String label
    ) {
        super(label);
        this.path = new ArrayList<>();
    }

    public SchemaNode(
            @NotNull String uuid,
            @NotNull String label,
            @Nullable List<String> path,
            @Nullable Double xCoordinate,
            @Nullable Double yCoordinate
    ) {
        super(uuid, label, xCoordinate, yCoordinate);
        this.path = path == null ? new ArrayList<>() : path;
        this.visible = true;
        this.disabled = false;
    }

    @JsonCreator
    public SchemaNode(
            @NotNull @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(LABEL_PROPERTY) String label,
            @Nullable @JsonProperty(PATH_PROPERTY) List<String> path,
            @Nullable @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
            @Nullable @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
            @Nullable @JsonProperty(VISIBLE_PROPERTY) Boolean visible,
            @Nullable @JsonProperty(DISABLED_PROPERTY) Boolean disabled
    ) {
        super(uuid, label, xCoordinate, yCoordinate);
        this.path = path == null ? new ArrayList<>() : path;
        this.visible = visible == null || visible;
        this.disabled = disabled != null && disabled;
    }

    @JsonProperty(VISIBLE_PROPERTY)
    public boolean isVisible() {
        return visible;
    }

    @JsonProperty(DISABLED_PROPERTY)
    public boolean isDisabled() {
        return disabled;
    }

    @JsonProperty(PATH_PROPERTY)
    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public void addPathComponent(String component) {
        this.path.add(component);
    }

    /**
     * JSON Path (RFC6901) identifier of that node.
     * Arrays will always be represented with index 0, e.g. /list/0/value in that path as the length of said list is
     * not available at creation time.
     */
    public String getPathAsJSONPointer() {
        return path.stream()
                .map(component -> component.replaceAll("~", "~0"))
                .map(component -> component.replaceAll("/", "~1"))
                .collect(Collectors.joining("/"));
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * Creates a deep clone of the current entity.
     *
     * @return The cloned entity
     */
    public abstract SchemaNode copy();

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchemaNode)) {
            return false;
        }
        SchemaNode that = (SchemaNode) o;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return getUuid().substring(0, 8);
    }
}
