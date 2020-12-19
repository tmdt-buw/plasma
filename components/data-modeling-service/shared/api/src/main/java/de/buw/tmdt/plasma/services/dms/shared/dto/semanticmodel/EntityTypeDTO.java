package de.buw.tmdt.plasma.services.dms.shared.dto.semanticmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.dms.shared.dto.Positioned;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EntityTypeDTO extends Positioned {

	private static final long serialVersionUID = 4109691502825894813L;

	public static final String ID_PROPERTY = "id";
	public static final String UUID_PROPERTY = "uuid";
	public static final String LABEL_PROPERTY = "label";
	public static final String ORIGINAL_LABEL_PROPERTY = "originalLabel";
	public static final String DESCRIPTION_PROPERTY = "description";
	public static final String CONCEPT_PROPERTY = "concept";
	public static final String MAPPED_TO_DATA_PROPERTY = "mappedToData";

	private final Long id;
	private final String uuid;
	private final String label;
	private final String originalLabel;
	private final String description;
	private final EntityConceptDTO concept;
	private boolean mappedToData;

	@Contract("_, _, null, null, _, _, _, _, _ -> fail")
	public EntityTypeDTO(
			@Nullable Double xCoordinate,
			@Nullable Double yCoordinate,
			@Nullable Long id,
			@Nullable String uuid,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConceptDTO concept,
			boolean mappedToData
	) {
		super(xCoordinate, yCoordinate);
		if (id == null && uuid == null) {
			throw new IllegalArgumentException("Either id or uuid must not be null.");
		}
		this.id = id;
		this.uuid = uuid;
		this.label = label;
		this.originalLabel = originalLabel;
		this.description = description;
		this.concept = concept;
		this.mappedToData = mappedToData;
	}

	public EntityTypeDTO(
			@Nullable Double xCoordinate,
			@Nullable Double yCoordinate,
			@NotNull Long id,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConceptDTO concept
	) {
		this(xCoordinate, yCoordinate, id, null, label, originalLabel, description, concept, false);
	}

	@JsonCreator
	public EntityTypeDTO(
			@Nullable @JsonProperty(X_COORDINATE_PROPERTY) Double xCoordinate,
			@Nullable @JsonProperty(Y_COORDINATE_PROPERTY) Double yCoordinate,
			@Nullable @JsonProperty(ID_PROPERTY) String idString,
			@Nullable @JsonProperty(UUID_PROPERTY) String uuidString,
			@NotNull @JsonProperty(LABEL_PROPERTY) String label,
			@NotNull @JsonProperty(ORIGINAL_LABEL_PROPERTY) String originalLabel,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@NotNull @JsonProperty(CONCEPT_PROPERTY) EntityConceptDTO concept,
			@JsonProperty(value = MAPPED_TO_DATA_PROPERTY, required = false, defaultValue = "false") boolean mappedToData
	) {
		super(xCoordinate, yCoordinate);
		try {
			this.id = idString != null ? Long.parseLong(idString) : null;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid id passed to constructor: " + idString + " is neither null nor a valid long literal.", e);
		}
		try {
			this.uuid = uuidString;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid uuid passed to constructor: " + uuidString + " is neither null nor a valid UUID literal.", e);
		}
		this.label = label;
		this.originalLabel = originalLabel;
		this.description = description;
		this.concept = concept;
		this.mappedToData = mappedToData;
	}

	@JsonProperty(ID_PROPERTY)
	public String getIdAsString() {
		return StringUtilities.toStringIfExists(id);
	}

	@JsonIgnore
	public Long getId() {
		return id;
	}

	@JsonProperty(UUID_PROPERTY)
	public String getUuidAsString() {
		return StringUtilities.toStringIfExists(uuid);
	}

	@JsonIgnore
	public String getUuid() {
		return uuid;
	}

	@JsonProperty(LABEL_PROPERTY)
	public String getLabel() {
		return label;
	}

	@JsonProperty(ORIGINAL_LABEL_PROPERTY)
	public String getOriginalLabel() {
		return originalLabel;
	}

	@JsonProperty(DESCRIPTION_PROPERTY)
	public String getDescription() {
		return description;
	}

	@JsonProperty(CONCEPT_PROPERTY)
	public EntityConceptDTO getConcept() {
		return concept;
	}

	@JsonProperty(MAPPED_TO_DATA_PROPERTY)
	public Boolean isMappedToData() {
		return mappedToData;
	}

	public void setMappedToData(boolean mappedToData) {
		this.mappedToData = mappedToData;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, label, originalLabel, description, concept);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !getClass().equals(o.getClass())) {
			return false;
		}
		EntityTypeDTO that = (EntityTypeDTO) o;
		return Objects.equals(id, that.id) &&
		       Objects.equals(label, that.label) &&
		       Objects.equals(originalLabel, that.originalLabel) &&
		       Objects.equals(description, that.description) &&
		       Objects.equals(concept, that.concept);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"EntityTypeDTO\""
		       + ", \"@super\":" + super.toString()
		       + ", \"id\":\"" + id + '"'
		       + ", \"uuid\":" + uuid
		       + ", \"label\":\"" + label + '"'
		       + ", \"originalLabel\":\"" + originalLabel + '"'
		       + ", \"description\":\"" + description + '"'
		       + ", \"concept\":" + concept
		       + ", \"mappedToData\":\"" + mappedToData + '"'
		       + '}';
	}
}