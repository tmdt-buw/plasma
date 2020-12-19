package de.buw.tmdt.plasma.services.dss.core.model;

import org.hibernate.annotations.DynamicUpdate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.UUID;

@DynamicUpdate
@Entity
@Table(name = "data_sources")
public class DataSourceModel {

	@Id
	@GeneratedValue
	@Column(nullable = false, unique = true, updatable = false, length = 16)
	private UUID uuid;

	@Column(nullable = false)
	@NotEmpty
	private String title;

	@Lob
	@NotEmpty
	@Column(nullable = false)
	private String description;

	@Lob
	@Column
	private String longDescription;

	@Column
	private String fileName;

	@NotNull
	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(@NotNull UUID uuid) {
		this.uuid = uuid;
	}

	@NotNull
	public String getTitle() {
		return title;
	}

	public void setTitle(@NotNull String title) {
		this.title = title;
	}

	@NotNull
	public String getDescription() {
		return description;
	}

	public void setDescription(@NotNull String description) {
		this.description = description;
	}

	@Nullable
	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(@Nullable String longDescription) {
		this.longDescription = longDescription;
	}

	public void setFileName(@Nullable String fileName) {
		this.fileName = fileName;
	}

	@Nullable
	public String getFileName() {
		return fileName;
	}
}