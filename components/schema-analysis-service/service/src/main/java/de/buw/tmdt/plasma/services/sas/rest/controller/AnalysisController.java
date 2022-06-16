package de.buw.tmdt.plasma.services.sas.rest.controller;

import de.buw.tmdt.plasma.datamodel.syntaxmodel.SyntaxModel;
import de.buw.tmdt.plasma.services.sas.core.basic.SchemaAnalysisHandler;
import de.buw.tmdt.plasma.services.sas.shared.api.AnalysisApi;
import de.buw.tmdt.plasma.services.sas.shared.dto.SchemaAnalysisDataProvisionDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController("Analysis Controller")
public class AnalysisController implements AnalysisApi {

	private final SchemaAnalysisHandler schemaAnalysisHandler;

	@Autowired
	public AnalysisController(SchemaAnalysisHandler schemaAnalysisHandler) {
		this.schemaAnalysisHandler = schemaAnalysisHandler;
	}

	@Override
	public void initAnalysis(String id) {
		UUID uuid = UUID.fromString(id);
		schemaAnalysisHandler.initAnalysis(uuid);
	}

	@Override
	public void addDataPoint(String id, SchemaAnalysisDataProvisionDTO schemaAnalysisDataProvisionDTO) {
		UUID uuid = UUID.fromString(id);
		schemaAnalysisHandler.addDataPoint(uuid, schemaAnalysisDataProvisionDTO);
	}

	@Override
	public void addDataPoints(String id, List<SchemaAnalysisDataProvisionDTO> schemaAnalysisDataProvisionDTOs) {
		UUID uuid = UUID.fromString(id);
		schemaAnalysisDataProvisionDTOs.forEach(schemaAnalysisDataProvisionDTO -> schemaAnalysisHandler.addDataPoint(uuid, schemaAnalysisDataProvisionDTO));
	}

	@Override
	public @NotNull SyntaxModel getResult(String id, int exampleLimit) {
		UUID uuid = UUID.fromString(id);
		return schemaAnalysisHandler.getResult(uuid, exampleLimit);
	}

	@Override
	public boolean isReady(String id) {
		UUID uuid = UUID.fromString(id);
		return schemaAnalysisHandler.hasResult(uuid);
	}

	@Override
	public void finish(String id) {
		UUID uuid = UUID.fromString(id);
		schemaAnalysisHandler.finish(uuid);
	}

	@Override
	public boolean existing(String id) {
		UUID uuid = UUID.fromString(id);
		return schemaAnalysisHandler.exists(uuid);
	}

	@Override
	public void delete(String id) {
		UUID uuid = UUID.fromString(id);
		schemaAnalysisHandler.delete(uuid);
	}
}
