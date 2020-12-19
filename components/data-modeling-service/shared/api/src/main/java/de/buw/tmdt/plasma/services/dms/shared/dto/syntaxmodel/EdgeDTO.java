package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import de.buw.tmdt.plasma.utilities.collections.CollectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;

public class EdgeDTO implements Serializable {

	private static final String FROM_PROPERTY = "from";
	private static final String TO_PROPERTY = "to";
	private static final String KEY_PROPERTY = "key";
	private static final String LABEL_PROPERTY = "label";
	private static final String OPERATIONS_PROPERTY = "operations";
	private static final long serialVersionUID = 40733795713697672L;

	private final UUID from;
	private final UUID to;
	private final String key;
	private final ArrayList<SyntacticOperationDTO> operations;

	public EdgeDTO(@NotNull SchemaNodeDTO from, @NotNull SchemaNodeDTO to) {
		this(from, to, null);
	}

	public EdgeDTO(@NotNull SchemaNodeDTO from, @NotNull SchemaNodeDTO to, String key) {
		this(from, to, key, null);
	}

	public EdgeDTO(
			@NotNull SchemaNodeDTO from,
			@NotNull SchemaNodeDTO to,
			@Nullable String key,
			@Nullable List<SyntacticOperationDTO> operations
	) {
		this.from = from.getUuid();
		this.to = to.getUuid();
		this.key = key;
		if (CollectionUtilities.containsNull(operations)) {
			throw new IllegalArgumentException("Operations must not contain null.");
		}
		this.operations = operations != null ? new ArrayList<>(operations) : new ArrayList<>();
	}

	@JsonCreator
	public EdgeDTO(
			@JsonProperty(FROM_PROPERTY) @NotNull String fromUuidString,
			@JsonProperty(TO_PROPERTY) @NotNull String toUuidString,
			@JsonProperty(KEY_PROPERTY) @Nullable String key,
			@JsonProperty(OPERATIONS_PROPERTY) @Nullable List<SyntacticOperationDTO> operations
	) {
		try {
			this.from = UUID.fromString(fromUuidString);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to deserialize tail uuid.", e);
		}
		try {
			this.to = UUID.fromString(toUuidString);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Failed to deserialize tail uuid.", e);
		}
		this.key = key;
		this.operations = operations != null ? new ArrayList<>(operations) : new ArrayList<>();
	}

	@JsonProperty(FROM_PROPERTY)
	@NotNull
	public UUID getFrom() {
		return from;
	}

	@JsonProperty(TO_PROPERTY)
	@NotNull
	public UUID getTo() {
		return to;
	}

	@JsonProperty(KEY_PROPERTY)
	@Nullable
	public String getKey() {
		return key;
	}

	@JsonProperty(LABEL_PROPERTY)
	@Nullable
	public String getLabel() {
		return key;
	}

	@JsonProperty(OPERATIONS_PROPERTY)
	@Nullable
	public List<SyntacticOperationDTO> getOperations() {
		return Collections.unmodifiableList(operations);
	}

	@Override
	public int hashCode() {

		return Objects.hash(from, to, key, operations);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		EdgeDTO edgeDTO = (EdgeDTO) o;
		return Objects.equals(from, edgeDTO.from) &&
		       Objects.equals(to, edgeDTO.to) &&
		       Objects.equals(key, edgeDTO.key) &&
		       Objects.equals(operations, edgeDTO.operations);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EdgeDTO\""
		       + ", \"from\":" + from
		       + ", \"to\":" + to
		       + ", \"key\":\"" + key + '"'
		       + ", \"operations\":" + StringUtilities.listToJson(operations)
		       + '}';
	}
}
