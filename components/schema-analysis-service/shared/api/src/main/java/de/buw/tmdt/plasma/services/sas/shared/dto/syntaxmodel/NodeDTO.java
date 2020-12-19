package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PrimitiveNodeDTO.class, name = "PrimitiveNodeDTO"),
		@JsonSubTypes.Type(value = ObjectNodeDTO.class, name = "ObjectNodeDTO"),
		@JsonSubTypes.Type(value = SetNodeDTO.class, name = "SetNodeDTO"),
		@JsonSubTypes.Type(value = CollisionNodeDTO.class, name = "CollisionNodeDTO"),
		@JsonSubTypes.Type(value = CompositeNodeDTO.class, name = "CompositeNodeDTO")
})
public abstract class NodeDTO implements Serializable {

	private static final long serialVersionUID = -8593050435911350942L;

	private static final String UUID_PROPERTY = "uuid";

	@JsonProperty(UUID_PROPERTY)
	private UUID uuid;

	@JsonCreator
	protected NodeDTO(UUID uuid) {
		this.uuid = uuid;
	}

	@NotNull
	@JsonProperty(UUID_PROPERTY)
	public UUID getUuid() {
		return uuid;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"NodeDTO\""
		       + ", \"uuid\":\"" + uuid
		       + '}';
	}
}
