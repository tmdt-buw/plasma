package de.buw.tmdt.plasma.ars.refinement.gr.shared.api;

import de.buw.tmdt.plasma.services.dms.shared.dto.DataSourceSchemaDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("HardcodedFileSeparator")
@RequestMapping(value = "api/plasma-ars-r-gr")
public interface GeneralizationRecommendationAPI {

	@NotNull
	@PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public DataSourceSchemaDTO getRecommendations(@NotNull String uuid, @NotNull DataSourceSchemaDTO combined);

}
