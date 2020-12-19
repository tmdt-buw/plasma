package de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class EntityConceptDTO implements Serializable {
    public static final String ID_PROPERTY = "id";
    public static final String UUID_PROPERTY = "uuid";
    private static final long serialVersionUID = 2351939502355153654L;
    private static final String NAME_PROPERTY = "name";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String SOURCE_URI_PROPERTY = "sourceURI";

    private final Long id;
    private final String uuid;
    private final String name;
    private final String description;
    private final String sourceURI;

    public EntityConceptDTO(@Nullable Long id, @Nullable String uuid, @NotNull String name, @NotNull String description, @NotNull String sourceURI) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.sourceURI = sourceURI;
    }

    @JsonCreator
    public EntityConceptDTO(
            @Nullable @JsonProperty(ID_PROPERTY) String id,
            @Nullable @JsonProperty(UUID_PROPERTY) String uuid,
            @NotNull @JsonProperty(NAME_PROPERTY) String name,
            @Nullable @JsonProperty(DESCRIPTION_PROPERTY) String description,
            @Nullable @JsonProperty(SOURCE_URI_PROPERTY) String sourceURI
    ) {
        this(
                id != null ? Long.parseLong(id) : null,
                uuid,
                name,
                description != null ? description : "",
                sourceURI != null ? sourceURI : ""
        );
    }

    @JsonProperty(ID_PROPERTY)
    @Nullable
    public String getSerializedId() {
        return StringUtilities.toStringIfExists(id);
    }

    @JsonIgnore
    @Nullable
    public Long getId() {
        return id;
    }

    @JsonProperty(UUID_PROPERTY)
    @Nullable
    public String getSerializedUuid() {
        return StringUtilities.toStringIfExists(uuid);
    }

    @JsonIgnore
    @Nullable
    public String getUuid() {
        return uuid;
    }

    @JsonProperty(NAME_PROPERTY)
    @NotNull
    public String getName() {
        return this.name;
    }

    @JsonProperty(SOURCE_URI_PROPERTY)
    @NotNull
    public String getSourceURI() {
        return sourceURI;
    }

    @JsonProperty(DESCRIPTION_PROPERTY)
    @NotNull
    public String getDescription() {
        return description;
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