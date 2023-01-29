package de.buw.tmdt.plasma.datamodel.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.datamodel.modification.operation.DataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonTypeName("PrimitiveNode")
public class PrimitiveNode extends MappableSyntaxNode {

    private static final long serialVersionUID = -144653268470507986L;

    public static final String CLEANSING_PATTERN_PROPERTY = "pattern";
    public static final String DATATYPE_PROPERTY = "datatype";
    public static final String EXAMPLES_PROPERTY = "examples";

    private String cleansingPattern;
    private List<String> examples;
    private DataType dataType;

    public PrimitiveNode(
            @NotNull String label,
            @NotNull DataType dataType
    ) {
        this(label, true, dataType, null, null);
    }

    public PrimitiveNode(
            @NotNull String label,
            boolean isValid,
            @NotNull DataType dataType
    ) {
        this(label, isValid, dataType, null, null);
    }

    public PrimitiveNode(
            @NotNull String label,
            boolean isValid,
            @NotNull DataType dataType,
            @Nullable List<String> examples,
            @Nullable String cleansingPattern
    ) {
        super(label, isValid);
        this.dataType = dataType;
        this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
        this.cleansingPattern = cleansingPattern;
    }

    public PrimitiveNode(
            @NotNull String label,
            List<String> path,
            boolean isValid,
            @NotNull DataType dataType,
            @Nullable List<String> examples,
            @Nullable String cleansingPattern
    ) {
        super(label, isValid);
        this.dataType = dataType;
        this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
        this.cleansingPattern = cleansingPattern;
        this.setPath(path);
    }

    @JsonCreator
    public PrimitiveNode(
            @NotNull @JsonProperty(UUID_PROPERTY) String uuid,
            @JsonProperty(LABEL_PROPERTY) @NotNull String label,
            @Nullable @JsonProperty(PATH_PROPERTY) List<String> path,
            @JsonProperty(XCOORDINATE_PROPERTY) Double xCoordinate,
            @JsonProperty(YCOORDINATE_PROPERTY) Double yCoordinate,
            @JsonProperty(VALID_PROPERTY) boolean isValid,
            @NotNull @JsonProperty(DATATYPE_PROPERTY) DataType dataType,
            @JsonProperty(EXAMPLES_PROPERTY) List<String> examples,
            @JsonProperty(CLEANSING_PATTERN_PROPERTY) String cleansingPattern,
            @JsonProperty(VISIBLE_PROPERTY) boolean visible,
            @JsonProperty(DISABLED_PROPERTY) boolean disabled
    ) {
        super(uuid, label, path, xCoordinate, yCoordinate, isValid, visible, disabled);
        this.dataType = dataType;
        this.examples = examples != null ? new ArrayList<>(examples) : new ArrayList<>();
        this.cleansingPattern = cleansingPattern;
    }

    @JsonProperty(CLEANSING_PATTERN_PROPERTY)
    public String getCleansingPattern() {
        return cleansingPattern;
    }

    public void setCleansingPattern(String cleansingPattern) {
        this.cleansingPattern = cleansingPattern;
    }

    @JsonProperty(EXAMPLES_PROPERTY)
    public List<String> getExamples() {
        return examples;
    }

    @JsonProperty(DATATYPE_PROPERTY)
    public DataType getDataType() {
        return dataType;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public PrimitiveNode copy() {
        PrimitiveNode node = new PrimitiveNode(
                getUuid(),
                getLabel(),
                getPath(),
                getXCoordinate(),
                getYCoordinate(),
                isValid(),
                getDataType(),
                getExamples(),
                getCleansingPattern(),
                isVisible(),
                isDisabled());

        return node;
    }


    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return super.toString() + " | PrimitiveNode: " + getPathAsJSONPointer();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cleansingPattern, dataType);
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }
}
