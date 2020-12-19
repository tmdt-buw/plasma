package de.buw.tmdt.plasma.services.dss.shared.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateDataSourceDTO {

	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String LONG_DESCRIPTION = "longDescription";
	static final String FILENAME = "filename";

	private final String title;
	private final String description;
	private final String longDescription;
	private final String filename;

	@JsonCreator
	public CreateDataSourceDTO(
			@NotNull @JsonProperty(TITLE) String title,
			@NotNull @JsonProperty(DESCRIPTION) String description,
			@Nullable @JsonProperty(LONG_DESCRIPTION) String longDescription,
			@Nullable @JsonProperty(FILENAME) String filename

	) {
		this.title = title;
		this.description = description;
		this.longDescription = longDescription;
		this.filename = filename;
	}

	@NotNull
	public String getTitle() {
		return title;
	}

	@NotNull
	public String getDescription() {
		return description;
	}

	@Nullable
	public String getLongDescription() {
		return longDescription;
	}

	@Nullable
	public String getFilename() {
		return filename;
	}

	@Override
	public String toString() {
		return "CreateDataSourceDTO{" +
		       "title='" + title + '\'' +
		       ", description='" + description + '\'' +
		       ", longDescription='" + longDescription + '\'' +
		       '}';
	}
}