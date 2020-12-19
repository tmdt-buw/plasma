package de.buw.tmdt.plasma.ars.refinement.gr.service.rest.controller;

import de.buw.tmdt.plasma.ars.refinement.gr.service.handler.GeneralizationRecommendationHandler;
import de.buw.tmdt.plasma.ars.refinement.gr.shared.api.GeneralizationRecommendationAPI;
import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController("Generalization Recommendation Controller")
public class GeneralizationRecommendationController implements GeneralizationRecommendationAPI {

	private final GeneralizationRecommendationHandler generalizationRecommendationHandler;

	@Autowired
	public GeneralizationRecommendationController(
			@NotNull GeneralizationRecommendationHandler generalizationRecommendationHandler
	) {

		this.generalizationRecommendationHandler = generalizationRecommendationHandler;
	}

	@NotNull
	@Override
	@Operation(description = "recommends generalizations for existing concepts")
	public DataSourceSchemaDTO getRecommendations(@NotNull String uuid, @NotNull DataSourceSchemaDTO combined) {
		return generalizationRecommendationHandler.recommendGeneralization(uuid, combined);
	}
}