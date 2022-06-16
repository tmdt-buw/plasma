package de.buw.tmdt.plasma.datamodel.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public class ModelError implements Serializable {

    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_MESSAGE = "message";
    private static final String PROPERTY_REASON = "details";

    @JsonProperty(PROPERTY_STATUS)
    private int status;

    @JsonProperty(PROPERTY_MESSAGE)
    private String message;

	@JsonProperty(PROPERTY_REASON)
	private String details;

    public ModelError(int status) {
        this(status, null, null);
    }

    public ModelError(int status, @Nullable String message) {
        this(status, message, null);
    }

    @JsonCreator
    public ModelError(@JsonProperty(PROPERTY_STATUS) int status,
					  @Nullable @JsonProperty(PROPERTY_MESSAGE) String message,
					  @Nullable @JsonProperty(PROPERTY_MESSAGE) String details) {
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

	public void setMessage(@Nullable String message) {
		this.message = message;
	}

	@Nullable
	public String getDetails() {
		return details;
	}

	public void setDetails(@Nullable String details) {
		this.details = details;
	}
}