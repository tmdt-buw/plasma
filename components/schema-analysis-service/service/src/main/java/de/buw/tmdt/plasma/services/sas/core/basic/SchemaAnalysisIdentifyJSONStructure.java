package de.buw.tmdt.plasma.services.sas.core.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.buw.tmdt.plasma.services.sas.core.basic.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.SetNode;

import java.io.Serializable;
import java.util.*;

public class SchemaAnalysisIdentifyJSONStructure implements Serializable {

	private static final int EXAMPLE_VALUE_LENGTH = 255;

	private static final int NUMBER_OF_EXAMPLE_VALUES_PER_TYPE = 6;

	private static final int NODE_LABEL_LENGTH = 254;

	private static final long serialVersionUID = 4213723179685942851L;

	public Node execute(String translationOutput) throws JsonProcessingException, JsonMappingException, SchemaAnalysisException {

		return parseJsonElement(new ObjectMapper().readTree(translationOutput));
	}

	private Node parseJsonElement(JsonNode jsonNode) throws SchemaAnalysisException {
		if (jsonNode.isNull()) {
			return null;
		} else if (jsonNode.isValueNode()) {
			return parseJSONPrimitive(jsonNode);
		} else if (jsonNode.isObject()) {
			return parseJSONObject((ObjectNode) jsonNode);
		} else if (jsonNode.isArray()) {
			return parseJsonArray((ArrayNode) jsonNode);
		}
		throw new SchemaAnalysisException("Unknown JSON SubType: \"" + jsonNode + "\".");
	}


	// Works fine
	private PrimitiveNode parseJSONPrimitive(JsonNode jsonNode) {
		String exampleValue = jsonNode.asText();
		String truncatedExampleValue = exampleValue.substring(0, Math.min(exampleValue.length(), EXAMPLE_VALUE_LENGTH));
		List<String> exampleValues = new ArrayList<>();
		exampleValues.add(truncatedExampleValue);
		return new PrimitiveNode(PrimitiveNode.DataType.UNKNOWN, exampleValues);
	}


	// children.put is wrong -> check merging
	private de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.ObjectNode parseJSONObject(ObjectNode object) throws SchemaAnalysisException {

		Map<String, Node> children = new HashMap<>();
		de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.ObjectNode objectNode =
				new de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.ObjectNode(children, null);

		for (Iterator<Map.Entry<String, JsonNode>> it = object.fields(); it.hasNext();) {
			Map.Entry<String, JsonNode> entry = it.next();
			String nodeLabel = entry.getKey().substring(0, Math.min(entry.getKey().length(), NODE_LABEL_LENGTH));
			objectNode.mergeChild(nodeLabel, parseJsonElement(entry.getValue()));

		}
		return objectNode;
	}

	// children.add is wrong --> check merging
	private SetNode parseJsonArray(ArrayNode array) throws SchemaAnalysisException {
		int primitiveCounter = 0;
		Set<Node> children = new HashSet<>();
		SetNode setNode = new SetNode(children);
		for (JsonNode node : array) {
			if (!node.isValueNode()) {
				setNode.mergeChild(parseJsonElement(node));
			} else if (primitiveCounter < NUMBER_OF_EXAMPLE_VALUES_PER_TYPE) {
				primitiveCounter++;
				setNode.mergeChild(parseJsonElement(node));
			}
		}
		return setNode;
	}

}
