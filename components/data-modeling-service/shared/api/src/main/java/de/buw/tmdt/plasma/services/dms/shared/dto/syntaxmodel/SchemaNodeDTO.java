package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.*;
import de.buw.tmdt.plasma.services.dms.shared.dto.Positioned;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({
		@JsonSubTypes.Type(value = PrimitiveDTO.class, name = "PrimitiveDTO"),
		@JsonSubTypes.Type(value = ObjectNodeDTO.class, name = "ObjectNodeDTO"),
		@JsonSubTypes.Type(value = SetNodeDTO.class, name = "SetNodeDTO"),
		@JsonSubTypes.Type(value = CollisionSchemaDTO.class, name = "CollisionSchemaDTO"),
		@JsonSubTypes.Type(value = CompositeDTO.class, name = "CompositeDTO")
})
public class SchemaNodeDTO extends Positioned {

	protected static final String UUID_PROPERTY = "id";
	protected static final String LABEL_PROPERTY = "label";
	protected static final String IS_VALID__PROPERTY = "isValid";
	protected static final String OPERATIONS_PROPERTY = "operations";
	private static final long serialVersionUID = 8398288916936841021L;

	private final UUID uuid;
	private final String label;
	private final boolean isValid;
	private final ArrayList<SyntacticOperationDTO> operations;

	public SchemaNodeDTO(
			@Nullable Double xCoordinate,
			@Nullable Double yCoordinate,
			@NotNull UUID uuid,
			@NotNull String label,
			@NotNull List<SyntacticOperationDTO> operations,
			boolean isValid
	) {
		super(xCoordinate, yCoordinate);
		this.uuid = uuid;
		this.label = label;
		this.isValid = isValid;
		if (CollectionUtilities.containsNull(operations)) {
			throw new IllegalArgumentException("Operations must not contain null.");
		}
		this.operations = new ArrayList<>(operations);
	}

	@JsonCreator
	public SchemaNodeDTO(
			@Nullable @JsonProperty(X_COORDINATE_PROPERTY) Double xCoordinate,
			@Nullable @JsonProperty(Y_COORDINATE_PROPERTY) Double yCoordinate,
			@JsonProperty(UUID_PROPERTY) @NotNull String uuidString,
			@JsonProperty(LABEL_PROPERTY) @NotNull String label,
			@JsonProperty(OPERATIONS_PROPERTY) @NotNull List<SyntacticOperationDTO> operations,
			@JsonProperty(IS_VALID__PROPERTY) boolean isValid
	) {
		super(xCoordinate, yCoordinate);
		this.uuid = UUID.fromString(uuidString);
		this.label = label;
		this.isValid = isValid;
		if (CollectionUtilities.containsNull(operations)) {
			throw new IllegalArgumentException("Operations must not contain null.");
		}
		this.operations = new ArrayList<>(operations);
	}

	@JsonProperty(IS_VALID__PROPERTY)
	public boolean isValid() {
		return isValid;
	}

	@JsonIgnore
	@NotNull
	public UUID getUuid() {
		return uuid;
	}

	@JsonProperty(UUID_PROPERTY)
	@NotNull
	public String getUUIDString() {
		return this.uuid.toString();
	}

	@JsonProperty(LABEL_PROPERTY)
	@NotNull
	public String getLabel() {
		return this.label;
	}

	@JsonProperty(OPERATIONS_PROPERTY)
	@NotNull
	public List<SyntacticOperationDTO> getOperations() {
		return operations;
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, isValid, operations);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		SchemaNodeDTO that = (SchemaNodeDTO) o;
		return isValid == that.isValid &&
		       Objects.equals(uuid, that.uuid) &&
		       Objects.equals(operations, that.operations);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SchemaNodeDTO\""
		       + ", \"@super\":" + super.toString()
		       + ", \"uuid\":" + uuid
		       + ", \"isValid\":\"" + isValid + '"'
		       + ", \"operations\":" + StringUtilities.listToJson(operations)
		       + '}';
	}
}
