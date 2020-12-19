package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

@JsonTypeName("PrimitiveNodeDTO")
public class PrimitiveNodeDTO extends NodeDTO {

	private static final long serialVersionUID = 3226181900274436204L;
	private static final String ENTITY_TYPE_PROPERTY = "entity_type";
	private static final String DATA_TYPE_PROPERTY = "data_type";
	private static final String CLEANSING_PATTERN_PROPERTY = "cleaning_pattern";
	private static final String EXAMPLES_PROPERTY = "examples";
	private static final String ENTITY_CONCEPT_SUGGESTIONS_PROPERTY = "entity_concept_suggestions";
	private static final String UUID_PROPERTY = "uuid";

	@JsonProperty(ENTITY_TYPE_PROPERTY)
	private final EntityTypeDTO entityType;
	@JsonProperty(DATA_TYPE_PROPERTY)
	private final DataTypeDTO dataType;
	@JsonProperty(CLEANSING_PATTERN_PROPERTY)
	private final String cleansingPattern;
	@JsonProperty(EXAMPLES_PROPERTY)
	private final List<String> examples;
	@JsonProperty(ENTITY_CONCEPT_SUGGESTIONS_PROPERTY)
	private final List<EntityConceptSuggestionDTO> entityConceptSuggestions;


	@JsonCreator
	public PrimitiveNodeDTO(@NotNull @JsonProperty(UUID_PROPERTY) UUID uuid,
	                        @JsonProperty(ENTITY_TYPE_PROPERTY) EntityTypeDTO entityType,
	                        @JsonProperty(DATA_TYPE_PROPERTY) DataTypeDTO dataType,
                            @JsonProperty(CLEANSING_PATTERN_PROPERTY) String cleansingPattern,
                            @JsonProperty(EXAMPLES_PROPERTY) List<String> examples,
                            @JsonProperty(ENTITY_CONCEPT_SUGGESTIONS_PROPERTY) List<EntityConceptSuggestionDTO> entityConceptSuggestions
	                        ) {
		super(uuid);
		this.entityType = entityType;
		this.dataType = dataType;
		this.cleansingPattern = cleansingPattern;
		this.examples = examples;
		this.entityConceptSuggestions = entityConceptSuggestions;
	}

	@JsonProperty(ENTITY_TYPE_PROPERTY)
	public EntityTypeDTO getEntityType() {
		return entityType;
	}

	@JsonProperty(DATA_TYPE_PROPERTY)
	public DataTypeDTO getDataType() {
		return dataType;
	}

	@JsonProperty(CLEANSING_PATTERN_PROPERTY)
	public String getCleansingPattern() {
		return cleansingPattern;
	}

	@JsonProperty(EXAMPLES_PROPERTY)
	public List<String> getExamples() {
		return examples;
	}

	@JsonProperty(ENTITY_CONCEPT_SUGGESTIONS_PROPERTY)
	public List<EntityConceptSuggestionDTO> getEntityConceptSuggestions() {
		return entityConceptSuggestions;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"PrimitiveNodeDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + ", \"entityType\":" + entityType
		       + ", \"dataType\":\"" + dataType + '"'
		       + ", \"cleansingPattern\": \"" + cleansingPattern + '"'
		       + ", \"examples\":" + StringUtilities.listToJson(examples)
		       + ", \"entityConceptSuggestions\":" + StringUtilities.listToJson(entityConceptSuggestions)
		       + '}';
	}

	public enum DataTypeDTO {
		UNKNOWN("Unknown"),
		STRING("String"),
		BOOLEAN("Boolean"),
		NUMBER("Number"),
		BINARY("Binary");

		public final String identifier;

		DataTypeDTO(String identifier) {
			this.identifier = identifier;
		}

		public static DataTypeDTO fromIdentifier(String string) {
			for (DataTypeDTO dataType : DataTypeDTO.values()) {
				if (dataType.identifier.equals(string)) {
					return dataType;
				}
			}
			throw new IllegalArgumentException("Unknown identifier for DataType `" + string + "`.");
		}
	}

}
