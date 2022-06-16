package de.buw.tmdt.plasma.services.sas.core.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.buw.tmdt.plasma.services.sas.core.basic.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PerformSchemaAnalysis {

	private Logger logger = LoggerFactory.getLogger(PerformSchemaAnalysis.class);

	public Node performSchemaAnalysis(String inputData) {

		SchemaAnalysisIdentifyJSONStructure schemaAnalysisIdentifyJSONStructure = new SchemaAnalysisIdentifyJSONStructure();
		SchemaAnalysisAggregateRecognizedSchema schemaAnalysisAggregateRecognizedSchema = new SchemaAnalysisAggregateRecognizedSchema(20);

		// Step 1: Identify Json Structure

		Node recognizedSchemaTree = null;
		try {
			recognizedSchemaTree = schemaAnalysisIdentifyJSONStructure.execute(inputData);
		} catch (JsonProcessingException | SchemaAnalysisException e) {
			logger.error("Could not identify Json Structure", e);
		}

		if (recognizedSchemaTree != null) {

			// Step 2: Aggregate Recognized Schema
			schemaAnalysisAggregateRecognizedSchema.addNode(recognizedSchemaTree);
		}

		// Step 3: Suggest Entity Concepts

		// Step 4: Publish
		return schemaAnalysisAggregateRecognizedSchema.getMergedRecognizedNode();
	}
}
