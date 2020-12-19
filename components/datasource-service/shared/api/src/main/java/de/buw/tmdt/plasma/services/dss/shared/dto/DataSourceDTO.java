package de.buw.tmdt.plasma.services.dss.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataSourceDTO extends CreateDataSourceDTO {

	private static final String UUID = "uuid";

	private final String uuid;

	@JsonCreator
	public DataSourceDTO(
			@NotNull @JsonProperty(UUID) String uuid,
			@NotNull @JsonProperty(TITLE) String title,
			@NotNull @JsonProperty(DESCRIPTION) String description,
			@Nullable @JsonProperty(LONG_DESCRIPTION) String longDescription,
			@Nullable @JsonProperty(FILENAME) String filename

	) {
		super(title, description, longDescription, filename);
		this.uuid = uuid;
	}

	@NotNull
	public String getUuid() {
		return uuid;
	}

	@Override
	public String toString() {
		return "DataSourceDTO{" +
		       "uuid='" + uuid + '\'' +
		       "} " + super.toString();
	}
}