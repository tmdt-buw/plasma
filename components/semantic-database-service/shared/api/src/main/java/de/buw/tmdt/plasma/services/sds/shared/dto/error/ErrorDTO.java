package de.buw.tmdt.plasma.services.sds.shared.dto.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public class ErrorDTO {

	private static final String PROPERTY_STATUS = "status";
	private static final String PROPERTY_MESSAGE = "message";
	private static final String PROPERTY_REASON = "details";

	@JsonProperty(PROPERTY_STATUS)
	private int status;

	@JsonProperty(PROPERTY_MESSAGE)
	private String message;

	@JsonProperty(PROPERTY_REASON)
	private String details;

	public ErrorDTO(int status) {
		this(status, null, null);
	}

	public ErrorDTO(int status, String message) {
		this(status, message, null);
	}

	@JsonCreator
	public ErrorDTO(int status, @Nullable String message, @Nullable String details) {
		this.status = status;
		this.message = message;
		this.details = details;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Nullable
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Nullable
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}