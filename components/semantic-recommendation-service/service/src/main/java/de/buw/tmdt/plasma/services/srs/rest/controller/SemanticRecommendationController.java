package de.buw.tmdt.plasma.services.srs.rest.controller;

import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import de.buw.tmdt.plasma.services.srs.api.SemanticRecommendationAPI;
import de.buw.tmdt.plasma.services.srs.handler.SemanticLabelingHandler;
import de.buw.tmdt.plasma.services.srs.handler.SemanticModelingHandler;
import de.buw.tmdt.plasma.services.srs.handler.SemanticRefinementHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController("RecommendationController")
public class SemanticRecommendationController implements SemanticRecommendationAPI {

	private final SemanticRefinementHandler refinementHandler;
	private final SemanticLabelingHandler labelingHandler;
	private final SemanticModelingHandler modelingHandler;


	@Autowired
	public SemanticRecommendationController(
			@NotNull SemanticRefinementHandler refinementHandler,
			@NotNull SemanticLabelingHandler labelingHandler,
			@NotNull SemanticModelingHandler modelingHandler
			) {
		this.refinementHandler = refinementHandler;
		this.labelingHandler = labelingHandler;
		this.modelingHandler = modelingHandler;
	}

	@NotNull
	@Override
	@Operation(description = "do a semantic labeling")
	public DataSourceSchemaDTO performSemanticLabeling(@NotNull String uuid, @NotNull DataSourceSchemaDTO dataSourceSchemaDTO) {
		return labelingHandler.performLabeling(uuid, dataSourceSchemaDTO);
	}

	@NotNull
	@Override
	@Operation(description = "do a semantic modeling")
	public DataSourceSchemaDTO performSemanticModeling(@NotNull String uuid, @NotNull DataSourceSchemaDTO dataSourceSchemaDTO) {
		return modelingHandler.performModeling(uuid,dataSourceSchemaDTO);
	}
	@NotNull
	@Override
	@Operation(description = "request semantic refinement recommendations")
	public DataSourceSchemaDTO performSemanticRefinement(@NotNull String uuid, @NotNull DataSourceSchemaDTO dataSourceSchemaDTO) {
		return refinementHandler.performRefinement(uuid,dataSourceSchemaDTO);
	}
}