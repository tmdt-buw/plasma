package de.buw.tmdt.plasma.services.sas.core.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.PrimitiveNode;
import de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel;
import de.buw.tmdt.plasma.services.sas.core.basic.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.services.sas.core.converter.CombinedModelConverter;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.shared.dto.SchemaAnalysisDataProvisionDTO;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class SchemaAnalysisHandler {

	private final AnalysisService analysisService;
	private final CombinedModelConverter combinedModelConverter;

	private final Logger logger = LoggerFactory.getLogger(SchemaAnalysisHandler.class);

	@Autowired
	public SchemaAnalysisHandler(AnalysisService analysisService,
	                             CombinedModelConverter combinedModelConverter) {
		this.analysisService = analysisService;

		this.combinedModelConverter = combinedModelConverter;
	}

	public void initAnalysis(UUID uuid) {
		analysisService.createAnalysis(uuid);
		logger.info("Initialized Schema Analysis with UUID:{}", uuid);
	}

	public void addDataPoint(UUID uuid, SchemaAnalysisDataProvisionDTO schemaAnalysisDataProvisionDTO) {
		if (!analysisService.exists(uuid)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested id is not existing: " + uuid);
		}
		// logger.info("Adding Datapoint to Schema Analysis with UUID:{} : {}", uuid, schemaAnalysisDataProvisionDTO.getData());
		SchemaAnalysisIdentifyJSONStructure schemaAnalysisIdentifyJSONStructure = new SchemaAnalysisIdentifyJSONStructure();
		Node result;
		try {
			result = schemaAnalysisIdentifyJSONStructure.execute(schemaAnalysisDataProvisionDTO.getData());
		} catch (JsonProcessingException | SchemaAnalysisException e) {
			logger.error("Could not create Json from input data", e);
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Could not create semantic model");
		}

		// logger.info("Identified Sub Schema: {}", result);

		analysisService.addNodeToAnalysis(uuid, result);
	}

	public @NotNull SyntaxModel getResult(UUID uuid, int exampleLimit) {
		if (!analysisService.exists(uuid)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested id is not existing: " + uuid);
		}

		SyntaxModel syntaxModel = combinedModelConverter.toCombinedModel(analysisService.getResult(uuid));

		syntaxModel.getNodes().stream()
				.filter(schemaNode -> schemaNode instanceof PrimitiveNode)
				.map(schemaNode -> (PrimitiveNode) schemaNode)
				.forEach(primitiveNode -> primitiveNode.setExamples(new ArrayList<>(primitiveNode.getExamples().subList(0, Math.min(exampleLimit, primitiveNode.getExamples().size())))));
		return syntaxModel;
	}

	public void finish(UUID uuid) {
		if (!analysisService.exists(uuid)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested id is not existing: " + uuid);
		}
		analysisService.calculateResult(uuid);
	}

	public boolean hasResult(UUID uuid) {
		return analysisService.hasResult(uuid);
	}

	public boolean exists(UUID uuid) {
		return analysisService.exists(uuid);
	}

	public void delete(UUID uuid) {
		if (!analysisService.exists(uuid)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested id is not existing: " + uuid);
		}
		analysisService.delete(uuid);
	}
}
