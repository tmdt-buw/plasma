package de.buw.tmdt.plasma.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for all elements that can be used inside a combined model.
 */
public abstract class CombinedModelElement implements Serializable {

    private static final long serialVersionUID = -3619721225687115309L;

    public static final String UUID_PROPERTY = "uuid";
    public static final String LABEL_PROPERTY = "label";

    /**
     * Fixed UUID that identifies this object.
     * Available from the creation of the element.
     * Might be empty if element is used as a template.
     */
    private String uuid;

    /**
     * The label which is a readable form.
     */
    @NotNull
    private String label;

    public CombinedModelElement(
            @NotNull String label
    ) {
        this.label = label;
        this.uuid = UUID.randomUUID().toString();
    }

    @JsonProperty(UUID_PROPERTY)
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? UUID.randomUUID().toString() : uuid;
    }

    protected void removeUuid() {
        this.uuid = null;
    }

    @JsonProperty(LABEL_PROPERTY)
    @NotNull
    public String getLabel() {
        return label;
    }

    public void setLabel(@NotNull String label) {
        this.label = label;
    }


    public abstract CombinedModelElement copy();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CombinedModelElement that = (CombinedModelElement) o;
        return Objects.equals(getUuid(), that.getUuid());
    }

    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"CombinedModelElement\""
                + ",\"uuid\":\"" + uuid + '"'
                + ",\"label\":\"" + label + '"'
                + '}';
    }
}
