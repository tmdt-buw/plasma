package de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class EntityConceptDTO implements Serializable {
    private static final long serialVersionUID = 2351939502355153654L;
    public static final String ID_PROPERTY = "id";
    public static final String UUID_PROPERTY = "uuid";
    private static final String NAME_PROPERTY = "name";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String SOURCE_URI_PROPERTY = "sourceURI";

    private final Long id;
    private final String uuid;
    private final String name;
    private final String description;
    private final String sourceURI;

    public EntityConceptDTO(Long id, String uuid, @NotNull String name, @NotNull String description, @NotNull String sourceURI) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.sourceURI = sourceURI;
    }

    @JsonCreator
    public EntityConceptDTO(
            @JsonProperty(ID_PROPERTY) String id,
            @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(NAME_PROPERTY) String name,
            @NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @NotNull @JsonProperty(SOURCE_URI_PROPERTY) String sourceURI
    ) {
        this(
                id != null ? Long.parseLong(id) : null,
                uuid,
                name,
                description,
                sourceURI
        );
    }

    @JsonProperty(ID_PROPERTY)
    public String getSerializedId() {
        return StringUtilities.toStringIfExists(id);
    }

    @JsonIgnore
    public Long getId() {
        return id;
    }

    @JsonProperty(UUID_PROPERTY)
    public String getSerializedUuid() {
        return StringUtilities.toStringIfExists(uuid);
    }

    @JsonIgnore
    public String getUuid() {
        return uuid;
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

    public int hashCode() {
        return this.name.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass().equals(o.getClass())) {
            EntityConceptDTO that = (EntityConceptDTO) o;
            return this.name.equals(that.name);
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("MagicCharacter")
    public String toString() {
        return "{\"@class\":\"EntityConceptDTO\""
                + ", \"id\":\"" + id + '"'
                + ", \"uuid\":\"" + uuid + '"'
                + ", \"name\":\"" + name + '"'
                + ", \"description\":\"" + description + '"'
                + '}';
    }
}
