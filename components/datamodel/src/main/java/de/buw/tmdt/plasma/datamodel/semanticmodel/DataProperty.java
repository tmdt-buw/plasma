package de.buw.tmdt.plasma.datamodel.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@JsonTypeName("DataProperty")
public class DataProperty extends Relation {
    public DataProperty( String fromId, String toId, @NotNull String uri) {
        super(fromId, toId, uri);
    }

    public DataProperty(String fromId, String toId, @NotNull String uri, String label) {
        super(fromId, toId, uri, label);
    }

    public DataProperty(String fromId, String toId, @NotNull String uri, String label, String description) {
        super(fromId, toId, uri, label, description);
    }

    public DataProperty(String uuid, String fromId, String toId, @NotNull String uri, String label, String description, Set<String> properties) {
        this(uuid, fromId, toId, uri, label, description, properties, false, false);
    }

    @JsonCreator
    public DataProperty(
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
        super(uuid, from, to, uri, label, description, properties, arrayContext, provisional);
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "DataProperty " + super.toString();
    }

    /**
     * Creates a deep clone of the current entity.
     *
     * @return The cloned entity
     */
    public DataProperty copy() {
        return new DataProperty(getUuid(),
                getFrom(),
                getTo(),
                getURI(),
                getLabel(),
                getDescription(),
                getProperties(),
                isArrayContext(),
                isProvisional()
        );
    }

    @Override
    public DataProperty instanciate(String fromId, String toId) {
        return new DataProperty(fromId, toId, getURI(), getLabel(), getDescription());
    }
}
