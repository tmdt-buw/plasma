package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@JsonTypeName("CompositeNodeDTO")
public class CompositeNodeDTO extends NodeDTO {
	private static final long serialVersionUID = 2455514534412466302L;
	private static final String COMPONENTS_PROPERTY = "components";
	private static final String SPLITTER_PROPERTY = "splitter";
	private static final String EXAMPLES_PROPERTY = "examples";
	private static final String CLEANSING_PATTERN_PROPERTY = "cleansing_pattern";
	private static final String UUID_PROPERTY = "uuid";

	@JsonProperty(value = COMPONENTS_PROPERTY)
	private List<PrimitiveNodeDTO> components;

	@JsonProperty(value = SPLITTER_PROPERTY)
	private List<SplitterDTO> splitter;

	@JsonProperty(value = EXAMPLES_PROPERTY)
	private List<String> examples;

	@JsonProperty(value = CLEANSING_PATTERN_PROPERTY)
	private String cleansingPattern;

	@JsonCreator
	public CompositeNodeDTO(@NotNull @JsonProperty(COMPONENTS_PROPERTY) List<PrimitiveNodeDTO> components,
	                        @NotNull @JsonProperty(SPLITTER_PROPERTY) List<SplitterDTO> splitter,
	                        @Nullable @JsonProperty(EXAMPLES_PROPERTY) List<String> examples,
	                        @Nullable @JsonProperty(CLEANSING_PATTERN_PROPERTY) String cleansingPattern,
	                        @NotNull @JsonProperty(UUID_PROPERTY) UUID uuid) {
		super(uuid);
		this.components = components;
		this.splitter = splitter;
		this.examples = examples;
		this.cleansingPattern = cleansingPattern;
	}

	@NotNull
	@JsonProperty(COMPONENTS_PROPERTY)
	public List<PrimitiveNodeDTO> getComponents() {
		return components;
	}

	@NotNull
	@JsonProperty(SPLITTER_PROPERTY)
	public List<SplitterDTO> getSplitter() {
		return splitter;
	}

	@Nullable
	@JsonProperty(EXAMPLES_PROPERTY)
	public List<String> getExamples() {
		return examples;
	}

	@Nullable
	@JsonProperty(CLEANSING_PATTERN_PROPERTY)
	public String getCleansingPattern() {
		return cleansingPattern;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"CompositeNodeDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + ", \"components\":" + StringUtilities.listToJson(components)
		       + ", \"splitter\":" + StringUtilities.listToJson(splitter)
		       + ", \"examples\":" + StringUtilities.listToJson(examples)
		       + ", \"cleansingPattern\": \"" + cleansingPattern + '"'
		       + '}';
	}
}
