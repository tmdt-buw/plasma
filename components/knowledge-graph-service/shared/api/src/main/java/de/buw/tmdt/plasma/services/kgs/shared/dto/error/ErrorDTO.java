package de.buw.tmdt.plasma.services.kgs.shared.dto.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public class ErrorDTO {

    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_MESSAGE = "message";
    private static final String PROPERTY_REASON = "details";

    @JsonProperty(PROPERTY_STATUS)
    private final int status;

    @JsonProperty(PROPERTY_MESSAGE)
    private final String message;

    @JsonProperty(PROPERTY_REASON)
    private final String details;

    public ErrorDTO(int status, @Nullable String message) {
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

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "ErrorDTO{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}