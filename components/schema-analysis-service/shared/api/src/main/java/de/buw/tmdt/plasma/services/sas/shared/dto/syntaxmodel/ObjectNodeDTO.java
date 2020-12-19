package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

@JsonTypeName("ObjectNodeDTO")
public class ObjectNodeDTO extends NodeDTO {

	private static final long serialVersionUID = 5785866523731880749L;
	private static final String CHILDREN_PROPERTY = "children";
	private static final String UUID_PROPERTY = "uuid";

	@JsonProperty(CHILDREN_PROPERTY)
	private Map<String, NodeDTO> children;

	@JsonCreator
	public ObjectNodeDTO(@NotNull @JsonProperty(CHILDREN_PROPERTY) Map<String, NodeDTO> children,
	                     @NotNull @JsonProperty(UUID_PROPERTY) UUID uuid) {
		super(uuid);
		this.children = children;
	}

	@NotNull
	@JsonProperty(CHILDREN_PROPERTY)
	public Map<String, NodeDTO> getChildren() {
		return children;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"ObjectNodeDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + ", \"children\":" + StringUtilities.mapToJson(children)
		       + '}';
	}
}
