package de.buw.tmdt.plasma.services.sds.shared.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.buw.tmdt.plasma.services.sds.shared.dto.Database;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RequestDTO implements Serializable {
	private static final long serialVersionUID = 633665332047425073L;

	private static final String PROPERTY_LABELS = "labels";
	private static final String PROPERTY_DATABASES = "databases";

	private final Set<String> labels;
	private final Set<Database> databases;

	@JsonCreator
	public RequestDTO(
			@NotNull @JsonProperty(PROPERTY_LABELS) Set<String> labels,
			@JsonProperty(value = PROPERTY_DATABASES, defaultValue = "[]") Set<Database> databases
	) {
		this.databases = new HashSet<>(databases);
		this.labels = new HashSet<>(labels);
	}

	@JsonProperty(PROPERTY_LABELS)
	public Set<String> getLabels() {
		return Collections.unmodifiableSet(labels);
	}

	@JsonProperty(value = PROPERTY_DATABASES)
	public Set<Database> getDatabases() {
		return Collections.unmodifiableSet(databases);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RequestDTO)) {
			return false;
		}
		RequestDTO that = (RequestDTO) o;
		return Objects.equals(labels, that.labels) &&
				Objects.equals(databases, that.databases);
	}

	@Override
	public int hashCode() {
		return Objects.hash(labels, databases);
	}

	@Override
	@SuppressWarnings("MagicCharacter")
	public String toString() {
		return "{\"@class\":\"RequestDTO\""
		       + ", \"labels\":" + StringUtilities.setToJson(labels)
		       + ", \"databases\":" + StringUtilities.setToJson(databases)
		       + '}';
	}
}
