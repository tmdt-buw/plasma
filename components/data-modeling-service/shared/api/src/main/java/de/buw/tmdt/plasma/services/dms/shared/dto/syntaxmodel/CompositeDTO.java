package de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.services.dms.shared.dto.syntaxmodel.operation.SyntacticOperationDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonTypeName("CompositeDTO")
public class CompositeDTO extends SchemaNodeDTO {

	private static final long serialVersionUID = 5858068735685107958L;
	private static final String SPLITTER_PROPERTY = "splitter";
	private static final String CLEANSING_PATTERN_PROPERTY = "cleansingPattern";

	private final List<SplitterDTO> splitter;
	private final String cleansingPattern;

	@JsonCreator
	public CompositeDTO(
			@Nullable @JsonProperty(X_COORDINATE_PROPERTY) Double xCoordinate,
			@Nullable @JsonProperty(Y_COORDINATE_PROPERTY) Double yCoordinate,
			@NotNull @JsonProperty(UUID_PROPERTY) UUID uuid,
			@JsonProperty(LABEL_PROPERTY) @NotNull String label,
			@NotNull @JsonProperty(OPERATIONS_PROPERTY) List<SyntacticOperationDTO> operations,
			@JsonProperty(IS_VALID__PROPERTY) boolean isValid,
			@NotNull @JsonProperty(CLEANSING_PATTERN_PROPERTY) String cleansingPattern,
			@NotNull @JsonProperty(SPLITTER_PROPERTY) List<SplitterDTO> splitter
	) {
		super(xCoordinate, yCoordinate, uuid, label, operations, isValid);
		this.cleansingPattern = cleansingPattern;
		this.splitter = Collections.unmodifiableList(splitter);
	}

	@JsonProperty(CLEANSING_PATTERN_PROPERTY)
	@NotNull
	public String getCleansingPattern() {
		return cleansingPattern;
	}

	@NotNull
	@JsonProperty(SPLITTER_PROPERTY)
	public List<SplitterDTO> getSplitter() {
		return Collections.unmodifiableList(splitter);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"CompositeDTO\""
		       + ", \"@super\":" + super.toString()
		       + ", \"cleansingPattern\":\"" + cleansingPattern + '"'
		       + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		CompositeDTO that = (CompositeDTO) o;
		return Objects.equals(cleansingPattern, that.cleansingPattern);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), cleansingPattern);
	}
}
