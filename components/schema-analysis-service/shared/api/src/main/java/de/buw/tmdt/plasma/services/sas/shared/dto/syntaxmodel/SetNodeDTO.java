package de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@JsonTypeName("SetNodeDTO")
public class SetNodeDTO extends NodeDTO {
	private static final long serialVersionUID = -842163987384241710L;
	private static final String CHILDREN_PROPERTY = "children";
	private static final String UUID_PROPERTY = "uuid";

	@JsonProperty(CHILDREN_PROPERTY)
	private List<ChildDTO> children;

	@JsonCreator
	public SetNodeDTO(@NotNull @JsonProperty(UUID_PROPERTY) UUID uuid,
	                  @JsonProperty(CHILDREN_PROPERTY) List<ChildDTO> children) {
		super(uuid);
		this.children = children;

	}

	@JsonProperty(CHILDREN_PROPERTY)
	public List<ChildDTO> getChildren() {
		return children;
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"SetNodeDTO\""
		       + ", \"@super\":\"" + super.toString()
		       + ", \"children\":" + StringUtilities.listToJson(children)
		       + '}';
	}


	public static class ChildDTO implements Serializable {

		private static final String SELECTOR_PROPERTY = "selector";
		private static final String NODE_PROPERTY = "node";
		private static final String ID_PROPERTY = "id";
		private static final long serialVersionUID = 4633425774948533930L;

		@JsonProperty(SELECTOR_PROPERTY)
		private SelectorDTO selector;

		@JsonProperty(NODE_PROPERTY)
		private NodeDTO node;

		@JsonProperty(ID_PROPERTY)
		private Long id;

		@JsonCreator
		public ChildDTO(@JsonProperty(ID_PROPERTY) Long id,
		                @JsonProperty(NODE_PROPERTY) NodeDTO node,
		                @JsonProperty(SELECTOR_PROPERTY) SelectorDTO selector) {
			this.id = id;
			this.node = node;
			this.selector = selector;
		}

		@JsonProperty(SELECTOR_PROPERTY)
		public SelectorDTO getSelector() {
			return selector;
		}

		@JsonProperty(NODE_PROPERTY)
		public NodeDTO getNode() {
			return node;
		}

		@JsonProperty(ID_PROPERTY)
		public Long getId() {
			return id;
		}

		@Override
		@SuppressWarnings("MagicCharacter")
		public String toString() {
			return "{\"@class\":\"ChildDTO\""
			       + ", \"selector\":" + selector
			       + ", \"node\":" + node
			       + ", \"id\":" + id
			       + '}';
		}

	}


}
