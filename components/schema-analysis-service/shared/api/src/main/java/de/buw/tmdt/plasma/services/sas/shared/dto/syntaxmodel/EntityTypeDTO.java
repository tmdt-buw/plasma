package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.EntityConceptDTO;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class EntityTypeDTO implements Serializable {
	private static final long serialVersionUID = 4109691502825894813L;

	public static final String ID_PROPERTY = "id";
	public static final String UUID_PROPERTY = "uuid";
	public static final String LABEL_PROPERTY = "label";
	public static final String ORIGINAL_LABEL_PROPERTY = "originalLabel";
	public static final String DESCRIPTION_PROPERTY = "description";
	public static final String CONCEPT_PROPERTY = "concept";
	public static final String DESCRIBES_DATA_PROPERTY = "describesData";

	private final Long id;
	private final String uuid;
	private final String label;
	private final String originalLabel;
	private final String description;
	private final EntityConceptDTO concept;
	private final Boolean describesData;

	public EntityTypeDTO(
			Long id,
			String uuid,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConceptDTO concept,
			@Nullable Boolean describesData
	) {
		/*if (id == null && uuid == null) {
			throw new IllegalArgumentException("Either id or uuid must not be null.");
		}*/
		this.id = id;
		this.uuid = uuid;
		this.label = label;
		this.originalLabel = originalLabel;
		this.description = description;
		this.concept = concept;
		this.describesData = describesData;
	}

	public EntityTypeDTO(
			@NotNull Long id,
			@NotNull String label,
			@NotNull String originalLabel,
			@NotNull String description,
			@NotNull EntityConceptDTO concept
	) {
		this(id, null, label, originalLabel, description, concept, null);
	}

	@JsonCreator
	public EntityTypeDTO(
			@Nullable @JsonProperty(ID_PROPERTY) String idString,
			@Nullable @JsonProperty(UUID_PROPERTY) String uuidString,
			@NotNull @JsonProperty(LABEL_PROPERTY) String label,
			@NotNull @JsonProperty(ORIGINAL_LABEL_PROPERTY) String originalLabel,
			@NotNull @JsonProperty(DESCRIPTION_PROPERTY) String description,
			@NotNull @JsonProperty(CONCEPT_PROPERTY) EntityConceptDTO concept,
			@Nullable @JsonProperty(value = DESCRIBES_DATA_PROPERTY, required = false, defaultValue = "false") Boolean describesData
	) {

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
		this.describesData = null;
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

	@JsonProperty(DESCRIBES_DATA_PROPERTY)
	public Boolean getDescribesData() {
		return describesData;
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
		       + ", \"id\":\"" + id + '"'
		       + ", \"uuid\":" + uuid
		       + ", \"label\":\"" + label + '"'
		       + ", \"originalLabel\":\"" + originalLabel + '"'
		       + ", \"description\":\"" + description + '"'
		       + ", \"concept\":" + concept
		       + ", \"describesData\":\"" + describesData + '"'
		       + '}';
	}
}
