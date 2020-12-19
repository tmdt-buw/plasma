package de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RelationConceptDTO implements Serializable {
    private static final long serialVersionUID = -5112996969222323409L;
    public static final String ID_PROPERTY = "id";
    private static final String UUID_PROPERTY = "uuid";
    private static final String NAME_PROPERTY = "name";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String SOURCE_URI_PROPERTY = "sourceURI";
    private static final String PROPERTIES_PROPERTY = "properties";

    private final Long id;
    private final String uuid;
    private final String name;
    private final String description;
    private final String sourceURI;
    private final HashSet<String> properties;

    private RelationConceptDTO(@Nullable Long id, @Nullable String uuid, @NotNull String name, @NotNull String description, @NotNull String sourceURI, @NotNull Set<String> properties) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.sourceURI = sourceURI;
        this.properties = new HashSet<>(properties);
    }

    @JsonCreator
    public RelationConceptDTO(
            @Nullable @JsonProperty(ID_PROPERTY) String id,
            @Nullable @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(NAME_PROPERTY) String name,
            @NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @NotNull @JsonProperty(SOURCE_URI_PROPERTY) String sourceURI,
            @NotNull @JsonProperty(PROPERTIES_PROPERTY) Set<String> properties
    ) {
        this(
                id != null ? Long.parseLong(id) : null,
                uuid,
                name,
                description,
                sourceURI,
                properties
        );
    }

    @JsonProperty(UUID_PROPERTY)
    public String getSerializedUuid() {
        return StringUtilities.toStringIfExists(uuid);
    }

    @JsonIgnore
    public String getUuid() {
        return uuid;
    }

    @JsonProperty(ID_PROPERTY)
    public String getSerializedId() {
        return StringUtilities.toStringIfExists(id);
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    @JsonProperty(NAME_PROPERTY)
    public String getName() {
        return this.name;
    }

    @JsonProperty(DESCRIPTION_PROPERTY)
    public String getDescription() {
        return description;
    }

    @JsonProperty(SOURCE_URI_PROPERTY)
    public String getSourceURI() {
        return sourceURI;
    }

    @JsonProperty(PROPERTIES_PROPERTY)
    public Set<String> getProperties() {
        return Collections.unmodifiableSet(this.properties);
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass().equals(o.getClass())) {
            RelationConceptDTO that = (RelationConceptDTO) o;
            return this.name.equals(that.name);
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"RelationConceptDTO\""
                + ", \"uuid\":\"" + uuid + '"'
                + ", \"name\":\"" + name + '"'
                + ", \"description\":\"" + description + '"'
                + ", \"sourceURI\":\"" + sourceURI + '"'
                + ", \"properties\":" + StringUtilities.setToJson(properties)
                + '}';
    }
}
