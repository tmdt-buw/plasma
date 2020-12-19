package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@JsonTypeName("CollisionNodeDTO")
public class CollisionNodeDTO extends NodeDTO {
	private static final long serialVersionUID = -6447531211699583320L;
	private static final String PRIMITIVE_NODE_PROPERTY = "primitiveNode";
	private static final String OBJECT_NODE_PROPERTY = "objectNode";
	private static final String SET_NODE_PROPERTY = "setNode";
	private static final String UUID_PROPERTY = "uuid";

	@JsonProperty(value = PRIMITIVE_NODE_PROPERTY)
	private PrimitiveNodeDTO primitiveNode;

	@JsonProperty(value = OBJECT_NODE_PROPERTY)
	private ObjectNodeDTO objectNode;

	@JsonProperty(value = SET_NODE_PROPERTY)
	private SetNodeDTO setNode;

	@JsonCreator
	public CollisionNodeDTO(
			@NotNull @JsonProperty(UUID_PROPERTY) UUID uuid,
			@Nullable @JsonProperty(PRIMITIVE_NODE_PROPERTY) PrimitiveNodeDTO primitiveNode,
			@Nullable @JsonProperty(OBJECT_NODE_PROPERTY) ObjectNodeDTO objectNode,
			@Nullable @JsonProperty(SET_NODE_PROPERTY) SetNodeDTO setNode) {
		super(uuid);
		this.primitiveNode = primitiveNode;
		this.objectNode = objectNode;
		this.setNode = setNode;
	}

	@Nullable
	@JsonProperty(PRIMITIVE_NODE_PROPERTY)
	public PrimitiveNodeDTO getPrimitiveNode() {
		return primitiveNode;
	}

	@Nullable
	@JsonProperty(OBJECT_NODE_PROPERTY)
	public ObjectNodeDTO getObjectNode() {
		return objectNode;
	}

	@Nullable
	@JsonProperty(SET_NODE_PROPERTY)
	public SetNodeDTO getSetNode() {
		return setNode;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"CollisionNodeDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + ", \"primitiveNode\":" + primitiveNode
		       + ", \"objectNode\":" + objectNode
		       + ", \"setNode\":" + setNode
		       + '}';
	}
}
