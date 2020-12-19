package de.buw.tmdt.plasma.services.kgs.shared.model.knowledgegraph;

import de.buw.tmdt.plasma.services.kgs.shared.model.Node;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class RelationConcept extends Node {

    private static final long serialVersionUID = -6853275953274480291L;

    private final String label;
    private final HashSet<Property> properties;
    private final String description;
    private final String sourceURI; //the source from which the entity concept was extracted

    public RelationConcept(
            @NotNull String id,
            @NotNull String label,
            @Nullable String description,
            @Nullable Set<Property> properties
    ) {
        this(id, label, description, null, properties);
    }

    public RelationConcept(
            @NotNull String id,
            @NotNull String label,
            @Nullable String description,
            @Nullable String sourceURI,
            @Nullable Set<Property> properties
    ) {
        super(id);
        this.label = label;
        this.description = description == null ? "" : description;
        this.sourceURI = sourceURI == null ? "" : sourceURI;
        this.properties = properties != null ? new HashSet<>(properties) : new HashSet<>();
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    @NotNull
    public Set<Property> getProperties() {
        return Collections.unmodifiableSet(properties);
    }

    @NotNull
    public String getDescription() {
        return description;
    }

    @NotNull
    public String getSourceURI() {
        return sourceURI;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), label, properties, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RelationConcept)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        RelationConcept that = (RelationConcept) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(properties, that.properties) &&
                Objects.equals(description, that.description) &&
                Objects.equals(sourceURI, that.sourceURI);
    }

    @Override
    public String toString() {
        return this.toPropertyValuePairStringBuilder().toString();
    }

    @Override
    protected StringUtilities.PropertyValuePairStringBuilder toPropertyValuePairStringBuilder() {
        StringUtilities.PropertyValuePairStringBuilder propertyValuePairStringBuilder = super.toPropertyValuePairStringBuilder();
        propertyValuePairStringBuilder.addPair("label", label);
        propertyValuePairStringBuilder.addPair("description", description);
        propertyValuePairStringBuilder.addPair("sourceURI", sourceURI);
        propertyValuePairStringBuilder.addPair("properties", properties);
        return propertyValuePairStringBuilder;
    }

    public enum Property {
        REFLEXIVE("Reflexive"),        // f.a. x: xRx
        IRREFLEXIVE("Irreflexive"),    // f.a. x: NOT xRx
        SYMMETRIC("Symmetric"),        // f.a. x,y: if xRy, then yRx
        ANTISYMMETRIC("Antisymmetric"), // f.a. x,y: if xRy and yRx, then x=y
        ASYMMETRIC("Asymmetric"),        // f.a. x,y: if xRy, then NOT yRx (same as irreflexive and antisymmetric)
        TRANSITIVE("Transitive");        // f.a. x,y,z: if xRy and yRz, then xRz
        public final String className;

        Property(@NotNull String className) {
            this.className = className;
        }

        @NotNull
        public static Property byClassName(@NotNull String value) {
            for (Property property : Property.values()) {
                if (property.getClassName().equals(value)) {
                    return property;
                }
            }
            throw new RuntimeException("Invalid Property " + value);
        }

        @NotNull
        public static Set<Property> byClassNames(@Nullable Collection<String> values) {
            if (values == null || values.isEmpty()) {
                return new HashSet<>();
            }

            return values.stream()
                    .map(Property::byClassName)
                    .collect(Collectors.toSet());
        }

        @NotNull
        public String getClassName() {
            return className;
        }
    }
}