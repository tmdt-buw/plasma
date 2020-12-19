package de.buw.tmdt.plasma.services.sas.core.basic;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.buw.tmdt.plasma.services.sas.core.basic.exception.SchemaAnalysisException;
import de.buw.tmdt.plasma.services.sas.core.converter.NodeDTOConverter;
import de.buw.tmdt.plasma.services.sas.core.model.syntaxmodel.Node;
import de.buw.tmdt.plasma.services.sas.shared.dto.SchemaAnalysisDataProvisionDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.StandardDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.StandardProvisionDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.semanticmodel.SemanticModelDTO;
import de.buw.tmdt.plasma.services.sas.shared.dto.syntaxmodel.NodeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class SchemaAnalysisHandler {

	private final AnalysisService analysisService;
	private final NodeDTOConverter nodeDTOConverter;


	private final Logger logger = LoggerFactory.getLogger(SchemaAnalysisHandler.class);

	@Autowired
	public SchemaAnalysisHandler(AnalysisService analysisService, NodeDTOConverter nodeDTOConverter) {
		this.analysisService = analysisService;
		this.nodeDTOConverter = nodeDTOConverter;

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

	public NodeDTO getResult(UUID uuid, int exampleLimit) {
		if (!analysisService.exists(uuid)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The requested id is not existing: " + uuid);
		}

		return nodeDTOConverter.nodeToDTO(analysisService.getResult(uuid), exampleLimit);
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
