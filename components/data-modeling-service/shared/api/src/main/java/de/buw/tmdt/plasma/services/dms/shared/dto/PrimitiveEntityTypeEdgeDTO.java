package de.buw.tmdt.plasma.services.dms.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.UUID;

public class PrimitiveEntityTypeEdgeDTO implements Serializable {

	private static final String PARENT_UUID = "from";
	private static final String ENTITY_TYPE_ID = "to";
	private static final String PRIMITIVE_UUID = "mappedPrimitive";
	private static final long serialVersionUID = -6888269571341658964L;

	private final UUID parentUUID;
	private final Long entityTypeId;
	private final UUID primitiveUUID;

	@JsonCreator
	public PrimitiveEntityTypeEdgeDTO(
			@JsonProperty(PARENT_UUID) @NotNull String parentUUID,
			@JsonProperty(ENTITY_TYPE_ID) @NotNull String serializedEntityTypeId,
			@JsonProperty(PRIMITIVE_UUID) @NotNull String primitiveUUID
	) {
		this.parentUUID = UUID.fromString(parentUUID);
		this.entityTypeId = Long.parseLong(serializedEntityTypeId);
		this.primitiveUUID = UUID.fromString(primitiveUUID);
	}

	public PrimitiveEntityTypeEdgeDTO(@NotNull UUID parentUUID, @NotNull Long entityTypeId, @NotNull UUID primitiveUUID) {
		this.parentUUID = parentUUID;
		this.primitiveUUID = primitiveUUID;
		this.entityTypeId = entityTypeId;
	}

	@NotNull
	@JsonIgnore
	public UUID getParentUUID() {
		return parentUUID;
	}

	@NotNull
	@JsonProperty(PARENT_UUID)
	public String getParentUUIAsString() {
		return parentUUID.toString();
	}

	@NotNull
	@JsonIgnore
	public UUID getPrimitiveUUID() {
		return primitiveUUID;
	}

	@NotNull
	@JsonProperty(PRIMITIVE_UUID)
	public String getPrimitiveUUIDAsString() {
		return primitiveUUID.toString();
	}

	@NotNull
	@JsonIgnore
	public Long getEntityTypeId() {
		return entityTypeId;
	}

	@NotNull
	@JsonProperty(ENTITY_TYPE_ID)
	public String getEntityTypeIdAsString() {
		return this.entityTypeId.toString();
	}

	@Override
	public String toString() {
		return "PrimitiveEntityTypeEdgeDTO{" +
		       "parentUUID=" + parentUUID +
		       ", entityTypeId=" + entityTypeId +
		       ", primitiveUUID=" + primitiveUUID +
		       '}';
	}
}
